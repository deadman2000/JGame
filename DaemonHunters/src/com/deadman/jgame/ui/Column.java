package com.deadman.jgame.ui;

public class Column extends Control
{
	public ColumnLayout columnLayout;

	public Column()
	{
		setLayout(columnLayout = new ColumnLayout());
	}
	
	public void fillContent()
	{
		columnLayout.horizontalMode = ColumnLayout.H_FILL;
	}
	
	public void setSpacing(int value)
	{
		columnLayout.spacing = value;
	}
	
	/*public void setHeightByContent()
	{
		columnLayout.heightByContent = true;
	}

	public void setTopPadding(int value)
	{
		columnLayout.topPadding = value;
	}

	public void setBottomPadding(int value)
	{
		columnLayout.bottomPadding = value;
	}*/
}
