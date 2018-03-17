package com.deadman.jgame.ui;

@LayoutInfo("RowLayout")
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
	
	public void widthByContent()
	{
		layout.widthByContent = true;
	}

	public void setRightToLeft()
	{
		layout.rightToLeft = true;
	}
}
