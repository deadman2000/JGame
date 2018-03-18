package com.deadman.jgame.tests;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.Picture;

public class EngineTestImage extends GameEngine
{
	private Drawable d;
	
	public EngineTestImage(String path)
	{
		d = Picture.load(path);
	}
	
	@Override
	public void draw()
	{
		d.drawAt(0, 0);
	}
}
