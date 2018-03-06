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
	
	public boolean hasMethod(String name)
	{
		return info.hasMethod(name);
	}

	public boolean hasProperty(String name)
	{
		return info.hasProperty(name);
	}

	public void setProperty(String name, String value) throws ParseException
	{
		String code = info.resolvePropertySet(name, value);
		appendCode("%1$s." + code + ";");
	}

	public void addCall(String name, String[] callArgs) throws ParseException
	{
		String code = info.resolveCall(name, callArgs);
		appendCode("%1$s." + code + ";");
	}
}
