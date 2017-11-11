package com.deadman.dh.guild;

import java.util.ArrayList;
import java.util.HashMap;

import com.deadman.dh.R;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.jgame.drawing.Drawable;

public class GuildBuildingType
{
	public final int id;
	public final String name;
	public String nameWhat;
	public final int price;
	public final int buildTime;
	public byte learnSkill; // Скилл, который качается в этом помещении
	public int squarePerUnit; // Площадь на одного человека

	private Drawable _picIcon;
	private IsoMap _preview;

	public int minW = 2, minH = 3; // Минимальные размеры

	public ArrayList<IGuildPatternSource> mapPatterns = new ArrayList<>();

	public GuildBuildingType(int id, String name, int price, int buildTimeDays)
	{
		this.id = id;
		this.name = name;
		this.price = price;
		this.buildTime = buildTimeDays * GlobalEngine.getTimeFromDays(1);
	}

	public Drawable getPicture()
	{
		if (_picIcon == null)
		{
			if (id < R.ui.building_icon.length)
				_picIcon = Drawable.get(R.ui.building_icon[id]);
			else
				_picIcon = Drawable.get(R.ui.building_icon_1);
		}
		return _picIcon;
	}

	public IsoMap getMapPreview() // TODO Генерация карты
	{
		if (_preview == null)
		{
			_preview = GuildMapBuilder.buildPreview(this);
		}
		return _preview;
	}

	public boolean isTunnel()
	{
		return this == TUNNEL;
	}

	public boolean isEntry()
	{
		return this == ENTRY;
	}

	public boolean isPassSize(int w, int h)
	{
		return (w >= minW && h >= minH) || (w >= minH && h >= minW);
	}

	public void reset()
	{
		for (IGuildPatternSource p : mapPatterns)
			p.reset();
	}

	// Building
	private GuildBuildingType addPattern(GuildBuildPattern p)
	{
		mapPatterns.add(p);
		return this;
	}

	private GuildBuildingType addPatternGroup(GuildPatternGroup group)
	{
		mapPatterns.add(group);
		return this;
	}

	private GuildBuildingType setNameWhat(String value)
	{
		nameWhat = value;
		return this;
	}

	private GuildBuildingType setSkill(byte value)
	{
		learnSkill = value;
		return this;
	}

	private GuildBuildingType setUnitSquare(int value)
	{
		squarePerUnit = value;
		return this;
	}

	private static HashMap<Integer, GuildBuildingType> buildings;
	public static ArrayList<GuildBuildingType> all;
	public static GuildBuildingType ENTRY;
	public static GuildBuildingType TUNNEL;

	public static GuildBuildingType get(int id)
	{
		return buildings.get(id);
	}

	private static void register(GuildBuildingType t)
	{
		buildings.put(t.id, t);
		all.add(t);
	}

	public static void initBuildings()
	{
		buildings = new HashMap<>();
		all = new ArrayList<>();

		register(ENTRY = new GuildBuildingType(0, "Вход в гильдию", 0, 0)
				.addPattern(new GuildBuildPattern(R.maps.guild.b0_0_map)));

		register(TUNNEL = new GuildBuildingType(1, "Проход", 5, 0));

		register(new GuildBuildingType(2, "Штаб", 50, 10)
				.setNameWhat("штаба")
				.addPattern(new GuildBuildPattern(R.maps.guild.b2_0_map).setAnchor(GuildMapBuilder.ANCHOR_CENTER).setMax(1))
				.addPatternGroup(new GuildPatternGroup()
						.add(new GuildBuildPattern(R.maps.guild.b2_1_map).setAnchor(GuildMapBuilder.ANCHOR_TOP)) // Cabinet
						.add(new GuildBuildPattern(R.maps.guild.b2_2_map).setAnchor(GuildMapBuilder.ANCHOR_LEFT)) // Cabinet
						.generate("Candle")
						.generate("Image")
						.generate("Gobelen")));
		
		register(new GuildBuildingType(3, "Склад", 10, 5)
				.setNameWhat("склада"));

		register(new GuildBuildingType(4, "Казарма", 30, 9)
				.setNameWhat("казармы")
				.setUnitSquare(6));

		register(new GuildBuildingType(5, "Барак", 100, 20)
				.setNameWhat("барака")
				.setSkill(SWORD)
				.setUnitSquare(6));

		register(new GuildBuildingType(6, "Стрельбище", 70, 16)
				.setNameWhat("стрельбища")
				.setSkill(BOW)
				.setUnitSquare(12));

		register(new GuildBuildingType(7, "Мастерская", 150, 24)
				.setNameWhat("мастерской")
				.setSkill(CRAFT)
				.setUnitSquare(6));

		register(new GuildBuildingType(8, "Кузница", 200, 29)
				.setNameWhat("кузницы")
				.setSkill(CRAFT)
				.setUnitSquare(6));

		register(new GuildBuildingType(9, "Библиотека", 250, 18)
				.setNameWhat("библиотеки")
				.setSkill(SCIENCE)
				.setUnitSquare(6));

		register(new GuildBuildingType(10, "Лаборатория", 300, 32)
				.setNameWhat("лаборатории")
				.setSkill(SCIENCE)
				.setUnitSquare(12));

		register(new GuildBuildingType(11, "Обсерватория", 500, 65)
				.setNameWhat("обсерватории")
				.setSkill(SCIENCE)
				.setUnitSquare(24));
	}

	public static final byte SWORD = 1;
	public static final byte BOW = 2;
	public static final byte CRAFT = 3;
	public static final byte SCIENCE = 4;
}
