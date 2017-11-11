package com.deadman.jgame.ui;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;

public class ProgressBar extends Control
{
	private Drawable _bgr;
	private int _color;

	private int p; // Размер заполненной части в пикселях

	public ProgressBar(int res, int x, int y)
	{
		_bgr = getDrawable(res);
		setBounds(x, y, _bgr.width, _bgr.height);
	}

	public ProgressBar(Drawable bgr, int x, int y)
	{
		_bgr = bgr;
		setBounds(x, y, bgr.width, bgr.height); // anim.frames[0].width, anim.frames[0].height
	}

	public ProgressBar(int color, int x, int y, int w, int h)
	{
		_color = color;
		setBounds(x, y, w, h);
	}

	public void setValue(float val)
	{
		p = (int) (width * val);
	}

	public void setValue(int val)
	{
		p = width * (val % 100) / 100;
	}

	@Override
	public void draw()
	{
		super.draw();

		if (p > 0)
		{
			if (_bgr != null)
			{
				if (p > 1)
					_bgr.drawAt(scrX, scrY, 0, 0, p, height);
				_bgr.drawAt(scrX + p - 1, scrY, width - 1, 0, 1, height);
			}
			else
			{
				GameScreen.screen.drawRect(scrX, scrY, p, height, _color);
			}
		}
	}
}
