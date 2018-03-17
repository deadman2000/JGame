package com.deadman.dh.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.deadman.dh.R;
import com.deadman.dh.city.City;
import com.deadman.dh.global.Glyph;
import com.deadman.dh.global.MapStyle;
import com.deadman.dh.model.generation.GlobalMapGenerator;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.Effects;
import com.deadman.jgame.drawing.Picture;
import com.deadman.jgame.ui.ProgressStatus;

public class GlobalMap
{
	static final boolean DRAW_WAYS = false;
	public static final int MAP_ZOOM = 300;

	public byte[][] pixels;
	public float[][] heights;
	public City[] cities;
	public Glyph[] glyphs;
	public Poi[] points;
	private GlobalMapGenerator generator;

	private int width, height;
	public Rectangle rect;

	public GlobalMap(GlobalMapGenerator gen, int w, int h)
	{
		width = w;
		height = h;
		generator = gen;
		rect = new Rectangle(w, h);
	}

	public static final byte TYPE_SEA = 0x10; // вода
	public static final byte SEA_LIGHT_ID = TYPE_SEA | 0;
	public static final byte SEA_MIDDLE_ID = TYPE_SEA | 1;
	public static final byte SEA_DARK_ID = TYPE_SEA | 2;

	public static final byte TYPE_TERRAIN = 0x20; // суша
	public static final byte TERRAIN_ID = TYPE_TERRAIN | 0;
	public static final byte COAST_ID = TYPE_TERRAIN | 1;
	public static final byte COASTLINE_ID = TYPE_TERRAIN | 2;
	public static final byte FOREST_ID = TYPE_TERRAIN | 3;

	public static final byte TYPE_MOUNTAIN = 0x30; // горы
	public static final byte MOUNTAIN_ID = TYPE_MOUNTAIN | 0;
	public static final byte MOUNTAIN2_ID = TYPE_MOUNTAIN | 1;

	public static final byte TYPE_RIVER = 0x40; // реки
	public static final byte RIVER_ID = TYPE_RIVER | 0; // Маленькая река
	public static final byte RIVER2_ID = TYPE_RIVER | 1; // Средняя река
	public static final byte RIVER3_ID = TYPE_RIVER | 2; // Большая река

	public static boolean isFlag(byte val, byte t)
	{
		return (val & 0xF0) == t;
	}

	public static final byte CITY_ID = 100;
	public static final byte INK_ID = 101;
	public static final byte LABEL_ID = 102;

	public boolean isMapFlag(int x, int y, byte flag)
	{
		return (pixels[x][y] & 0xF0) == flag;
	}

	public BufferedImage fillBackground(MapStyle style, ProgressStatus status)
	{
		BufferedImage img = Picture.createImage(width, height);

		int n = width / 50;

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				img.setRGB(x, y, style.getColor(pixels[x][y]).getRGB());
			}

			n--;
			if (n == 0)
			{
				status.progress++;
				n = width / 50;
			}
		}

		Graphics2D g = img.createGraphics();
		drawGlyphs(g);

		drawCities(g, img, style);

		if (DRAW_WAYS)
		{
			g.setColor(Color.gray);
			for (Poi p1 : points)
			{
				for (Poi p2 : p1.neighborhood)
				{
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
			}

			g.setColor(Color.blue);
			for (Poi p1 : points)
			{
				for (Poi p2 : p1.traceList)
				{
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
			}

			for (City c : cities)
				img.setRGB(c.x, c.y, 0xFF00FF00);
		}

		Effects.randomizeColors(img);

		/*for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				RiverNode rn = generator.riverMap[x][y];
				if (rn != null)
					if (rn.id == -1)
						img.setRGB(x, y, 0xffff0000);
					else if (rn.id == -1)
						img.setRGB(x, y, 0xff00ff00);
					else
						img.setRGB(x, y, 0xff0000ff);
			}*/

		return img;
	}

	static Color TRANSPARENT = new Color(0, 0, 0, 0);

	private void drawCities(Graphics2D g, BufferedImage img, MapStyle style)
	{
		Color colorInk = style.getColor(GlobalMap.INK_ID);

		g.setColor(colorInk);
		for (City c : cities)
		{
			switch (c.level)
			{
				case City.LVL_SMALL:
					g.setColor(colorInk);
					g.drawOval(c.x - 1, c.y - 1, 2, 2);
					break;
				case City.LVL_MEDIUM:
					g.setColor(colorInk);
					g.drawOval(c.x - 2, c.y - 2, 4, 4);
					img.setRGB(c.x, c.y, colorInk.getRGB());
					break;
				case City.LVL_BIG:
					g.drawOval(c.x - 1, c.y - 1, 2, 2);
					g.drawOval(c.x - 3, c.y - 3, 6, 6);
					break;

				default:
					img.setRGB(c.x, c.y, colorInk.getRGB());
					break;
			}
		}

		setLabels(style);
	}

	void setLabels(MapStyle style)
	{
		Color colorLabel = style.getColor(GlobalMap.LABEL_ID);
		Font fntB = new Font("Times New Roman", Font.ITALIC | Font.BOLD, 13);
		AffineTransform transf = new AffineTransform();
		fntB = fntB.deriveFont(transf);

		//Sprite sprB = Sprite.getFont(fntB, colorLabel);

		//Font fntM = new Font("Times New Roman", Font.ITALIC, 10);

		//Sprite sprM = Sprite.getFont(fntM, colorLabel);

		for (City c : cities)
		{
			switch (c.level)
			{
				/*case City.LVL_SMALL:
					c.genLabel(fntM, colorLabel, Color.white);
					break;
				case City.LVL_MEDIUM:
					c.genLabel(fntM, colorLabel, TRANSPARENT);
					break;*/
				case City.LVL_BIG:
					c.genLabel(fntB, colorLabel, TRANSPARENT);
					break;
			}
			normalizeLabels(c);
		}

		for (City c : cities)
		{
			if (c.imgLabel != null)
				normalizeLabels(c);
		}

		for (City c : cities)
		{
			if (c.labelRect == null) continue;

			Rectangle r = c.labelRect;
			if (r.x < 0)
				r.x = 0;
			else if (r.x + r.width > width)
				r.x = width - r.width;
			if (r.y < 0)
				r.y = 0;
			else if (r.y + r.height > height)
				r.y = height - r.height;
		}
	}

	void normalizeLabels(City c2)
	{
		if (c2.level == 1) return;
		if (c2.imgLabel == null) return;

		Rectangle r2 = c2.labelRect;

		for (City c1 : cities)
		{
			if (c1 == c2) continue;
			if (c1.imgLabel == null) continue;
			if (c1.level == 1) continue;

			Rectangle r1 = c1.labelRect;
			if (r1.intersects(r2))
			{
				int botton = r1.y + r1.height;
				int right = r1.x + r1.width;

				// Наложение сверху
				if (botton > r2.y && botton < r2.y + r2.height)
				{
					r2.y = r1.y + r1.height; // Смещаем вниз исходный
					continue;
				}

				// Наложение справа
				if (r1.x < r2.x + r2.width && r1.x > r2.x)
				{
					r2.x = r1.x - r2.width; // Смещаем влево исходный
					continue;
				}

				// Наложение снизу
				if (r1.y > r2.y && r1.y < r2.y + r2.height)
				{
					if (c2.level > 1)
						r1.y = r2.y + r2.height; // Смещаем вниз чужой
					else
						r2.y = r1.y - r2.height; // Смещаем вверх исходный
					continue;
				}

				// Наложение слева
				if (right > r2.x && right < r2.x + r2.width)
				{
					if (c2.level > 1)
						r1.x = r2.x - r1.width; // Смещаем влево чужой
					else
						r2.x = r1.x + r1.width; // Смещаем вправо исходный
					continue;
				}
			}
		}
	}

	private void drawGlyphs(Graphics2D g)
	{
		if (glyphs == null) return;

		Drawable[][] glyph_img = loadGplyphs();

		Drawable imgG;
		for (Glyph gl : glyphs)
		{
			Drawable[] imgs = glyph_img[gl.type];
			imgG = imgs[gl.subType % imgs.length];
			drawGlyph(g, imgG, gl.x, gl.y);
		}
	}

	private Drawable[][] loadGplyphs()
	{
		Drawable[][] glyph_img = new Drawable[Glyph.TYPES_COUNT][];
		glyph_img[Glyph.T_MOUNT] = loadGlyphs(R.glyphs.mount);
		glyph_img[Glyph.T_BIGMOUNT] = loadGlyphs(R.glyphs.bigmount);
		glyph_img[Glyph.T_TREE] = loadGlyphs(R.glyphs.tree);
		glyph_img[Glyph.T_SEA] = loadGlyphs(R.glyphs.sea);
		return glyph_img;
	}

	private void drawGlyph(Graphics2D g, Drawable img, int x, int y)
	{
		img.drawTo(g, x - img.width / 2, y - img.height + 3);
	}

	private Drawable[] loadGlyphs(int[] ids)
	{
		Drawable[] glyphs = new Drawable[ids.length];
		for (int i = 0; i < ids.length; i++)
		{
			glyphs[i] = Drawable.get(ids[i]);
		}
		return glyphs;
	}

	/**
	 * Возвращает список ближайших точек интереса
	 * @param x
	 * @param y
	 * @param r радиус поиска
	 * @return
	 */
	public Poi[] getPois(int x, int y, int r)
	{
		int r2 = r * r;
		ArrayList<Poi> result = new ArrayList<>();
		for (Poi p : points)
		{
			if (p.distanceSq(x, y) < r2)
				result.add(p);
		}

		return result.toArray(new Poi[result.size()]);
	}

	public Way trace(Poi traceFrom, Poi traceTo, Mission m)
	{
		System.out.println("Trace " + traceFrom + " to " + traceTo);
		if (traceFrom == traceTo) return null;

		for (Poi p : points)
			p.weight = 0;

		// Эвристический поиск любого пути
		ArrayList<Poi> points = new ArrayList<>();
		points.add(traceFrom);
		{
			ArrayList<Poi> ignore = new ArrayList<>();
			ignore.add(traceFrom);

			Poi p = traceFrom;
			while (p != traceTo)
			{
				p = p.getNeighborTo2(traceTo, ignore);
				if (p == null)
				{
					if (points.size() == 1)
					{
						// Вернулись домой, а пути все нет
						System.out.println("no way");
						return null;
					}

					// Обратный шаг
					points.remove(points.size() - 1);
					p = points.get(points.size() - 1);
					continue;
				}

				points.add(p);

				if (p == traceTo) break;

				ignore.add(p);
			}
		}

		// Оптимизация
		{
			// Расстановка весов
			Poi p1 = points.get(0);
			p1.weight = 0;
			for (int i = 1; i < points.size(); i++)
			{
				Poi p2 = points.get(i);
				p2.weight = (short) (p1.weight + p1.distance(p2));
				p1 = p2;
			}
		}

		for (int i = 0; i < points.size() - 1; i++)
		{
			Poi p1 = points.get(i);
			Poi p2 = points.get(i + 1);

			for (Poi n : p1.neighborhood)
			{
				if (n.weight == 0 || n == p2) continue;
				int pos = points.indexOf(n);
				if (pos < i) continue;

				short w = (short) (p1.weight + p1.distance(n));

				if (w < p2.weight) // Тут путь короче, чем уже найденый
				{
					System.out.println("Optimized " + p2.weight + " => " + w);
					int k = points.indexOf(n);

					int c = k - i - 1;
					for (int j = 0; j < c; j++)
					{
						points.get(i + 1).weight = 0;
						points.remove(i + 1);
					}

					for (int j = i; j < points.size() - 1; j++)
					{
						p2 = points.get(j + 1);
						p2.weight = (short) (p1.weight + p1.distance(p2));
						p1 = p2;
					}

					i--;
					break;
				}
			}
		}

		return new Way(traceFrom, traceTo, m, points.toArray(new Poi[points.size()]));
	}

	public SubMap getSubTerrain(double tx, double ty, double tw, double th)
	{
		return generator.getSubMap(tx, ty, tw, th);
	}
}
