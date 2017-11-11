package com.deadman.dh.isometric;

import java.util.ArrayList;

public class RouteNode
{
	public MapCell cell;

	public ArrayList<RouteNode> childs = new ArrayList<>();
	public RouteNode parent;

	public int cost;

	public int num;

	public boolean currentTurn; // Можем походить в этот ход

	public RouteNode(MapCell c)
	{
		cell = c;
	}

	@Override
	public String toString()
	{
		return "[" + num + "] " + cell.toString() + " " + (currentTurn ? "C" : "N") + " " + cost;
	}

	public void setParrent(RouteNode node, int nodeCost)
	{
		assert(node.parent != this);
		
		if (parent != null)
			parent.childs.remove(this);
		
		num = node.num + 1;
		cost = node.cost + nodeCost;
		parent = node;
		for (RouteNode p : childs)
		{
			p.updateParent();
		}
	}

	private void updateParent()
	{
		num = parent.num + 1;
		cost = parent.cost + cell.moveCost(parent.cell);
		
		for (RouteNode p : childs)
		{
			p.updateParent();
		}
	}
}
