package com.deadman.pixelgame;

public class SpriteObject extends GameObject
{
	private Sprite _sprite;
	
	public SpriteObject(Sprite sprite)
	{
		_sprite = sprite;
		setState(0);
	}
	
	private int _st;
	public void setState(int st)
	{
		_st = st;
		drawable = _sprite.frames[st];
	}

	public int getState()
	{
		return _st;
	}
}
