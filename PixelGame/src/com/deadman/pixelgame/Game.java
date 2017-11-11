package com.deadman.pixelgame;

public class Game
{
	public static final String TITLE = "--- PixelGameEngine v0.1 - programmed by dead_man ---";

	public static void run(GameScreen scr, String packagePath) throws Exception
	{
		Game.screen = scr;

		init(packagePath);
		new Thread(gameWatch, "Game Watch").start();
	}

	public static GameScreen screen;

	private static long delay = 19; // 20 => x50 FPS
	public static long ticks = 0;

	public static GameCharacter mainCharacter;

	public static Package res;

	private static void init(String path) throws Exception
	{
		res = new Package(path, screen);

		Sprite sprCur = res.loadSprite("cursor");
		screen.cursor = new GameCursor(sprCur);
		screen.font = res.loadSprite("Font1");

		screen.loadGui(res.loadBackground("GUI"));

		screen.loadScene(res.loadScene("Scene-1"));

		Sprite sprMan = res.loadSprite("MainChar");
		mainCharacter = new GameCharacter();
		mainCharacter.setStateDrawable(GameCharacter.ST_STAND | GameCharacter.UP, sprMan.frames[16]);
		mainCharacter.setStateDrawable(GameCharacter.ST_STAND | GameCharacter.DOWN, sprMan.frames[0]);
		mainCharacter.setStateDrawable(GameCharacter.ST_STAND | GameCharacter.LEFT, sprMan.frames[1]);
		mainCharacter.setStateDrawable(GameCharacter.ST_STAND | GameCharacter.RIGHT, new DrawTransform(sprMan.frames[1], -1.f));
		mainCharacter.setStateDrawable(GameCharacter.ST_MOVE | GameCharacter.UP, sprMan.animations[0]);
		mainCharacter.setStateDrawable(GameCharacter.ST_MOVE | GameCharacter.DOWN, sprMan.animations[1]);
		mainCharacter.setStateDrawable(GameCharacter.ST_MOVE | GameCharacter.LEFT, sprMan.animations[0]);
		mainCharacter.setStateDrawable(GameCharacter.ST_MOVE | GameCharacter.RIGHT, new DrawTransform(sprMan.animations[0], -1.f));

		mainCharacter.setState(0);
		mainCharacter.setPosition(200, 120);
		screen.drawable.add(mainCharacter);

		Game.screen.init();
		//Sound.playOGG("game.ogg");
	}

	private static Runnable gameWatch = new Runnable()
	{
		@Override
		public void run()
		{
			while (true)
			{
				long time = System.currentTimeMillis();

				tick();

				long d = delay - (System.currentTimeMillis() - time);
				//log("Delay "+d);
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
				else
					System.out.println("Negative delay!!! " + d);
			}
		}
	};

	private static void tick()
	{
		//long time = System.nanoTime();

		screen.paint();

		//TODO: Scripting

		ticks++;

		//if (ticks % 20 == 0) Main.window.setTitle(TITLE + "  \\\\ " + ((System.nanoTime() - time) / 1000));
		//if (ticks % 20 == 0) System.out.println((System.nanoTime() - time)/1000);
	}

	public static void log(String string)
	{
		System.out.println(ticks + ": " + string);
	}
}
