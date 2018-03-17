package com.deadman.dh.model.items;

import java.util.Random;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;

/**
 * Класс для отображения списка предметов
 * @author dead_man
 *
 */
public class ItemsGrid extends Control
{
	public static int PAD_X = 3;
	public static int PAD_Y = 3;

	private ItemSlot[] slots;
	private Drawable[] _bgrs;

	public ItemsPage page;

	private ItemMovingValidator validator;

	public ItemsGrid()
	{
	}
	
	public ItemsGrid(int x, int y)
	{
		super(x, y);
	}

	public void setBgrs(int[] bgrs)
	{
		_bgrs = new Drawable[bgrs.length];
		for (int i = 0; i < bgrs.length; i++)
		{
			_bgrs[i] = getDrawable(bgrs[i]);
		}
	}

	public void setPage(ItemsPage page)
	{
		this.page = page;

		setSize(page.width * ItemSlot.ITEM_WIDTH + (page.width - 1) * PAD_X, page.height * ItemSlot.ITEM_HEIGHT + (page.height - 1) * PAD_Y);
		int c = page.width * page.height;
		if (slots == null)
		{
			slots = new ItemSlot[c];
			for (int i = 0; i < c; i++)
			{
				slots[i] = createSlot();
			}
		}
		else if (slots.length < c)
		{
			ItemSlot[] newSlots = new ItemSlot[c];
			System.arraycopy(slots, 0, newSlots, 0, slots.length);

			for (int i = slots.length; i < c; i++)
			{
				newSlots[i] = createSlot();
			}
			slots = newSlots;
		}
		else if (slots.length > c)
		{
			for (int i = c; i < slots.length; i++)
			{
				removeSlot(slots[i]);
			}

			ItemSlot[] newSlots = new ItemSlot[c];
			System.arraycopy(slots, 0, newSlots, 0, c);
			slots = newSlots;
		}

		int posX;
		int posY = 0;
		for (int iy = 0; iy < page.height; iy++)
		{
			posX = 0;
			for (int ix = 0; ix < page.width; ix++)
			{
				ItemSlot slot = slots[ix + iy * page.width];
				slot.pageX = ix;
				slot.pageY = iy;
				slot.setPosition(posX, posY);

				posX += ItemSlot.ITEM_WIDTH + PAD_X;
			}
			posY += ItemSlot.ITEM_HEIGHT + PAD_Y;
		}
	}

	private static Random RND = new Random();

	private ItemSlot createSlot()
	{
		ItemSlot slot = new ItemSlot();
		if (_bgrs == null || _bgrs.length == 0)
			slot.bgrColor = 0xff000000;
		else
		{
			Drawable bgr = _bgrs[RND.nextInt(_bgrs.length)];
			slot.bgrWithItem = bgr;
			slot.bgrWithoutItem = bgr;
		}
		slot.validator = validator;
		slot.bind(bind);
		addControl(slot);
		return slot;
	}

	private void removeSlot(ItemSlot slot)
	{
		slot.bind(null);
		slot.remove();
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

	private boolean enabled = true;

	public void enable()
	{
		if (enabled) return;
		enabled = true;
		for (ItemSlot it : slots)
			it.enabled = true;
	}

	public void disable()
	{
		if (!enabled) return;
		enabled = false;
		for (ItemSlot it : slots)
			it.enabled = false;
	}

	public void setValidator(ItemMovingValidator validator)
	{
		this.validator = validator;
		if (slots != null)
		{
			for (ItemSlot s : slots)
				s.validator = validator;
		}
	}
}
