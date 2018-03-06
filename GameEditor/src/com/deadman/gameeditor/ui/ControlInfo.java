package com.deadman.gameeditor.ui;

import org.eclipse.jdt.core.IType;

public class ControlInfo extends ClassInfo
{
	public LayoutInfo defaultLayout;

	public ControlDescription createDescription(ControlDescription parent, int deep) throws ParseException
	{
		ControlDescription control = new ControlDescription(this, parent, deep);
		if (defaultLayout != null)
			control.layout = defaultLayout.createDescription(control, deep + 1);
		return control;
	}

	@Override
	public void init(UICompiler compiler, IType type)
	{
		super.init(compiler, type);

		try
		{
			String layout = getAnnotationValue(type, "LayoutInfo", null);
			if (layout != null && layout.length() > 0)
				defaultLayout = compiler.layouts.get(layout);
		}
		catch (Exception e)
		{
		}
	}

	@Override
	protected ClassStorage<?> getStorage()
	{
		return compiler.controls;
	}
}
