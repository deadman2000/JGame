package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;

public class HListView extends BaseListView
{
	private ContentItem content;
	private HScrollBar scrollBar;

	public int item_top_padding = 0;
	
	class ContentItem extends Row
	{
		public ContentItem()
		{
			layout.widthByContent = true;
			layout.verticalMode = RowLayout.V_FILL;
		}
		
		@Override
		protected void onResize()
		{
			super.onResize();
			update();
		}
	}

	public HListView()
	{
		clip = true;
		ColumnLayout layout = new ColumnLayout();
		layout.horizontalMode = ColumnLayout.H_FILL;
		setLayout(layout);

		addControl(content = new ContentItem());
		ColumnLayout.settings(content).fillHeight();
	}

	public void setSpacing(int value)
	{
		content.setSpacing(value);
		refreshLayout();
	}

	public void setScrollBar(HScrollBar sb)
	{
		if (scrollBar != null)
		{
			scrollBar.removeControlListener(scrollBar_listener);
			scrollBar.remove();
		}
		
		scrollBar = sb;
		scrollBar.addControlListener(scrollBar_listener);
		addControl(scrollBar);
		updateScrollBar();
	}

	private void updateScrollBar()
	{
		int m = content.width - width;
		if (m > 0)
		{
			scrollBar.show();
			scrollBar.max = m;
		}
		else
		{
			if (scrollBar.visible)
			{
				scrollBar.setPos(0);
				scrollBar.hide();
			}
		}
	}

	private ControlListener scrollBar_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == ACTION_POSITION_CHANGED)
			{
				content.x = -scrollBar.getPos();
			}
		};
	};

	@Override
	public void update()
	{
		if (scrollBar != null)
			updateScrollBar();
	}
	
	@Override
	public void addItem(ListViewItem lvi)
	{
		content.addControl(lvi);
		super.addItem(lvi);
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
			int d = -first.width;
			scrollBar.shift(d);
		}
	}

	public void scrollRight()
	{
		if (scrollBar.max <= 0) return;
		ListViewItem first = getFirstVisible();
		if (first != null)
		{
			int d = first.width;
			scrollBar.shift(d);
		}
	}

	@Override
	protected void ensureSelectedVisible()
	{
		if (scrollBar.getPos() > selectedItem.x)
		{
			scrollBar.shift(selectedItem.x - scrollBar.getPos());
			return;
		}

		int scrollPos = scrollBar.getPos() + width;
		int itemPos = selectedItem.x + selectedItem.width;
		if (itemPos > scrollPos)
			scrollBar.shift(itemPos - scrollPos);
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
