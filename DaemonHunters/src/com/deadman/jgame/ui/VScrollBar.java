package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.jgame.drawing.GameScreen;

public class VScrollBar extends Control
{
	private Button btUp, btDown, btScroll;

	public VScrollBar(int bt_up, int bt_up_pr, int bt_scrol, int bt_scroll_pr, int bt_down, int bt_down_pr)
	{
		btUp = new Button(bt_up, bt_up_pr);
		btUp.setPosition(0, 0);
		btUp.anchor = ANCHOR_LEFT_TOP;
		btUp.addControlListener(btnTop_listener);
		addControl(btUp);

		btScroll = new Button(bt_scrol, bt_scroll_pr);
		btScroll.anchor = ANCHOR_TOP;
		btScroll.setPosition(0, btUp.height);
		btScroll.addControlListener(btnScroll_listener);
		addControl(btScroll);

		btDown = new Button(bt_down, bt_down_pr);
		btDown.setPosition(0, height - btDown.height);
		btDown.anchor = ANCHOR_LEFT | ANCHOR_BOTTOM;
		btDown.addControlListener(btnBottom_listener);
		addControl(btDown);

		width = btUp.width;
	}

	@Override
	public void setBounds(int x, int y, int width, int height, int anchor)
	{
		super.setBounds(x, y, width, height, anchor);
		btDown.setPosition(0, height - btDown.height);
	}

	public int min = 0, max = 10;
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
			setPos(pos - 1);
		}
	};

	ControlListener btnBottom_listener = new ControlListener()
	{
		@Override
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			setPos(pos + 1);
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
			btScroll.setPosition(0, bty);
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
		btScroll.setPosition(0, by);
	}

	@Override
	public void onScreenChanged()
	{
		super.onScreenChanged();
		calcScroll();
	}

}
