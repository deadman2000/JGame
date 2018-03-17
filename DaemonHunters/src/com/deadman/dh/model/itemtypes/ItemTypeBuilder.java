package com.deadman.dh.model.itemtypes;

import java.util.ArrayList;
import java.util.HashSet;

import com.deadman.dh.model.Element;
import com.deadman.dh.model.items.ItemEffect;
import com.deadman.dh.model.items.ItemSlotType;
import com.deadman.jgame.drawing.Drawable;

public class ItemTypeBuilder
{
	enum ItemTypeEnum
	{
		BASIC,
		ARMOR,
		MEELEWEAPON,
		RANGEDWEAPON
	};

	private ItemTypeEnum _type = ItemTypeEnum.BASIC;
	private final int _id;
	private String _name;
	private Drawable _icon;
	private Drawable _isoDrawable;
	private ArrayList<ItemEffect> _effects = new ArrayList<>();
	private HashSet<ItemSlotType> _slots = new HashSet<>();
	private int _stackSize = 1;
	private boolean _consumable;
	private String _equipSprite;

	private Element _damageElement;
	private int _damageValue;
	private int _damageAmp;
	private int _attackTime;

	private int[] _armor = new int[Element.MAX_INDEX];

	public ItemTypeBuilder(int id)
	{
		_id = id;
	}

	public ItemType end()
	{
		ItemType it = createItemType();
		it.id = _id;
		if (_name == null) onError("No name");
		it.name = _name;
		if (_icon == null) onError("No icon");
		it.icon = _icon;
		it.isoDrawable = _isoDrawable;
		it.effects = _effects;
		it.slots = _slots;
		it.stackSize = _stackSize;
		it.consumable = _consumable;
		it.equipSprite = _equipSprite;

		if (it instanceof WeaponType)
		{
			WeaponType w = (WeaponType) it;
			w.attackTime = _attackTime;
		}

		if (it instanceof MeleeWeaponType)
		{
			MeleeWeaponType meele = (MeleeWeaponType) it;
			meele.element = _damageElement;
			meele.value = _damageValue;
			meele.amp = _damageAmp;
		}

		if (it instanceof ArmorType)
		{
			ArmorType a = (ArmorType) it;
			a.armor = _armor;
		}

		ItemType.itemTypes.put(_id, it);
		return it;
	}

	private ItemType createItemType()
	{
		switch (_type)
		{
			case BASIC:
				return new ItemType();
			case ARMOR:
				return new ArmorType();
			case MEELEWEAPON:
				return new MeleeWeaponType();
			case RANGEDWEAPON:
				return new RangedWeaponType();
			default:
				return null;
		}
	}

	public ItemTypeBuilder effect(ItemEffect eff)
	{
		_effects.add(eff);
		return this;
	}

	public ItemTypeBuilder icon(int dr)
	{
		_icon = Drawable.get(dr);
		return this;
	}

	public ItemTypeBuilder iso(int dr)
	{
		_isoDrawable = Drawable.get(dr);
		return this;
	}

	public ItemTypeBuilder name(String name)
	{
		_name = name;
		return this;
	}

	public ItemTypeBuilder stack(int count)
	{
		if (count < 1)
			onError("Stack size can't be less one");

		_stackSize = count;
		return this;
	}

	public ItemTypeBuilder slots(ItemSlotType... slots)
	{
		for (ItemSlotType s : slots)
			_slots.add(s);
		return this;
	}

	public ItemTypeBuilder handsEquip()
	{
		_slots.add(ItemSlotType.LEFTHAND);
		_slots.add(ItemSlotType.RIGHTHAND);
		return this;
	}

	public ItemTypeBuilder consumable()
	{
		_consumable = true;
		return this;
	}

	/**
	 * Задает название спрайта, отрисовывающимся на персонаже
	 * @param equipSprite
	 * @return
	 */
	public ItemTypeBuilder equipSprite(String equipSprite)
	{
		_equipSprite = equipSprite;
		return this;
	}

	// Weapon

	public ItemTypeBuilder meele(Element e, int v, int a)
	{
		_type = ItemTypeEnum.MEELEWEAPON;
		_damageElement = e;
		_damageValue = v;
		_damageAmp = a;
		return this;
	}

	public ItemTypeBuilder ranged()
	{
		_type = ItemTypeEnum.RANGEDWEAPON;
		return this;
	}

	public ItemTypeBuilder attackTime(int time)
	{
		_attackTime = time;
		return this;
	}

	// Armor

	public ItemTypeBuilder armor(ItemSlotType slot)
	{
		_type = ItemTypeEnum.ARMOR;
		_slots.add(slot);
		return this;
	}

	public ItemTypeBuilder defence(Element element, int value)
	{
		if (_type != ItemTypeEnum.ARMOR) onError("Defence only for armor");
		_armor[element.index] = value;
		return this;
	}

	private void onError(String message)
	{
		System.out.println("For item #" + _id + ": " + message);
	}

	public ItemTypeBuilder twoHanded()
	{
		return this;
	}
}
