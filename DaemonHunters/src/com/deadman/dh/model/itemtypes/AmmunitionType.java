package com.deadman.dh.model.itemtypes;

public class AmmunitionType extends ItemType
{
	public AmmunitionType(int id, int icon)
	{
		super(id, icon);
	}

	@Override
	public boolean isMultiply()
	{
		return true;
	}
}
