package com.deadman.jgame.io;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

public class DataInputStreamBE extends FilterInputStream
{
	private FileChannel fchannel;
	private long mark = -1;

	public DataInputStreamBE(InputStream in)
	{
		super(in);
	}

	public DataInputStreamBE(FileInputStream in)
	{
		super(in);
		fchannel = in.getChannel();
	}

	public DataInputStreamBE(String fileName) throws FileNotFoundException
	{
		this(new FileInputStream(fileName));
	}

	public static DataInputStreamBE readZip(String path) throws IOException
	{
		BufferedInputStream buff = new BufferedInputStream(new FileInputStream(path));

		try
		{
			return new DataInputStreamBE(new GZIPInputStream(buff));
		}
		catch (Exception e)
		{
			return new DataInputStreamBE(buff);
		}
	}

	public final byte readByte() throws IOException
	{
		int b = in.read();
		if (b < 0)
			throw new EOFException();
		return (byte) b;
	}

	public final int readShort() throws IOException
	{
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return ch1 | (ch2 << 8);
	}

	public final int readInt() throws IOException
	{
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ch1 | (ch2 << 8) | (ch3 << 16) | (ch4 << 24);
	}

	public final int readIntPack() throws IOException
	{
		int val = 0;
		int bits = 0;
		int b;
		while (true)
		{
			b = readByte();
			val |= (b & 0x7f) << bits;
			bits += 7;
			if ((b & 0x80) == 0) return val;
		}
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

	public static final Charset defaultCharset = Charset.forName("UTF-8");
	public Charset charset = defaultCharset;

	public String readString(int length) throws IOException
	{
		byte[] bytes = new byte[length];
		int i = read(bytes);
		if (i != length)
		{
			System.err.println(i + " != " + length);
		}

		return new String(bytes, charset);
	}

	@Override
	public boolean markSupported()
	{
		if (fchannel == null)
			return super.markSupported();
		return fchannel != null;
	}

	@Override
	public synchronized void mark(int readlimit)
	{
		if (fchannel == null)
			super.mark(readlimit);
		else
		{
			try
			{
				mark = fchannel.position();
			}
			catch (IOException ex)
			{
				mark = -1;
			}
		}
	}

	@Override
	public synchronized void reset() throws IOException
	{
		if (fchannel == null)
			super.reset();
		else
		{
			if (mark == -1) { throw new IOException("not marked"); }
			fchannel.position(mark);
		}
	}
}
