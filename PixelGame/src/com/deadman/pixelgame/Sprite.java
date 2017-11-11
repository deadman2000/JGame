package com.deadman.pixelgame;

import java.io.FileInputStream;
import java.io.IOException;

public class Sprite
{
	public Palette pal;
	public Picture[] frames;
	public Animation[] animations;

	public static Sprite load(String fileName)
	{
		DataStream in = null;
		try
		{
			in = new DataStream(new FileInputStream(fileName));

			Sprite spr = new Sprite();
			int frmsCount = in.readShort();
			spr.frames = new Picture[frmsCount];

			for (int i = 0; i < frmsCount; i++)
			{
				int size = in.readShort();
				spr.frames[i] = Picture.loadRLE(in, size);
			}

			return spr;
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
}
