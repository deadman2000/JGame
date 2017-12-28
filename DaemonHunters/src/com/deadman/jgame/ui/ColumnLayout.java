package com.deadman.jgame.ui;

public class ColumnLayout extends Layout
{
	public boolean heightByContent = false; // Изменять высоту контрола под размер дочерних элементов
	public int horizontalMode = H_NONE;
	public int spacing = 0;

	@Override
	public void apply()
	{
		int visible = 0; // Количество видимых элементов
		int h = 0; // Сумма высот у нерастянутых элементов
		int cnt = 0; // Количество растянутых элементов
		for (Control c : target.childs())
		{
			if (!c.visible || c.removed) continue;

			visible++;

			ColumnSettings sett = c.getLayoutSettings(ColumnSettings.class, defSettings);
			if (sett.isFillHegiht)
				cnt++;
			else
				h += c.height;
		}

		int fillHeight = 0;
		if (cnt > 0 && target.height > h)
			fillHeight = (target.height - h - (visible - 1) * spacing) / cnt;

		int y = 0;
		for (Control c : target.childs())
		{
			if (!c.visible || c.removed) continue;
			ColumnSettings sett = c.getLayoutSettings(ColumnSettings.class, defSettings);

			int height = c.height, width = c.width;
			
			int hm = sett.horizontalMode != H_NONE ? sett.horizontalMode : horizontalMode;
			switch (hm)
			{
				case H_FILL:
					c.x = 0;
					width = target.width;
					break;
			}

			c.y = y;

			if (sett.isFillHegiht)
				height = fillHeight;

			c.setSize(width, height);
			
			y += c.height + spacing;
		}
		
		if (heightByContent)
			target.setHeight(y - spacing);
	}

	public static ColumnSettings settings(Control c)
	{
		return c.getLayoutSettings(ColumnSettings.class);
	}

	private static ColumnSettings defSettings = new ColumnSettings();

	public static class ColumnSettings extends ChildSettings
	{
		private boolean isFillHegiht = false;
		private int horizontalMode = H_NONE;

		public ColumnSettings fillHeight()
		{
			isFillHegiht = true;
			return this;
		}

		public ColumnSettings fillWidth()
		{
			horizontalMode = H_FILL;
			return this;
		}
	}

	public static final int H_NONE = 0;
	public static final int H_FILL = 1;
}
