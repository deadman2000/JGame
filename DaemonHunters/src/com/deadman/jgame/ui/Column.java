package com.deadman.jgame.ui;

public class Column extends Control
{
	public ColumnLayout layout;

	public Column()
	{
		setLayout(layout = new ColumnLayout());
	}
	
	public void fillContent()
	{
		layout.horizontalMode = ColumnLayout.H_FILL;
	}
	
	@Property("spacing")
	public void setSpacing(int value)
	{
		layout.spacing = value;
	}
	
	public void heightByContent()
	{
		layout.heightByContent = true;
	}
}
