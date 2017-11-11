package com.deadman.gameeditor.resources;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Класс части изображения
 * @author dead_man
 */
public class PicturePart
{
	public int x, y;
	public int width, height;
	public int aX, aY;

	public PicturePart(int x, int y, int width, int height, int aX, int aY)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.aX = aX;
		this.aY = aY;
	}

	public Rectangle getBounds()
	{
		return new Rectangle(-aX, -aY, width, height);
	}
}
