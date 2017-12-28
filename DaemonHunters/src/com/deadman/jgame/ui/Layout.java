package com.deadman.jgame.ui;

public abstract class Layout
{
	public Control target;
	
	public abstract void apply();

	public static abstract class ChildSettings
	{
	}
}
