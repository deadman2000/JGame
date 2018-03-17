package com.deadman.dh.city;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.deadman.dh.Game;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.dh.model.Poi;
import com.deadman.dh.model.generation.CityBuilder;
import com.deadman.jgame.drawing.Picture;
import com.deadman.jgame.ui.LoadingScreen;
import com.deadman.jgame.ui.ProgressStatus;

public class City extends Poi
{
	public int id;
	public String name;
	public int level; // Уровень города. Чем больше, тем крупнее
	public static final int LVL_SMALL = 1;
	public static final int LVL_MEDIUM = 2;
	public static final int LVL_BIG = 3;

	public Picture imgLabel;
	public Rectangle labelRect;

	public Picture picMap;
	public ArrayList<Building> houses;
	public short[][] housesMap;
	public byte[][] map;
	public int width, height;

	public Guild guild;

	public City(int lvl)
	{
		level = lvl;
	}

	public int mapRadius()
	{
		switch (level)
		{
			case LVL_SMALL:
				return 1;
			case LVL_MEDIUM:
				return 2;
			case LVL_BIG:
				return 3;
			default:
				System.err.println("Unsupported map level: " + level);
				return 0;
		}
	}

	@Override
	public String toString()
	{
		return name + " (" + x + ":" + y + ")";
	}

	public String description()
	{
		return "город " + name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public Guild createGuild()
	{
		if (guild != null) return guild;
		guild = new Guild(this);
		Game.guilds.add(guild);
		return guild;
	}

	public long seed;

	static final BufferedImage dumb_img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
	// TODO Убрать когда будут шрифты для карты
	public void genLabel(Font font, Color color, Color bgrColor)
	{
		Graphics g = dumb_img.getGraphics();
		Graphics2D g2 = (Graphics2D) g;

		//String title = name + " " + id;
		String title = name;

		FontRenderContext frc = g2.getFontRenderContext();
		GlyphVector gv = font.createGlyphVector(frc, title);
		Rectangle bounds = gv.getPixelBounds(null, 0, 0);

		BufferedImage img = Picture.createImage(bounds.width + 2, bounds.height + 2);
		g = img.getGraphics();
		g2 = (Graphics2D) g;

		if (bgrColor.getAlpha() != 0)
		{
			g.setColor(bgrColor);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
		}

		if (this.level >= 3)
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setFont(font);
		g.setColor(color);
		g.drawString(title, -bounds.x + 1, -bounds.y + 1);

		imgLabel = new Picture(img);
		if (level > 1)
			labelRect = new Rectangle(x + 2, y - imgLabel.height - 2, imgLabel.width, imgLabel.height);
		else
			labelRect = new Rectangle(x + 4, y - imgLabel.height - 2, imgLabel.width, imgLabel.height);
	}

	// http://java-sl.com/gp_effects.html
	/*public void genLabel_(GameFont font, Color bgrColor)
	{
		BufferedImage img = font.getLabel(name, bgrColor);
	
		/*BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = img.getGraphics();
		Graphics2D g2 = (Graphics2D) g;
	
		FontRenderContext frc = g2.getFontRenderContext();
		GlyphVector gv = font.createGlyphVector(frc, name);
		Rectangle bounds = gv.getPixelBounds(null, 0, 0);
	
		img = new BufferedImage(bounds.width + 2, bounds.height + 2, BufferedImage.TYPE_INT_ARGB);
		g = img.getGraphics();
		g2 = (Graphics2D) g;
	
		if (bgrColor.getAlpha() != 0)
		{
			g.setColor(bgrColor);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
		}
	
		if (this.level >= 3)
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
		g.setFont(font);
		g.setColor(color);
		g.drawString(name, -bounds.x + 1, -bounds.y + 1);*/
	/*
			imgLabel = new Picture(img);
			if (level > 1)
				labelRect = new Rectangle(x + 2, y - imgLabel.height - 2, imgLabel.width, imgLabel.height);
			else
				labelRect = new Rectangle(x + 1 - imgLabel.width / 2, y - imgLabel.height - 2, imgLabel.width, imgLabel.height);
		}*/

	public void generate()
	{
		generate(null);
	}

	CityBuilder builder;

	public void generate(ProgressStatus st)
	{
		if (builder != null)
			return;

		CityBuilder b = new CityBuilder(this, st);
		b.buildMap();
		houses = b.houses;
		housesMap = b.housesMap;
		map = b.map;
		width = map.length;
		height = map[0].length;
		builder = b;
	}

	public IsoMap createIsoMap()
	{
		System.out.println("Begin create iso-map");

		IsoMap m = new IsoMap(width, height, 5, 0);
		m.fillFloor(0);

		System.out.println("Set roads...");
		IsoSprite sprRoad = IsoSprite.byName("RockFloor");
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (map[x][y] == 1)
				{
					MapCell cell = m.getCell(x, y, m.zeroLevel);
					sprRoad.setTo(cell);
				}
			}
		}

		System.out.println("Set houses...");
		for (Building h : houses)
		{
			h.type.map.copyTo(m, h.x, h.y);
		}

		System.out.println("Completed");
		return m;
	}

	public void show()
	{
		if (housesMap == null || picMap == null)
		{
			Thread th = new Thread(building);
			th.start();
		}
		else
		{
			new CityEngine(this).show();
		}
	}

	Runnable building = new Runnable()
	{
		@Override
		public void run()
		{
			CityEngine eng = new CityEngine(City.this);
			ProgressStatus st = new ProgressStatus();
			LoadingScreen.showLoading(st, eng);
			generate(st);
			picMap = builder.getPicture();
			st.progress = st.max;
		}
	};

	public Building getHouse(int id)
	{
		return houses.get(id);
	}
}
