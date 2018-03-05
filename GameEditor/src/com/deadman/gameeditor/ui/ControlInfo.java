package com.deadman.gameeditor.ui;

public class ControlInfo extends ClassInfo
{
	public ControlDescription createDescription(ControlDescription parent, int deep) throws ParseException
	{
		ControlDescription control = new ControlDescription(this, parent, deep);
		return control;
	}

	@Override
	protected ClassStorage<?> getStorage()
	{
		return compiler.controls;
	}
}
