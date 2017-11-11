package com.deadman.pixelgame;

import java.awt.Point;

public interface IDrawable
{
	void draw();
	
	boolean contains(Point p);
}
