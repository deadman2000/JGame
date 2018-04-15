package com.deadman.dh.model.items;

import java.util.ArrayList;

import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;

/**
 * Класс для отображения обмундирования персонажа
 * @author dead_man
 *
 */
public class AmmunitionGrid extends Control
{
	private UnitItemsPage page;
	private Drawable _bgItem;
	private ArrayList<ItemSlot> _slots = new ArrayList<>();

	public AmmunitionGrid(int x, int y)
	{
		background = getDrawable(R.ui.ammunition);
		_bgItem = getDrawable(R.ui.ammunition_bgr);

		setBounds(x, y, background.width, background.height);

		initSlot(AmmunitionSlot.BACKPACK, 10, 2, ItemSlotType.BACKPACK);
		initSlot(AmmunitionSlot.HELM, 45, 2, ItemSlotType.HEAD);
		initSlot(AmmunitionSlot.AMULET, 69, 2, ItemSlotType.AMULET);
		initSlot(AmmunitionSlot.CLOAK, 88, 2, ItemSlotType.CLOAK);
		initSlot(AmmunitionSlot.RIGHTHAND, 10, 25, ItemSlotType.RIGHTHAND);
		initSlot(AmmunitionSlot.BODY, 45, 25, ItemSlotType.BODY);
		initSlot(AmmunitionSlot.LEFTHAND, 78, 25, ItemSlotType.LEFTHAND);
		initSlot(AmmunitionSlot.GLOVES, 10, 48, ItemSlotType.GLOVE);
		initSlot(AmmunitionSlot.LEGGINS, 45, 48, ItemSlotType.LEGGIN);
		initSlot(AmmunitionSlot.BRACERS, 78, 48, ItemSlotType.BRACER);
		initSlot(AmmunitionSlot.RING1, 2, 72, ItemSlotType.RING);
		initSlot(AmmunitionSlot.RING2, 21, 72, ItemSlotType.RING);
		initSlot(AmmunitionSlot.BOOTS, 45, 72, ItemSlotType.BOOT);
		initSlot(AmmunitionSlot.RING3, 69, 72, ItemSlotType.RING);
		initSlot(AmmunitionSlot.RING4, 88, 72, ItemSlotType.RING);
	}

	private void initSlot(AmmunitionSlot s, int posX, int posY, ItemSlotType type)
	{
		ItemSlot slot = new ItemSlot();
		slot.x = posX;
		slot.y = posY;
		slot.type = type;
		slot.bgrWithItem = _bgItem;
		slot.bgrWithoutItem = null;
		slot.pageX = s.id;
		slot.pageY = 0;
		slot.bind(bind);
		addControl(slot);
		_slots.add(slot);
	}

	ItemSlotBind bind = new ItemSlotBind()
	{
		@Override
		public void setItem(ItemSlot slot, Item item) throws Exception
		{
			if (item != null && item.type.isTwoHanded)
			{
				if (slot.pageX == AmmunitionSlot.LEFTHAND.id)
				{
					Item right = page.get(AmmunitionSlot.RIGHTHAND);
					if (right != null)
					{
						if (page.unit.backpack.isFull())
							throw new Exception();

						page.set(null, AmmunitionSlot.RIGHTHAND);
						page.unit.backpack.put(right);
					}
				}
				else if (slot.pageX == AmmunitionSlot.RIGHTHAND.id)
				{
					Item left = page.get(AmmunitionSlot.LEFTHAND);
					if (left != null)
					{
						if (page.unit.backpack.isFull())
							throw new Exception();

						page.set(null, AmmunitionSlot.LEFTHAND);
						page.unit.backpack.put(left);
					}
				}
			}

			page.set(item, slot.pageX, slot.pageY);
		}

		@Override
		public Item getItem(ItemSlot slot)
		{
			return page.get(slot.pageX, slot.pageY);
		}
	};

	public void setPage(UnitItemsPage page)
	{
		this.page = page;
	}

	public void setValidator(ItemMovingValidator validator)
	{
		for (ItemSlot s : _slots)
			s.validator = validator;
	}
}
