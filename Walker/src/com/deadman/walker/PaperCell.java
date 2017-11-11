package com.deadman.walker;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.resources.XCF;

public class PaperCell
{
	private static Drawable w_n, w_e, w_s, w_w, floor;

	public static void init(XCF xcf)
	{
		floor = xcf.getDrawable("map_floor");
		w_n = xcf.getDrawable("wall_n");
		w_e = xcf.getDrawable("wall_e");
		w_s = xcf.getDrawable("wall_s");
		w_w = xcf.getDrawable("wall_w");
	}

	public void drawAt(int x, int y)
	{
		if (_wn) w_n.drawAt(x, y - 2);
		if (_we) w_e.drawAt(x + 7, y);
		if (_ws) w_s.drawAt(x, y + 7);
		if (_ww) w_w.drawAt(x -2, y);
	}

	private boolean _wn, _we, _ww, _ws;

	public void explore(Cell cell)
	{
		//_floor = true;
		if (cell.walls != null)
		{
			_wn = cell.walls[Direction.NORTH.index] != null;
			_we = cell.walls[Direction.EAST.index] != null;
			_ww = cell.walls[Direction.WEST.index] != null;
			_ws = cell.walls[Direction.SOUTH.index] != null;
		}
	}

	public void drawFloor(int x, int y)
	{
		floor.drawAt(x, y);
	}
}
