package com.deadman.jgame.tests;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.Picture;

public class EngineTestMirroring extends GameEngine
{
	private Drawable pic;
	
	public EngineTestMirroring()
	{
		pic = Picture.load("test/horse.png");
	}

	@Override
	public void draw()
	{
		pic.drawMHAt(0, 0);
		pic.drawRLAt(0, 50);
		pic.drawRRAt(50, 0);
		pic.drawMVAt(50, 50);
	}
}
