package com.deadman.jgame.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.io.DataInputStreamBE;

public class ResourceManager
{
	public static void init()
	{
		if (_inst == null)
		{
			_inst = new ResourceManager();
			try
			{
				_inst.load();
			}
			catch (IOException ex)
			{
				JOptionPane.showMessageDialog(null, ex.toString());
				ex.printStackTrace();
			}
		}
	}

	private static ResourceManager _inst;

	private ResourceEntry[] entries;

	private void load() throws IOException
	{
		File file = new File("res/resources.csv");
		if (!file.exists()) return;

		ArrayList<ResourceEntry> list = new ArrayList<>();
		FileReader fileReader = new FileReader(file);
		BufferedReader br = new BufferedReader(fileReader);

		String line;
		while ((line = br.readLine()) != null)
		{
			String[] parts = line.split(";");
			int i = Integer.parseInt(parts[0]);

			ResourceEntry entry = ResourceEntry.create(parts);
			if (entry == null) continue;

			list.ensureCapacity(i + 1);
			int newSize = i + 1;
			if (list.size() < newSize)
			{
				list.ensureCapacity(newSize);
				while (list.size() < newSize)
				{
					list.add(null);
				}
			}
			list.set(i, entry);
		}
		br.close();

		entries = list.toArray(new ResourceEntry[] {});
	}

	private ResourceEntry getEntry(int id)
	{
		return entries[id];
	}

	public static ResourceEntry getResource(int id)
	{
		return _inst.getEntry(id);
	}

	public static Drawable[] getParts(int id)
	{
		return ((PicPartEntry) getResource(id)).getArray();
	}

	public static DataInputStreamBE getInputStream(int id)
	{
		return getResource(id).getInputStream();
	}

	public static Object getField(String path)
	{
		return _inst.resolve(path);
	}

	private Object resolve(String path)
	{
		String[] parts = path.split("\\.");

		StringBuilder className = new StringBuilder("com.deadman.dh.");

		for (int i = 0; i < parts.length - 1; i++)
		{
			if (i > 0)
				className.append("$");
			className.append(parts[i]);
		}

		try
		{
			Class<?> cl = Class.forName(className.toString());
			Field f = cl.getField(parts[parts.length - 1]);
			return f.get(null);
		}
		catch (Exception e)
		{
		}
		return null;
	}

}
