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
	private ItemsPage page;
	private Drawable _bgItem;
	private ArrayList<ItemSlot> _slots = new ArrayList<>();

	public AmmunitionGrid(int x, int y)
	{
		background = getDrawable(R.ui.ammunition);
		_bgItem = getDrawable(R.ui.ammunition_bgr);

		setBounds(x, y, background.width, background.height);

		initSlot(IND_BACKPACK, 10, 2, ItemSlot.TYPE_BACKPACK);
		initSlot(IND_HELM, 45, 2, ItemSlot.TYPE_HEAD);
		initSlot(IND_AMULET, 69, 2, ItemSlot.TYPE_AMULET);
		initSlot(IND_CLOAK, 88, 2, ItemSlot.TYPE_CLOAK);
		initSlot(IND_RIGHTHAND, 10, 25, ItemSlot.TYPE_RIGHTHAND);
		initSlot(IND_BODY, 45, 25, ItemSlot.TYPE_BODY);
		initSlot(IND_LEFTHAND, 78, 25, ItemSlot.TYPE_LEFTHAND);
		initSlot(IND_GLOVES, 10, 48, ItemSlot.TYPE_GLOVE);
		initSlot(IND_LEGGINS, 45, 48, ItemSlot.TYPE_LEGGIN);
		initSlot(IND_BRACERS, 78, 48, ItemSlot.TYPE_BRACER);
		initSlot(IND_RING1, 2, 72, ItemSlot.TYPE_RING);
		initSlot(IND_RING2, 21, 72, ItemSlot.TYPE_RING);
		initSlot(IND_BOOTS, 45, 72, ItemSlot.TYPE_BOOT);
		initSlot(IND_RING3, 69, 72, ItemSlot.TYPE_RING);
		initSlot(IND_RING4, 88, 72, ItemSlot.TYPE_RING);
	}

	private void initSlot(int ind, int posX, int posY, short type)
	{
		ItemSlot slot = new ItemSlot();
		slot.x = posX;
		slot.y = posY;
		slot.type = type;
		slot.bgrWithItem = _bgItem;
		slot.bgrWithoutItem = null;
		slot.pageX = ind;
		slot.pageY = 0;
		slot.bind(bind);
		addControl(slot);
		_slots.add(slot);
	}

	ItemSlotBind bind = new ItemSlotBind()
	{
		@Override
		public void setItem(ItemSlot slot, Item item)
		{
			page.set(item, slot.pageX, slot.pageY);
		}

		@Override
		public Item getItem(ItemSlot slot)
		{
			return page.get(slot.pageX, slot.pageY);
		}
	};

	public void setPage(ItemsPage page)
	{
		this.page = page;
	}

	public void setValidator(ItemMovingValidator validator)
	{
		for (ItemSlot s : _slots)
			s.validator = validator;
	}

	public static final int AMMUNITION_COUNT = 15;

	public static final int IND_BACKPACK = 0;
	public static final int IND_HELM = 1;
	public static final int IND_AMULET = 2;
	public static final int IND_CLOAK = 3;
	public static final int IND_RIGHTHAND = 4;
	public static final int IND_BODY = 5;
	public static final int IND_LEFTHAND = 6;
	public static final int IND_GLOVES = 7;
	public static final int IND_LEGGINS = 8;
	public static final int IND_BRACERS = 9;
	public static final int IND_RING1 = 10;
	public static final int IND_RING2 = 11;
	public static final int IND_BOOTS = 12;
	public static final int IND_RING3 = 13;
	public static final int IND_RING4 = 14;
}
