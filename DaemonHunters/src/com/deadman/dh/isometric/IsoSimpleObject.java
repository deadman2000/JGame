package com.deadman.dh.isometric;

import com.deadman.jgame.drawing.Drawable;

public class IsoSimpleObject extends IsoObject
{
	public IsoSprite sprite;
	public byte subState; // Для двери и проч.

	public IsoSimpleObject(IsoSprite sprite, byte state)
	{
		this.sprite = sprite;
		_state = state;
	}

	@Override
	public String toString()
	{
		return sprite + " R:" + getRotation() + " S:" + getState();
	}

	public void setNextState()
	{
		_state = sprite.getNextState(_state);
	}
	
	@Override
	public float getLightSource()
	{
		return sprite.light_source;
	}

	@Override
	public float getLightRate()
	{
		return sprite.light_rate;
	}

	public Drawable getDrawable()
	{
		return sprite.getPic(_state);
	}

	public boolean contains(int x, int y)
	{
		Drawable pic = getDrawable();
		if (pic == null) return false;
		return pic.containsAt(x, y);
	}

	@Override
	public void drawAt(int x, int y)
	{
		Drawable draw = getDrawable();
		if (draw != null) draw.drawAt(x, y);
	}

	@Override
	public byte getSlotType()
	{
		return sprite.type;
	}

	public boolean isDoor()
	{
		return sprite.subType == IsoSprite.ST_WALL_DOOR;
	}

	@Override
	public boolean isMovable()
	{
		if (isDoor())
			return true;
		if (sprite.type == IsoSprite.OBJECT)
			return sprite.zheight > 0; // Для лестниц
		return false;
	}

	public void copyTo(MapCell target)
	{
		sprite.setTo(target, _state);
	}

	public void copyToRandom(MapCell target)
	{
		sprite.setTo(target, sprite.getRandomState(getRotation()));
	}
}
