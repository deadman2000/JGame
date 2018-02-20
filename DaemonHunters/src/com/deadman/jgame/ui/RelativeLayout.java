package com.deadman.jgame.ui;

public class RelativeLayout extends Layout
{
	@Override
	public void apply()
	{
		for (Control control : target.childs())
		{
			applyLayout(control);
		}
	}
	
	private void applyLayout(Control control)
	{
		RelativeSettings sett = control.getLayoutSettings(RelativeSettings.class, null);
		if (sett == null) return;

		int x = control.x, y = control.y, width = control.width, height = control.height;

		if ((sett.anchor & ANCHOR_LEFT) != 0)       // По левому краю 
		{
			x = sett.paddingLeft;
			if ((sett.anchor & ANCHOR_RIGHT) != 0)
				width = target.width - sett.paddingLeft - sett.paddingRight;
		}
		else if ((sett.anchor & ANCHOR_RIGHT) != 0) // По правому краю
			x = target.width - sett.paddingRight - width;
		else                                        // По центру
			x = (target.width - width) / 2 + sett.paddingLeft - sett.paddingRight;

		if ((sett.anchor & ANCHOR_TOP) != 0)        // По верху 
		{
			y = sett.paddingTop;
			if ((sett.anchor & ANCHOR_BOTTOM) != 0)
				height = target.height - sett.paddingTop - sett.paddingBottom;
		}
		else if ((sett.anchor & ANCHOR_BOTTOM) != 0) // По низу
			y = target.height - sett.paddingBottom - height;
		else                                         // По центру
			y = (target.height - height) / 2 + sett.paddingTop - sett.paddingBottom;

		control.setBounds(x, y, width, height);
	}

	@Override
	public void onChildResize(Control child)
	{
		applyLayout(child);
	}

	public static RelativeSettings settings(Control c)
	{
		return c.getLayoutSettings(RelativeSettings.class);
	}

	public static class RelativeSettings extends ChildSettings
	{
		private int paddingLeft = 0, paddingRight = 0, paddingTop = 0, paddingBottom = 0;
		private int anchor = 0;

		public RelativeSettings alignLeft(int padding)
		{
			paddingLeft = padding;
			anchor |= ANCHOR_LEFT;
			return this;
		}

		public RelativeSettings alignRight()
		{
			anchor |= ANCHOR_RIGHT;
			return this;
		}

		public RelativeSettings alignRight(int padding)
		{
			paddingRight = padding;
			anchor |= ANCHOR_RIGHT;
			return this;
		}

		public RelativeSettings alignTop(int padding)
		{
			paddingTop = padding;
			anchor |= ANCHOR_TOP;
			return this;
		}

		public RelativeSettings alignTop()
		{
			anchor |= ANCHOR_TOP;
			return this;
		}

		public RelativeSettings alignBottom()
		{
			anchor |= ANCHOR_BOTTOM;
			return this;
		}

		public RelativeSettings alignBottom(int padding)
		{
			paddingBottom = padding;
			anchor |= ANCHOR_BOTTOM;
			return this;
		}

		public RelativeSettings alignRightTop()
		{
			anchor |= ANCHOR_RIGHT_TOP;
			return this;
		}

		public RelativeSettings alignRightTop(int right, int top)
		{
			anchor |= ANCHOR_RIGHT_TOP;
			paddingRight = right;
			paddingTop = top;
			return this;
		}

		public RelativeSettings fill()
		{
			anchor = ANCHOR_ALL;
			return this;
		}

		public RelativeSettings fillHeight()
		{
			anchor |= ANCHOR_TOP | ANCHOR_BOTTOM;
			return this;
		}

		public RelativeSettings fillWidth()
		{
			anchor |= ANCHOR_LEFT | ANCHOR_RIGHT;
			return this;
		}

		public RelativeSettings fill(int left, int top, int right, int bottom)
		{
			paddingLeft = left;
			paddingTop = top;
			paddingRight = right;
			paddingBottom = bottom;
			anchor |= ANCHOR_ALL;
			return this;
		}

		public RelativeSettings fill(int padding)
		{
			paddingLeft = padding;
			paddingTop = padding;
			paddingRight = padding;
			paddingBottom = padding;
			anchor |= ANCHOR_ALL;
			return this;
		}

		public RelativeSettings center()
		{
			anchor = ANCHOR_NONE;
			return this;
		}
	}

	public static final int ANCHOR_NONE = 0;
	public static final int ANCHOR_LEFT = 1 << 0;
	public static final int ANCHOR_RIGHT = 1 << 1;
	public static final int ANCHOR_TOP = 1 << 2;
	public static final int ANCHOR_BOTTOM = 1 << 3;
	public static final int ANCHOR_LEFT_TOP = ANCHOR_LEFT | ANCHOR_TOP;
	public static final int ANCHOR_RIGHT_TOP = ANCHOR_RIGHT | ANCHOR_TOP;
	public static final int ANCHOR_ALL = ANCHOR_LEFT | ANCHOR_RIGHT | ANCHOR_TOP | ANCHOR_BOTTOM;
}
