package com.deadman.dh;

import com.deadman.dh.model.Element;
import com.deadman.dh.model.items.HealEffect;
import com.deadman.dh.model.items.ItemSlotType;
import com.deadman.dh.model.items.LightEffect;
import com.deadman.dh.model.itemtypes.ItemType;

public final class ItemTypes
{
	static
	{
		System.out.println("Init item types");
	}

	public static final ItemType sword = ItemType	.create(0)
													.name("Меч")
													.icon(R.items.sword)
													.equipSprite("sword")
													.meele(Element.PHYSICAL, 10, 5)
													.slots(ItemSlotType.RIGHTHAND)
													.attackTime(30)
													.end();

	public static final ItemType bow = ItemType	.create(1)
												.name("Лук")
												.icon(R.items.bow)
												.slots(ItemSlotType.RIGHTHAND)
												.twoHanded()
												.ranged()
												.attackTime(120)
												.end();

	public static final ItemType red_potion = ItemType	.create(2)
														.name("Злье лечения")
														.icon(R.items.red_potion)
														.consumable()
														.effect(new HealEffect(50))
														.end();

	public static final ItemType armor = ItemType	.create(3)
													.name("Доспех")
													.icon(R.items.armor)
													.armor(ItemSlotType.BODY)
													.defence(Element.PHYSICAL, 5)
													.end();

	public static final ItemType shield = ItemType	.create(4)
													.name("Щит")
													.icon(R.items.shield)
													.armor(ItemSlotType.LEFTHAND)
													.defence(Element.PHYSICAL, 1)
													.end();

	public static final ItemType arrow = ItemType	.create(5)
													.name("Стрела")
													.icon(R.items.arrow)
													.stack(100)
													.end();

	public static final ItemType bottle = ItemType	.create(6)
													.name("Пустая склянка")
													.icon(R.items.bottle)
													.stack(10)
													.end();

	public static final ItemType book = ItemType.create(7)
												.name("Книга")
												.icon(R.items.book)
												.end();

	public static final ItemType torch = ItemType	.create(8)
													.name("Факел")
													.icon(R.items.torch)
													.iso(R.iso.SmallTorh)
													.effect(new LightEffect(10))
													.handsEquip()
													.end();

	public static final ItemType dead_unit = ItemType	.create(9)
														.name("Труп")
														.icon(R.items.dead_unit)
														.end();

	public static final ItemType player1 = ItemType	.create(10001)
													.name("Player1")
													.icon(R.items.player1)
													.iso(R.iso.Player1)
													.end();

	public static final ItemType player2 = ItemType	.create(10002)
													.name("Player2")
													.icon(R.items.player2)
													.iso(R.iso.Player2)
													.end();
}
