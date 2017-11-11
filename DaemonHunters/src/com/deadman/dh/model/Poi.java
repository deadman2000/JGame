package com.deadman.dh.model;

import java.util.ArrayList;

import com.deadman.dh.Game;
import com.deadman.jgame.GameLoop;

public class Poi extends MapObject
{
	public Poi()
	{
	}

	public Poi(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public long getSeed()
	{
		return (x + y * GameLoop.GLOBAL_MAP_WIDTH) * Game.seed;
	}

	public double distance(Poi p)
	{
		return Math.sqrt(distanceSq(p));
	}

	public int distanceSq(Poi p)
	{
		int dx = x - p.x;
		int dy = y - p.y;
		return dx * dx + dy * dy;
	}

	public int distanceSq(int x, int y)
	{
		x -= this.x;
		y -= this.y;
		return x * x + y * y;
	}

	public double getAngle(Poi p)
	{
		return Math.atan2(p.y - y, p.x - x);
	}

	public ArrayList<Poi> neighborhood = new ArrayList<>();

	public Poi getNeighborTo(Poi pb, ArrayList<Poi> ignore)
	{
		double a = getAngle(pb);
		Poi mp = null;
		double ma = Double.MAX_VALUE;
		for (Poi n : neighborhood)
		{
			if (ignore.contains(n)) continue;

			double da = getAngle(n);
			double wa = a - da;
			if (wa > Math.PI)
				wa -= Math.PI * 2;
			else if (wa < -Math.PI)
				wa += Math.PI * 2;

			wa = Math.abs(wa);
			if (wa > Math.PI / 2) continue;

			if (wa < ma)
			{
				ma = wa;
				mp = n;
			}
		}

		return mp;
	}

	public Poi getNeighborTo2(Poi pb, ArrayList<Poi> ignore)
	{
		double a = getAngle(pb);
		Poi mp = null;
		double ma = Double.MAX_VALUE;
		for (Poi n : neighborhood)
		{
			if (ignore.contains(n)) continue;

			double da = getAngle(n);
			double wa = a - da;
			if (wa > Math.PI)
				wa -= Math.PI * 2;
			else if (wa < -Math.PI)
				wa += Math.PI * 2;

			wa = Math.abs(wa);

			if (wa < ma)
			{
				ma = wa;
				mp = n;
			}
		}

		return mp;
	}

	public boolean hasInDirection(Poi p)
	{
		double a = this.getAngle(p);
		for (Poi n : neighborhood)
		{
			double da = this.getAngle(n);
			if (Math.abs(a - da) < Math.PI / 4) return true;
		}

		return false;
	}

	// Проложенные пути

	// Дороги
	public ArrayList<Poi> traceList = new ArrayList<>();

	public void traceTo(Poi wp)
	{
		if (traceList.contains(wp)) return;
		traceList.add(wp);
	}

	public boolean canTraceTo(Poi wp)
	{
		if (traceList.contains(wp)) return true;
		return traceList.size() < 4;
	}

	public void normalize()
	{
		neighborhood.trimToSize();
		traceList.trimToSize();
		ways = null;
	}

	public ArrayList<Way> ways = new ArrayList<>();

	public boolean hasWayTo(Poi pb)
	{
		for (Way w : ways)
			if (w.to == pb || w.from == pb) return true;

		return false;
	}

	public short weight;

	public String description()
	{
		return "пункт " + this;
	}

	public String getName()
	{
		return this.toString();
	}

	public boolean hasSquad()
	{
		for (Squad s : Game.squads)
			if (s.location == this)
				return true;
		return false;
	}

	public ArrayList<Squad> getAccessibleSquads()
	{
		ArrayList<Squad> accSquads = new ArrayList<>();

		for (Squad s : Game.squads)
			if (s.location == this && s.isAccessible())
				accSquads.add(s);

		return accSquads;
	}
}
