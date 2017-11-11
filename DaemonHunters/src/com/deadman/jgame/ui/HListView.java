package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;

public class HListView extends BaseListView
{
	private HScrollBar scrollBar;
	private int spacing = 5; // Промежуток между итемами

	public int item_top_padding = 0;

	public HListView(int x, int y, int width, int height)
	{
		setBounds(x, y, width, height);
		clip = true;
	}
	
	public void setScrollBar(HScrollBar sb)
	{
		if (scrollBar != null)
		{
			scrollBar.removeControlListener(scrollBar_listener);
			scrollBar.remove();
		}
		
		scrollBar = sb;
		scrollBar.setBounds(0, height - scrollBar.height, width, scrollBar.height, ANCHOR_LEFT | ANCHOR_BOTTOM | ANCHOR_RIGHT);
		scrollBar.addControlListener(scrollBar_listener);
		addControl(scrollBar);

		scrollBar.max = -width;
		scrollBar.visible = false;
	}

	@Override
	public void clear()
	{
		super.clear();

		scrollBar.max = -width;
		scrollBar.visible = false;
	}

	private ControlListener scrollBar_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == ACTION_POSITION_CHANGED)
			{
				int tx = -scrollBar.getPos();
				for (ListViewItem lvi : items)
				{
					lvi.setPosition(tx, item_top_padding);
					tx += lvi.width + spacing;
				}
			}
		};
	};

	@Override
	protected void updateItemsPos()
	{
		// TODO Реализовать
	}

	@Override
	public void addItem(ListViewItem lvi)
	{
		int ix = 0;
		if (items.size() > 0)
		{
			ListViewItem lviLast = items.get(items.size() - 1);
			ix = lviLast.x + lviLast.width + spacing;
		}

		lvi.setPosition(ix, item_top_padding);
		super.addItem(lvi);

		scrollBar.max += lvi.width;
		if (items.size() > 1)
			scrollBar.max += spacing;

		if (scroll_visible)
			scrollBar.visible = scrollBar.max > 0;
	}

	public void setItems(ListViewItem[] values)
	{
		clear();

		for (int i = 0; i < values.length; i++)
			addItem(values[i]);
	}

	public void setItems(Collection<ListViewItem> list)
	{
		clear();
		for (ListViewItem it : list)
			addItem(it);
	}

	private boolean scroll_visible = true;

	public void setScrollbarVisible(boolean value)
	{
		scroll_visible = value;
		if (value)
			scrollBar.visible = scrollBar.max > 0;
		else
			scrollBar.visible = false;
	}

	private ListViewItem getFirstVisible()
	{
		for (int i = 0; i < items.size(); i++)
		{
			ListViewItem it = items.get(i);
			if (it.x + it.width > 0) { return it; }
		}
		return null;
	}

	public void scrollLeft()
	{
		if (scrollBar.max <= 0) return;
		ListViewItem first = getFirstVisible();
		if (first != null)
		{
			int d = -first.width - spacing;
			scrollBar.shift(d);
		}
	}

	public void scrollRight()
	{
		if (scrollBar.max <= 0) return;
		ListViewItem first = getFirstVisible();
		if (first != null)
		{
			int d = first.width + spacing;
			scrollBar.shift(d);
		}
	}

	@Override
	protected void ensureSelectedVisible()
	{
		int s = 0;
		for (ListViewItem i : items)
		{
			if (i == selectedItem) break;
			s += i.width + spacing;
		}
		
		if (scrollBar.getPos() > s)
		{
			scrollBar.shift(s - scrollBar.getPos());
			return;
		}

		int scrollR = scrollBar.getPos() + width;
		int itemR = s + selectedItem.width;
		if (itemR > scrollR)
			scrollBar.shift(itemR - scrollR);
	}

	@Override
	public void onMouseWheel(Point p, MouseWheelEvent e)
	{
		if (!intersectLocal(p)) return;
		
		if (e.getWheelRotation() > 0)
			scrollBar.shift(10);
		else
			scrollBar.shift(-10);
		e.consume();
	}
}
