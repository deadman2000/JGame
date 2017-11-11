package com.deadman.dh.model;

public class Rectangle
{
	public int x, y, width, height;

	public Rectangle(int x, int y, int w, int h)
	{
		this.x = x;
		this.y = y;
		width = w;
		height = h;
	}

	public int square()
	{
		return width * height;
	}

	public boolean intersect(Rectangle r)
	{
		return r.right() >= x && right() >= r.x && r.bottom() >= y && bottom() >= r.y;
	}

	public int right()
	{
		return x + width - 1;
	}

	public int bottom()
	{
		return y + height - 1;
	}

	public boolean intersect(int px, int py)
	{
		return px >= x && px <= right() && py >= y && py <= bottom();
	}

	@Override
	public String toString()
	{
		return "[" + x + ":" + y + " " + width + "x" + height + "]";
	}
}
