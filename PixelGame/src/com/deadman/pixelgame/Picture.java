package com.deadman.pixelgame;

import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.IOException;

public class Picture extends DrawableObject
{
	public int width;
	public int height;

	public int anchorX;
	public int anchorY;

	public byte[] rle;

	public Palette pal;

	public static Picture loadRLE(String fileName)
	{
		DataStream in = null;
		try
		{
			in = new DataStream(new FileInputStream(fileName));
			return loadRLE(in);
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

		return null;
	}

	public static Picture loadRLE(DataStream in) throws IOException
	{
		return loadRLE(in, 0);
	}

	public static Picture loadRLE(DataStream in, int size) throws IOException
	{
		if (size == 0) size = in.available();

		Picture pic = new Picture();
		pic.width = in.readShort();
		pic.height = in.readShort();
		pic.anchorX = in.readShort();
		pic.anchorY = in.readShort();
		pic.rle = new byte[size - 8];
		in.read(pic.rle);
		return pic;
	}

	//public boolean onTop = false;

	public void drawAt(int x, int y)
	{
		int p = 0;
		for (int iy = 0; iy < height; iy++)
		{
			int ix = 0;
			while (ix < width)
			{
				int c = rle[p++] & 0xFF;
				if (c == 0) // Skip
				{
					int cnt = rle[p++];
					ix += cnt;
				}
				else
					for (int i = 0; i < c; i++)
					{
						Game.screen.setPixel(x, y, ix, iy, anchorX, anchorY, (byte) (rle[p] + pal.offset));
						p++;
						ix++;
					}
			}
		}
	}

	@Override
	public boolean contains(int x, int y)
	{
		x += anchorX;
		y += anchorY;
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	@Override
	public Rectangle getBounds()
	{
		return new Rectangle(-anchorX, -anchorY, width, height);
	}

}
