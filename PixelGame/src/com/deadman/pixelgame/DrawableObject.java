package com.deadman.pixelgame;

import java.awt.Rectangle;

public abstract class DrawableObject
{
	public abstract void drawAt(int x, int y);

	public abstract boolean contains(int x, int y);

	public abstract Rectangle getBounds();

	public void reset()
	{
	}
}
