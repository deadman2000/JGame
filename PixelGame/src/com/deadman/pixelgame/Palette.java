package com.deadman.pixelgame;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;

public class Palette
{
	public int offset = -1;

	public int[] _colors;

	public Palette()
	{
	}

	public Palette(int[] colors)
	{
		_colors = colors;
	}

	public static Palette loadPalette(String fileName)
	{
		return loadPalette(fileName, 0);
	}

	public static Palette loadPaletteDOS(String fileName)
	{
		return loadPalette(fileName, 2);
	}

	public static Palette loadPalette(String fileName, int shift)
	{
		Palette pal = new Palette();

		FileInputStream in = null;
		try
		{
			in = new FileInputStream(fileName);
			pal._colors = new int[in.available() / 3];
			byte[] cBuff = new byte[3];
			for (int i = 0; i < pal._colors.length; i++)
			{
				in.read(cBuff);
				pal._colors[i] = new Color(cBuff[0] << shift, cBuff[1] << shift, cBuff[2] << shift).getRGB();
			}
		}
		catch (IOException ex)
		{
		}
		finally
		{
			if (in != null) try
			{
				in.close();
			}
			catch (IOException e)
			{
			}
		}

		return pal;
	}

	public int getNextOffset()
	{
		return offset + _colors.length;
	}

}
