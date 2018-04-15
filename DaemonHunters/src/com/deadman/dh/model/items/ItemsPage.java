package com.deadman.dh.model.items;

import java.util.ArrayList;

/**
 * Класс для хранения предметов, типа рюкзаков, хранилища гильдии, экипировки экипажа
 * @author dead_man
 */
public class ItemsPage
{
	public final Item[] items;

	public final String name;
	public final int width;
	public final int height;

	public ItemsPage(String name, int w, int h)
	{
		this.name = name;
		items = new Item[w * h];
		width = w;
		height = h;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public Item get(int x, int y)
	{
		return items[x + y * width];
	}

	public Item get(AmmunitionSlot slot)
	{
		return items[slot.id];
	}

	public void set(Item item, int x, int y)
	{
		/*if (item != null)
			System.out.println(name + " set item " + item + " to [" + x + ":" + y + "]");
		else if (items[x + y * width] != null) System.out.println("Remove " + items[x + y * width] + " from [" + x + ":" + y + "] in " + name);*/

		items[x + y * width] = item;
		onItemMoved(item, x, y);
	}

	public void set(Item item, AmmunitionSlot slot)
	{
		items[slot.id] = item;
		onItemMoved(item, slot.id, 0);
	}

	/**
	 * Кладет предмет в первый свободный слот
	 * @param item
	 * @return
	 */
	public boolean put(Item item)
	{
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] == null)
			{
				int x = i % width;
				int y = i / width;
				set(item, x, y);
				return true;
			}
		}
		return false;
	}

	public void putItems(ItemsPage fromPage)
	{
		for (int i = 0; i < fromPage.items.length; i++)
		{
			Item it = fromPage.items[i];
			if (it != null)
			{
				fromPage.items[i] = null;
				put(it);
			}
		}
	}

	public boolean isFull()
	{
		for (int i = 0; i < items.length; i++)
			if (items[i] == null) return false;
		return true;
	}

	private ArrayList<IItemsPageListener> _listeners;

	public void addListener(IItemsPageListener l)
	{
		if (_listeners == null)
			_listeners = new ArrayList<>();
		_listeners.add(l);
	}

	public void removeListener(IItemsPageListener l)
	{
		if (_listeners == null) return;
		_listeners.remove(l);
	}

	protected void onItemMoved(Item item, int x, int y)
	{
		if (_listeners == null) return;
		for (IItemsPageListener l : _listeners)
		{
			l.onItemMoved(this, item, x, y);
		}
	}

}
