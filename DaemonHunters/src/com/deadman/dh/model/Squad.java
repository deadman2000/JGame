package com.deadman.dh.model;

import java.util.ArrayList;

import com.deadman.dh.GameEvent;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.model.items.ItemsPage;
import com.deadman.dh.Game;

public class Squad extends MapObject
{
	public Squad()
	{
		items = new ItemsPage("Squad", 6, 2);
	}

	public Squad(Guild g, String name)
	{
		guild = g;
		location = g.city;

		x = location.x;
		y = location.y;
		_x = x;
		_y = y;

		items = new ItemsPage("Squad", 6, 2);
		this.name = name;
	}

	public String name;
	public Guild guild;

	public Horse horse;
	private Cart _cart;

	public Unit[] units;
	public ItemsPage items;

	public String getName()
	{
		return name;
	}

	// Перемещение

	public Poi location;
	private double _x, _y; // Точная позиция

	public void moveTo(Poi dest)
	{
		moveTo(dest, null);
	}

	public void moveTo(Mission m)
	{
		moveTo(m.point, m);
	}

	public void moveTo(Poi dest, Mission m)
	{
		Way w;
		if (location != null)
		{
			w = Game.map.trace(location, dest, m);
		}
		else if (toPoi != null)
		{
			w = Game.map.trace(toPoi, dest, m);
			if (wayInd > 0)
			{
				Poi from = way.points[wayInd - 1];
				w.setFrom(from);
			}
		}
		else
		{
			System.err.println("Unimplemented situation");
			return;
		}
		if (w == null)
		{
			// Может означать, что мы пришли
			way = null;
			Game.global.squadsPanel.update();
			return;
		}

		System.out.println("Move squad: " + w.points.length);

		way = w;
		way.mission = m;
		wayInd = 1;
		setDirection(w.points[wayInd]);
		Game.global.squadsPanel.update();
	}

	public void moveToHome()
	{
		moveTo(guild.city);
	}

	public double speed = 0.01; // Скорость перемещения отряда. пикселей карты в 1 секунду игрового времени

	public Way way;

	private Poi toPoi; // Пункт назначения
	private int wayInd;
	private double sinD, cosD;

	public void tick(int seconds)
	{
		if (way != null)
		{
			double len = speed * seconds;
			while (true)
			{
				double dx = toPoi.x - _x;
				double dy = toPoi.y - _y;
				double dist2 = dx * dx + dy * dy;

				if (dist2 <= len * len)
				{
					wayInd++;
					if (wayInd == way.points.length) // Пришли 
					{
						wayCompleted();
						return;
					}

					len -= Math.sqrt(dist2);

					location = toPoi;
					_x = toPoi.x;
					_y = toPoi.y;

					setDirection(way.points[wayInd]);
				}
				else
					break;
			}

			if (len > 0)
			{
				_x += sinD * len;
				_y += cosD * len;
				x = (int) Math.round(_x);
				y = (int) Math.round(_y);

				location = null;
			}
		}
	}

	void setDirection(Poi p)
	{
		toPoi = p;
		double a = Math.atan2(p.x - _x, p.y - _y);
		sinD = Math.sin(a);
		cosD = Math.cos(a);
	}

	private void wayCompleted()
	{
		if (way.mission != null)
			GameEvent.add(0, String.format("%s прибыл на задание", name), GameEvent.TYPE_SQUAD_MISSION_BEGIN, way.mission, this);
		else if (way.to != guild.city)
			GameEvent.add(0, String.format("%s прибыл в %s", name, way.to.description()), GameEvent.TYPE_SQUAD_MOVE_COMPLETE, way.to);
		//Game.engine.showMessage("Отряд прибыл в " + way.to.description());
		location = way.to;
		x = way.to.x;
		y = way.to.y;
		_x = x;
		_y = y;
		way = null;
		toPoi = null;
	}

	// Units

	public void setUnit(Unit unit, int pos)
	{
		if (units[pos] != null)
			guild.squadFreeUnits.add(units[pos]);
		units[pos] = unit;
		if (unit != null && guild != null)
			guild.squadFreeUnits.remove(unit);
	}

	public void removeUnit(Unit unit)
	{
		if (units != null)
			for (int i = 0; i < units.length; i++)
			if (units[i] == unit)
			{
			guild.squadFreeUnits.add(unit);
			units[i] = null;
			break;
			}
	}

	public Unit getUnit(int pos)
	{
		return units[pos];
	}

	public ArrayList<Unit> getUnits()
	{
		ArrayList<Unit> list = new ArrayList<>();
		if (units != null)
			for (Unit u : units)
			if (u != null)
				list.add(u);
		return list;
	}

	// Cart

	public void setCart(Cart c)
	{
		_cart = c;

		// Переносим юнитов

		Unit[] newUnits = new Unit[c.unitsCapacity];
		if (units != null)
		{
			if (units.length > newUnits.length)
			{
				for (int i = newUnits.length; i < units.length; i++)
				{
					if (units[i] != null)
						removeUnit(units[i]);
				}
			}

			System.arraycopy(units, 0, newUnits, 0, Math.min(units.length, newUnits.length));
		}
		units = newUnits;
	}

	public Cart getCart()
	{
		return _cart;
	}

	public void remove()
	{
		guild.squadFreeUnits.addAll(getUnits());
		guild.storage.putItems(items);
		guild.squads.remove(this);
		Game.squads.remove(this);
	}

	public boolean isAccessible()
	{
		return _cart != null && horse != null && getUnits().size() > 0 && way == null;
	}

	public boolean inHome()
	{
		return location == guild.city;
	}
}
