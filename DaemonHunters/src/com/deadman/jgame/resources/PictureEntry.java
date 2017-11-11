package com.deadman.jgame.resources;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.Picture;

class PictureEntry extends ResourceEntry
{
	private Drawable _drawable;
	
	public PictureEntry(String[] csv)
	{
		super(csv);
	}

	@Override
	public int getType()
	{
		return PICTURE;
	}

	@Override
	public Drawable getDrawable()
	{
		if (_drawable == null)
			_drawable = Picture.load(path);
		return _drawable;
	}
}
