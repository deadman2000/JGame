package com.deadman.walker;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.resources.XCF;

public class WallType
{
	public Drawable pic[];

	public WallType(XCF xcf, String name)
	{
		pic = new Drawable[7];
		for (int i = 0; i < pic.length; i++)
		{
			pic[i] = xcf.getDrawable(name + "-" + i);
		}
	}
}
