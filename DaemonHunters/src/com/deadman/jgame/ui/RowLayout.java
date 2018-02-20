package com.deadman.jgame.ui;

import java.util.ArrayList;
import java.util.Collections;

public class RowLayout extends Layout
{
	public boolean widthByContent = false; // Изменять высоту контрола под размер дочерних элементов
	public int verticalMode = V_NONE;
	public int spacing = 0;
	public boolean rightToLeft = false;
	public int leftPadding;

	@Override
	public void apply()
	{
		int visible = 0; // Количество видимых элементов
		int w = 0; // Сумма ширин у нерастянутых элементов
		int cnt = 0; // Количество растянутых элементов
		for (Control c : target.childs())
		{
			if (!c.visible || c.removed) continue;

			visible++;

			RowSettings sett = c.getLayoutSettings(RowSettings.class, defSettings);
			if (sett.isFillWidth)
				cnt++;
			else
				w += c.width;
		}

		int fillWidth = 0;
		if (cnt > 0 && target.width > w)
		{
			fillWidth = (target.width - w - (visible - 1) * spacing) / cnt;
			if (fillWidth < 0) fillWidth = 0;
		}

		if (rightToLeft)
			alignRightToLeft(fillWidth);
		else
			alignLeftToRight(fillWidth);
	}

	private void alignLeftToRight(int fillWidth)
	{
		int x = leftPadding;
		for (Control c : target.childs())
		{
			if (!c.visible || c.removed) continue;
			RowSettings sett = c.getLayoutSettings(RowSettings.class, defSettings);

			int height = c.height, width = c.width;

			int vm = sett.verticalMode != V_NONE ? sett.verticalMode : verticalMode;
			switch (vm)
			{
				case V_FILL:
					c.y = 0;
					height = target.height; // TODO Возможно надо менять размеры через методы. Проверить
					break;
			}

			c.x = x;

			if (sett.isFillWidth)
				width = fillWidth;

			c.setSize(width, height);

			x += c.width + spacing;
		}

		if (widthByContent)
			target.setWidth(x - spacing);
	}

	private void alignRightToLeft(int fillWidth)
	{
		int x = target.width;
		ArrayList<Control> rev = new ArrayList<>(target.childs());
		Collections.reverse(rev);
		for (Control c : rev)
		{
			if (!c.visible || c.removed) continue;
			RowSettings sett = c.getLayoutSettings(RowSettings.class, defSettings);

			int height = c.height, width = c.width;

			int vm = sett.verticalMode != V_NONE ? sett.verticalMode : verticalMode;
			switch (vm)
			{
				case V_FILL:
					c.y = 0;
					height = target.height; // TODO Возможно надо менять размеры через методы. Проверить
					break;
			}

			if (sett.isFillWidth)
				width = fillWidth;

			c.setSize(width, height);

			x -= c.width;
			c.x = x;
			x -= spacing;
		}

		// TODO widthByContent
	}

	public static RowSettings settings(Control c)
	{
		return c.getLayoutSettings(RowSettings.class);
	}

	private static RowSettings defSettings = new RowSettings();

	public static class RowSettings extends ChildSettings
	{
		private boolean isFillWidth = false;
		private int verticalMode = V_NONE;

		public RowSettings fillWidth()
		{
			isFillWidth = true;
			return this;
		}

		public RowSettings fillHeight()
		{
			verticalMode = V_FILL;
			return this;
		}
	}

	public static final int V_NONE = 0;
	public static final int V_FILL = 1;
}
