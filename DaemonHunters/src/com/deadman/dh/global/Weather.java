package com.deadman.dh.global;

import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Control;

public class Weather extends Control
{
	private final GlobalMapView view;

	private Snow snow;
	private Clouds clouds;
	private AnimalsLayer animals;

	public Weather(GlobalMapView view)
	{
		this.view = view;

		clouds = new Clouds(view);

		snow = new Snow();
		addControl(snow);
		snow.fillParent();
		snow.visible = false;

		animals = new AnimalsLayer(150);
	}

	public void drawWorld()
	{
		// Животные
		GameScreen.screen.setBrightness(view.worldBrightness);
		if (GlobalEngine.timeSpeedLvl < 5 || GameLoop.paused)
			animals.draw(view.centerX, view.centerY);

		// Облака
		if (clouds != null)
			clouds.draw();
	}

	public void drawView()
	{
		if (snow != null && snow.visible)
			snow.draw();
	}

	public void tick(long ticks)
	{
		if (snow != null)
			snow.tick(ticks);
	}

	public void tickWorld(int dt)
	{
		if (GlobalEngine.timeSpeedLvl < 5)
		{
			animals.tick(dt);
		}
	}

	public void start()
	{
		if (clouds != null)
			clouds.start();
	}
}
