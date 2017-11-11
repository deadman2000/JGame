package com.deadman.dh;

import com.deadman.dh.global.Snow;
import com.deadman.jgame.GameEngine;

public class Intro extends GameEngine
{
	private Snow snow;

	public Intro()
	{
		snow = new Snow(100);
		addControl(snow);
		snow.fillParent();
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
