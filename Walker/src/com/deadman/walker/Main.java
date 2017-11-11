package com.deadman.walker;

import com.deadman.dh.model.items.ItemCursor;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.drawing.Picture;

public class Main
{
	public static void main(String[] args)
	{
		try
		{
			GameScreen.SCALE_FACTOR = 4;
			GameScreen.START_WIDTH = 384 * 4;
			GameScreen.START_HEIGHT = 216 * 4;
			GameScreen.init("--- Walker v0.1 - programmed by dead_man ---");

			init();

			GameLoop.run();
			System.exit(0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	private static void init()
	{
		GameScreen.defaultCursor = new ItemCursor(Picture.load("res/cursor.png"));
				
		WalkerEngine eng = new WalkerEngine();
		eng.show();
	}

}
