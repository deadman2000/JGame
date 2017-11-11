package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.jgame.drawing.GameScreen;

public class HScrollBar extends Control
{
	Button btLeft, btRight, btScroll;

	public HScrollBar(int bt_left, int bt_left_pr, int bt_scroll, int bt_scroll_pr, int bt_right, int bt_right_pr)
	{
		btLeft = new Button(bt_left, bt_left_pr);
		btLeft.setPosition(0, 0);
		btLeft.anchor = ANCHOR_LEFT_TOP;
		btLeft.addControlListener(btn_listener);
		addControl(btLeft);

		btScroll = new Button(bt_scroll, bt_scroll_pr);
		btScroll.anchor = ANCHOR_TOP;
		btScroll.setPosition(btLeft.width, 0);
		btScroll.addControlListener(btn_listener);
		addControl(btScroll);

		btRight = new Button(bt_right, bt_right_pr);
		btRight.anchor = ANCHOR_RIGHT | ANCHOR_TOP;
		btRight.setPosition(width - btRight.width, 0);
		btRight.addControlListener(btn_listener);
		addControl(btRight);
		
		height = btLeft.height;
	}

	public int min = 0, max = 10;
	private int pos;
	int pressX;

	public int getPos()
	{
		return pos;
	}

	ControlListener btn_listener = new ControlListener()
	{
		@Override
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			if (sender == btLeft)
				setPos(pos - 1);
			else if (sender == btRight)
				setPos(pos + 1);
			else if (sender == btScroll)
				pressX = GameScreen.screen.cursorPos.x - btScroll.x;
		}

		@Override
		public void onReleased(Object sender, Point p, MouseEvent e)
		{
			if (sender == btScroll)
				calcScroll();
		}
	};

	@Override
	public void moveMouse(Point point, MouseEvent e)
	{
		if (btScroll.isPressed)
		{
			int btx = GameScreen.screen.cursorPos.x - pressX;
			btx = Math.min(Math.max(btx, btLeft.width), width - btRight.width - btScroll.width);
			btScroll.setPosition(btx, 0);
			int p = min + (max - min) * (btScroll.x - btLeft.width) / (width - btLeft.width - btScroll.width - btRight.width);
			if (p != pos)
			{
				pos = p;
				onAction(ACTION_POSITION_CHANGED);
			}
		}
		else
			super.moveMouse(point, e);
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

	private void calcScroll()
	{
		if (max == min) return;

		int bx = (pos - min) * (width - btRight.width - btLeft.width - btScroll.width) / (max - min) + btLeft.width;
		btScroll.setPosition(bx, 0);
	}
}
