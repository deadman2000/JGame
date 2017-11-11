package com.deadman.jgame.resources;

import java.io.IOException;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.io.DataInputStreamBE;

public class ResourceEntry
{
	public final String path;

	public ResourceEntry(String[] csv)
	{
		path = csv[2];
	}

	public int getType()
	{
		return FILE;
	}

	public Drawable getDrawable()
	{
		System.err.println("Is not picture " + this);
		return null;
	}
	
	public DataInputStreamBE getGZip()
	{
		try
		{
			return DataInputStreamBE.readZip(path);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public DataInputStreamBE getInputStream()
	{
		try
		{
			return new DataInputStreamBE(path);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString()
	{
		return path;
	}

	public static ResourceEntry create(String[] parts)
	{
		int t = Integer.parseInt(parts[1]);
		switch (t)
		{
			case PICTURE:
				return new PictureEntry(parts);
			case PICPARTS:
				return new PicPartEntry(parts);
			case FONT:
				return new FontEntry(parts);
			case FILE:
				return new ResourceEntry(parts);
			default:
				System.err.println("Entry " + t + " is not implemented");
				return null;
		}
	}

	public static final int PICTURE = 0;
	public static final int PICPARTS = 1;
	public static final int FONT = 2;
	public static final int FILE = 3;
}
