package com.deadman.dh.model;

import java.util.ArrayList;
import java.util.Random;

import com.deadman.dh.Game;
import com.deadman.dh.battle.BattleSide;
import com.deadman.dh.battle.ComputerSide;
import com.deadman.dh.battle.MissionEngine;
import com.deadman.dh.battle.PlayerSide;

public class BattleSideInfo
{
	private boolean isEnemy;
	private ArrayList<UnitInfo> units = new ArrayList<>();

	public BattleSideInfo(boolean enemy)
	{
		isEnemy = enemy;
	}

	public BattleSideInfo addUnits(String type, int min, int max)
	{
		units.add(new UnitInfo(type, min, max));
		return this;
	}

	static Random rnd = new Random();

	public BattleSide getSide(MissionEngine engine)
	{
		// Генерация врагов
		BattleSide side = Game.AI_CONTROL ? new PlayerSide(engine) : new ComputerSide(engine);
		side.isEnemy = isEnemy;

		for (UnitInfo ui : units)
		{
			int cnt;
			if (ui.countMin == ui.countMax)
				cnt = ui.countMin;
			else
				cnt = ui.countMin + rnd.nextInt(ui.countMax - ui.countMin + 1);

			for (int j = 0; j < cnt; j++)
			{
				GameCharacter ch = GameCharacter.generateClass(ui.type, engine.mission.level);
				ch.name = "Unit #" + j;
				ch.equip(Game.ItemTypes.sword);
				side.addUnit(ch);
			}
		}

		return side;
	}

	class UnitInfo
	{
		public String type;
		public int countMin;
		public int countMax;

		public UnitInfo(String t, int min, int max)
		{
			type = t;
			countMin = min;
			countMax = max;
		}
	}
}