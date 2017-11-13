package com.deadman.dh.model.itemtypes;

import java.util.ArrayList;
import java.util.Hashtable;

import com.deadman.dh.R;
import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.Element;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.items.HealEffect;
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

	protected ItemType addEffect(ItemEffect eff)
	{
		effects.add(eff);
		return this;
	}

	protected ItemType setIso(int dr)
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

	public static final MeeleWeaponType sword = new MeeleWeaponType(0, R.items.sword, 30).damage(Element.PHYSICAL, 10, 5);
	public static final WeaponType bow = new RangedWeaponType(1, R.items.bow, 120).twoHanded();
	public static final ItemType red_potion = new PotionType(2, R.items.red_potion).addEffect(new HealEffect(50));
	public static final ArmorType armor = new ArmorType(3, ArmorType.BODY, R.items.armor);
	public static final ItemType shield = new ItemType(4, R.items.shield);
	public static final AmmunitionType arrow = new AmmunitionType(5, R.items.arrow);
	public static final ItemType bottle = new ItemType(6, R.items.bottle);
	public static final ItemType book = new ItemType(7, R.items.book);
	public static final ItemType torch = new TorchType(8, R.items.torch, 10).setIso(R.iso.SmallTorh);
	public static final ItemType dead_unit = new ItemType(9, R.items.dead_unit);

	public static final ItemType player1 = new ItemType(10001, R.items.player1).setIso(R.iso.Player1);
	public static final ItemType player2 = new ItemType(10002, R.items.player2).setIso(R.iso.Player2);

}
