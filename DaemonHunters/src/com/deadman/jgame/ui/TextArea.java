package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseWheelEvent;

import com.deadman.dh.Game;
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

	private void adjustScroll()
	{
		if (la.height > height)
		{
			if (vscroll == null)
			{
				vscroll = Game.createVScrollInfo();
				vscroll.setBounds(width - vscroll.width, 0, vscroll.width, height);
				addControl(vscroll);
				vscroll.addControlListener(scroll_listener);
			}

			vscroll.visible = true;
			vscroll.max = la.height - height + 1;
			vscroll.setPos(0);
		}
		else
		{
			if (vscroll != null)
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
