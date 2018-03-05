package com.deadman.gameeditor.ui;

public class LayoutDescription extends InstanceDescription
{
	public final LayoutInfo info;

	public LayoutDescription(LayoutInfo layoutInfo, ControlDescription parent, int deep)
	{
		super(layoutInfo, parent, deep);
		info = layoutInfo;
	}
}
