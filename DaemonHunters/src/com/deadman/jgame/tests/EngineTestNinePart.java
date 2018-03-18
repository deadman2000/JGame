package com.deadman.jgame.tests;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.NinePart;
import com.deadman.jgame.drawing.PicPart;

public class EngineTestNinePart extends GameEngine
{
	private NinePart np;

	public EngineTestNinePart()
	{
		np = new NinePart(PicPart.load("test/test9.ppr"));
	}

	@Override
	public void draw()
	{
		np.drawAt(10, 10, 50, 50);
		np.fillAt(70, 70, 50, 50);
	}
}
