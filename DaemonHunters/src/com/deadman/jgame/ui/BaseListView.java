package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public abstract class BaseListView extends Control
{
	public ArrayList<ListViewItem> items = new ArrayList<>();

	protected int selectedInd = -1;
	protected ListViewItem selectedItem;
	protected Control content;

	@Override
	protected void onResize()
	{
		super.onResize();
		update(false);
	}

	public abstract void update(boolean withLayout);

	public void addItem(ListViewItem lvi)
	{
		items.add(lvi);
		lvi.addControlListener(lvi_listener);
		//update();
	}

	public void removeItem(ListViewItem lvi)
	{
		items.remove(lvi);
		lvi.remove();
		//update();

		if (items.size() > 0)
		{
			selectedInd--;
			if (!selectNext())
			{
				selectedInd++;
				selectPrev();
			}
		}
		else
		{
			selectedInd = -1;
			selectedItem = null;
		}
	}

	public int itemsCount()
	{
		return items.size();
	}

	public void clear()
	{
		for (ListViewItem lvi : items)
		{
			lvi.remove();
			lvi.removeControlListener(lvi_listener);
		}
		items.clear();
		//update();
	}

	private ControlListener lvi_listener = new ControlListener()
	{
		@Override
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			selectItem((ListViewItem) sender);
		};

		@Override
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			if (e.getClickCount() > 1)
				BaseListView.this.onAction(ACTION_ITEM_DBLCLICK, sender);
		};
	};

	public boolean selectNext()
	{
		if (selectedInd >= items.size() - 1) return false;

		for (int i = selectedInd + 1; i < items.size(); i++)
		{
			ListViewItem it = items.get(i);
			if (it.visible)
			{
				selectItem(it);
				return true;
			}
		}
		
		return false;
	}

	public boolean selectPrev()
	{
		if (selectedInd <= 0) return false;

		for (int i = selectedInd - 1; i >= 0; i--)
		{
			ListViewItem it = items.get(i);
			if (it.visible)
			{
				selectItem(it);
				return true;
			}
		}
		
		return false;
	}

	public void selectItem(ListViewItem lvi)
	{
		if (lvi == selectedItem) return;

		if (selectedItem != null) selectedItem.onDeselected();
		selectedItem = lvi;
		if (lvi != null)
		{
			selectedInd = items.indexOf(selectedItem);
			lvi.onSelected();
		}
		onAction(ACTION_ITEM_SELECTED, lvi);
	}

	/*public void selectIndex(int i)
	{
		selectedInd = i;
		if (i == -1 || i >= items.size())
			selectItem(null);
		else
		{
			selectItem(items.get(i));
			ensureSelectedVisible();
		}
	}*/

	public void selectFirst()
	{
		for (ListViewItem it : items)
		{
			if (it.visible)
			{
				selectItem(it);
				break;
			}
		}
	}

	protected abstract void ensureSelectedVisible();

	public ListViewItem selectedItem()
	{
		return selectedItem;
	}

	public int selectedIndex()
	{
		return selectedInd;
	}

	public void selectByTag(Object tag)
	{
		if (items.size() == 0)
		{
			selectItem(null);
			return;
		}

		for (ListViewItem lvi : items)
			if (lvi.tag == tag)
			{
				selectItem(lvi);
				return;
			}

		selectFirst();
	}
}
