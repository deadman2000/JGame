package com.deadman.walker;

public enum Moving
{
	NONE,
	FORWARD,
	BACKWARD,
	ROTATE_LEFT,
	ROTATE_RIGHT,
	SHIFT_LEFT,
	SHIFT_RIGHT,
	FORWARD_BARRIER;

	public boolean posChanged()
	{
		switch (this)
		{
			case FORWARD:
			case BACKWARD:
			case SHIFT_LEFT:
			case SHIFT_RIGHT:
				return true;

			default:
				return false;
		}
	}
}
