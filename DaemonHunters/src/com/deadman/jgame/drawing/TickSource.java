package com.deadman.jgame.drawing;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;

public enum TickSource
{
	FRAMES,
	ENGINE;

	public static TickSource fromInt(int val)
	{
		switch (val)
		{
			case 0:
			default:
				return FRAMES;
			case 1:
				return ENGINE;
		}
	}

	public long getTicks()
	{
		switch (this)
		{
			case FRAMES:
			default:
				return GameLoop.frames;
			case ENGINE:
				return GameEngine.current.ticks;
		}
	}
}
