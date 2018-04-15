package com.deadman.dh.model.itemtypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemEffect;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.dh.model.items.ItemSlotType;
import com.deadman.jgame.drawing.Drawable;

public class ItemType
{
	public int id;
	public String name; // Localized name 
	public Drawable icon;
	public ArrayList<ItemEffect> effects;
	public HashSet<ItemSlotType> slots;
	public int stackSize = 1;
	public Drawable isoDrawable;
	public boolean consumable;
	public String equipSprite;
	public boolean isTwoHanded;

	public boolean canEquip(ItemSlot slot)
	{
		return slots.contains(slot.type);
	}

	public Item generate()
	{
		return new Item(this);
	}

	public boolean canActivate()
	{
		for (ItemEffect e : effects)
			if (e.canActivate()) return true;
		return false;
	}

	public boolean activate(GameCharacter owner, IsoObject target)
	{
		boolean activated = false;

		for (ItemEffect e : effects)
		{
			if (e.activate(owner, target))
				activated = true;
		}

		return activated;
	}

	public void applyPassive(MapCell cell)
	{
		for (ItemEffect e : effects)
			e.applyPassive(cell);
	}

	public void applyPassive(GameCharacter unit)
	{
		for (ItemEffect e : effects)
			e.applyPassive(unit);
	}

	// Items

	public static Hashtable<Integer, ItemType> itemTypes = new Hashtable<>();

	public static ItemType getItemType(int id)
	{
		return itemTypes.get(id);
	}

	public static ItemTypeBuilder create(int id)
	{
		return new ItemTypeBuilder(id);
	}
}
