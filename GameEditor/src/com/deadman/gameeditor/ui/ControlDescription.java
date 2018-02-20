package com.deadman.gameeditor.ui;

import java.util.ArrayList;

public class ControlDescription
{
	public final ControlInfo info;
	public final int deep;

	public ControlDescription parent;
	public final ArrayList<ControlDescription> childs;

	public LayoutDescription layout;
	public String id;

	public ControlDescription(ControlInfo controlInfo, int deep, String[] constructorArgs) throws ParseException
	{
		info = controlInfo;
		this.deep = deep;
		childs = new ArrayList<>();

		constructorArgs = info.resolveConstruct(constructorArgs);

		System.out.println(info.type().getElementName() + "(" + join(constructorArgs) + ")");
	}

	public boolean hasMethod(String name)
	{
		return info.hasMethod(name);
	}

	public boolean hasProperty(String name)
	{
		return info.hasProperty(name);
	}

	public void addChild(ControlDescription control)
	{
		childs.add(control);
		control.parent = this;
	}

	public void setProperty(String name, String value) throws ParseException
	{
		value = info.resolvePropertySet(name, value);
		System.out.println(info.type().getElementName() + "." + name + " = " + value);
	}

	public void addCall(String name, String[] callArgs) throws ParseException
	{
		callArgs = info.resolveCall(name, callArgs);
		System.out.println(info.type().getElementName() + "." + name + "(" + join(callArgs) + ")");
	}

	private String join(String[] args)
	{
		if (args == null)
			return "";
		return String.join(", ", args);
	}
}
