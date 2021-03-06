package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;

public class VListView extends BaseListView
{
	private ContentItem content;
	private VScrollBar scrollBar;
	public boolean heightByContent = false; // TODO MaxHeight

	class ContentItem extends Column
	{
		public ContentItem()
		{
			layout.heightByContent = true;
			layout.horizontalMode = ColumnLayout.H_FILL;
		}

		@Override
		protected void onResize()
		{
			super.onResize();
			update(false);
		}
	}

	public VListView()
	{
		clip = true;
		RowLayout layout = new RowLayout(); // Строка для контента и скроллбара
		layout.verticalMode = RowLayout.V_FILL;
		setLayout(layout);

		addControl(content = new ContentItem());
		RowLayout.settings(content).fillWidth();
	}

	public void setSpacing(int value)
	{
		content.setSpacing(value);
		refreshLayout();
	}

	public void setScrollBar(ScrollBarTheme theme)
	{
		setScrollBar(new VScrollBar(theme));
	}
	
	public void setScrollBar(VScrollBar sb)
	{
		if (scrollBar != null)
		{
			scrollBar.removeControlListener(scrollBar_listener);
			scrollBar.remove();
		}
		sb.scrollDelta = 10;

		scrollBar = sb;
		addControl(scrollBar);
		scrollBar.addControlListener(scrollBar_listener);
		updateScrollBar();
	}

	private void updateScrollBar()
	{
		int m = content.height - height;
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
				content.y = -scrollBar.getPos();
			}
		};
	};

	@Override
	public void update(boolean withLayout)
	{
		if (withLayout)
			content.refreshLayout();

		if (heightByContent)
			setHeight(content.height);

		if (scrollBar != null)
			updateScrollBar();
	}

	@Override
	public void addItem(ListViewItem lvi) // TODO Может быть перетащить в базовый класс
	{
		content.addControl(lvi);
		super.addItem(lvi);
	}

	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_DOWN)
		{
			selectNext();
			e.consume();
		}
		else if (key == KeyEvent.VK_UP)
		{
			selectPrev();
			e.consume();
		}
	}

	@Override
	protected void ensureSelectedVisible()
	{
		if (scrollBar.getPos() > selectedItem.y)
		{
			scrollBar.shift(selectedItem.y - scrollBar.getPos());
			return;
		}

		int scrollB = scrollBar.getPos() + height;
		int itemB = selectedItem.y + selectedItem.height;
		if (itemB > scrollB)
			scrollBar.shift(itemB - scrollB);
	}

	@Override
	public void onMouseWheel(Point p, MouseWheelEvent e)
	{
		if (scrollBar == null) return;

		if (!intersectLocal(p)) return;

		if (e.getWheelRotation() > 0)
			scrollBar.shift(10);
		else
			scrollBar.shift(-10);
		e.consume();
	}
}
