package com.deadman.jgame.ui;

public class Column extends Control
{
	protected ColumnLayout layout;

	public Column()
	{
		setLayout(layout = new ColumnLayout());
	}
	
	public void fillContent()
	{
		layout.horizontalMode = ColumnLayout.H_FILL;
	}
	
	public void setSpacing(int value)
	{
		layout.spacing = value;
	}
}
