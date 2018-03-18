package com.deadman.dh.global;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.model.GlobalMap;
import com.deadman.dh.model.generation.PerlinNoise;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.resources.ResourceManager;

public class Eagle
{
	private float z_factor = 1.3f;
	private int width, height;
	
	private static final PerlinNoise perlin = PerlinNoise.create();
	private static final float PIx2 = (float) (Math.PI * 2);

	private static Drawable[] picsEAGLE;

	private static final int TIME_SMALL = 5000 / GameLoop.TICK_DELAY;
	private static int PS_RND = 0;

	private static final double speed = 0.05;


	private float x, y;
	private int px, py;
	private float dir;
	private float time;
	private int smallTime;
	private float dd; // Отвечает за степень уклона

	public Eagle()
	{
		if (picsEAGLE == null)
			picsEAGLE = ResourceManager.getParts(R.animals.eagle_ppr);
		
		time = Game.rnd.nextFloat() * 1000.0f;
		x = -1;
	}

	public void tick()
	{
		if (x == -1)
		{
			genPos();
			genTime();
			return;
		}

		smallTime--;

		double n = perlin.noise1(time);

		dir += n * dd;
		while (dir > PIx2)
			dir -= PIx2;
		while (dir < 0)
			dir += PIx2;

		//System.out.println(String.format("%.8f   -  %.8f", n, dir));
		//System.out.println(n);

		if (n > 0.1 || n < -0.1) // Крутые повороты
			time += 0.02f;
		else
			time += 0.005f;

		x += Math.cos(dir) * speed;
		y += Math.sin(dir) * speed;

		px = (int) x;
		py = (int) y;

		if (px < 0 || py < 0 || px >= width || py >= height)
		{
			genPos();
		}
		else if (smallTime < 0)
		{
			int mx = (int) (px / z_factor);
			int my = (int) (py / z_factor);

			if (PS_RND++ % 5 == 0 && !Game.map.isMapFlag(mx, my, GlobalMap.TYPE_SEA))
				genPos();
			else
				genTime();
		}
	}

	void genPos()
	{
		z_factor = 1.1f + Game.rnd.nextFloat() * 0.4f; // 1.1 ... 1.5
		width = (int) (GlobalEngine.GLOBAL_MAP_WIDTH * z_factor);
		height = (int) (GlobalEngine.GLOBAL_MAP_HEIGHT * z_factor);
		
		while (true)
		{
			x = Game.rnd.nextInt(width);
			y = Game.rnd.nextInt(height);

			int mx = (int) (x / z_factor);
			int my = (int) (y / z_factor);
			if (!Game.map.isMapFlag(mx, my, GlobalMap.TYPE_SEA))
				break;
		}
		px = (int) x;
		py = (int) y;
		dir = Game.rnd.nextFloat() * PIx2;
		smallTime = Game.rnd.nextInt(TIME_SMALL / 2) + TIME_SMALL / 2;
		dd = 0.02f + Game.rnd.nextFloat() * 0.08f;
	}

	void genTime()
	{
		smallTime = 25000 / GameLoop.TICK_DELAY + Game.rnd.nextInt(10000 / GameLoop.TICK_DELAY);
	}

	public void drawAt(int tx, int ty)
	{
		tx *= z_factor - 1;
		ty *= z_factor - 1;
		
		tx += px;
		ty += py;

		if (smallTime < TIME_SMALL)
			picsEAGLE[8].drawAt(tx, ty);
		else
		{
			int di = (int) ((dir + Math.PI / 8.0) / (Math.PI / 4.0));
			picsEAGLE[di].drawAt(tx, ty);
		}
	}
}
