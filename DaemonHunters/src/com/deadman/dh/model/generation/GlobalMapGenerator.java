package com.deadman.dh.model.generation;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.deadman.dh.R;
import com.deadman.dh.RandomName;
import com.deadman.dh.city.City;
import com.deadman.dh.global.Glyph;
import com.deadman.dh.model.GlobalMap;
import com.deadman.dh.model.Poi;
import com.deadman.dh.model.RiverNode;
import com.deadman.dh.model.SubMap;
import com.deadman.dh.model.Way;
import com.deadman.jgame.io.NullOutputStream;
import com.deadman.jgame.ui.ProgressStatus;

public class GlobalMapGenerator
{
	private Random random;
	private long _seed;

	private final int width, height;
	private RandomName names;

	private final int MIN_BIG_CITY_DIST = 30; // Минимальное расстояние между большими городами. В координатах глобальной карты

	// 880736908807540521L - 3 озера
	// -6710744642298176581 - крест
	// 7377206927136848952
	//seed = 532565961276571566L;
	// 5756094913787764892 - наложение надписей
	//seed = -6895224255702320842L;

	public GlobalMapGenerator(int w, int h, long seed)
	{
		width = w;
		height = h;
		_seed = seed;

		System.out.println("GlobalMap Seed = " + _seed);

		names = new RandomName(R.names_txt, seed);

		initLog();
	}

	//region GlobalMap

	private float[][] _levels;
	private boolean[][] _filter;
	private byte[][] _pixels;
	private ArrayList<City> _cities;
	private ArrayList<Poi> _points;
	private ArrayList<Glyph> _glyphs;
	private double lvlM2 = 0, lvlM = 0, lvlF = 0, lvlT = 0, lvlC = 0, lvlSD = 0, lvlSM = 0;
	private PerlinNoise perlin;

	private ProgressStatus st;

	boolean[][] _wayBuff;

	public GlobalMap generate(ProgressStatus status)
	{
		random = new Random(_seed);
		st = status;

		_filter = new boolean[width][height];
		buildTerrainPerlin();
		st.progress += 10;
		_pixels = getPixels(_levels);
		buildPoi();
		st.progress += 10;
		buildRivers();
		st.progress += 10;
		buildCities();
		st.progress += 5;
		buildWays();
		st.progress += 5;

		buildGlyphs();
		System.out.println("Completed");

		GlobalMap map = new GlobalMap(this, width, height);
		map.pixels = _pixels;
		map.cities = _cities.toArray(new City[_cities.size()]);
		map.glyphs = _glyphs.toArray(new Glyph[_glyphs.size()]);
		map.points = _points.toArray(new Poi[_points.size()]);

		_levels = null;
		_filter = null;
		_glyphs = null;
		_pointMap = null;
		System.gc();

		st.progress += 10;

		return map;
	}

	public BufferedImage getHeightsMap()
	{
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				int l = (int) (((_levels[x][y] + -mapMin) / mapAmp) * 255);
				int color = (l & 0xFF) | ((l & 0xFF) << 8) | ((l & 0xFF) << 16) | 0xff000000;
				img.setRGB(x, y, color);
			}
		return img;
	}

	private static final float FOREST_PERC = 50f;

	private byte[][] getPixels(float[][] levels)
	{
		long __time = System.currentTimeMillis();
		System.out.println("Build pixels...");

		int w = levels.length;
		int h = levels[0].length;
		byte[][] pixels = new byte[w][h];
		//System.out.println("lvlM2 = " + lvlM2 + "; lvlM = " + lvlM + "; lvlF = " + lvlF);
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
			{
				double v = levels[x][y];
				if (v > lvlM2)
					pixels[x][y] = GlobalMap.MOUNTAIN2_ID;
				else if (v > lvlM)
					pixels[x][y] = GlobalMap.MOUNTAIN_ID;
				else if (v > lvlF)
				{
					if (perlin.noise2(x / FOREST_PERC, y / FOREST_PERC) > 0)
						pixels[x][y] = GlobalMap.FOREST_ID;
					else
						pixels[x][y] = GlobalMap.TERRAIN_ID;
				}
				else if (v > lvlT)
					pixels[x][y] = GlobalMap.TERRAIN_ID;
				else if (v > lvlC)
					pixels[x][y] = GlobalMap.COAST_ID;
				else if (v > lvlSD)
					pixels[x][y] = GlobalMap.SEA_DARK_ID;
				else if (v > lvlSM)
					pixels[x][y] = GlobalMap.SEA_MIDDLE_ID;
				else
					pixels[x][y] = GlobalMap.SEA_LIGHT_ID;
			}

		byte[][] buff = copyPixels(pixels);
		for (int x = 0; x < w; x++)
		{
			for (int y = 0; y < h; y++)
			{
				byte v = pixels[x][y];

				// Жесткая граница
				if ((v == GlobalMap.COAST_ID || v == GlobalMap.TERRAIN_ID) && isNear4Less(pixels, GlobalMap.SEA_DARK_ID, x, y))
					buff[x][y] = GlobalMap.COASTLINE_ID;
			}
		}

		System.out.println("Build pixels time: " + (System.currentTimeMillis() - __time) + "ms");

		return buff;
	}

	private static double DEPTH = 512f;
	private float mapMin, mapMax, mapAmp;

	private void buildTerrainPerlin()
	{
		long __time = System.currentTimeMillis();
		System.out.println("Build terrain...");

		perlin = PerlinNoise.create(random.nextLong());
		_levels = new float[width][height];
		mapMin = Float.MAX_VALUE;
		mapMax = Float.MIN_VALUE;

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				double mx = (double) x / width;
				double my = (double) y / height;
				double v = perlin.turbulence2(mx, my, DEPTH);
				v += perlin.noise2(128.0 * mx, 128.0 * my) / 64.0;
				v += perlin.noise2(256.0 * mx, 256.0 * my) / 128.0;
				float fv = (float) v;
				_levels[x][y] = fv;
				if (mapMin > v) mapMin = fv;
				if (mapMax < v) mapMax = fv;
			}

		mapAmp = mapMax - mapMin;
		//System.out.println("min = " + mapMin + " max = " + mapMax + " amp = " + mapAmp);

		int[] counts = new int[300];
		float d = mapAmp / (counts.length - 1);

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				int ind = (int) ((_levels[x][y] - mapMin) / d);
				counts[ind]++;
			}

		int sum = 0;
		for (int i = 0; i < counts.length; i++)
		{
			sum += counts[i];
			double r = (double) sum / (width * height);
			float range = mapMin + i * d;

			//System.out.println(i + " : r=" + r + " " + range);

			if (lvlM2 == 0 && r > 0.98)
				lvlM2 = range;

			if (lvlM == 0 && r > 0.80)
				lvlM = range;

			if (lvlF == 0 && r > 0.35)
				lvlF = range;

			if (lvlT == 0 && r > 0.20)
				lvlT = range;

			if (lvlC == 0 && r > 0.15)
				lvlC = range;

			if (lvlSD == 0 && r > 0.13)
				lvlSD = range;

			if (lvlSM == 0 && r > 0.12)
				lvlSM = range;
		}

		System.out.println("Build terrain time: " + (System.currentTimeMillis() - __time) + "ms");
	}

	//endregion

	//region SubTerrain

	final double SUB_DEPTH = 512.0 * 64.0;

	public float[][] getSubTerrain(double tx, double ty, double tw, double th)
	{
		long __time = System.currentTimeMillis();
		System.out.println("Build subterrain...");

		int mapW = (int) (tw * GlobalMap.MAP_ZOOM);
		int mapH = (int) (th * GlobalMap.MAP_ZOOM);

		float[][] terrain = new float[mapW][mapH];

		for (int x = 0; x < mapW; x++)
		{
			double mx = tx / width + (tw / width) * ((double) x / mapW);

			for (int y = 0; y < mapH; y++)
			{
				double my = ty / height + (th / height) * ((double) y / mapH);
				double v = perlin.turbulence2(mx, my, SUB_DEPTH);
				v += perlin.noise2(128.0 * mx, 128.0 * my) / 64.0;
				v += perlin.noise2(256.0 * mx, 256.0 * my) / 128.0;
				terrain[x][y] = (float) v;
			}
		}

		System.out.println("Build subterrain time: " + (System.currentTimeMillis() - __time) + "ms");
		return terrain;
	}

	// Возвращает карту выбранного участка (tx, ty - tw, th) размера x300
	public SubMap getSubMap(double tx, double ty, double tw, double th)
	{
		if (tx < 0) tx = 0;
		if (ty < 0) ty = 0;

		float[][] terrain = getSubTerrain(tx, ty, tw, th);
		byte[][] subPixels = getPixels(terrain);

		//Random rnd = new Random(); // TODO передача Random или seed

		// Реки
		int l = (int) tx;
		int r = (int) Math.round(tx + tw + 0.5);
		int t = (int) ty;
		int b = (int) Math.round(ty + th + 0.5);
		for (int x = l; x <= r; x++)
			for (int y = t; y <= b; y++)
			{
				RiverNode n = riverMap[x][y];
				if (n != null)
				{
					//System.out.println("River node " + n);
					int shiftX = (int) ((x - tx) * GlobalMap.MAP_ZOOM);
					int shiftY = (int) ((y - ty) * GlobalMap.MAP_ZOOM);

					if (n.parents != null && n.parents.size() > 0)
						for (RiverNode p : n.parents)
							drawRiver(subPixels, p, n, n.next, shiftX, shiftY);
					else
						drawRiver(subPixels, null, n, n.next, shiftX, shiftY);
				}
			}

		return new SubMap(subPixels, tx, ty, tw, th);
	}

	private void drawRiver(byte[][] subPixels, RiverNode parent, RiverNode n, RiverNode next, int shiftX, int shiftY)
	{
		byte v = n.getRiverType();
		int r = n.getRadius();

		if (parent == null && next == null) return;

		int x1 = 0, y1 = 0;
		if (parent != null)
		{
			// TODO случайный сдвиг
			if (parent.x < n.x)
				x1 = 0;
			else if (parent.x == n.x)
				x1 = GlobalMap.MAP_ZOOM / 2;
			else
				x1 = GlobalMap.MAP_ZOOM;
			if (parent.y < n.y)
				y1 = 0;
			else if (parent.y == n.y)
				y1 = GlobalMap.MAP_ZOOM / 2;
			else
				y1 = GlobalMap.MAP_ZOOM;
		}

		int x2 = GlobalMap.MAP_ZOOM / 2;
		int y2 = GlobalMap.MAP_ZOOM / 2;

		int x3 = 0, y3 = 0;
		if (next != null)
		{
			// TODO случайный сдвиг
			if (next.x < n.x)
				x3 = 0;
			else if (next.x == n.x)
				x3 = GlobalMap.MAP_ZOOM / 2;
			else
				x3 = GlobalMap.MAP_ZOOM;
			if (next.y < n.y)
				y3 = 0;
			else if (next.y == n.y)
				y3 = GlobalMap.MAP_ZOOM / 2;
			else
				y3 = GlobalMap.MAP_ZOOM;
		}

		if (parent == null)
			drawRandomLine(subPixels, v, x2, y2, x3, y3, shiftX, shiftY, 2, r);
		else if (next == null)
			drawRandomLine(subPixels, v, x1, y1, x2, y2, shiftX, shiftY, 2, r);
		else
			drawBezier(subPixels, v, x1, y1, x2, y2, x3, y3, shiftX, shiftY, r);
	}

	private void drawBezier(byte[][] arr, byte val, int x1, int y1, int x2, int y2, int x3, int y3, int shiftX, int shiftY, int r)
	{
		int POINTS_ON_CURVE = 5;
		double[] p = new double[POINTS_ON_CURVE * 2];

		BezierCurve.bezier2D(new double[] { x1, y1, x2, y2, x3, y3 }, p);

		int c = p.length - 3;
		for (int i = 0; i < c; i += 2)
			drawRandomLine(arr, val, (int) p[i], (int) p[i + 1], (int) p[i + 2], (int) p[i + 3], shiftX, shiftY, 2, r);
	}

	private void drawRandomLine(byte[][] arr, byte val, int x1, int y1, int x2, int y2, int shiftX, int shiftY, double d, int r)
	{
		double px, py;
		double cx = x1, cy = y1;

		int len = 1;
		double a;

		setCircle(arr, val, x1 + shiftX, y1 + shiftY, r);
		setCircle(arr, val, x2 + shiftX, y2 + shiftY, r);

		while (true)
		{
			px = cx;
			py = cy;

			if (y2 != py)
				a = Math.atan((double) (x2 - px) / (y2 - py));
			else
				a = Math.PI / 2.0 * Math.signum(x2 - px);

			if (py > y2) a += Math.PI;

			if (d > 0)
				a += random.nextGaussian() * Math.PI / d;

			cx = px + Math.sin(a) * len;
			cy = py + Math.cos(a) * len;

			setCircle(arr, val, (int) cx + shiftX, (int) cy + shiftY, r);

			if (Math.abs(cx - x2) < 1 && Math.abs(cy - y2) < 1) break;
		}
	}

	//endregion

	//region POI

	private final int POI_RANGE = 2;
	private final float POI_PERC = 8f;
	private Poi[][] _pointMap;

	private void buildPoi()
	{
		System.out.println("Build POI...");
		_riverPoints = new ArrayList<>();

		float[][] levels_poi = new float[width][height];
		_pointMap = new Poi[width][height];
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				levels_poi[x][y] = (float) perlin.noise2((float) x / POI_PERC, (float) y / POI_PERC);

		ArrayList<Poi> points = new ArrayList<>();
		for (int x = POI_RANGE; x < width - POI_RANGE; x++)
			for (int y = POI_RANGE; y < height - POI_RANGE; y++)
			{
				float v = levels_poi[x][y];
				if (_levels[x][y] <= lvlC) continue;

				boolean isPass = true;

				for (int dx = x - POI_RANGE; dx <= x + POI_RANGE; dx++)
				{
					for (int dy = y - POI_RANGE; dy <= y + POI_RANGE; dy++)
					{
						if (dx == x && dy == y) continue;

						if (levels_poi[dx][dy] < v)
						{
							isPass = false;
							break;
						}
					}

					if (!isPass)
						break;
				}

				if (isPass)
				{
					// Нашли точку, которая самая низкая в окрестностях
					int r = random.nextInt(10);
					if (r < 1)
					{
						_riverPoints.add(new Point(x, y));
					}
					else
					{
						if (_levels[x][y] > lvlM2) continue;

						Poi p = new Poi(x, y);
						points.add(p);
						_pointMap[x][y] = p;
					}
				}
			}

		_points = points;
	}

	//endregion POI

	//region Rivers

	private static final int MAX_RIVER_SIZE = 50000;

	public RiverNode[][] riverMap; // Вспомогательный массив. Карта рек
	ArrayList<Point> _riverPoints; // Точки для пролегания рек
	ArrayList<RiverNode> rivers = new ArrayList<>();

	private void buildRivers()
	{
		System.out.println("Build rivers...");
		long time = System.nanoTime();
		riverMap = new RiverNode[width][height];

		for (int n = 0; n < _riverPoints.size(); n++)
		{
			//System.out.println("Build river " + n + "...");
			Point p = _riverPoints.get(n);
			RiverNode r = buildRiver(p, n);
			if (r == null) continue;
			rivers.add(r);
		}

		_riverPoints = null;
		System.out.println("Total rivers: " + rivers.size() + " time: " + (System.nanoTime() - time));

		// Помечаем реки на карте
		riverMap = new RiverNode[width][height];
		for (RiverNode r : rivers)
		{
			RiverNode node = r;
			while (node != null)
			{
				//System.out.println("River #" + node.id + " " + node.width + " " + node);
				riverMap[node.x][node.y] = node;

				byte curr = _pixels[node.x][node.y];
				if (GlobalMap.isFlag(curr, GlobalMap.TYPE_SEA) || curr == GlobalMap.COASTLINE_ID)
				{
					// Если впали в море, то идем дальше и пропускаем установку реки на карте
					node = node.next;
					continue;
					//node.next = null;
					//break;
				}

				_pixels[node.x][node.y] = node.getRiverType();
				aroundSetFilter.work(node.x, node.y, 2);
				node = node.next;
			}
		}
	}

	private RiverNode buildRiver(Point p, int id)
	{
		if (GlobalMap.isFlag(_pixels[p.x][p.y], GlobalMap.TYPE_RIVER) || riverMap[p.x][p.y] != null)
		{
			//System.out.println("Is already river");
			return null;
		}

		RiverNode curr = new RiverNode(p.x, p.y, id);
		riverMap[p.x][p.y] = curr;

		ArrayList<RiverNode> around = getRiverAround(riverMap, curr);
		if (around == null || around.size() == 0) return null;

		RiverNode startRiver = curr;

		int k = 0;
		while (k < MAX_RIVER_SIZE)
		{
			k++;
			curr = getMinRiver(around);

			if (curr == null)
				break;

			// Впали в реку
			if (curr.used)
				break;

			byte currT = _pixels[curr.x][curr.y];
			if (currT == GlobalMap.SEA_MIDDLE_ID || currT == GlobalMap.SEA_DARK_ID)
			{
				//System.out.println("Sea found");
				curr = curr.temp_parent; // Шаг назад, чтобы не считать реки в воде
				break;
			}

			// Вышли на границу
			if (curr.x == 0 || curr.y == 0 || curr.x == width - 1 || curr.y == height - 1)
				break;

			around.remove(curr);
			ArrayList<RiverNode> newAround = getRiverAround(riverMap, curr);
			around.addAll(newAround);
		}

		// Впали в реку. Увеличиваем ее объем
		if (curr.used)
		{
			int dw = curr.temp_parent.width;
			RiverNode node = curr;
			while (node != null)
			{
				node.width += dw;
				node = node.next;
			}
		}

		// Идем обратно и отмечаем следующий нод
		while (curr != null)
		{
			curr.used = true;

			if (curr.temp_parent != null)
			{
				curr.addParent(curr.temp_parent);
				curr.temp_parent.next = curr;
			}

			curr = curr.temp_parent;
		}

		return startRiver;
	}

	private ArrayList<RiverNode> getRiverAround(final RiverNode[][] riverMap, final RiverNode curr)
	{
		final ArrayList<RiverNode> around = new ArrayList<>(4);

		AroundIterator iterator = new AroundIterator()
		{
			@Override
			void iterate(int tx, int ty)
			{
				RiverNode node = riverMap[tx][ty];
				if (node == null)
				{
					node = new RiverNode(tx, ty, curr);
					riverMap[tx][ty] = node;
					around.add(node);
				}
				else
				{
					if (node.used) // впали в другую реку
					{
						node.temp_parent = curr;
						around.add(node);
					}
					else if (node.id != curr.id) // забираем себе чужую неиспользованную ноду
					{
						node.id = curr.id;
						node.width = curr.width + 1;
						node.temp_parent = curr;
						around.add(node);
					}
				}
			}
		};

		iterator.work(curr.x, curr.y);

		return around;
	}

	RiverNode getMinRiver(ArrayList<RiverNode> points)
	{
		float min = Float.MAX_VALUE;
		RiverNode minP = null;
		for (RiverNode p : points)
		{
			if (p.next != null) return p;

			if (_levels[p.x][p.y] < min)
			{
				min = _levels[p.x][p.y];
				minP = p;
			}
		}
		return minP;
	}

	//endregion

	//region Cities

	// TODO Константы для суммы и порогов
	// У воды
	private final int cr0 = 185; // Без города
	private final int cr1 = 30; // Большой
	private final int cr2 = 20; // Средний
	private final int cr3 = 5; // Малый

	// Без воды
	private final int cn0 = 200;
	private final int cn1 = 5;
	private final int cn2 = 1;
	private final int cn3 = 0;

	void buildCities()
	{
		System.out.println("Build cities...");
		_cities = new ArrayList<>();
		ArrayList<Poi> toRemove = new ArrayList<Poi>();
		// TODO: расчистка окрестностей от леса

		for (int i = 0; i < _points.size(); i++)
		{
			Poi p = _points.get(i);

			City city;
			Point river = getNearMask(_pixels, p.x, p.y, 3, GlobalMap.TYPE_RIVER);
			if (river == null) river = getNearValue(_pixels, p.x, p.y, 3, GlobalMap.COASTLINE_ID);

			if (river != null)
			{
				int r = random.nextInt(cr0 + cr1 + cr2 + cr3);
				if (r < cr0)
					continue;

				if (r < cr0 + cr1)
					city = new City(1);
				else if (r < cr0 + cr1 + cr2)
					city = new City(2);
				else
					city = new City(3);

				p.x = river.x;
				p.y = river.y;
			}
			else
			{
				int r = random.nextInt(cn0 + cn1 + cn2 + cn3);
				if (r < cn0)
					continue;

				if (r < cn0 + cn1)
					city = new City(1);
				else if (r < cn0 + cn1 + cn2)
					city = new City(2);
				else
					city = new City(3);
			}

			city.id = _cities.size();
			city.name = names.getRandomName();
			city.seed = random.nextLong();
			
			if (city.level == 3 && hasBigCityNear(p))
				city.level = 2;

			city.x = p.x;
			city.y = p.y;

			// Если переместили на чужое место, удалить точку
			Poi exists = _pointMap[p.x][p.y];
			if (exists != null)
			{
				if (exists instanceof City)
					_cities.remove(exists);
				toRemove.add(exists);
			}

			_pointMap[p.x][p.y] = city;

			_points.set(i, city);
			_cities.add(city);

			// Помечаем территорию вокруг, чтобы не было глифов
			_filter[p.x][p.y] = true;
			aroundSetFilter.work(p.x, p.y, 5);
		}

		for (Poi p : toRemove)
		{
			_points.remove(p);
			if (p instanceof City)
				_cities.remove(p);
		}
	}

	private boolean hasBigCityNear(Poi p)
	{
		for (City c : _cities)
		{
			if (c.level == 3 && c.distance(p) < MIN_BIG_CITY_DIST)
				return true;
		}
		return false;
	}

	//endregion

	//region Ways

	static final int W_CELL_SIZE = 40;
	static final double W_NEIGHBOR_DIST_SQ = 30 * 30;

	class MapCell
	{
		ArrayList<Poi> points = new ArrayList<>();
	}

	MapCell[][] _pointIndexes;

	MapCell getPointCell(Poi p)
	{
		return _pointIndexes[p.x / W_CELL_SIZE][p.y / W_CELL_SIZE];
	}

	MapCell getPointCell(int x, int y)
	{
		if (x < 0 || y < 0 || x >= _pointIndexes.length || y >= _pointIndexes[0].length) return null;
		return _pointIndexes[x][y];
	}

	ArrayList<Poi> getPointNearCell(int x, int y)
	{
		MapCell cell = getPointCell(x, y);
		if (cell == null) return null;

		ArrayList<Poi> points = new ArrayList<>(cell.points);

		MapCell near;

		near = getPointCell(x - 1, y);
		if (near != null) points.addAll(near.points);

		near = getPointCell(x - 1, y - 1);
		if (near != null) points.addAll(near.points);

		near = getPointCell(x - 1, y + 1);
		if (near != null) points.addAll(near.points);

		near = getPointCell(x, y - 1);
		if (near != null) points.addAll(near.points);

		near = getPointCell(x, y + 1);
		if (near != null) points.addAll(near.points);

		near = getPointCell(x + 1, y - 1);
		if (near != null) points.addAll(near.points);

		near = getPointCell(x + 1, y);
		if (near != null) points.addAll(near.points);

		near = getPointCell(x + 1, y + 1);
		if (near != null) points.addAll(near.points);

		return points;
	}

	private void addWayPoint(Poi p)
	{
		MapCell cell = getPointCell(p);
		if (cell == null)
		{
			cell = new MapCell();
			_pointIndexes[p.x / W_CELL_SIZE][p.y / W_CELL_SIZE] = cell;
		}
		cell.points.add(p);
	}

	static final int MAX_NEIGHBOR_DIST2 = 30 * 30;
	static final int MAX_NEIGHBOR_COUNT = 15;
	static final int MAX_CITY_DIST2 = 200 * 200;

	private void buildWays()
	{
		System.out.println("Build ways...");

		ArrayList<Way> ways = new ArrayList<>();

		int mw = width / W_CELL_SIZE;
		if (width % W_CELL_SIZE > 0) mw++;
		int mh = height / W_CELL_SIZE;
		if (height % W_CELL_SIZE > 0) mh++;

		_pointIndexes = new MapCell[mw][mh];

		_wayBuff = new boolean[width][height];
		for (Poi p : _points)
			addWayPoint(p);

		for (int mx = 0; mx < mw; mx++)
		{
			for (int my = 0; my < mh; my++)
			{
				MapCell cell = getPointCell(mx, my);
				if (cell == null) continue;

				ArrayList<Poi> near = getPointNearCell(mx, my);

				ArrayList<PointDist> dists = new ArrayList<>();
				for (Poi p : cell.points)
				{
					for (Poi n : near)
					{
						if (p == n) continue;
						if (isWaterOnLine(p.x, p.y, n.x, n.y)) continue;

						double d = p.distanceSq(n);
						if (d < MAX_NEIGHBOR_DIST2)
							dists.add(new PointDist(p, n, d));
					}
				}

				Collections.sort(dists);
				for (int i = 0; i < dists.size(); i++)
				{
					PointDist pd = dists.get(i);

					if (pd.a.neighborhood.contains(pd.b)) continue;
					if (pd.a.neighborhood.size() >= MAX_NEIGHBOR_COUNT) continue;
					if (pd.b.neighborhood.size() >= MAX_NEIGHBOR_COUNT) continue;
					if (pd.a.hasInDirection(pd.b)) continue;

					if (isRoadOnLine(pd.a.x, pd.a.y, pd.b.x, pd.b.y)) continue;
					setRoadOnLine(pd.a.x, pd.a.y, pd.b.x, pd.b.y);

					pd.a.neighborhood.add(pd.b);
					pd.b.neighborhood.add(pd.a);
				}
			}
		}

		for (int i = 0; i < _points.size(); i++)
		{
			Poi wp = _points.get(i);
			if (wp.neighborhood.size() < 2 && !(wp instanceof City))
			{
				MapCell cell = getPointCell(wp);
				cell.points.remove(wp);
				for (Poi n : wp.neighborhood)
					n.neighborhood.remove(wp);

				_points.remove(i);
				i--;
			}
		}

		ArrayList<CityDistance> cityDist = new ArrayList<>();
		for (int i = 1; i < _cities.size(); i++)
		{
			City ca = _cities.get(i);

			for (int j = 0; j < i; j++)
			{
				City cb = _cities.get(j);

				double d2 = ca.distanceSq(cb);
				if (d2 < MAX_CITY_DIST2)
				{
					cityDist.add(new CityDistance(ca, cb, d2));
				}
			}
		}

		Collections.sort(cityDist);

		for (int c = 0; c < cityDist.size(); c++)
		{
			CityDistance cw = cityDist.get(c);
			Poi pa = getCityWPoint(cw.a);
			Poi pb = getCityWPoint(cw.b);
			Way way = buildWay(pa, pb);
			if (way != null)
				ways.add(way);
		}

		for (Poi p : _points)
			p.normalize();

		_pointIndexes = null;
		_wayBuff = null;
	}

	class PointDist implements Comparable<PointDist>
	{
		public Poi a;
		public Poi b;
		public double d;

		public PointDist(Poi pa, Poi pb, double dist)
		{
			a = pa;
			b = pb;
			d = dist;
		}

		@Override
		public int compareTo(PointDist o)
		{
			return (int) ((d - o.d) * 10000000);
		}
	}

	class CityDistance implements Comparable<CityDistance>
	{
		public City a, b;
		public double d;

		public CityDistance(City ca, City cb, double dist)
		{
			a = ca;
			b = cb;
			d = dist;
		}

		@Override
		public int compareTo(CityDistance o)
		{
			return (int) ((d - o.d) * 10000000);
		}
	}

	private Way buildWay(Poi pa, Poi pb)
	{
		log.println(pa + " - " + pb);

		ArrayList<Poi> ignore = new ArrayList<>();

		ArrayList<Poi> points = new ArrayList<Poi>(4);
		points.add(pa);
		ignore.add(pa);

		Poi p = pa;

		while (true)
		{
			p = p.getNeighborTo(pb, ignore);
			if (p == null)
			{
				if (points.size() == 1)
				{
					// Вернулись домой, а пути все нет
					log.println("\tCicle way");
					for (Poi wp : points)
					{
						log.println("\t\t" + wp.toString());
					}
					return null;
				}

				// Обратный шаг
				log.println("\t\tBack");
				ignore.add(p);
				points.remove(points.size() - 1);
				p = points.get(points.size() - 1);
				continue;
			}
			else
				log.println("\t" + p.toString());

			points.add(p);

			// Путь найден
			if (p == pb)
				break;

			// Если попали в точку с уже проложеным маршрутом до cb, то прекратить поиски и состыковать пути
			if (p.hasWayTo(pb))
			{
				log.println("\tCrossroad: " + p);
				break;
			}

			if (p instanceof City)
			{
				log.println("\tComing in city: " + p);
				return null;
			}

			if (points.size() > 40)
			{
				log.println("\tBig distance: " + pa.distance(pb));
				return null;
			}

			ignore.add(p);
		}

		for (int i = 1; i < points.size(); i++)
		{
			Poi wp = points.get(i);
			Poi prev = points.get(i - 1);
			if (!prev.canTraceTo(wp) || !wp.canTraceTo(prev))
			{
				log.println("\tCan't trace " + prev + ": " + prev.traceList.size() + " \t " + wp + ": " + wp.traceList.size());
				return null;
			}
		}

		for (int i = 1; i < points.size(); i++)
		{
			Poi wp = points.get(i);
			Poi prev = points.get(i - 1);
			wp.traceTo(prev);
			prev.traceTo(wp);
		}

		Way way = new Way(pa, pb, null, points.toArray(new Poi[points.size()]));

		// информация в Poi о всех путях, которые через нее пролегают
		for (Poi wp : points)
			wp.ways.add(way);

		//way.finish();
		return way;
	}

	private Poi getCityWPoint(Poi c)
	{
		MapCell cell = getPointCell(c);
		if (cell != null)
		{
			for (Poi p : cell.points)
				if (p.x == c.x && p.y == c.y)
					return p;
		}
		return null;
	}

	//endregion

	//region Glyphs

	void buildGlyphs()
	{
		System.out.println("Build glyphs...");
		_glyphs = new ArrayList<>();
		buildGlyphs(4, 8f, GlobalMap.MOUNTAIN_ID, Glyph.T_MOUNT);
		buildGlyphs(8, 10f, GlobalMap.MOUNTAIN2_ID, Glyph.T_BIGMOUNT);
		buildGlyphs(2, 3f, GlobalMap.FOREST_ID, Glyph.T_TREE);
		buildGlyphs(100, 150f, GlobalMap.SEA_LIGHT_ID, Glyph.T_SEA);
		Collections.sort(_glyphs);
	}

	// glyph_perc - плотность глифов
	void buildGlyphs(int glyph_rad, float glyph_perc, byte terr_type, int glyph_type)
	{
		float[][] levels2 = new float[width][height];
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				if (_pixels[x][y] != terr_type) continue;
				levels2[x][y] = (float) perlin.noise2((float) x / glyph_perc, (float) y / glyph_perc); // Шум для глифов
			}

		for (int y = glyph_rad; y < height - glyph_rad; y++)
			for (int x = width - glyph_rad - 1; x >= glyph_rad; x--)
			{
				float v = levels2[x][y];
				if (_pixels[x][y] != terr_type) continue;

				boolean isPass = true;

				for (int dy = y - glyph_rad; dy <= y + glyph_rad; dy++)
				{
					for (int dx = x - glyph_rad; dx <= x + glyph_rad; dx++)
					{
						if (dx == x && dy == y) continue;

						if (levels2[dx][dy] < v || _filter[dx][dy])
						{
							isPass = false;
							break;
						}
					}

					if (!isPass)
						break;
				}

				if (isPass)
				{
					Glyph g = new Glyph();
					g.x = x;
					g.y = y;
					g.type = glyph_type;
					g.subType = Math.abs(random.nextInt());

					_glyphs.add(g);
				}
			}
	}

	//endregion

	//region Tools

	private static byte[][] copyPixels(byte[][] pixels)
	{
		int w = pixels.length;
		int h = pixels[0].length;
		byte[][] myClone = new byte[w][];
		for (int x = 0; x < w; x++)
		{
			byte[] row = pixels[x];
			myClone[x] = new byte[h];
			System.arraycopy(row, 0, myClone[x], 0, h);
		}
		return myClone;
	}

	private void setValue(byte[][] arr, int x, int y, byte val)
	{
		if (x < 0 || y < 0 || x >= arr.length || y >= arr[x].length) return;
		arr[x][y] = val;
	}

	private static boolean isNear4Less(byte[][] pixels, byte t, int x, int y)
	{
		if (x > 0 && pixels[x - 1][y] <= t)
			return true;
		if (x < pixels.length - 1 && pixels[x + 1][y] <= t)
			return true;
		if (y > 0 && pixels[x][y - 1] <= t)
			return true;
		if (y < pixels[x].length - 1 && pixels[x][y + 1] <= t)
			return true;

		return false;
	}

	private Point getNearMask(byte[][] pixels, int x, int y, int r, int mask)
	{
		int rx = x + r;
		int ry = y + r;
		for (int tx = x - r; tx <= rx; tx++)
		{
			for (int ty = y - r; ty <= ry; ty++)
			{
				if (tx >= 0 && ty >= 0 && tx < width && ty < height)
					if ((pixels[tx][ty] & mask) > 0) return new Point(tx, ty);
			}
		}
		return null;
	}

	private Point getNearValue(byte[][] pixels, int x, int y, int r, byte flag)
	{
		int rx = x + r;
		int ry = y + r;
		for (int tx = x - r; tx <= rx; tx++)
		{
			for (int ty = y - r; ty <= ry; ty++)
			{
				if (tx >= 0 && ty >= 0 && tx < width && ty < height)
					if (pixels[tx][ty] == flag) return new Point(tx, ty);
			}
		}
		return null;
	}

	/*private boolean isNear8Mask(byte[][] pixels, int x, int y, int mask)
	{
		if (x > 0)
		{
			if ((pixels[x - 1][y] & mask) > 0)
				return true;
			if (y > 0 && (pixels[x - 1][y - 1] & mask) > 0)
				return true;
			if (y < height - 1 && (pixels[x - 1][y + 1] & mask) > 0)
				return true;
		}
	
		if (x < width - 1)
		{
			if ((pixels[x + 1][y] & mask) > 0)
				return true;
			if (y > 0 && (pixels[x + 1][y - 1] & mask) > 0)
				return true;
			if (y < height - 1 && (pixels[x + 1][y + 1] & mask) > 0)
				return true;
		}
	
		if (y > 0 && (pixels[x][y - 1] & mask) > 0)
			return true;
		if (y < height - 1 && (pixels[x][y + 1] & mask) > 0)
			return true;
	
		return false;
	}*/

	/*private boolean isNear4(byte[][] pixels, byte t, int x, int y)
	{
		if (x > 0 && pixels[x - 1][y] == t)
			return true;
		if (x < width - 1 && pixels[x + 1][y] == t)
			return true;
		if (y > 0 && pixels[x][y - 1] == t)
			return true;
		if (y < height - 1 && pixels[x][y + 1] == t)
			return true;
	
		return false;
	}
	
	private boolean isNear8(byte[][] pixels, byte t, int x, int y)
	{
		if (x > 0)
		{
			if (pixels[x - 1][y] == t)
				return true;
			if (y > 0 && pixels[x - 1][y - 1] == t)
				return true;
			if (y < height - 1 && pixels[x - 1][y + 1] == t)
				return true;
		}
	
		if (x < width - 1)
		{
			if (pixels[x + 1][y] == t)
				return true;
			if (y > 0 && pixels[x + 1][y - 1] == t)
				return true;
			if (y < height - 1 && pixels[x + 1][y + 1] == t)
				return true;
		}
	
		if (y > 0 && pixels[x][y - 1] == t)
			return true;
		if (y < height - 1 && pixels[x][y + 1] == t)
			return true;
	
		return false;
	}*/

	abstract class AroundIterator
	{
		abstract void iterate(int x, int y);

		public byte val;

		public void work(int x, int y, int r)
		{
			int rx = x + r;
			int ry = y + r;
			for (int tx = x - r; tx <= rx; tx++)
			{
				for (int ty = y - r; ty <= ry; ty++)
				{
					if (tx >= 0 && ty >= 0 && tx < width && ty < height && !(tx == x && ty == y))
						iterate(tx, ty);
				}
			}
		}

		public void work(int x, int y)
		{
			work(x, y, 1);
		}
	}

	AroundIterator aroundSetFilter = new AroundIterator()
	{
		@Override
		void iterate(int x, int y)
		{
			_filter[x][y] = true;
		}
	};

	AroundIterator aroundSetPixels = new AroundIterator()
	{
		@Override
		void iterate(int x, int y)
		{
			_pixels[x][y] = val;
		}
	};

	private boolean isWaterOnLine(int x1, int y1, int x2, int y2)
	{
		int dx = x2 - x1;
		int dy = y2 - y1;
		int adx = Math.abs(dx);
		int ady = Math.abs(dy);

		double mx, my;
		int c;
		if (adx > ady)
		{
			c = adx;
			mx = x2 > x1 ? 1 : -1;
			my = (double) dy / adx;
		}
		else
		{
			c = ady;
			mx = (double) dx / ady;
			my = y2 > y1 ? 1 : -1;
		}

		double x = x1;
		double y = y1;
		int ix = x1;
		int iy = y1;
		for (int i = 0; i < c; i++)
		{
			if ((_pixels[ix][iy] & 0xF0) == GlobalMap.TYPE_SEA)
				return true;

			x += mx;
			y += my;
			ix = (int) x;
			iy = (int) y;
		}

		return false;
	}

	private boolean isRoadOnLine(int x1, int y1, int x2, int y2)
	{
		int dx = x2 - x1;
		int dy = y2 - y1;
		int adx = Math.abs(dx);
		int ady = Math.abs(dy);

		double mx, my;
		int c;
		if (adx > ady)
		{
			c = adx;
			mx = x2 > x1 ? 1 : -1;
			my = (double) dy / adx;
		}
		else
		{
			c = ady;
			mx = (double) dx / ady;
			my = y2 > y1 ? 1 : -1;
		}

		double x = x1;
		double y = y1;
		int ix = x1;
		int iy = y1;
		for (int i = 0; i < c - 2; i++)
		{
			x += mx;
			y += my;
			ix = (int) x;
			iy = (int) y;

			if (_wayBuff[ix][iy]) return true;
		}

		return false;
	}

	private void setRoadOnLine(int x1, int y1, int x2, int y2)
	{
		int dx = x2 - x1;
		int dy = y2 - y1;
		int adx = Math.abs(dx);
		int ady = Math.abs(dy);

		double mx, my;
		int c;
		if (adx > ady)
		{
			c = adx;
			mx = x2 > x1 ? 1 : -1;
			my = (double) dy / adx;
		}
		else
		{
			c = ady;
			mx = (double) dx / ady;
			my = y2 > y1 ? 1 : -1;
		}

		double x = x1;
		double y = y1;
		int ix = x1;
		int iy = y1;
		for (int i = 0; i < c - 2; i++)
		{
			x += mx;
			y += my;
			ix = (int) x;
			iy = (int) y;

			_wayBuff[ix][iy] = true;
		}
	}

	private void setCircle(byte[][] map, byte val, int x, int y, int r)
	{
		if (r == 0) return;

		setValue(map, x, y, val);

		if (r > 1)
		{
			setValue(map, x - 1, y, val);
			setValue(map, x + 1, y, val);
			setValue(map, x, y - 1, val);
			setValue(map, x, y + 1, val);
		}

		if (r > 2)
		{
			setValue(map, x - 2, y - 1, val);
			setValue(map, x - 2, y, val);
			setValue(map, x - 2, y + 1, val);

			setValue(map, x + 2, y - 1, val);
			setValue(map, x + 2, y, val);
			setValue(map, x + 2, y + 1, val);

			setValue(map, x - 1, y - 2, val);
			setValue(map, x, y - 2, val);
			setValue(map, x + 1, y - 2, val);

			setValue(map, x - 1, y + 2, val);
			setValue(map, x, y + 2, val);
			setValue(map, x + 1, y + 2, val);

			setValue(map, x + 1, y + 1, val);
			setValue(map, x - 1, y + 1, val);
			setValue(map, x + 1, y - 1, val);
			setValue(map, x - 1, y - 1, val);
		}
	}

	//endregion

	//region Log

	PrintStream log;

	void initLog()
	{
		log = new PrintStream(new NullOutputStream());
		/*try
		{
			log = new PrintStream(new FileOutputStream("build.log", false));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}*/
	}

	//endregion
}
