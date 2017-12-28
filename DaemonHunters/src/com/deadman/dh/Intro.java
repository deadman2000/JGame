package com.deadman.dh;

import com.deadman.dh.global.Snow;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.ui.FillLayout;

public class Intro extends GameEngine
{
	private Snow snow;

	public Intro()
	{
		setLayout(new FillLayout());
		addControl(snow = new Snow(100));
	}

	@Override
	public void tick()
	{
		snow.tick(ticks);
	}

	@Override
	public void draw()
	{
		snow.draw();
	}
}
