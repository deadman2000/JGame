package com.deadman.jgame;

import com.deadman.jgame.drawing.GameScreen;

public class GameLoop
{
	public static final int FPS = 50; // 50
	public static final int TICK_DELAY = 1000 / FPS;

	public static long frames = 0;
	public static boolean paused = false; // Установка в true не будет вызывать tick у текущего Engine

	public static void run()
	{
		while (!isStopped)
		{
			long time = System.currentTimeMillis();

			frames++;

			if (GameEngine.current != null && !paused)
			{
				GameEngine.current.ticks++;
				GameEngine.current.tick();
			}

			GameScreen.screen.redraw();

			long d = TICK_DELAY - (System.currentTimeMillis() - time);
			//Game.screen.setTitle("" + d);

			if (d > 0)
			{
				try
				{
					Thread.sleep(d);
				}
				catch (InterruptedException e)
				{
					break;
				}
			}
			//else System.err.println("Negative delay!!! " + d);
		}

		System.out.println("Game closed");

		if (GameScreen.screen.isFullscreen)
			GameScreen.screen.setWindowed();
	}

	public static boolean isStopped = false;

	public static void quit()
	{
		isStopped = true;
	}
}
