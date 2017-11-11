package com.deadman.dh.model.itemtypes;

import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.RangedWeapon;

public class RangedWeaponType extends WeaponType
{
	public RangedWeaponType(int id, int icon, int attackTime)
	{
		super(id, icon, attackTime);
	}

	@Override
	public Item generate()
	{
		return new RangedWeapon(this);
	}
}
