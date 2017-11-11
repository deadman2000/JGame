package com.deadman.dh.model;

import java.util.ArrayList;

public class RiverNode
{
	public int id;
	public int x, y;
	public int width;

	public RiverNode temp_parent;

	//public RiverNode parent;
	//public RiverNode parent2;

	public ArrayList<RiverNode> parents;

	public RiverNode next;
	public boolean used = false;

	public RiverNode(int x, int y, int riverId)
	{
		this.x = x;
		this.y = y;
		id = riverId;
		width = 0;
	}

	public RiverNode(int x, int y, RiverNode par)
	{
		this.x = x;
		this.y = y;
		temp_parent = par;
		id = par.id;
		width = par.width + 1;
	}

	public void addParent(RiverNode n)
	{
		if (parents == null)
			parents = new ArrayList<>();
		parents.add(n);
	}

	public byte getRiverType()
	{
		if (width > 800)
			return GlobalMap.RIVER3_ID;
		else if (width > 400)
			return GlobalMap.RIVER2_ID;
		else
			return GlobalMap.RIVER_ID;
	}

	public int getRadius()
	{
		if (width < 200)
			return 2;
		return width / 100;
	}

	@Override
	public String toString()
	{
		String str = "ID:" + id + " {" + x + ":" + y + "} " + used + " ";
		if (parents != null)
			for (int i = 0; i < parents.size(); i++)
			{
				RiverNode p = parents.get(i);
				str += " P" + i + ":" + p.id + "{" + p.x + ":" + p.y + "}";
			}

		if (next != null)
			str += " N:" + next.id + "{" + next.x + ":" + next.y + "}";
		return str;
	}
}
