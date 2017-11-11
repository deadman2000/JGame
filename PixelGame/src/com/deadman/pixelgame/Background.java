package com.deadman.pixelgame;

import java.io.IOException;
import java.io.InputStream;

public class Background extends Matrix
{
	public Palette pal;

	public static Background loadStream(InputStream stream) throws IOException
	{
		DataStream in = new DataStream(stream);
		try
		{
			Background b = new Background();
			b.width = in.readShort();
			b.height = in.readShort();
			b.pixels = new byte[b.width * b.height];
			in.read(b.pixels);
			return b;
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
}
