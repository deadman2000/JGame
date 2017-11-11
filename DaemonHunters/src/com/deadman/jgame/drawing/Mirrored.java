package com.deadman.jgame.drawing;

public class Mirrored extends Drawable
{
	public Drawable original;
	private int _shift;

	public Mirrored(Drawable original, int shift)
	{
		this.original = original;
		_shift = shift;
		width = original.width;
		height = original.height;
		anchorX = -original.anchorX - 1 + shift + width; // Смещаем, чтобы копия была по другую сторону якоря
		anchorY = original.anchorY;
	}

	@Override
	protected void draw(int x, int y)
	{
		original.drawMH(x, y); // Рисуем без якоря, мы его копируем
	}

	@Override
	protected void draw(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		original.draw(tx, ty, tw, th, -fx, fy, -w, h);
	}

	@Override
	public Drawable subpic(int x, int y, int w, int h)
	{
		Drawable sub = original.subpic(width - x - w - original.anchorX - anchorX, y, w, h);
		return new Mirrored(sub, _shift);
	}

	@Override
	public Drawable replaceColors(int[] from, int[] to)
	{
		return new Mirrored(original.replaceColors(from, to), _shift); // TODO оптимизировать. может есть уже колоризованный оригинал
	}
	
	@Override
	public String toString()
	{
		return String.format("Mirror (%d:%d) of %s", anchorX, anchorY, original.toString());
	}
}
