package com.deadman.jgame.drawing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JOptionPane;

public class PicPart extends Drawable
{
	public Drawable original;
	private int offsetX, offsetY;

	public PicPart(Drawable pic, int offsetX, int offsetY, int width, int height)
	{
		this.original = pic;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
		this.anchorX = 0;
		this.anchorY = 0;
	}

	public PicPart(Drawable pic, int offsetX, int offsetY, int width, int height, int anchorX, int anchorY)
	{
		this.original = pic;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
		this.anchorX = anchorX;
		this.anchorY = anchorY;
	}

	@Override
	protected void draw(int x, int y)
	{
		original.drawAt(x, y, offsetX, offsetY, width, height);
	}

	@Override
	protected void drawMH(int x, int y)
	{
		//original.drawAt(x, y, width, height, original.width - width - offsetX, offsetY, -width, height);
		original.drawAt(x, y, width, height, -original.width + width + offsetX, offsetY, -width, height);
	}

	@Override
	public String toString()
	{
		return "Part (" + offsetX + ":" + offsetY + " " + width + "x" + height + " A:" + anchorX + ":" + anchorY + ") of " + original;
	}

	@Override
	protected void draw(int x, int y, int w, int h)
	{
		original.drawAt(x, y, w, h, offsetX, offsetY, width, height);
	}

	@Override
	protected void fill(int x, int y, int w, int h)
	{
		original.fillAt(x, y, w, h, offsetX, offsetY, width, height);
	}

	@Override
	protected void draw(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		original.drawAt(tx, ty, tw, th, offsetX + fx, offsetY + fy, w, h);
	}

	@Override
	protected void fill(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		original.fillAt(tx, ty, tw, th, offsetX + fx, offsetY + fy, w, h);
	}

	@Override
	protected boolean contains(int x, int y)
	{
		return original.containsAt(offsetX + x, offsetY + y);
	}

	public static PicPart[] load(String fileName)
	{
		return load(fileName, null);
	}

	public static PicPart[] load(String fileName, Hashtable<String, String> attr)
	{
		File ppr = new File(fileName);
		if (!ppr.exists()) return null;

		int dotIndex = fileName.lastIndexOf(".");
		String filePart = fileName.substring(0, dotIndex);
		Drawable pic = Picture.load(filePart + ".png");

		ArrayList<PicPart> list = new ArrayList<>();
		try
		{
			FileReader fileReader = new FileReader(ppr);
			BufferedReader br = new BufferedReader(fileReader);

			String line;
			while ((line = br.readLine()) != null)
			{
				String[] parts = line.split(";");
				if (parts.length == 6)
				{
					PicPart pp = new PicPart(pic, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
					list.add(pp);
				}
				else if (attr != null)
				{
					parts = line.split("=", 2);
					if (parts.length == 2)
					{
						attr.put(parts[0], parts[1]);
					}
				}
			}
			br.close();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, ex.toString());
			ex.printStackTrace();
			return null;
		}

		return list.toArray(new PicPart[list.size()]);
	}
}
