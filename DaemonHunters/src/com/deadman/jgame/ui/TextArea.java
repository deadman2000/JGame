package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseWheelEvent;

import com.deadman.jgame.drawing.GameFont;

public class TextArea extends Control
{
	private Label la;
	private VScrollBar vscroll;
	private static final int RIGHT_PAD = 11; // TODO Брать из размера скролла

	public TextArea(GameFont font)
	{
		addControl(la = new Label(font));
		la.word_wrap = true;
		la.autosize = true;
		clip = true;
	}

	public void setText(String text)
	{
		la.y = 0;
		la.setText(text);
		adjustScroll();
	}

	@Override
	protected void onResize()
	{
		la.setSize(width - RIGHT_PAD, la.height);
		adjustScroll();
	}

	public void setVScrollBar(ScrollBarTheme theme)
	{
		setScrollBar(new VScrollBar(theme));
	}
	
	public void setScrollBar(VScrollBar scrollBar)
	{
		vscroll = scrollBar;
		vscroll.addControlListener(scroll_listener);
		addControl(vscroll);
		adjustScroll();
	}

	private void adjustScroll()
	{
		if (vscroll == null) return;

		vscroll.setBounds(width - vscroll.width, 0, vscroll.width, height);
		
		if (la.height > height)
		{
			vscroll.visible = true;
			vscroll.max = la.height - height + 1;
			vscroll.setPos(0);
		}
		else
		{
			vscroll.visible = false;
		}
	}

	private ControlListener scroll_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == ACTION_POSITION_CHANGED)
			{
				la.y = -vscroll.getPos();
			}
		};
	};

	@Override
	public void onMouseWheel(Point p, MouseWheelEvent e)
	{
		if (!intersectLocal(p)) return;
		if (vscroll == null || !vscroll.visible) return;

		if (e.getWheelRotation() > 0)
			vscroll.shift(10);
		else
			vscroll.shift(-10);
		e.consume();
	}
}
