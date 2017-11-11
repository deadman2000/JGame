package com.deadman.walker;

public class Map
{
	public int width, height;

	public Cell[][] cells;

	public Map(int w, int h)
	{
		width = w;
		height = h;
		cells = new Cell[w][h];
	}

	public Cell getCell(int x, int y)
	{
		if (x < 0 || y < 0 || x >= width || y >= height)
			return null;
		return cells[x][y];
	}

	public Cell getCell(Position p)
	{
		return getCell(p.x, p.y);
	}

	public Cell getCell(Position p, Shift pos)
	{
		return getCell(p.x, p.y, p.dir, pos);
	}

	public Cell getCell(int x, int y, Direction dir, Shift pos)
	{
		switch (dir)
		{
			case NORTH:
				switch (pos)
				{
					case FAR_LEFT_LEFT:
						return getCell(x - 2, y - 2);
					case FAR_LEFT:
						return getCell(x - 1, y - 2);
					case FAR:
						return getCell(x, y - 2);
					case FAR_RIGHT:
						return getCell(x + 1, y - 2);
					case FAR_RIGHT_RIGHT:
						return getCell(x + 2, y - 2);

					case AHEAD_LEFT:
						return getCell(x - 1, y - 1);
					case AHEAD:
						return getCell(x, y - 1);
					case AHEAD_RIGHT:
						return getCell(x + 1, y - 1);

					case NEAR_LEFT:
						return getCell(x - 1, y);
					case HERE:
						return getCell(x, y);
					case NEAR_RIGHT:
						return getCell(x + 1, y);

					default:
						return null;
				}

			case EAST:
				switch (pos)
				{
					case FAR_LEFT_LEFT:
						return getCell(x + 2, y - 2);
					case FAR_LEFT:
						return getCell(x + 2, y - 1);
					case FAR:
						return getCell(x + 2, y);
					case FAR_RIGHT:
						return getCell(x + 2, y + 1);
					case FAR_RIGHT_RIGHT:
						return getCell(x + 2, y + 2);

					case AHEAD_LEFT:
						return getCell(x + 1, y - 1);
					case AHEAD:
						return getCell(x + 1, y);
					case AHEAD_RIGHT:
						return getCell(x + 1, y + 1);

					case NEAR_LEFT:
						return getCell(x, y - 1);
					case HERE:
						return getCell(x, y);
					case NEAR_RIGHT:
						return getCell(x, y + 1);

					default:
						return null;
				}

			case SOUTH:
				switch (pos)
				{
					case FAR_LEFT_LEFT:
						return getCell(x + 2, y + 2);
					case FAR_LEFT:
						return getCell(x + 1, y + 2);
					case FAR:
						return getCell(x, y + 2);
					case FAR_RIGHT:
						return getCell(x - 1, y + 2);
					case FAR_RIGHT_RIGHT:
						return getCell(x - 2, y + 2);

					case AHEAD_LEFT:
						return getCell(x + 1, y + 1);
					case AHEAD:
						return getCell(x, y + 1);
					case AHEAD_RIGHT:
						return getCell(x - 1, y + 1);

					case NEAR_LEFT:
						return getCell(x + 1, y);
					case HERE:
						return getCell(x, y);
					case NEAR_RIGHT:
						return getCell(x - 1, y);

					default:
						return null;
				}

			case WEST:
				switch (pos)
				{
					case FAR_LEFT_LEFT:
						return getCell(x - 2, y + 2);
					case FAR_LEFT:
						return getCell(x - 2, y + 1);
					case FAR:
						return getCell(x - 2, y);
					case FAR_RIGHT:
						return getCell(x - 2, y - 1);
					case FAR_RIGHT_RIGHT:
						return getCell(x - 2, y - 2);

					case AHEAD_LEFT:
						return getCell(x - 1, y + 1);
					case AHEAD:
						return getCell(x - 1, y);
					case AHEAD_RIGHT:
						return getCell(x - 1, y - 1);

					case NEAR_LEFT:
						return getCell(x, y + 1);
					case HERE:
						return getCell(x, y);
					case NEAR_RIGHT:
						return getCell(x, y - 1);

					default:
						return null;
				}

			default:
				return null;
		}
	}

	public boolean canMove(int fromX, int fromY, Direction direction)
	{
		Cell here = getCell(fromX, fromY);
		if (here == null)
			return false;
		if (!here.canMove(direction))
			return false;

		Cell dest = getCell(fromX, fromY, direction, Shift.AHEAD);
		if (dest == null)
			return false;
		if (!dest.isEmpty())
			return false;

		return true;
	}
}
