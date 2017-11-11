package com.deadman.jgame.ui;

import com.deadman.jgame.drawing.Drawable;

public class ListViewItem extends Control
{
	public Object tag;

	public ListViewItem()
	{
	}

	public ListViewItem(Drawable img, Object t)
	{
		super(img);
		tag = t;
	}

	public void onSelected()
	{
	}

	public void onDeselected()
	{
	}
}
