package com.deadman.dh.model.items;

public abstract class ItemMovingValidator
{
	public boolean canPick(Item item, Object source)
	{
		return true;
	}
	
	public boolean canDrop(Item item, Object target)
	{
		return true;
	}

	public boolean canUse(Item item, Object source)
	{
		return false;
	}

	public void useItem(Item item)
	{
	}
}
