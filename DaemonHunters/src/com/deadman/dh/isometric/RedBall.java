package com.deadman.dh.isometric;

import java.util.Random;

import com.deadman.jgame.drawing.GameScreen;

public class RedBall extends Particle
{
	static Random rnd = new Random();

	public RedBall(MapCell cell)
	{
		moveTo(cell, (byte) rnd.nextInt(16), (byte) rnd.nextInt(16), (byte) rnd.nextInt(MapCell.LEVEL_HEIGHT));
		up = rnd.nextBoolean();
	}

	@Override
	protected void draw(int sx, int sy)
	{
		if (z < 15)
			GameScreen.screen.drawRect(sx, sy, 2, 2, 0x70000000); // Тень
		else
			GameScreen.screen.drawRect(sx, sy, 1, 1, 0x90000000); // Тень
		GameScreen.screen.drawRect(sx, sy - z, 2, 2, 0xffff0000); // Точка
	}

	boolean up = true;

	@Override
	public void tick()
	{
		byte cz;
		if (up)
		{
			cz = (byte) (z + 1);
			if (cz >= MapCell.LEVEL_HEIGHT) up = false;
		}
		else
		{
			cz = (byte) (z - 1);
			if (cz <= 0) up = true;
		}

		setCellCoords(x, y, cz);
	}

}
