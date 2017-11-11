package com.deadman.dh.isometric;

import com.deadman.jgame.drawing.Drawable;

public class IsoObjectPart extends IsoSimpleObject
{
	public final IsoSimpleObject original;
	private final byte partX, partY; // Координаты внутри объекта

	public IsoObjectPart(IsoSimpleObject original, byte x, byte y)
	{
		super(original.sprite, original._state);
		this.original = original;
		this.partX = x;
		this.partY = y;
	}

	@Override
	public Drawable getDrawable()
	{
		//if (partX == 0 && partY == 0)
		return ((IsoBigSprite) original.sprite).getPart(partX, partY, original._state);
		//return null;
	}

	@Override
	public void remove()
	{
		IsoBigSprite bs = (IsoBigSprite) original.sprite;
		MapCell c = original.cell;
		MapCell[][] lvl = c.map.cells[c.z];

		for (byte x = 0; x < bs.width; x++)
			for (byte y = 0; y < bs.height; y++)
				lvl[c.x - x][c.y - y].setObject(null, sprite.type);
	}

	@Override
	public void copyTo(MapCell target)
	{
		if (partX == 0 && partY == 0)
			super.copyTo(target);
	}

	@Override
	public void copyToRandom(MapCell target)
	{
		if (partX == 0 && partY == 0)
			super.copyToRandom(target);
	}

	@Override
	public void setNextState()
	{
		original.setNextState();
	}
}
