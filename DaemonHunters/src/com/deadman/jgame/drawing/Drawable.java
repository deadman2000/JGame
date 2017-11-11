package com.deadman.jgame.drawing;

import java.awt.Graphics2D;
import java.awt.Point;

import com.deadman.jgame.resources.ResourceManager;

public abstract class Drawable
{
	public int width, height;
	public int anchorX, anchorY;

	public void setAnchor(int x, int y)
	{
		anchorX = x;
		anchorY = y;
	}

	public void setAnchorCenter()
	{
		anchorX = width / 2;
		anchorY = height / 2;
	}

	public void drawAt(Point p)
	{
		drawAt(p.x, p.y);
	}

	public void drawAt(int x, int y)
	{
		draw(x - anchorX, y - anchorY);
	}

	protected abstract void draw(int x, int y);

	public final void drawAt(int x, int y, int w, int h)
	{
		draw(x - anchorX, y - anchorY, w, h);
	}

	// draw - растянуть
	protected void draw(int x, int y, int w, int h)
	{
		System.err.println(this.getClass().getName() + " draw(x,y,w,h) not implemented");
	}

	public final void drawAt(int tx, int ty, int fx, int fy, int w, int h)
	{
		draw(tx - anchorX, ty - anchorY, w, h, fx, fy, w, h);
	}

	public final void drawAt(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		draw(tx - anchorX, ty - anchorY, tw, th, fx, fy, w, h);
	}

	protected void draw(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		System.err.println(this.getClass().getSimpleName() + " draw(tx,ty,tw,th,fx,fy,w,h) not implemented");
	}

	// fill - замостить
	public final void fillAt(int x, int y, int w, int h)
	{
		fill(x - anchorX, y - anchorY, w, h);
	}

	protected void fill(int x, int y, int w, int h)
	{
		System.err.println(this.getClass().getSimpleName() + " fill(x,y,w,h) not implemented");
	}

	public final void fillAt(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		fill(tx - anchorX, ty - anchorY, tw, th, fx, fy, w, h);
	}

	protected void fill(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		System.err.println(this.getClass().getSimpleName() + " fill(tx,ty,tw,th,fx,fy,w,h) not implemented");
	}

	/**
	 * Зеркальная по горизонтали отрисовка
	 * @param x
	 * @param y
	 */
	public final void drawMHAt(int x, int y)
	{
		drawMH(x + anchorX, y - anchorY);
	}

	protected void drawMH(int x, int y)
	{
		System.err.println(this.getClass().getSimpleName() + " drawMH(x,y) not implemented");
	}

	/**
	 * Зеркальная по горизонтали отрисовка
	 * @param x
	 * @param y
	 */
	public final void drawMVAt(int x, int y)
	{
		drawMV(x - anchorX, y - anchorY);
	}

	protected void drawMV(int x, int y)
	{
		System.err.println(this.getClass().getSimpleName() + " drawMV(x,y) not implemented");
	}

	/**
	 * Повернутая отрисовка
	 * @param x
	 * @param y
	 */
	public final void drawRRAt(int x, int y)
	{
		drawRR(x - anchorX, y - anchorY);
	}

	protected void drawRR(int x, int y)
	{
		System.err.println(this.getClass().getSimpleName() + " drawRR(x,y) not implemented");
	}

	/**
	 * Повернутая отрисовка
	 * @param x
	 * @param y
	 */
	public final void drawRLAt(int x, int y)
	{
		drawRL(x - anchorX, y - anchorY);
	}

	protected void drawRL(int x, int y)
	{
		System.err.println(this.getClass().getSimpleName() + " drawRL(x,y) not implemented");
	}

	public final boolean containsAt(int x, int y)
	{
		return contains(x + anchorX, y + anchorY);
	}

	protected boolean contains(int x, int y)
	{
		return true;
	}

	public static Drawable get(int id)
	{
		return ResourceManager.getResource(id).getDrawable();
	}

	public static Drawable get(String path)
	{
		Object val = ResourceManager.getField(path);
		if (val != null)
			return get((Integer)val);
		return null;
	}

	public void drawTo(Graphics2D g, int x, int y)
	{
		System.err.println(this.getClass().getSimpleName() + " drawTo not implemented");
	}

	public Drawable subpic(int x, int y, int width, int height)
	{
		System.err.println(this.getClass().getSimpleName() + " subpic not implemented");
		return null;
	}

	public Drawable getMirrored(int shift)
	{
		return new Mirrored(this, shift);
	}

	public Drawable replaceColors(int[] from, int[] to)
	{
		System.err.println(this.getClass().getSimpleName() + " replaceColors not implemented");
		return null;
	}
}
