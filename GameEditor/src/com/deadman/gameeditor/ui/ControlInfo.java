package com.deadman.gameeditor.ui;

public class ControlInfo extends ClassInfo
{
	public ControlDescription createDescription(ControlDescription parent, int deep, String[] constructorArgs) throws ParseException
	{
		ControlDescription control = new ControlDescription(this, deep, constructorArgs);
		if (parent != null)
			parent.addChild(control);
		return control;
	}

	@Override
	protected ClassStorage<?> getStorage()
	{
		return compiler.controls;
	}
}
