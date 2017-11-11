package com.deadman.jgame.io;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class DataOutputStream extends FilterOutputStream
{
	public DataOutputStream(OutputStream out)
	{
		super(out);
	}

	public DataOutputStream(String fileName) throws FileNotFoundException
	{
		super(new FileOutputStream(fileName));
	}

	public static DataOutputStream createZip(String fileName) throws FileNotFoundException, IOException
	{
		return new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(fileName))));
	}

	public void writeByte(byte val) throws IOException
	{
		write(val);
	}

	public void writeShort(int val) throws IOException
	{
		write(val & 0xFF);
		write((val >> 8) & 0xFF);
	}

	public void writeInt(int val) throws IOException
	{
		write(val & 0xFF);
		write((val >> 8) & 0xFF);
		write((val >> 16) & 0xFF);
		write((val >> 24) & 0xFF);
	}

	public void writeIntPack(int val) throws IOException
	{
		do
		{
			int b = val & 0x7f;
			if (val > 0x7f) b |= 0x80;
			write(b);
			val >>>= 7;
		} while (val > 0);
	}
}
