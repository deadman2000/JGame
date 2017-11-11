package com.deadman.pixelgame;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;

public class Matrix
{
	protected int width, height;
	public byte[] pixels;

	public void shift(int offset)
	{
		for (int i = 0; i < pixels.length; i++)
			pixels[i] += offset;
	}

	public byte get(Point p)
	{
		return pixels[p.x + p.y * width];
	}

	public static Matrix loadStream(InputStream stream) throws IOException
	{
		DataStream in = new DataStream(stream);
		try
		{
			Matrix m = new Matrix();
			m.width = in.readShort();
			m.height = in.readShort();
			m.pixels = new byte[m.width * m.height];
			in.read(m.pixels);
			return m;
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
	}

	// Bits

	public boolean isBitSet(Point p, int bit)
	{
		return (get(p) & (1 << bit)) > 0;
	}

	public boolean isBitSet(int x, int y, int bit)
	{
		if (x < 0 || y < 0 || x >= width || y >= height) return false;
		return (pixels[x + y * width] & (1 << bit)) > 0;
	}

	public boolean isMovable(Point p)
	{
		return isBitSet(p.x, p.y, BIT_MOVING);
	}

	public boolean isMovable(int x, int y)
	{
		return isBitSet(x, y, BIT_MOVING);
	}

	public boolean isOnTop(int x, int y)
	{
		return isBitSet(x, y, BIT_ONTOP);
	}

	public final int BIT_MOVING = 0;
	public final int BIT_ONTOP = 1;

	// Parts

	public int getPart(int x, int y, int offset, int len)
	{
		if (x < 0 || y < 0 || x >= width || y >= height) return 0;
		return (pixels[x + y * width] >> offset) & (0xFF >> (8 - len));
	}
}
