package com.deadman.gameeditor.resources;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Класс отрисовки
 * @author dead_man
 *
 */
public abstract class Drawable
{
	public abstract Rectangle getBounds();

	public boolean draggable()
	{
		return false;
	}
	
	public void setAnchor(int ax, int ay)
	{
	}

	public String getLabel()
	{
		return null;
	}
}
