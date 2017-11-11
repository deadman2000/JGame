package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;

public class VListView extends BaseListView
{
	private VScrollBar scrollBar;
	public int spacing = 0; // Вертикальный промежуток между итемами
	public int item_left_padding = 0;
	public int item_height = 16; // Высота одного итема
	public int item_width;

	public VListView(int x, int y, int width, int height)
	{
		setBounds(x, y, width, height);

		item_width = width;
		clip = true;
	}

	public void setScrollBar(VScrollBar sb)
	{
		if (scrollBar != null)
		{
			scrollBar.removeControlListener(scrollBar_listener);
			scrollBar.remove();
		}

		scrollBar = sb;
		addControl(scrollBar, 0);
		scrollBar.setBounds(width - scrollBar.width, 0, scrollBar.width, height, ANCHOR_RIGHT | ANCHOR_TOP | ANCHOR_BOTTOM);
		scrollBar.addControlListener(scrollBar_listener);
		updateScrollBar();
		
		item_width = width - scrollBar.width;
	}

	void updateScrollBar()
	{
		boolean needRecalc = false;
		int m = -spacing - height;
		for (ListViewItem lvi : items)
			if (lvi.visible)
				m += lvi.height + spacing;

		if (m > 0)
		{
			needRecalc = !scrollBar.visible;
			scrollBar.visible = true;
			scrollBar.max = m;
			item_width = width - scrollBar.width;
		}
		else
		{
			needRecalc = scrollBar.visible;

			if (scrollBar.visible)
				scrollBar.setPos(0);

			scrollBar.visible = false;
			item_width = width;
		}

		if (needRecalc)
		{
			for (ListViewItem lvi : items)
				lvi.width = item_width;
			updateItemsPos();
		}
	}

	@Override
	public void clear()
	{
		super.clear();

		if (scrollBar != null)
		{
			scrollBar.setPos(0);
			scrollBar.visible = false;
		}
	}

	private ControlListener scrollBar_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == ACTION_POSITION_CHANGED)
			{
				updateItemsPos();
			}
		};
	};

	@Override
	protected void updateItemsPos()
	{
		int ty = 0;
		if (scrollBar != null)
			ty = -scrollBar.getPos();
		for (ListViewItem lvi : items)
			if (lvi.visible)
			{
				lvi.setPosition(item_left_padding, ty);
				ty += lvi.height + spacing;
			}
	}

	public void update()
	{
		if (scrollBar != null)
			updateScrollBar();
		updateItemsPos();
	}

	@Override
	public void addItem(ListViewItem lvi)
	{
		int iy = 0;
		if (items.size() > 0)
		{
			ListViewItem lviLast = null;
			for (int i = items.size() - 1; i >= 0; i--)
			{
				lviLast = items.get(i);
				if (lviLast.visible) break;
			}

			if (lviLast.visible)
				iy = lviLast.y + lviLast.height + spacing;
		}

		if (item_height > 0)
			lvi.setBounds(item_left_padding, iy, item_width, item_height);
		else
			lvi.setBounds(item_left_padding, iy, item_width, lvi.height);

		super.addItem(lvi);

		if (scrollBar != null)
			updateScrollBar();
	}

	@Override
	public void onScreenChanged()
	{
		super.onScreenChanged();
		if (scrollBar != null)
			updateScrollBar();
	}

	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_DOWN)
		{
			if (selectedInd < items.size() - 1)
				selectIndex(selectedInd + 1);
			e.consume();
		}
		else if (key == KeyEvent.VK_UP)
		{
			if (selectedInd > 0)
				selectIndex(selectedInd - 1);
			e.consume();
		}
	}

	public void autoHeight()
	{
		height = size() * item_height;
	}

	@Override
	protected void ensureSelectedVisible()
	{
		int s = 0;
		for (ListViewItem i : items)
		{
			if (i == selectedItem) break;
			s += i.height + spacing;
		}
		
		if (scrollBar.getPos() > s)
		{
			scrollBar.shift(s - scrollBar.getPos());
			return;
		}

		int scrollB = scrollBar.getPos() + height;
		int itemB = s + selectedItem.height;
		if (itemB > scrollB)
			scrollBar.shift(itemB - scrollB);
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
