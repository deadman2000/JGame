package com.deadman.pixelgame;

import java.awt.Rectangle;

public class DrawTransform extends DrawableObject
{
	private DrawableObject drawable;
	private float _x_scale = 1.f;

	public DrawTransform(DrawableObject dr, float x_scale)
	{
		drawable = dr;
		_x_scale = x_scale;
	}

	@Override
	public void drawAt(int x, int y)
	{
		GameScreen.T_XSCALE = _x_scale;
		drawable.drawAt(x, y);
		GameScreen.T_XSCALE = 1.f;
	}

	@Override
	public boolean contains(int x, int y)
	{
		return drawable.contains(x, y);
	}

	@Override
	public Rectangle getBounds()
	{
		return drawable.getBounds();
	}
	
	@Override
	public void reset()
	{
		drawable.reset();
	}
}
