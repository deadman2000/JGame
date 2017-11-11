package com.deadman.dh.isometric;

public class HitResult
{
	public HitResult(MapCell c, Object o, boolean isLeft)
	{
		cell = c;
		object = o;
		this.isLeft = isLeft;
	}

	public MapCell cell;
	public Object object;
	public boolean isLeft;
}
