package com.deadman.dh.model.generation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import com.deadman.dh.Game;
import com.deadman.dh.city.Building;
import com.deadman.dh.city.BuildingType;
import com.deadman.dh.city.City;
import com.deadman.dh.global.MapStyle;
import com.deadman.dh.model.GlobalMap;
import com.deadman.dh.model.SubMap;
import com.deadman.jgame.drawing.Picture;
import com.deadman.jgame.ui.ProgressStatus;

public class CityBuilder
{
	private Picture picMap;

	private int maxHousesCount = 5000;
	private int minHousesCount = 100;

	private int width, height;

	public byte[][] map;
	public short[][] housesMap;
	private City city;
	public SubMap terrain;

	public ArrayList<Building> houses = new ArrayList<>();
	private ArrayList<StreetBuilder> streets;

	private Random random;

	ProgressStatus _st;

	public CityBuilder(City c, ProgressStatus st)
	{
		System.out.println("City seed = " + c.seed);
		random = new Random(c.seed);

		city = c;
		_st = st;
	}

	public void buildMap()
	{
		if (picMap != null)
		{
			picMap.delete();
			picMap = null;
		}

		BuildingType.initHouseTypes();

		// Получение карты территории
		int r = city.mapRadius();
		terrain = Game.map.getSubTerrain(city.x - r, city.y - r, 1 + r * 2, 1 + r * 2);
		width = terrain.mapWidth;
		height = terrain.mapHeight;

		map = new byte[width][height];
		housesMap = new short[width][height];

		// TODO Территория города. Строительство главных улиц. Строительство мелких улиц. Определение кварталов. Строительство домов.

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				byte b = terrain.map[x][y];
				if (!(GlobalMap.isFlag(b, GlobalMap.TYPE_TERRAIN) || GlobalMap.isFlag(b, GlobalMap.TYPE_MOUNTAIN) || b == GlobalMap.COAST_ID))
					map[x][y] = NATURE_ID;
			}
		}

		if (_st != null) _st.progress += 50;

		System.out.println("Build houses");
		streets = new ArrayList<>();
		ArrayList<StreetBuilder> sarr = streets;
		houses.clear();

		//roads.add(new Road(random.nextInt(width), random.nextInt(height), random.nextInt(4) * 2));

		int sx = 0, sy = 0;

		while (houses.size() < minHousesCount)
		{
			int j = 1000;
			while (j > 0)
			{
				sx = random.nextInt(width);
				sy = random.nextInt(height);
				if (map[sx][sy] == 0) break;
				j--;
			}
			if (j <= 0)
			{
				System.err.println("No territory for street");
				break;
			}

			sarr.add(new StreetBuilder(sx, sy, random.nextInt(8)));
			while (true)
			{
				if (sarr != streets) return;

				int c = sarr.size();
				if (c == 0) break;
				if (houses.size() >= maxHousesCount) break;

				for (int i = 0; i < c; i++)
				{
					if (sarr != streets) return;

					StreetBuilder s = sarr.get(i);
					if (!s.go())
					{
						sarr.remove(i);
						i--;
						c--;
					}
				}
			}

		}

		if (_st != null) _st.progress += 50;
	}

	public Picture getPicture()
	{
		if (picMap != null) return picMap;

		// Перенос территории на карту города
		BufferedImage image = Picture.createImage(width, height);
		MapStyle style = MapStyle.city;
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (map[x][y] == STREET_ID)
					image.setRGB(x, y, STREET_COLOR);
				else if (map[x][y] == HOUSE_ID)
					image.setRGB(x, y, HOUSE_COLOR);
				else
					image.setRGB(x, y, style.getColor(terrain.map[x][y]).getRGB());
			}
		}

		picMap = new Picture(image);
		return picMap;
	}

	Random colorR = new Random();

	class StreetBuilder
	{
		int x, y;
		int dir;
		int houses; // Количество домов на улице
		int l; // Расстояние до следующего поворота
		int w; // Ширина дороги (1-5)
		int crt; // it's Crossroad Time!

		public StreetBuilder(int sx, int sy, int d)
		{
			dir = d;
			x = sx;
			y = sy;
			//sectors = random.nextInt(5) + 15;
			houses = 20 + random.nextInt(20);
			genL();
			genCRT();
			genWidth();

			setStreet(x, y);
		}

		void genL()
		{
			//l = 25+random.nextInt(15);
			l = 5 + random.nextInt(5);
		}

		void genCRT()
		{
			crt = 25 + random.nextInt(15);
		}

		void genWidth()
		{
			int wr = random.nextInt(1000);
			if (wr > 990)
				w = 5;
			else if (wr > 970)
				w = 4;
			else
				w = 3;
		}

		public boolean go()
		{
			int td;

			// Turn
			x = turnX(dir, x);
			y = turnY(dir, y);

			if (isEnd()) return false;

			crt--;
			int pd = dir;

			if (!isDiagonal(pd))
			{
				int tx = x; // Крайняя левая точка улицы
				int ty = y;
				td = normalizeDir(pd - 2);
				if (w >= 2)
				{
					tx = turnX(td, tx);
					ty = turnY(td, ty);
				}
				if (w >= 4)
				{
					tx = turnX(td, tx);
					ty = turnY(td, ty);
				}

				if (!isMapSet(tx, ty, td, 1))
				{
					if (crt < 0)
						genCrossRoad();
					else
					{
						// add house left
						if (setRandomHouse(tx, ty, pd, td))
							houses--;
					}
				}

				tx = x; // Крайняя правая точка улицы
				ty = y;
				td = normalizeDir(pd + 2);
				if (w >= 3)
				{
					tx = turnX(td, tx);
					ty = turnY(td, ty);
				}
				if (w >= 5)
				{
					tx = turnX(td, tx);
					ty = turnY(td, ty);
				}

				if (!isMapSet(tx, ty, td, 1))
				{
					if (crt < 0)
						genCrossRoad();
					else
					{
						// add house right
						if (setRandomHouse(tx, ty, pd, td))
							houses--;
					}
				}
			}
			else
			{
				if (crt < 0)
					genCrossRoad();

				td = normalizeDir(dir + 1);
				setStreet(turnX(td, x), turnY(td, y));
			}

			// Painting
			setStreet(x, y);
			if (!isDiagonal(dir))
			{
				if (w >= 2)
				{
					int sx, sy;

					td = normalizeDir(dir - 2);
					sx = turnX(td, x);
					sy = turnY(td, y);

					setStreet(sx, sy);
					if (w >= 4)
					{
						sx = turnX(td, sx);
						sy = turnY(td, sy);
						setStreet(sx, sy);
					}

					if (w >= 3)
					{
						td = normalizeDir(dir + 2);
						sx = turnX(td, x);
						sy = turnY(td, y);
						setStreet(sx, sy);

						if (w >= 5)
						{
							sx = turnX(td, sx);
							sy = turnY(td, sy);
							setStreet(sx, sy);
						}
					}
				}
			}

			return houses > 0;
		}

		private void genCrossRoad()
		{
			genCRT();

			int action = random.nextInt(1000);

			if (!isDiagonal(dir))
			{
				if (action < 100)
				{
					// New road right/left
					int i = random.nextInt(2) * 2 - 1;
					createStreet(x, y, dir + i);
				}
				else if (action < 990)
				{
					// New road left & right
					createStreet(x, y, dir - 2);
					createStreet(x, y, dir + 2);
				}
				else
				{
					// Change self direction
					int i = random.nextInt(4);
					if (i < 2)
						dir = normalizeDir(dir - 2 + i);
					else
						dir = normalizeDir(dir + i - 1);
				}
			}
			else
			{
				if (action < 300)
				{
					// New road right/left
					int i = random.nextInt(2) * 2 - 1;
					createStreet(x, y, dir + i);
				}
				else if (action < 800)
				{
					// Выпрямляем
					int i = random.nextInt(2) * 2 - 1;
					dir = normalizeDir(dir + i);
				}
				else
				{
					// New road left & right
					createStreet(x, y, dir - 2);
					createStreet(x, y, dir + 2);
				}
			}
		}

		private boolean isEnd()
		{
			if (isDiagonal(dir))
			{
				int d, tx, ty;

				d = normalizeDir(dir - 1);
				tx = turnX(d, x);
				ty = turnY(d, y);

				if (isMapSet(tx, ty)) { return true; }

				d = normalizeDir(dir + 1);
				tx = turnX(d, x);
				ty = turnY(d, y);
				if (isMapSet(tx, ty)) { return true; }
			}

			if (x < 0 || y < 0 || x >= width || y >= height || map[x][y] > 0) { return true; }

			return false;
		}
	}

	private boolean isMapSet(int tx, int ty, int d, int count)
	{
		for (int i = 0; i < count; i++)
		{
			tx = turnX(d, tx);
			ty = turnY(d, ty);
			if (isMapSet(tx, ty))
				return true;
		}
		return false;
	}

	private boolean isMapSet(int tx, int ty)
	{
		return tx < 0 || ty < 0 || tx >= width || ty >= height || map[tx][ty] > 0;
	}

	private void setStreet(int tx, int ty)
	{
		if (tx < 0 || ty < 0 || tx >= width || ty >= height)
			return;

		map[tx][ty] = STREET_ID;
	}

	private void createStreet(int x, int y, int d)
	{
		d = normalizeDir(d);
		x = turnX(d, x);
		y = turnY(d, y);

		if (x < 0 || y < 0 || x >= width || y >= height) return;
		if (isMapSet(x, y)) return;

		streets.add(new StreetBuilder(x, y, d));
	}

	static int turnX(int dir, int x)
	{
		switch (dir)
		{
			case 1: // up-right
			case 2: // right
			case 3: // right-down
				return x + 1;

			case 5: // down-left
			case 6: // left
			case 7: // up-left
				return x - 1;

			case 0: // up
			case 4: // down
			default:
				return x;
		}
	}

	static int turnY(int dir, int y)
	{
		switch (dir)
		{
			case 0: // up
			case 1: // up-right
			case 7: // up-left
				return y - 1;

			case 3: // right-down
			case 4: // down
			case 5: // down-left
				return y + 1;

			case 2: // right
			case 6: // left
			default:
				return y;
		}
	}

	static boolean isDiagonal(int dir)
	{
		return (dir % 2) == 1;
	}

	static int normalizeDir(int d)
	{
		if (d < 0)
			return 8 + d;
		else if (d > 7)
			return d - 8;
		else
			return d;
	}

	// d1 - направление улицы
	// d2 - направление дома
	private boolean setRandomHouse(int tx, int ty, int d1, int d2)
	{
		if (houses.size() >= maxHousesCount) return true;

		BuildingType[] htList = null;
		if (d2 == 4)
			htList = BuildingType.N;
		else if (d2 == 2)
			htList = BuildingType.W;
		else if (d2 == 0)
			htList = BuildingType.S;
		else if (d2 == 6)
			htList = BuildingType.E;
		else
			System.err.println("Not implemented house type for dir " + d2);

		for (int i = 0; i < 10; i++)
		{
			if (trySetRandomHouse(htList, tx, ty, d1, d2)) return true;
		}
		return false;
	}

	private boolean trySetRandomHouse(BuildingType[] htList, int tx, int ty, int d1, int d2)
	{
		BuildingType h = htList[random.nextInt(htList.length)];

		int dx = 0;
		int dy = 0;

		if (d1 == 0 || d2 == 0)
			dy = ty - h.height;
		else if (d1 == 4 || d2 == 4)
			dy = ty + 1;
		else
			System.err.println("err: " + d1 + " " + d2);

		if (d1 == 2 || d2 == 2)
			dx = tx + 1;
		else if (d1 == 6 || d2 == 6)
			dx = tx - h.width;
		else
			System.err.println("err: " + d1 + " " + d2);

		for (int x = 0; x < h.width; x++)
			for (int y = 0; y < h.height; y++)
				if (isMapSet(x + dx, y + dy))
					return false;

		//int c = random.nextInt() | 0xFF000000;

		int hPos = houses.size();
		houses.add(new Building(hPos, h, dx, dy));

		int hx, hy;
		for (int x = 0; x < h.width; x++)
			for (int y = 0; y < h.height; y++)
			{
				hx = x + dx;
				hy = y + dy;
				map[hx][hy] = HOUSE_ID;
				housesMap[hx][hy] = (short) (hPos + 1);
			}

		return true;
	}

	private static final byte STREET_ID = 1;
	private static final byte HOUSE_ID = 2;
	private static final byte NATURE_ID = 3;

	private static final int HOUSE_COLOR = 0xFFA0A0A0;
	private static final int STREET_COLOR = 0xFFFFFFFF; // 0xFFD0D0D0

}
