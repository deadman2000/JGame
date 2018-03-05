package com.deadman.dh.guild;

import java.util.ArrayList;

import com.deadman.dh.Game;
import com.deadman.dh.GameEvent;
import com.deadman.dh.R;
import com.deadman.dh.city.City;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.model.Cart;
import com.deadman.dh.model.GPoint;
import com.deadman.dh.model.Horse;
import com.deadman.dh.model.Rectangle;
import com.deadman.dh.model.Squad;
import com.deadman.dh.model.Unit;
import com.deadman.dh.model.items.ItemsPage;
import com.deadman.jgame.drawing.Drawable;

public class Guild
{
	public City city;
	public ArrayList<Unit> units = new ArrayList<>();
	public ArrayList<Unit> squadFreeUnits = new ArrayList<>();
	public ArrayList<Squad> squads = new ArrayList<>();
	public ArrayList<GuildBuilding> buildings = new ArrayList<>();
	public ArrayList<GPoint> tunnels = new ArrayList<>();

	private IsoMap _map;

	public final int width, height;
	public static final byte MAP_NONE = 0;
	public static final byte MAP_TUNNEL = 1;
	public static final byte MAP_ENTRY = 2;
	public static final byte MAP_BARRAK = 3;

	public double buildProgress;
	public ItemsPage storage;

	public Drawable marker;

	private int _maxUnits;

	public Guild(City c)
	{
		city = c;
		marker = Drawable.get(R.ui.red_flag); // .getShifted(city.id * 333)

		storage = new ItemsPage("Storage", 10, 2);
		Game.ItemTypes.sword
				.generate()
				.moveTo(storage, 0, 0);
		Game.ItemTypes.bow
				.generate()
				.moveTo(storage, 0, 1);
		Game.ItemTypes.red_potion
				.generate()
				.moveTo(storage, 1, 0);
		Game.ItemTypes.bottle
				.generate()
				.setCount(5)
				.moveTo(storage, 1, 1);
		Game.ItemTypes.shield
				.generate()
				.moveTo(storage, 2, 0);
		Game.ItemTypes.arrow
				.generate()
				.setCount(100)
				.moveTo(storage, 2, 1);

		width = 100;
		height = 100;

		buildings.add(new GuildBuilding(this, new Rectangle(47, 47, 6, 6), 0));

		/*buildMap = new byte[width][height];
		for (int x = 45; x <= 55; x++)
			for (int y = 45; y <= 55; y++)
			{
				buildMap[x][y] = MAP_ENTRY;
			}*/
	}

	// Текущее время в секундах, количество прошедшего времени в секундах
	public void tick(long time, int dt)
	{
		double maxP = 0;

		for (GuildBuilding b : buildings)
		{
			if (b.type == null) continue;

			b.tick(time, dt);

			if (b.isBuild())
			{
				double p = b.buildingPerc();
				if (maxP < p) maxP = p;
			}
		}

		if (maxP == 0)
			buildProgress = -1;
		else
			buildProgress = maxP;
	}

	public void addUnit(Unit unit)
	{
		units.add(unit);
		squadFreeUnits.add(unit);
		unit.guild = this;
	}

	public void removeUnit(Unit unit)
	{
		storage.putItems(unit.ammunition);
		storage.putItems(unit.backpack);

		for (Squad sq : squads)
			sq.removeUnit(unit);

		units.remove(unit);
		squadFreeUnits.remove(unit);
	}

	// TODO Потом убрать
	public Squad makeSquad(ArrayList<Unit> units)
	{
		Squad sq = new Squad(this, "Отряд A");
		sq.setCart(Cart.carts[0]);
		for (int i = 0; i < units.size(); i++)
			sq.setUnit(units.get(i), i);
		sq.horse = Horse.generate();
		squads.add(sq);
		Game.squads.add(sq);
		return sq;
	}

	// TODO Потом убрать
	public void generateUnits()
	{
		for (int i = 0; i < 4; i++)
		{
			Unit u = Unit.generate();

			Game.ItemTypes.red_potion
					.generate()
					.equip(u);

			Game.ItemTypes.torch
					.generate()
					.equip(u);

			if (i < 2)
				Game.ItemTypes.sword
						.generate()
						.equip(u);
			else
			{
				Game.ItemTypes.bow
						.generate()
						.equip(u);
				Game.ItemTypes.arrow
						.generate()
						.setCount(100)
						.equip(u);
			}

			addUnit(u);
		}

		makeSquad(units);
	}

	// Возвращает true, если в гильдии есть место для рекрутов
	public boolean canRecruit()
	{
		return units.size() < maxUnitCapcity();
	}

	public ArrayList<Squad> getCurrentSquads()
	{
		ArrayList<Squad> accSquads = new ArrayList<>();

		for (Squad s : squads)
			if (s.inHome())
				accSquads.add(s);

		return accSquads;
	}

	public Squad createSquad(String name)
	{
		Squad sq = new Squad(this, name);
		squads.add(sq);
		Game.squads.add(sq);
		return sq;
	}

	public void show()
	{
		new GuildEngine(this).show();
	}

	/**
	 * @return Максимально число юнитов, которое может содержать гильдия
	 */
	public int maxUnitCapcity()
	{
		return _maxUnits;
	}

	private void recalcMaxUnits()
	{
		int c = 0;
		for (GuildBuilding b : buildings)
		{
			if (!b.isBuild())
				c += b.getMaxUnits();
		}
		_maxUnits = c;
	}

	public GuildBuilding getBuildingAt(short x, short y)
	{
		for (GuildBuilding gb : buildings)
			if (gb.rect.intersect(x, y))
				return gb;
		return null;
	}

	public GuildBuilding build(GuildBuildingType type, Rectangle rect)
	{
		Game.gold -= type.price * rect.square();
		GuildBuilding b = new GuildBuilding(this, rect, type);
		if (Game.FAST_BUILD)
			b.build_progress = -1;

		GuildMapBuilder.append(this, b);

		buildings.add(b);
		for (int i = 0; i < tunnels.size(); i++)
		{
			GPoint p = tunnels.get(i);
			if (rect.intersect(p.x, p.y))
			{
				tunnels.remove(i);
				i--;
			}
		}

		return b;
	}

	public void buildTunnel(GuildBuildingType type, ArrayList<GPoint> points)
	{
		Game.gold -= type.price * points.size();
		GuildMapBuilder.append(this, points);
		tunnels.addAll(points);
	}

	public void onBuildCompleted(GuildBuilding build)
	{
		GameEvent.add(R.ui.event_build, String.format("Завершено строительство %s в городе %s", build.type.nameWhat, city.name), GameEvent.TYPE_BUILDING_COMPLETE, city);
		if (_map != null)
			GuildMapBuilder.append(this, build);
		recalcMaxUnits();
	}

	public IsoMap getMap()
	{
		if (_map == null)
			_map = GuildMapBuilder.buildMap(this);
		return _map;
	}
}
