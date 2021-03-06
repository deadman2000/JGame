package com.deadman.gameeditor.ui;

import org.eclipse.jdt.core.IType;

public class LayoutInfo extends ClassInfo
{
	public LayoutSettingsInfo settings;

	@Override
	public void init(UICompiler compiler, IType type)
	{
		super.init(compiler, type);

		// Ищем настройки чайлдов
		try
		{
			for (IType inner : type.getTypes())
			{
				if (inner.getElementName().contains("Settings"))
				{
					settings = new LayoutSettingsInfo();
					settings.init(compiler, inner);
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public LayoutDescription createDescription(ControlDescription parent, int deep) throws ParseException
	{
		return new LayoutDescription(this, parent, deep);
	}

	@Override
	protected ClassStorage<?> getStorage()
	{
		return compiler.layouts;
	}
}
