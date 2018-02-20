package com.deadman.gameeditor.ui;

public class LayoutDescription
{
	public final LayoutInfo info;
	public final int deep;

	public LayoutDescription(LayoutInfo layoutInfo, int deep, String[] constructorArgs) throws ParseException
	{
		info = layoutInfo;
		this.deep = deep;
		
		constructorArgs = info.resolveConstruct(constructorArgs);
		
		System.out.println(info.type().getElementName() + "(" + join(constructorArgs) + ")");
	}
	
	// TODO Объединить с join из ControlDesscription
	private String join(String[] args)
	{
		if (args == null)
			return "";
		return String.join(", ", args);
	}
}
