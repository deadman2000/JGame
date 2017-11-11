package com.deadman.jgame.resources;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.NinePart;
import com.deadman.jgame.drawing.PicPart;
import com.deadman.jgame.drawing.Picture;

public class PicPartEntry extends ResourceEntry
{
	private Drawable[] _parts;
	private Drawable _drawable;
	private NinePart _np;
	private String[] _csv;

	public PicPartEntry(String[] csv)
	{
		super(csv);
		_csv = csv;
	}

	@Override
	public int getType()
	{
		return PICPARTS;
	}

	@Override
	public Drawable getDrawable()
	{
		if (_np == null)
		{
			getArray();
			if (_parts.length < 8)
			{
				System.err.println("It's not nine-part " + this);
				System.exit(1);
				return null;
			}

			_np = new NinePart(_parts);
		}
		return _np;
	}

	public Drawable[] getArray()
	{
		if (_parts == null)
		{
			_drawable = Picture.load(path);
			_parts = new Drawable[_csv.length - 3];
			for (int i = 3; i < _csv.length; i++)
			{
				_parts[i - 3] = load(_csv[i]);
			}
		}

		return _parts;
	}

	private PicPart load(String str)
	{
		String[] parts = str.split(",");
		if (parts.length < 6)
		{
			System.err.println("Wrong res " + str);
			return null;
		}

		return new PicPart(_drawable, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
	}

	@Override
	public String toString()
	{
		getArray();
		return path + " parts[" + _parts.length + "]";
	}
}
