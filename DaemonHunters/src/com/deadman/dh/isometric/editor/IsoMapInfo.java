package com.deadman.dh.isometric.editor;

import java.io.File;
import java.io.IOException;

import com.deadman.jgame.io.DataInputStreamBE;
import com.deadman.jgame.resources.ResourceManager;

public class IsoMapInfo
{
	public final int width;
	public final int height;
	public final int zheight;
	public final int zerolevel;

	public IsoMapInfo(int w, int h, int l, int zl)
	{
		width = w;
		height = h;
		zheight = l;
		zerolevel = zl;
	}

	public static IsoMapInfo load(String fileName)
	{
		System.out.println("Load map info " + fileName);

		File f = new File(fileName);
		if (!f.exists()) return null;

		try
		{
			return loadMap(DataInputStreamBE.readZip(fileName));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static IsoMapInfo loadMap(int id)
	{
		return loadMap(ResourceManager.getResource(id)
				.getGZip());
	}

	public static IsoMapInfo loadMap(DataInputStreamBE in)
	{
		try
		{
			int h = in.readShort() & 0xFFFF;
			int w = in.readShort() & 0xFFFF;
			int l = in.readShort() & 0xFFFF;
			int zl = in.readShort() & 0xFFFF;

			IsoMapInfo map = new IsoMapInfo(w, h, l, zl);
			in.close();

			return map;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

}
