package com.deadman.walker;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.resources.XCF;
import com.deadman.jgame.ui.Control;

public class PaperMap extends Control
{
	static final int CELL_SIZE = 7;

	private static Drawable pos;

	public static void init(XCF xcf)
	{
		pos = xcf.getDrawable("map_pos");
		PaperCell.init(xcf);
	}

	private Map map;
	public PaperCell[][] cells;

	public PaperMap(int x, int y, int w, int h)
	{
		setBounds(x, y, w, h);
		clip = true;
	}

	public void setMap(Map map)
	{
		this.map = map;
		cells = new PaperCell[map.width][map.height];
	}

	public void explore(int x, int y)
	{
		Cell cell = map.getCell(x, y);
		if (cell == null) return;
		PaperCell pc = cells[x][y];
		if (pc == null)
			pc = cells[x][y] = new PaperCell();
		pc.explore(cell);
	}

	@Override
	public void onDraw()
	{
		int cw = width / CELL_SIZE;
		int ch = height / CELL_SIZE;
		int fromX = WalkerEngine.position.x - cw / 2;
		int fromY = WalkerEngine.position.y - ch / 2;

		for (int dx = 0; dx < cw; dx++)
		{
			int x = fromX + dx;
			if (x < 0) continue;
			if (x >= map.width) break;
			
			int sx = scrX + dx * CELL_SIZE;
			
			for (int dy = 0; dy < ch; dy++)
			{
				int y = fromY + dy;
				if (y < 0) continue;
				if (y >= map.height) break;

				PaperCell c = cells[x][y];
				if (c != null)
					c.drawFloor(sx, scrY + dy * CELL_SIZE);
			}
		}

		for (int dx = 0; dx < cw; dx++)
		{
			int x = fromX + dx;
			if (x < 0) continue;
			if (x >= map.width) break;
			
			int sx = scrX + dx * CELL_SIZE;
			
			for (int dy = 0; dy < ch; dy++)
			{
				int y = fromY + dy;
				if (y < 0) continue;
				if (y >= map.height) break;

				PaperCell c = cells[x][y];
				if (c != null)
					c.drawAt(sx, scrY + dy * CELL_SIZE);
			}
		}

		int sx = scrX + cw / 2 * CELL_SIZE;
		int sy = scrY + ch / 2 * CELL_SIZE;
		switch (WalkerEngine.position.dir)
		{
			case NORTH:
				pos.drawAt(sx + 1, sy + 1);
				break;
			case SOUTH:
				pos.drawMVAt(sx + 1, sy + 1);
				break;
			case EAST:
				pos.drawRRAt(sx + 1, sy + 1);
				break;
			case WEST:
				pos.drawRLAt(sx + 1, sy + 1);
				break;
			default:
				break;
		}
	}
}
