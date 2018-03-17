package com.deadman.jgame.ui;

public abstract class Layout
{
	public Control target;
	public String name;
	
	public abstract void apply();

	public static abstract class ChildSettings
	{
	}

	public void onChildResize(Control child)
	{
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
