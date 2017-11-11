package com.deadman.dh.model.itemtypes;

import com.deadman.dh.model.Element;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.MeleeWeapon;

public class MeeleWeaponType extends WeaponType
{
	public MeeleWeaponType(int id, int icon, int attackTime)
	{
		super(id, icon, attackTime);
	}

	// Для урона
	public Element element;
	public int value;
	public int amp;
	
	public MeeleWeaponType damage(Element e, int v, int a)
	{
		element = e;
		value = v;
		amp = a;
		return this;
	}

	@Override
	public Item generate()
	{
		return new MeleeWeapon(this);
	}
}
