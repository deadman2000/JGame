package com.deadman.gameeditor.ui;

public class LayoutDescription extends InstanceDescription
{
	public final LayoutInfo info;

	public LayoutDescription(LayoutInfo layoutInfo, ControlDescription parent, int deep)
	{
		super(layoutInfo, parent, deep);
		info = layoutInfo;
	}

	@Override
	protected void writeParentAppend(UIParser parser)
	{
		if (parent.parent == null) // is root
			writeCode(parser, "setLayout(%1$s);");
		else
			writeCode(parser, "%2$s.setLayout(%1$s);");
	}
}
