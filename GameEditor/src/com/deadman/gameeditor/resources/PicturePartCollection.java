package com.deadman.gameeditor.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

public class PicturePartCollection
{
	public Hashtable<String, String> attributes = new Hashtable<>();
	public ArrayList<PicturePart> parts = new ArrayList<>();
	public String fileName;
	public String imageFileName;

	public String getCSV()
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < parts.size(); i++)
		{
			PicturePart part = parts.get(i);
			sb.append(part.x);
			sb.append(';');
			sb.append(part.y);
			sb.append(';');
			sb.append(part.width);
			sb.append(';');
			sb.append(part.height);
			sb.append(';');
			sb.append(part.aX);
			sb.append(';');
			sb.append(part.aY);
			if (i < parts.size() - 1)
				sb.append("\r\n");
		}

		for (Entry<String, String> e : attributes.entrySet())
		{
			sb.append("\r\n");
			sb.append(e.getKey());
			sb.append("=");
			sb.append(e.getValue());
		}

		return sb.toString();
	}

	public void save()
	{
		if (fileName != null)
		{
			try
			{
				PrintWriter out = new PrintWriter(fileName);
				String csv = getCSV();
				out.print(csv);
				out.close();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static PicturePartCollection loadCSV(String txt)
	{
		PicturePartCollection collection = new PicturePartCollection();

		String[] lines = txt.split("\r\n");
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i];
			String[] parts = line.split(";");
			if (parts.length == 6)
			{
				collection.parts.add(new PicturePart(
						Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
						Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
						Integer.parseInt(parts[4]), Integer.parseInt(parts[5])));
			}
			else
			{
				parts = line.split("=", 2);
				if (parts.length == 2)
					collection.attributes.put(parts[0], parts[1]);
			}
		}

		return collection;
	}

	public static PicturePartCollection loadFile(String fileName)
	{
		String txt;
		try
		{
			txt = new String(Files.readAllBytes(new File(fileName).toPath()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		PicturePartCollection pp = PicturePartCollection.loadCSV(txt);
		pp.fileName = fileName;
		int dot = fileName.lastIndexOf(".");
		pp.imageFileName = fileName.substring(0, dot + 1) + "png";

		return pp;
	}

	private Image image;
	private Image originalImg;

	private Image getOriginalImage()
	{
		if (originalImg != null)
			return originalImg;

		if (fileName == null || fileName.isEmpty())
			return null;

		try
		{
			return originalImg = loadImage(imageFileName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Image getImage()
	{
		if (image != null)
			return image;
		return getOriginalImage();
	}

	private Image loadImage(String fileName) throws IOException
	{
		File f = new File(fileName);
		if (!f.exists() || f.isDirectory()) return null;

		FileInputStream stream = new FileInputStream(f);
		try
		{
			Display display = Display.getCurrent();
			ImageData data = new ImageData(stream);
			if (data.transparentPixel > 0) { return new Image(display, data, data.getTransparencyMask()); }
			return new Image(display, data);
		}
		finally
		{
			stream.close();
		}
	}
}
