package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;

public abstract class ControlListener
{
	public void onPressed(Object sender, Point p, MouseEvent e)
	{
	}

	public void onReleased(Object sender, Point p, MouseEvent e)
	{
	}

	public void onClick(Object sender, Point p, MouseEvent e)
	{
	}

	public void onFocusLoss(Object sender)
	{
	}

	public void onAction(Object sender, int action, Object tag)
	{
	}

	public void onMouseEnter(Object sender)
	{
	}

	public void onMouseLeave(Object sender)
	{
	}

	public void onMouseMove(Control control, Point p, MouseEvent e)
	{
	}
	
	public void onControlPressed(Control control, MouseEvent e)
	{
	}
}
