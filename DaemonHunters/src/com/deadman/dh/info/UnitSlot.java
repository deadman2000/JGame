package com.deadman.dh.info;

import com.deadman.dh.R;
import com.deadman.dh.model.Unit;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;

public class UnitSlot extends Control
{
	public boolean enabled = true;
	public final int index;
	public Unit unit;
	static Drawable bgr = getDrawable(R.ui.portrait_slot);

	public UnitSlot(int ind, int x, int y)
	{
		super(bgr, x, y);
		index = ind;
	}

	@Override
	public void draw()
	{
		super.draw();

		if (unit != null)
			unit.portrait.drawAt(scrX + 2, scrY + 2);

		if (!enabled)
			disabled_bgr.drawAt(scrX + 2, scrY + 2, Unit.PORTRAIT_WIDTH, Unit.PORTRAIT_HEIGHT);
	}
}
