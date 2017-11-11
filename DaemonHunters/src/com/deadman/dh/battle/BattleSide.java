package com.deadman.dh.battle;

import java.util.ArrayList;
import java.util.HashSet;

import com.deadman.dh.Game;
import com.deadman.dh.isometric.CalcFOV;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.LightMode;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.isometric.MapChangeListener;
import com.deadman.dh.model.GameCharacter;

public abstract class BattleSide implements MapChangeListener
{
	protected final MissionEngine eng;
	protected final IsoMap map;
	public final BattleCell[][][] cells; // Область видимости и пр.

	public final ArrayList<GameCharacter> units = new ArrayList<>();
	public boolean isEnemy; // Признак того, что это враг по отношению к игроку
	public GameCharacter lastSelect; // Последний выбранный юнит

	public BattleSide(MissionEngine e)
	{
		eng = e;
		map = e.map;
		map.addChangeListener(this);

		cells = new BattleCell[map.zheight][map.width][map.height];
		for (int z = 0; z < map.zheight; z++)
			for (int x = 0; x < map.width; x++)
				for (int y = 0; y < map.height; y++)
					cells[z][x][y] = new BattleCell();
	}

	public boolean isPlayer()
	{
		return true;
	}

	@Override
	public void onMapChanged()
	{
		buildVisibleArea();
	}

	public void beginTurn()
	{
		System.out.println("New Turn " + this);
		for (GameCharacter u : units)
		{
			u.resetActionPoints();
			u.resetMoveArea();
		}
	}

	public boolean isActive()
	{
		for (int i = 0; i < units.size(); i++)
		{
			if (units.get(i)
					.isActive())
				return true;
		}
		return false;
	}

	public void addUnit(GameCharacter ch)
	{
		units.add(ch);
		ch.side = this;
	}

	public boolean isVisible(MapCell cell)
	{
		if (Game.VIEW_ALL && isPlayer()) return true;
		return cells[cell.z][cell.x][cell.y].visible;
	}

	public boolean isDiscovered(MapCell cell)
	{
		if (Game.VIEW_ALL && isPlayer()) return true;
		if (Game.VISION_AREA)
			return cells[cell.z][cell.x][cell.y].visible;
		else
			return cells[cell.z][cell.x][cell.y].discovered;
	}

	public void resetVisibleArea()
	{
		for (int z = 0; z < map.zheight; z++)
			for (int x = 0; x < map.width; x++)
				for (int y = 0; y < map.height; y++)
					cells[z][x][y].calc_visible = false;

		buildVisibleArea();
	}

	public void buildVisibleArea()
	{
		for (int i = 0; i < units.size(); i++)
		{
			GameCharacter ch = units.get(i);
			new CalcFOV(map, cells, LightMode.VISION, ch.visionRange, ch.cell, ch.getRotation()).calculate();

			if (ch.nearCell != null)
				new CalcFOV(map, cells, LightMode.VISION, ch.visionRange, ch.nearCell, ch.getRotation()).calculate();
		}

		for (int z = 0; z < map.zheight; z++)
			for (int x = 0; x < map.width; x++)
				for (int y = 0; y < map.height; y++)
				{
					BattleCell c = cells[z][x][y];
					if (c.calc_visible)
					{
						c.discovered = true;
						c.visible = true;
					}
					else
					{
						c.visible = false;
					}
				}
		
		checkVisibleEnemies();
	}

	private HashSet<GameCharacter> currentVisible = new HashSet<>();

	public ArrayList<GameCharacter> getVisibleEnemies()
	{
		ArrayList<GameCharacter> units = new ArrayList<>();

		for (BattleSide s : eng.sides)
		{
			if (s.isEnemy == isEnemy) continue;

			for (GameCharacter c : s.units)
			{
				if (c.isActive() && isVisible(c.cell))
					units.add(c);
			}
		}

		return units;
	}

	public void checkVisibleEnemies()
	{
		for (BattleSide s : eng.sides)
		{
			if (s.isEnemy == isEnemy) continue;

			for (GameCharacter c : s.units)
			{
				if (c.isActive()) // Живой
					checkVisibleUnit(c);
			}
		}
	}

	public boolean checkVisibleUnit(GameCharacter ch)
	{
		if (isVisible(ch.cell))
		{
			if (currentVisible.add(ch))
				onFoundEnemy(ch);
			return true;
		}
		else
		{
			currentVisible.remove(ch);
			return false;
		}
	}

	@Override
	public void onMoving(MapCell cell)
	{
		GameCharacter ch = cell.ch;
		if (ch == null)
		{
			System.err.println("Moving nothing");
			return;
		}

		if (ch.side.isEnemy != isEnemy) // Враг
		{
			if (checkVisibleUnit(ch))
				onEnemyMoving(ch);
		}
	}

	protected void onEnemyMoving(GameCharacter ch)
	{
	}

	protected void onFoundEnemy(GameCharacter ch)
	{
	}

	public void onMissionEnd()
	{
		for (GameCharacter ch : units)
			ch.cell = null;
	}
}
