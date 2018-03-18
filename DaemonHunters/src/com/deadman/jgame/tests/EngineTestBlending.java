package com.deadman.jgame.tests;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.Picture;

public class EngineTestBlending extends GameEngine
{
	private Drawable pic, bgr;

	public EngineTestBlending()
	{
		pic = Picture.load("test/test_alpha.png");
		bgr = Picture.load("test/test_bgr.png");
	}

	@Override
	public void draw()
	{
		super.draw();
		
		bgr.drawAt(0, 0);
		
		screen.drawRect(10, 10, 50, 50, 0x7fff0000);
		pic.drawAt(30, 30);
	}
}
