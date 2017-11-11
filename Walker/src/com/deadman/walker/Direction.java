package com.deadman.walker;

public enum Direction
{
	NORTH(0)
	{
		@Override
		public Direction left()
		{
			return WEST;
		}
		
		@Override
		public Direction right()
		{
			return EAST;
		}
		
		@Override
		public Direction opposite()
		{
			return SOUTH;
		}
	},
	EAST(1)
	{
		@Override
		public Direction left()
		{
			return NORTH;
		}
		
		@Override
		public Direction right()
		{
			return SOUTH;
		}
		
		@Override
		public Direction opposite()
		{
			return WEST;
		}
	},
	SOUTH(2)
	{
		@Override
		public Direction left()
		{
			return EAST;
		}
		
		@Override
		public Direction right()
		{
			return WEST;
		}
		
		@Override
		public Direction opposite()
		{
			return NORTH;
		}
	},
	WEST(3)
	{
		@Override
		public Direction left()
		{
			return SOUTH;
		}
		
		@Override
		public Direction right()
		{
			return NORTH;
		}
		
		@Override
		public Direction opposite()
		{
			return EAST;
		}
	};

	Direction(int ind)
	{
		index = ind;
	}

	public final int index;

	public abstract Direction left();

	public abstract Direction right();
	
	public abstract Direction opposite();

	public Direction translate(Moving move)
	{
		switch (move)
		{
			case BACKWARD:
				return opposite();

			case SHIFT_LEFT:
				return left();
			case SHIFT_RIGHT:
				return right();

			case FORWARD:
			default:
				return this;
		}
	}

}
