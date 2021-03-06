package com.deadman.dh.model.items;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;

public class ItemSlot extends Control
{
	public static int ITEM_WIDTH = 16;
	public static int ITEM_HEIGHT = 16;

	private ItemSlotBind _bind;

	public Drawable bgrWithItem, bgrWithoutItem;

	public boolean enabled = true;

	public int pageX, pageY;

	public ItemMovingValidator validator;

	public ItemSlotType type = ItemSlotType.ALL;

	public ItemSlot()
	{
		setSize(ITEM_WIDTH, ITEM_HEIGHT);
	}

	public Item getItem()
	{
		if (_bind != null)
			return _bind.getItem(this);
		return null;
	}

	public void setItem(Item item) throws Exception
	{
		if (_bind != null)
			_bind.setItem(this, item);
	}

	public void bind(ItemSlotBind bind)
	{
		_bind = bind;
	}

	@Override
	public void draw()
	{
		super.draw();

		Item item = getItem();
		if (item != null)
		{
			if (bgrWithItem != null) bgrWithItem.drawAt(scrX, scrY);

			int x = scrX + (width >> 1);
			int y = scrY + (height >> 1);

			// Отрисовка предмета
			item.drawAt(x, y);
		}
		else
		{
			if (bgrWithoutItem != null) bgrWithoutItem.drawAt(scrX, scrY);
		}

		if (!enabled) disabled_bgr.drawAt(scrX, scrY, width, height);
	}

	protected boolean canMove(Item it)
	{
		return true;
	}

	@Override
	protected void onMouseMove(Point p, MouseEvent e)
	{
		super.onMouseMove(p, e);

		p = clientToScreen(p);
		Item item = getItem();
		if (ItemInfoPanel.ENABLED && _pickedItem == null)
			ItemInfoPanel	.panel()
							.show(this, item);
	}

	@Override
	protected void onMouseLeave()
	{
		if (ItemInfoPanel.ENABLED && getItem() != null) ItemInfoPanel.hidePanel();
	}

	@Override
	protected void onPressed(Point p, MouseEvent e)
	{
		if (ItemInfoPanel.ENABLED) ItemInfoPanel.hidePanel();

		if (!enabled) return;

		Item item = getItem();
		Item picked = pickedItem();

		try
		{
			if (picked != null) // В руках предмет
			{
				if (type != ItemSlotType.ALL && !picked.type.canEquip(this))
					return;

				if (validator != null && !validator.canDrop(picked, this))
					return;

				if (item == null) // Нажали в пустую
				{
					if (e.getButton() == 3 && picked.count > 1) // ПКМ - Кладем 1
					{
						setItem(picked.cloneOne());
						picked.count--;
					}
					else // Кладем все что взяли
					{
						setItem(picked);
						drop();
					}
				}
				else // Нажали в слот с предметом
				{
					if (item.type.stackSize > 1 && item.type == picked.type) // Тот же тип и возможность объединения
					{
						if (e.getButton() == 3 && picked.count > 1) // ПКМ - Кладем 1 
						{
							if (item.count < item.type.stackSize)
							{
								item.count++;
								picked.count--;
							}
						}
						else // Переносим сколько взяли
						{
							// Кладем не больше, чем помещается в стек
							if (item.count + picked.count <= item.type.stackSize)
							{
								item.count += picked.count;
								drop();
							}
							else
							{
								int cnt = item.type.stackSize - item.count;
								item.count += cnt;
								picked.count -= cnt;
							}
						}
					}
					else // Не можем объеденить. Меняем местами
					{
						pick(item, this);
						setItem(picked);
					}
				}
			}
			else // В руках ничего нет
			{
				if (item == null) return; // В ячейке тоже ничего нет

				if (validator != null && !validator.canPick(item, this))
					return;

				// Нажали на предмет

				if (e.getButton() == 3) // ПКМ - проверяем, можно ли использовать итем
				{
					if (validator != null && validator.canUse(item, this))
					{
						validator.useItem(item);
						if (item.count == 0)
							setItem(null);
						return;
					}
				}

				if (e.getButton() == 3 && item.count > 1) // ПКМ - берем половину
				{
					int cnt = item.count / 2;
					pick(item	.cloneOne()
								.setCount(cnt), this);
					item.count -= cnt;
				}
				else if (e.isShiftDown() && item.count > 1) // GameScreen.KEY_SHIFT
				{
					int c = item.count / 2;
					pick(item	.cloneOne()
								.setCount(c), this);
					item.count -= c;
				}
				else
				{
					pick(item, this); // Берем все
					setItem(null);
				}
			}
		}
		catch (Exception ex)
		{
			return;
		}
	}

	public static boolean somePages(Object source1, Object source2)
	{
		if (source1 == source2)
			return true;
		if (source1 instanceof ItemSlot)
		{
			if (source2 instanceof ItemSlot)
				return ((ItemSlot) source1).parent == ((ItemSlot) source2).parent;
		}
		return false;
	}

	private static Item _pickedItem = null;

	public static Item pickedItem()
	{
		return _pickedItem;
	}

	private static Object _pickedSource = null;

	public static Object pickedSource()
	{
		return _pickedSource;
	}

	public static void pick(Item it, Object source)
	{
		_pickedItem = it;
		_pickedSource = source;
	}

	public static void drop()
	{
		_pickedItem = null;
		_pickedSource = null;
	}
}
