package com.deadman.dh.model.itemtypes;

import com.deadman.dh.model.items.ItemSlot;
import com.deadman.dh.model.items.LightEffect;

public class TorchType extends ItemType
{
	public TorchType(int id, int icon, int light)
	{
		super(id, icon);
		addEffect(new LightEffect(light));
	}
	
	@Override
	public boolean canEquip(ItemSlot slot)
	{
		return slot.type == ItemSlot.TYPE_LEFTHAND || slot.type == ItemSlot.TYPE_RIGHTHAND;
	}
}
