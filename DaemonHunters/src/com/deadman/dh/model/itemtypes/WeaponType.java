package com.deadman.dh.model.itemtypes;

import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.items.AmmunitionGrid;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemSlot;

public abstract class WeaponType extends ItemType
{
	public WeaponType(int id, int icon, int attackTime)
	{
		super(id, icon);
		this.attackTime = attackTime;
	}

	public int attackTime;
	public boolean twoHanded;

	public WeaponType twoHanded()
	{
		twoHanded = true;
		return this;
	}
	
	@Override
	public boolean canEquip(ItemSlot slot)
	{
		return slot.type == ItemSlot.TYPE_RIGHTHAND;
	}
	
	@Override
	public void equip(Item item, GameCharacter unit)
	{
		item.moveTo(unit.ammunition, AmmunitionGrid.IND_RIGHTHAND);
	}
}
