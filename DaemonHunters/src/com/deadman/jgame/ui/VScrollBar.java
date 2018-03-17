package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.jgame.drawing.GameScreen;

public class VScrollBar extends Control
{
	private Button btUp, btDown, btScroll;

	public VScrollBar(ScrollBarTheme theme)
	{
		setLayout(new RelativeLayout());

		btUp = new Button(theme.up, theme.up_pr);
		btUp.addControlListener(btnTop_listener);
		addControl(btUp);

		btDown = new Button(theme.down, theme.down_pr);
		RelativeLayout.settings(btDown).alignBottom();
		btDown.addControlListener(btnBottom_listener);
		addControl(btDown);

		if (theme.v_bgr != null)
		{
			Control bgr = new Control(theme.v_bgr);
			RelativeLayout.settings(bgr).alignTop(btUp.height).alignBottom(btDown.height);
			addControl(bgr);
		}
		
		btScroll = new Button(theme.v_pos, theme.v_pos_pr);
		btScroll.setPosition((btUp.width - btScroll.width) / 2, btUp.height);
		btScroll.addControlListener(btnScroll_listener);
		addControl(btScroll);

		width = btUp.width;
	}

	public int min = 0, max = 10;
	public int scrollDelta = 1;

	private int pos;
	private int pressY;

	public int getPos()
	{
		return pos;
	}

	public void setPos(int i)
	{
		pos = Math.min(Math.max(i, min), max);
		calcScroll();
		onAction(ACTION_POSITION_CHANGED);
	}

	public void shift(int d)
	{
		setPos(pos + d);
	}
	
	ControlListener btnTop_listener = new ControlListener()
	{
		@Override
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			setPos(pos - scrollDelta);
		}
	};

	ControlListener btnBottom_listener = new ControlListener()
	{
		@Override
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			setPos(pos + scrollDelta);
		}
	};

	ControlListener btnScroll_listener = new ControlListener()
	{
		@Override
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			pressY = GameScreen.screen.cursorPos.y - btScroll.y;
		}

		@Override
		public void onReleased(Object sender, Point p, MouseEvent e)
		{
			calcScroll();
		}
	};

	@Override
	public void moveMouse(Point point, MouseEvent e)
	{
		if (btScroll.isPressed)
		{
			int bty = GameScreen.screen.cursorPos.y - pressY;
			bty = Math.min(Math.max(bty, btUp.height), height - btDown.height - btScroll.height);
			btScroll.setPosition(btScroll.x, bty);
			int p = min + (max - min) * (btScroll.y - btUp.height) / (height - btUp.height - btScroll.height - btDown.height);
			if (p != pos)
			{
				pos = p;
				onAction(ACTION_POSITION_CHANGED);
			}
		}
		else
			super.moveMouse(point, e);
	}

	private void calcScroll()
	{
		if (max == min) return;

		int by = (pos - min) * (height - btDown.height - btUp.height - btScroll.height) / (max - min) + btUp.height;
		btScroll.setPosition(btScroll.x, by);
	}

	@Override
	protected void onResize()
	{
		super.onResize();
		calcScroll();
	}
}
