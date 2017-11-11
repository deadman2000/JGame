package com.deadman.dh.model.itemtypes;

import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.Potion;

public class PotionType extends ItemType
{
	public PotionType(int id, int icon)
	{
		super(id, icon);
	}
	
	@Override
	public Item generate()
	{
		return new Potion(this);
	}
}
