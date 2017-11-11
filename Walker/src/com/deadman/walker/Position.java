package com.deadman.walker;

public class Position
{
	public int x, y;
	public Direction dir;

	public Position(int x, int y, Direction dir)
	{
		this.x = x;
		this.y = y;
		this.dir = dir;
	}

	public Position translate(Moving move)
	{
		switch (move)
		{
			case FORWARD:
				switch (dir)
				{
					case NORTH:
						return new Position(x, y - 1, dir);
					case EAST:
						return new Position(x + 1, y, dir);
					case SOUTH:
						return new Position(x, y + 1, dir);
					default:
						return new Position(x - 1, y, dir);
				}
			case BACKWARD:
				switch (dir)
				{
					case NORTH:
						return new Position(x, y + 1, dir);
					case EAST:
						return new Position(x - 1, y, dir);
					case SOUTH:
						return new Position(x, y - 1, dir);
					default:
						return new Position(x + 1, y, dir);
				}
			case SHIFT_LEFT:
				switch (dir)
				{
					case NORTH:
						return new Position(x - 1, y, dir);
					case EAST:
						return new Position(x, y - 1, dir);
					case SOUTH:
						return new Position(x + 1, y, dir);
					default:
						return new Position(x, y + 1, dir);
				}
			case SHIFT_RIGHT:
				switch (dir)
				{
					case NORTH:
						return new Position(x + 1, y, dir);
					case EAST:
						return new Position(x, y + 1, dir);
					case SOUTH:
						return new Position(x - 1, y, dir);
					default:
						return new Position(x, y - 1, dir);
				}
			case ROTATE_LEFT:
				return new Position(x, y, dir.left());
			case ROTATE_RIGHT:
				return new Position(x, y, dir.right());
			case NONE:
			default:
				return this;
		}
	}

	@Override
	public String toString()
	{
		return x + ":" + y + " " + dir;
	}

	public boolean isOdd()
	{
		return (x + y + dir.index) % 2 == 0;
	}
}
