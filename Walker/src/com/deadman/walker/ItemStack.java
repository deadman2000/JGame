package com.deadman.walker;

import java.util.ArrayList;

import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.jgame.drawing.Drawable;

public class ItemStack extends ArrayList<Item>
{
	private static final long serialVersionUID = 7721636379338139705L;

	public void drawAt(int x, int y, int distance)
	{
		for (int i = 0; i < size(); i++)
		{
			Item it = get(i);
			Drawable pic = ((WItemType) it.type).pic[distance / 2];
			if (pic != null)
			{
				pic.drawAt(x + shiftX(it), y + shiftY(it));
			}
		}
	}

	public boolean pick(int x, int y)
	{
		for (int i = size() - 1; i >= 0; i--)
		{
			Item it = get(i);
			Drawable top = ((WItemType) it.type).pic[0];
			if (top.containsAt(x - shiftX(it), y - shiftY(it)))
			{
				ItemSlot.pick(it, this);
				remove(i);
				return true;
			}
		}
		return false;
	}
	
	private int shiftX(Item it)
	{
		return (it.hashCode() % 5) - 2;
	}

	private int shiftY(Item it)
	{
		return ((it.hashCode() >> 0xffff) % 5) - 2;
	}

}
