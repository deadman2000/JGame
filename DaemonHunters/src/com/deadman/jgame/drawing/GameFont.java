package com.deadman.jgame.drawing;

import java.awt.image.BufferedImage;

import com.deadman.jgame.resources.FontEntry;
import com.deadman.jgame.resources.ResourceManager;

public class GameFont
{
	public int height;
	public int width;
	public Picture[] frames;
	private Picture noChar;

	public String name;

	public GameFont(String name, Picture[] frms)
	{
		this.name = name;
		frames = frms;

		noChar = getCharPic('?');
		if (noChar == null)
		{
			for (int i = 0; i < frms.length; i++)
			{
				if (frms[i] != null)
				{
					noChar = frms[i];
					break;
				}
			}
		}

		Picture p = getCharPic('A');
		if (p == null)
			p = getCharPic('0');

		if (p != null)
		{
			height = p.height + p.anchorY;
			width = p.width + p.anchorX;

			if (frames[' '] == null)
			{
				BufferedImage img = new BufferedImage(p.width, 1, BufferedImage.TYPE_INT_ARGB);
				frames[' '] = new Picture(img);
			}
		}
		else
			System.err.println("No base symbol in font");
	}

	public GameFont outline(int color)
	{
		for (Picture p : frames)
			if (p != null)
				p.outline(color);
		return this;
	}

	public GameFont shadow(int color)
	{
		for (Picture p : frames)
			if (p != null)
				p.shadow(color);
		return this;
	}

	public static boolean WARN_NO_CHAR = true;

	public Picture getCharPic(char ch)
	{
		Picture p;
		if (ch >= frames.length || (p = frames[ch]) == null)
		{
			if (WARN_NO_CHAR) 
				System.err.println(name + " no pic for char " + ch);
			return noChar;
		}
		return p;
	}

	public boolean hasCharPic(char ch)
	{
		return ch < frames.length && frames[ch] != null;
	}

	public void drawAt(int x, int y, String text)
	{
		for (int i = 0; i < text.length(); i++)
		{
			char ch = text.charAt(i);
			if (ch == ' ')
				x += width;
			else
			{
				Picture p = getCharPic(ch);
				p.drawAt(x, y);
				x += p.width + 1;
			}
		}
	}

	public int getTextWidth(String text)
	{
		int w = 0;
		for (int i = 0; i < text.length(); i++)
		{
			Picture p = getCharPic(text.charAt(i));
			w += p.width + 1;
		}
		return w - 1;
	}

	public static GameFont get(int id)
	{
		return ((FontEntry) ResourceManager.getResource(id)).getFont();
	}

	public static GameFont get(int id, int[] pal)
	{
		return ((FontEntry) ResourceManager.getResource(id)).getFont(pal);
	}
}
