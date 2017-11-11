package com.deadman.walker;

import com.deadman.dh.model.itemtypes.ItemType;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.resources.XCF;

public class WItemType extends ItemType
{
	public Drawable pic[];

	public WItemType(String name, XCF xcf)
	{
		this(name, xcf, name);
	}
	
	public WItemType(String name, XCF xcf, String worldName)
	{
		super(name, xcf.getDrawable(name));
		
		pic = new Drawable[3];
		for (int i = 0; i < pic.length; i++)
			pic[i] = xcf.getDrawable(worldName + "-" + i);
	}
}
