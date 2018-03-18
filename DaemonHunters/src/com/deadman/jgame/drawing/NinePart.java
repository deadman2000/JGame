package com.deadman.jgame.drawing;

public class NinePart extends Drawable
{
	private final Drawable[] _parts;
	private final int pl, pr, pt, pb; // paddings

	public NinePart(Drawable[] parts)
	{
		_parts = parts;
		pl = parts[0].width;
		pt = parts[0].height;
		pr = parts[4].width;
		pb = parts[4].height;
	}

	@Override
	protected void draw(int x, int y)
	{
	}

	@Override
	protected void fill(int x, int y, int w, int h)
	{
		if (_parts.length > 8)
			_parts[8].fillAt(x + pl, y + pt, w - pl - pr, h - pt - pb);

		_parts[0].drawAt(x, y);                   // top left
		_parts[2].drawAt(x + w - pr, y);          // top right
		_parts[4].drawAt(x + w - pr, y + h - pb); // bottom right
		_parts[6].drawAt(x, y + h - pb);          // bottom left

		_parts[1].fillAt(x + pl, y, w - pl - pr, pt);          // top 
		_parts[3].fillAt(x + w - pr, y + pt, pr, h - pt - pb); // right
		_parts[5].fillAt(x + pl, y + h - pb, w - pl - pr, pb); // bottom
		_parts[7].fillAt(x, y + pt, pl, h - pt - pb);          // left
	}

	@Override
	protected void draw(int x, int y, int w, int h)
	{
		if (_parts.length > 8)
			_parts[8].drawAt(x + pl, y + pt, w - pl - pr, h - pt - pb);

		_parts[0].drawAt(x, y);                   // top left
		_parts[2].drawAt(x + w - pr, y);          // top right
		_parts[4].drawAt(x + w - pr, y + h - pb); // bottom right
		_parts[6].drawAt(x, y + h - pb);          // bottom left

		_parts[1].drawAt(x + pl, y, w - pl - pr, pt);          // top 
		_parts[3].drawAt(x + w - pr, y + pt, pr, h - pt - pb); // right
		_parts[5].drawAt(x + pl, y + h - pb, w - pl - pr, pb); // bottom
		_parts[7].drawAt(x, y + pt, pl, h - pt - pb);          // left
	}
}
