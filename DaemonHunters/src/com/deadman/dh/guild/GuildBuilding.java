package com.deadman.dh.guild;

import java.util.ArrayList;

import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.model.Rectangle;
import com.deadman.dh.model.Unit;

public class GuildBuilding
{
	public Guild guild;
	public GuildBuildingType type;

	public int build_progress = 0;

	public final ArrayList<Unit> units = new ArrayList<>();

	public Rectangle rect;

	// Конструктор для загрузки игры
	public GuildBuilding(Guild g, Rectangle rect, int buildingTypeId)
	{
		this(g, rect, GuildBuildingType.get(buildingTypeId));
	}

	public GuildBuilding(Guild g, Rectangle rect, GuildBuildingType buildingType)
	{
		guild = g;

		this.rect = rect;
		type = buildingType;
		if (type.buildTime == 0)
			build_progress = -1;
	}

	// Вместимость (кол-во людей)
	public int getMaxUnits()
	{
		if (type.squarePerUnit == 0) return 0;
		return square() / type.squarePerUnit;
	}

	public int square()
	{
		return rect.square();
	}

	private int squarePU() // Площадь на 1 юнита
	{
		if (units.size() == 0) return 0;
		return square() / units.size();
	}

	public boolean isUnitsFull()
	{
		return units.size() >= getMaxUnits();
	}

	public void tick(long time, int dt)
	{
		if (build_progress >= 0)
		{
			build_progress += dt;
			if (build_progress >= type.buildTime)
			{
				build_progress = -1;
				guild.onBuildCompleted(this);
			}
			return;
		}

		if (type.learnSkill != 0)
		{
			for (Unit u : units)
			{
				switch (type.learnSkill)
				{
					case GuildBuildingType.SWORD:
						u.skSword.learn(dt, squarePU());
						break;
					case GuildBuildingType.BOW:
						u.skThrow.learn(dt, squarePU());
						break;
					case GuildBuildingType.CRAFT:
						u.skCraft.learn(dt, squarePU());
						break;
					case GuildBuildingType.SCIENCE:
						u.skScience.learn(dt, squarePU());
						break;
					default:
						break;
				}
			}
		}
	}

	public double buildingPerc()
	{
		return (double) build_progress / type.buildTime;
	}

	/**
	 * Возвращает true, если еще строится
	 * @return
	 */
	public boolean isBuild()
	{
		return build_progress >= 0;
	}

	public int buildRemainDays()
	{
		return GlobalEngine.getDaysFromTime(type.buildTime - build_progress);
	}

	@Override
	public String toString()
	{
		return type.name + " " + rect;
	}

	public String info()
	{
		if (type.squarePerUnit > 0)
			return String.format("ПЛОЩАДЬ: %d\nРАЗМЕР: %dx%d\nМЕСТ: %d\nЗАНЯТО: %d", square(), rect.width, rect.height, getMaxUnits(), units.size());
		return String.format("ПЛОЩАДЬ: %d\nРАЗМЕР: %dx%d", square(), rect.width, rect.height);
	}
}
