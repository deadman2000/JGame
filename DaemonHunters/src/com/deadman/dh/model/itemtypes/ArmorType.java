package com.deadman.dh.model.itemtypes;

import com.deadman.dh.model.items.Armor;
import com.deadman.dh.model.items.Item;

public class ArmorType extends ItemType
{
	public int[] armor;
	
	@Override
	public Item generate()
	{
		return new Armor(this);
	}
}
