package com.deadman.dh.model;

public abstract class MapObject
{
	public int x, y;

	public abstract String getName();

	@Override
	public String toString()
	{
		return "[" + x + ":" + y + "]";
	}
}
