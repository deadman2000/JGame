package com.deadman.dh.isometric;

import com.deadman.jgame.ui.Control;

public class IsoControl extends Control
{
	public int cellX, cellY;
	public IsoViewer viewer;

	public IsoControl(int cellX, int cellY)
	{
		this.cellX = cellX;
		this.cellY = cellY;
	}

	public IsoControl(MapCell cell)
	{
		cellX = cell.x;
		cellY = cell.y;
	}
	
	@Override
	public void remove()
	{
		viewer.removeIsoControl(this);
	}
}
