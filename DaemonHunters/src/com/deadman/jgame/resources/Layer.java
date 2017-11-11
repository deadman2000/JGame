package com.deadman.jgame.resources;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import com.deadman.jgame.drawing.Animation;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.Picture;
import com.deadman.jgame.drawing.TickSource;

public class Layer extends Picture
{
	public String name;

	public ArrayList<Layer> layers;

	@Override
	public String toString()
	{
		return name + " " + anchorX + ":" + anchorY + " " + width + "x" + height;
	}

	public int posX, posY; // Исходные координаты на слое

	/**
	 * Конструктор для группы слоев
	 * @param name
	 */
	public Layer(String name, int x, int y)
	{
		this.name = name;
		posX = x;
		posY = y;
		layers = new ArrayList<Layer>();
	}

	/**
	 * Конструктор для слоя
	 * @param name
	 * @param img
	 * @param anchorX
	 * @param anchorY
	 */
	public Layer(String name, BufferedImage img, int x, int y)
	{
		super(img, -x, -y);

		this.name = name;
		posX = x;
		posY = y;
	}

	public void addLayer(Layer layer)
	{
		layers.add(layer);
	}

	public Layer getLayer(String name)
	{
		for (Layer l : layers)
			if (l.name.equals("name"))
				return l;
		System.err.println("Layer " + name + " not found");
		return null;
	}

	// Анимация

	public Drawable getAnimation()
	{
		ArrayList<Frame> indFrames = new ArrayList<>();
		int ax = 0, ay = 0;
		String anchorName = name + "-a";
		for (Layer l : layers)
		{
			int num = -1, delay = -1;

			if (!l.name.startsWith(name)) continue;

			if (l.name.equals(anchorName))
			{
				ax = l.posX - posX;
				ay = l.posY - posY;
				continue;
			}

			String[] parts = l.name.substring(name.length()).split("-");

			for (int i = 1; i < parts.length; i++)
			{
				String part = parts[i];
				if (part.startsWith("f"))
				{
					try
					{
						num = Integer.parseInt(part.substring(1));
					}
					catch (Exception ex)
					{
						continue;
					}
				}
				else if (part.startsWith("d"))
				{
					try
					{
						delay = Integer.parseInt(part.substring(1));
					}
					catch (Exception ex)
					{
						continue;
					}
				}
			}

			if (num == -1 || delay == -1)
			{
				System.err.println("Wrong animation-frame layer " + l.name);
			}
			else
			{
				indFrames.add(new Frame(num, delay, l));
			}
		}
		Collections.sort(indFrames);

		ArrayList<Drawable> frames = new ArrayList<>();
		ArrayList<Integer> delays = new ArrayList<>();

		int duration = 0;
		for (Frame f : indFrames)
		{
			duration += f.delay;
			frames.add(f.layer);
			delays.add(duration);
		}

		if (delays.size() == 0)
		{
			System.err.println("Wrong animation layer: " + this);
			return null;
		}
		// TODO Tick source
		Animation a = new Animation(frames, delays, TickSource.FRAMES);
		a.anchorX = ax;
		a.anchorY = ay;
		return a;
	}

	static class Frame implements Comparable<Frame>
	{
		public int number;
		public int delay;
		public Layer layer;

		public Frame(int n, int d, Layer l)
		{
			number = n;
			delay = d;
			layer = l;
		}

		@Override
		public int compareTo(Frame o)
		{
			return this.number - o.number;
		}
	}

	@Override
	public void drawTo(Graphics2D g, int x, int y)
	{
		g.drawImage(getImage(), x, y, null);
	}

	public boolean remove(Layer layer)
	{
		if (layers.remove(layer))
			return true;
		
		for (Layer l : layers)
		{
			if (l.layers != null && l.remove(layer))
				return true;
		}
		
		return false;
	}
}
