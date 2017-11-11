package com.deadman.dh.isometric.editor;

import com.deadman.dh.R;
import com.deadman.dh.isometric.IsoBigSprite;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.ListViewItem;

public class InstrItem extends ListViewItem
{
	private Drawable pic;

	private final static Drawable dr_wall = getDrawable(R.editor.ie_instr_wall);
	private final static Drawable dr_floor = getDrawable(R.editor.ie_instr_floor);

	private int dx;

	private boolean drawFloor = true, drawWall = true;

	public InstrItem(IsoSprite spr)
	{
		tag = spr;
		pic = spr.getPic();
		if (pic == null)
			System.err.println("No pic " + spr);
		
		switch (spr.type)
		{
			case IsoSprite.FLOOR:
				drawWall = false;
				break;
			case IsoSprite.OBJECT:
				drawWall = false;
				break;
			case IsoSprite.OBJ_ON_WALL:
				drawFloor = false;
				break;
		}
		
		if (spr instanceof IsoBigSprite)
		{
			IsoBigSprite bs = (IsoBigSprite) spr;
			width = 36 + (bs.width - 1) * 16 + (bs.height - 1) * 16;
			dx = (bs.width - 1) * 16;
		}
		else
			width = 36;
		height = 57;
	}

	@Override
	public void onSelected()
	{
		bgrColor = 0xFF808080;
	}

	@Override
	public void draw()
	{
		super.draw();

		int drawX = scrX + dx;
		if (drawFloor)
			dr_floor.drawAt(drawX, scrY + 2);
		if (drawWall)
			dr_wall.drawAt(drawX, scrY + 2);
		if (pic != null)
			pic.drawAt(drawX, scrY + 2);
	}

	@Override
	public void onDeselected()
	{
		bgrColor = 0;
	}
}
