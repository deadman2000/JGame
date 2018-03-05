package com.deadman.dh.model.itemtypes;

import java.util.ArrayList;
import java.util.Hashtable;

import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemEffect;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.jgame.drawing.Drawable;

public class ItemType
{
	public int id;
	public String name; // Localized name 
	public Drawable icon;
	public ArrayList<ItemEffect> effects = new ArrayList<>();

	public Drawable isoDrawable;

	public ItemType(int id, int icon)
	{
		this(id, Drawable.get(icon));
	}

	public ItemType(int id, Drawable icon)
	{
		this.icon = icon;

		itemTypes.put(id, this);
	}

	public boolean isMultiply()
	{
		return false;
	}

	public boolean canEquip(ItemSlot slot)
	{
		return false;
	}

	public void equip(Item item, GameCharacter unit)
	{
		unit.moveToBackpack(item);
	}

	public Item generate()
	{
		return new Item(this);
	}

	public ItemType addEffect(ItemEffect eff)
	{
		effects.add(eff);
		return this;
	}

	public ItemType setIso(int dr)
	{
		isoDrawable = Drawable.get(dr);
		return this;
	}

	public void activate(GameCharacter owner, IsoObject target)
	{
		for (ItemEffect e : effects)
			e.activate(owner, target);
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

	private static Hashtable<Integer, ItemType> itemTypes = new Hashtable<>();

	public static ItemType getItemType(int id)
	{
		return itemTypes.get(id);
	}
}
