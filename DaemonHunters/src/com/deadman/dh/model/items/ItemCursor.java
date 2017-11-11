package com.deadman.dh.model.items;

import com.deadman.jgame.drawing.Drawable;

public class ItemCursor extends Drawable
{
	private Drawable cursor;

	public ItemCursor(Drawable cur)
	{
		width = 16;
		height = 16;
		cursor = cur;
	}

	@Override
	protected void draw(int x, int y)
	{
		if (ItemSlot.pickedItem() != null)
			ItemSlot.pickedItem().drawAt(x, y);
		else if (cursor != null)
			cursor.drawAt(x, y);
	}
}
