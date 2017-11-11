package com.deadman.dh.city;

public class Building
{
	public int id;
	public BuildingType type;
	public int x, y;

	public Building(int id, BuildingType t, int x, int y)
	{
		this.id = id;
		type = t;
		this.x = x;
		this.y = y;
	}
}
