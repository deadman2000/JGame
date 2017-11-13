package com.deadman.dh.model;

import java.util.ArrayList;
import java.util.Random;

import com.deadman.dh.R;
import com.deadman.dh.battle.BattleSide;
import com.deadman.dh.battle.MissionEngine;
import com.deadman.dh.battle.PlayerSide;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.itemtypes.ItemType;

public class MissionScenario
{
	public String name;
	public int minLevel, maxLevel; // Допустимые уровни игры для появления миссии
	private int mapId;

	/** Очки редкости
	  * Чем больше тем больше шансов получить этот сценарий */
	public int rarityPoints;
	public int pointRange;
	private ArrayList<BattleSideInfo> sides = new ArrayList<>();

	public MissionScenario(String name, int minLevel, int maxLevel, int map, int rarity)
	{
		this.name = name;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		mapId = map;
		rarityPoints = rarity;
	}

	public MissionScenario addSide(BattleSideInfo s)
	{
		sides.add(s);
		return this;
	}

	public void generate(MissionEngine engine, Squad squad)
	{
		IsoMap map = IsoMap.loadMap(mapId);
		engine.setMap(map);
		//terrain = Game.map.getSubTerrain(mission.point.x + 0.5 - r, mission.point.y + 0.5 - r, r * 2, r * 2);
		UnitStack[] units = new UnitStack[sides.size() + 1];

		PlayerSide player = new PlayerSide(engine, squad);
		engine.sides.add(player);
		engine.player = player;
		units[0] = new UnitStack(player.units);

		// Генерация врагов
		for (int i = 0; i < sides.size(); i++)
		{
			BattleSideInfo bsi = sides.get(i);

			BattleSide side = bsi.getSide(engine);
			engine.sides.add(side);
			units[i + 1] = new UnitStack(side.units);
		}

		for (int z = 0; z < map.zheight; z++)
		{
			MapCell[][] lvl = map.cells[z];
			for (int x = 0; x < map.width; x++)
				for (int y = 0; y < map.height; y++)
				{
					MapCell c = lvl[x][y];
					for (int i = 0; i < c.items.size(); i++)
					{
						Item it = c.items.get(i);
						if (it.type == ItemType.player1)
						{
							c.items.remove(i);
							i--;
							c.setUnit(units[0].popUnit());
						}
						else if (it.type == ItemType.player2)
						{
							c.items.remove(i);
							i--;
							c.setUnit(units[1].popUnit());
						}
					}
				}
		}

		// TODO если остались нерасставленные юниты 
		for (int i = 0; i < engine.sides.size(); i++)
		{
			BattleSide s = engine.sides.get(i);
			for (int j = 0; j < s.units.size(); j++)
			{
				if (s.units.get(j).cell == null)
				{
					s.units.remove(j);
					j--;
				}
			}
		}

		map.setLight(GlobalEngine.currentBrightness());

		System.out.println("Scenario generate end");
	}

	class UnitStack
	{
		private ArrayList<GameCharacter> _units;
		private int _pos;

		public UnitStack(ArrayList<GameCharacter> units)
		{
			_units = units;
		}

		public GameCharacter popUnit()
		{
			if (_pos < _units.size())
				return _units.get(_pos++);
			return null;
		}
	}

	private static ArrayList<MissionScenario> scenarios = new ArrayList<>();

	public static MissionScenario getScenario(int level)
	{
		int points = 0; // Сумма всех очков
		ArrayList<MissionScenario> list = new ArrayList<>(); // Список сценариев участвующих в рулетке
		for (MissionScenario s : scenarios)
		{
			if (s.minLevel <= level && s.maxLevel >= level)
			{
				s.pointRange = points; // Запоминаем закопительное число очков
				points += s.rarityPoints;
				list.add(s);
			}
		}

		int rnd = new Random().nextInt(points); // [0..points)
		for (int i = list.size() - 1; i >= 0; i--)
		{
			MissionScenario s = list.get(i);
			if (s.pointRange <= rnd) return s;
		}

		System.err.println("Scenario not found Points:" + points + "  rnd: " + rnd);
		return null;
	}

	public static void initScenarios()
	{
		scenarios.add(new MissionScenario("Ведьмин шабаш", 1, 100, R.maps.scenario_map, 100)
				.addSide(new BattleSideInfo(true).addUnits("witch", 4, 6)));
	}
}
