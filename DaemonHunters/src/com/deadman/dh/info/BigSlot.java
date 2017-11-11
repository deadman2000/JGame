package com.deadman.dh.info;

import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;

public class BigSlot extends Control
{
	static Drawable bgr = getDrawable(R.ui.squad_slot);
	public Drawable icon;
	public boolean enabled = true;
	
	public BigSlot(int x, int y)
	{
		super(bgr, x, y);
	}

	@Override
	public void draw()
	{
		super.draw();

		if (icon != null)
			icon.drawAt(scrX + 4, scrY + 4);

		if (!enabled)
			disabled_bgr.drawAt(scrX + 4, scrY + 4, width - 8, height - 8);
	}
}
