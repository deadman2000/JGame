package com.deadman.pixelgame;

import java.awt.Rectangle;

public class Animation extends DrawableObject
{
	private DrawableObject[] _frames;
	private int[] _delays;

	private int _curr;
	private int _ticks = -1;
	long last_time = -1;

	private DrawableObject drawable;

	public Animation(DrawableObject[] frames, int[] delays)
	{
		_frames = frames;
		_delays = delays;

		drawable = _frames[0];
	}

	@Override
	public void reset()
	{
		Game.log("Animation reset");
		_ticks = -1;
		_curr = 0;
	}

	private void tick()
	{
		_ticks++;
		if (_ticks >= _delays[_curr])
		{
			if (_curr + 1 < _frames.length)
				_curr++;
			else
			{
				_ticks -= _delays[_curr];
				_curr = 0;
			}

			drawable = _frames[_curr];
		}
	}

	public void drawAt(int x, int y)
	{
		if (Game.ticks != last_time)
		{
			last_time = Game.ticks;
			tick();
		}

		drawable.drawAt(x, y);
	}

	@Override
	public boolean contains(int x, int y)
	{
		return drawable.contains(x, y);
	}

	@Override
	public Rectangle getBounds()
	{
		return drawable.getBounds();
	}
}
