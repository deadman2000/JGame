package com.deadman.jgame.ui;

public class Row extends Control
{
	protected RowLayout layout;
	
	public Row()
	{
		setLayout(layout = new RowLayout());
	}

	public void fillContent()
	{
		layout.verticalMode = RowLayout.V_FILL;
	}
	
	@Property("spacing")
	public void setSpacing(int value)
	{
		layout.spacing = value;
	}

	public void setRightToLeft()
	{
		layout.rightToLeft = true;
	}
}
