package com.deadman.dh.isometric;

import java.util.Hashtable;

import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.drawing.Drawable;

import org.w3c.dom.Node;

public class IsoBigSprite extends IsoSprite
{
	public byte width, height; // Размеры

	public Hashtable<Byte, Drawable[]> subPics; // Нарезанные картинки

	public IsoBigSprite() // Конструктор для тестов
	{
	}

	public IsoBigSprite(Node node)
	{
		super(node);

		width = (byte) GameResources.getInt(node, "width", 1);
		height = (byte) GameResources.getInt(node, "height", 1);
		createSubPics();
	}

	public void createSubPics()
	{
		subPics = new Hashtable<>();

		for (Byte s : pics.keySet())
		{
			Drawable full = pics.get(s);
			Drawable[] parts = new Drawable[width * height];

			int r = (s >> 4) & 0x0F;
			if (r % 2 == 0)
			{
				for (byte x = 0; x < width; x++)
					for (byte y = 0; y < height; y++)
						parts[x + y * width] = getSubPic(full, x, y);
			}
			else // Для повернутых меняем местами x и y
			{
				for (byte x = 0; x < width; x++)
					for (byte y = 0; y < height; y++)
						parts[x + y * width] = getSubPic(full, y, x);
			}

			subPics.put(s, parts);
		}
	}

	@Override
	public void setTo(MapCell cell, byte state)
	{
		MapCell[][] level = cell.map.cells[cell.z];

		IsoSimpleObject obj = new IsoSimpleObject(this, state); // Оригинал
		obj.cell = level[cell.x][cell.y];

		// Расставляем части для отрисовки
		int rotation = (state >> 4) & 0x0F;
		if (rotation % 2 == 0)
		{
			for (byte x = 0; x < width; x++)
				for (byte y = 0; y < height; y++)
				{
					IsoObjectPart part = new IsoObjectPart(obj, x, y);
					level[cell.x - x][cell.y - y].setObject(part, type);
				}
		}
		else
		{
			for (byte x = 0; x < width; x++)
				for (byte y = 0; y < height; y++)
				{
					IsoObjectPart part = new IsoObjectPart(obj, x, y);
					level[cell.x - y][cell.y - x].setObject(part, type);
				}
		}
	}

	public Drawable getPart(byte x, byte y, byte state)
	{
		Drawable[] pics = subPics.get(state);
		if (pics != null) return pics[x + y * width];
		return null;
	}

	// TODO Делить на куски заранее
	private Drawable getSubPic(Drawable picture, byte x, byte y)
	{
		// TODO переделать. Сейчас сделано под 2х2
		if (x == 0 && y == 0)
			return picture.subpic(2, -14, 32, 66);

		if (x == 1 && y == 0)
		{
			Drawable dr = picture.subpic(-14, -14, 16, 58);
			if (dr != null)
			{
				dr.anchorX += -16; // Смещаем кусок под свою ячейку
				dr.anchorY += -8;
			}
			return dr;
		}

		if (x == 0 && y == 1)
		{
			Drawable dr = picture.subpic(34, -14, 16, 58);
			if (dr != null)
			{
				dr.anchorX += 16; // Смещаем кусок под свою ячейку
				dr.anchorY += -8;
			}
			return dr;
		}

		return null;
	}

	@Override
	public IsoSprite replaceColors(int[] from, int[] to)
	{
		System.err.println("replaceColors not implemented for IsoBigSprite");
		return null;
	}
}
