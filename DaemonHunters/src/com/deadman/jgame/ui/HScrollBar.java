package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.jgame.drawing.GameScreen;

public class HScrollBar extends Control
{
	private Button btLeft, btRight, btScroll;

	public HScrollBar(ScrollBarTheme theme)
	{
		setLayout(new RelativeLayout());

		btLeft = new Button(theme.left, theme.left_pr);
		btLeft.addControlListener(btn_listener);
		addControl(btLeft);

		btRight = new Button(theme.right, theme.right_pr);
		RelativeLayout.settings(btRight).alignRight();
		btRight.addControlListener(btn_listener);
		addControl(btRight);

		if (theme.h_bgr != null)
		{
			Control bgr = new Control(theme.h_bgr);
			RelativeLayout.settings(bgr).alignLeft(btLeft.width).alignRight(btRight.width);
			addControl(bgr);
		}

		btScroll = new Button(theme.h_pos, theme.h_pos_pr);
		btScroll.setPosition(btLeft.width, 0);
		btScroll.addControlListener(btn_listener);
		addControl(btScroll);

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
