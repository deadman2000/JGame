package com.deadman.jgame.ui;

public class FillLayout extends Layout
{
	@Override
	public void apply()
	{
		for (Control c : target.childs())
		{
			c.setBounds(0, 0, target.width, target.height);
		}
	}
}
