package com.deadman.dh.model.itemtypes;

import com.deadman.dh.model.Element;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.items.AmmunitionGrid;
import com.deadman.dh.model.items.Armor;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemSlot;

public class ArmorType extends ItemType
{
	private final short typeID;
	
	public ArmorType(int id, short typeID, int icon)
	{
		super(id, icon);
		this.typeID = typeID;
	}

	@Override
	public Item generate()
	{
		Armor ar = new Armor(this);
		ar.setArmor(Element.PHYSICAL, 1);
		return ar;
	}
	
	@Override
	public boolean canEquip(ItemSlot slot)
	{
		switch (typeID)
		{
			case HELM: return slot.type == ItemSlot.TYPE_HEAD;
			case AMULET: return slot.type == ItemSlot.TYPE_AMULET;
			case CLOAK: return slot.type == ItemSlot.TYPE_CLOAK;
			case BODY: return slot.type == ItemSlot.TYPE_BODY;
			case SHEILD: return slot.type == ItemSlot.TYPE_LEFTHAND;
			case BRACERS: return slot.type == ItemSlot.TYPE_BRACER;
			case LEGGINS: return slot.type == ItemSlot.TYPE_LEGGIN;
			case GLOVES: return slot.type == ItemSlot.TYPE_GLOVE;
			case BOOTS: return slot.type == ItemSlot.TYPE_BOOT;
			case RING: return slot.type == ItemSlot.TYPE_RING;
			case BACKPACK: return slot.type == ItemSlot.TYPE_BACKPACK;
			default: return false;
		}
	}
	
	@Override
	public void equip(Item item, GameCharacter unit)
	{
		switch (typeID)
		{
			case BACKPACK:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_BACKPACK);
				break;
			case HELM:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_HELM);
				break;
			case AMULET:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_AMULET);
				break;
			case CLOAK:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_CLOAK);
				break;
			case BODY:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_BODY);
				break;
			case SHEILD:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_LEFTHAND);
				break;
			case GLOVES:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_GLOVES);
				break;
			case LEGGINS:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_LEGGINS);
				break;
			case BRACERS:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_BRACERS);
				break;
			case BOOTS:
				item.moveTo(unit.ammunition, AmmunitionGrid.IND_BOOTS);
				break;
			case RING:
				if (unit.ammunition.get(AmmunitionGrid.IND_RING1, 0) == null)
					item.moveTo(unit.ammunition, AmmunitionGrid.IND_RING1);
				else if (unit.ammunition.get(AmmunitionGrid.IND_RING2, 0) == null)
					item.moveTo(unit.ammunition, AmmunitionGrid.IND_RING2);
				else if (unit.ammunition.get(AmmunitionGrid.IND_RING3, 0) == null)
					item.moveTo(unit.ammunition, AmmunitionGrid.IND_RING3);
				else if (unit.ammunition.get(AmmunitionGrid.IND_RING4, 0) == null)
					item.moveTo(unit.ammunition, AmmunitionGrid.IND_RING4);
				else
					unit.moveToBackpack(item);
				break;
			default:
				unit.moveToBackpack(item);
				break;
		}
	}

	public static final short HELM = 0;
	public static final short AMULET = 1;
	public static final short CLOAK = 2;
	public static final short BODY = 3;
	public static final short SHEILD = 5;
	public static final short BRACERS = 6;
	public static final short LEGGINS = 7;
	public static final short GLOVES = 8;
	public static final short BOOTS = 9;
	public static final short RING = 10;
	public static final short BACKPACK = 11;
}
