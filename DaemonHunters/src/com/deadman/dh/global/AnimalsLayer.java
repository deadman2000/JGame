package com.deadman.dh.global;

public class AnimalsLayer
{
	private Eagle[] eagles;

	public AnimalsLayer(int cnt)
	{
		eagles = new Eagle[cnt];

		for (int i = 0; i < cnt; i++)
			eagles[i] = new Eagle();
	}

	public void tick(int dt)
	{
		for (int i = 0; i < eagles.length; i++)
			for (int j = 0; j < dt; j++)
				eagles[i].tick();
	}

	public void draw(int x, int y)
	{
		for (int i = 0; i < eagles.length; i++)
			eagles[i].drawAt(x, y);
		//Game.screen.drawRect(x, y, Eagle.WIDTH, Eagle.HEIGHT, 0x7fff0000);
	}
}
