package com.deadman.gameeditor.ui;

public class ControlDescription extends InstanceDescription
{
	public final ControlInfo info;
	public LayoutDescription layout;

	public ControlDescription(ControlInfo controlInfo, ControlDescription parent, int deep)
	{
		super(controlInfo, parent, deep);
		info = controlInfo;
	}
	
	public LayoutInfo layoutInfo()
	{
		if (layout != null) return layout.info;
		return info.defaultLayout;
	}
}
