package com.deadman.pixelgame;

import java.io.UnsupportedEncodingException;

public class SpeakText
{
	private int _time;
	byte[][] _lines;
	int[] shifts;
	int _x, _y;

	private static final int CHAR_MARGIN = -1;
	private static final int LINE_HEIGHT = 10;

	public SpeakText(String text, int x, int y)
	{
		_time = Math.max(text.length() * 100, 80);

		String[] lines = text.split("\n");
		_lines = new byte[lines.length][];
		shifts = new int[lines.length];

		int maxWidth = 0;
		int[] widths = new int[lines.length];

		for (int l = 0; l < lines.length; l++)
		{
			byte[] line = getBytes(lines[l]);
			if (line == null) continue;
			_lines[l] = line;
			int w = 0;
			int ll = line.length;
			for (int i = 0; i < ll; i++)
			{
				Picture ch = getCharPic(line[i]);
				if (ch != null)
				{
					w += ch.width;
					if (i < ll - 1)
						w += CHAR_MARGIN;
				}
			}

			widths[l] = w;
			if (w > maxWidth) maxWidth = w;
		}

		for (int l = 0; l < lines.length; l++)
		{
			shifts[l] = (maxWidth - widths[l]) / 2;
		}

		_y = Math.max(y - LINE_HEIGHT * lines.length, 0);
		_x = x - maxWidth / 2;
		if (_x + maxWidth > GameScreen.GAME_WIDTH)
			_x = GameScreen.GAME_WIDTH - maxWidth;
	}

	private byte[] getBytes(String string)
	{
		try
		{
			return string.getBytes("CP1251");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private Picture getCharPic(byte ch)
	{
		//System.out.println(ch & 0xff);
		int ind = (ch & 0xFF) - 0x20;
		if (ind < 0 || ind >= Game.screen.font.frames.length) return null;
		return Game.screen.font.frames[ind];
	}

	public void draw()
	{
		if (elapsed()) return;

		for (int l = 0; l < _lines.length; l++)
		{
			byte[] line = _lines[l];
			int x = shifts[l];
			for (int i = 0; i < line.length; i++)
			{
				Picture ch = getCharPic(line[i]);
				if (ch != null)
				{
					ch.drawAt(_x + x, _y + l * LINE_HEIGHT);
					x += ch.width + CHAR_MARGIN;
				}
			}
		}
		_time--;
	}

	public boolean elapsed()
	{
		return _time <= 0;
	}

	public void stop()
	{
		_time = 0;
	}
}
