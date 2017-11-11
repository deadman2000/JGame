package com.deadman.dh.isometric;

public class IsoWay
{
	public final MapCell[] cells;
	public final int[] costs;
	public final int cost;
	public final boolean completed; // Путь может быть пройден целиком

	public IsoWay(MapCell[] cells, int[] costs, int cost, boolean completed)
	{
		this.cells = cells;
		this.costs = costs;
		this.cost = cost;
		this.completed = completed;
	}
}
