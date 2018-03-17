package com.deadman.dh.model.items;

import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.itemtypes.ItemType;

public class Item
{
	public ItemType type;
	public int count;

	private static final int[] it_shifts_x = new int[] { 0, -1, 1, 2, -2, -1, 0, 1, -2, -1 };
	private static final int[] it_shifts_y = new int[] { 0, -2, -1, 0, 1, -2, -1, -2, 2, -1 };
	private static final int IT_SHIFTS_COUNT = it_shifts_y.length;

	public Item(ItemType type)
	{
		this.type = type;
		count = 1;
		//System.out.println("Created " + this);
	}

	public void drawAt(int x, int y)
	{
		type.icon.drawAt(x, y);
		if (count > 1) // Отрисовка подписи количества
		{
			String text = "" + count;
			int lx = x + ItemSlot.ITEM_WIDTH / 2 - GlobalEngine.fnt3x5_white.getTextWidth(text) - 1;
			int ly = y + ItemSlot.ITEM_HEIGHT / 2 - 6;
			GlobalEngine.fnt3x5_white.drawAt(lx, ly, text);
		}
	}

	public void drawIsoAt(int x, int y)
	{
		if (type.isoDrawable != null)
			type.isoDrawable.drawAt(x, y);
		else
			type.icon.drawAt(x + 18 + it_shifts_x[hashCode() % IT_SHIFTS_COUNT], y + 38 + it_shifts_y[hashCode() % IT_SHIFTS_COUNT]);
	}

	public Item setCount(int value)
	{
		count = value;
		return this;
	}

	public Item clone()
	{
		return new Item(type);
	}

	public void moveTo(ItemsPage itemsPage, AmmunitionSlot slot)
	{
		itemsPage.set(this, slot);
	}

	public void moveTo(ItemsPage itemsPage, int x, int y)
	{
		itemsPage.set(this, x, y);
	}

	public void equip(GameCharacter unit)
	{
		for (ItemSlotType slot : type.slots)
		{
			for (int i = 0; i < slot.cells.length; i++)
			{
				if (unit.ammunition.get(slot.cells[i]) == null)
				{
					moveTo(unit.ammunition, slot.cells[i]);
					return;
				}
			}
		}

		unit.moveToBackpack(this);
	}

	@Override
	public String toString()
	{
		if (count > 1)
			return type.id + " (" + count + ") #" + hashCode();
		else
			return type.id + " #" + hashCode();
	}

	public String getName()
	{
		return type.name;
	}

	public boolean canActivate() 
	{
		return type.canActivate();
	}

	public boolean activate(GameCharacter owner, IsoObject target)
	{
		if (type.activate(owner, target)) // Используем эффекты
		{
			if (type.consumable)
				count--; // Потребляем
			return true;
		}
		else
			return false;
	}

	public void applyPassive(MapCell cell)
	{
		type.applyPassive(cell);
	}

	public void applyPassive(GameCharacter unit)
	{
		type.applyPassive(unit);
	}

	public void appendDescription(StringBuilder sb)
	{
	}

	public String equipSprite()
	{
		return type.equipSprite;
	}
}
