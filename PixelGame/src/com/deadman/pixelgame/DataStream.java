package com.deadman.pixelgame;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataStream extends FilterInputStream
{
	public DataStream(InputStream in)
	{
		super(in);
	}

	public DataStream(String s)
	{
		super(new ByteArrayInputStream(parseHex(s)));
	}
	
	public final int readShort() throws IOException
	{
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (ch1 << 0) + (ch2 << 8);
	}
	
	static byte[] parseHex(String s)
	{
		//s = s.replace(" ", "");
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			if (i + 1 >= len) break;

			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
