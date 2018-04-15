package com.deadman.dh.model.items;

public abstract class ItemSlotBind
{
	public abstract Item getItem(ItemSlot slot);
	
	public abstract void setItem(ItemSlot slot, Item item) throws Exception;
}
