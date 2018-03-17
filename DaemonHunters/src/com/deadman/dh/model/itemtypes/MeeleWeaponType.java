package com.deadman.dh.model.itemtypes;

import com.deadman.dh.model.Element;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.MeleeWeapon;

public class MeeleWeaponType extends WeaponType
{
	// Для урона
	public Element element;
	public int value;
	public int amp;

	@Override
	public Item generate()
	{
		return new MeleeWeapon(this);
	}
}
