package com.deadman.jgame.drawing;

import java.util.ArrayList;

public class Animation extends Drawable
{
	public TickSource tickSource;

	public Drawable[] frames;
	public int[] delays; // Время перехода на кадр. От начала отсчета

	public int duration;
	private int animOffset;

	public Animation(Drawable[] frames, int[] delays, TickSource tickSource)
	{
		this.frames = frames;
		this.delays = delays;
		this.duration = delays[delays.length - 1];
		this.tickSource = tickSource;
		width = frames[0].width;
		height = frames[0].height;
	}

	public Animation(ArrayList<Drawable> frames, ArrayList<Integer> delays, TickSource tickSource)
	{
		this(frames.toArray(new Drawable[frames.size()]), toIntArray(delays), tickSource);
	}

	// TODO Переделать. вынести в базовый класс
	public Animation getShifted(int offset)
	{
		Animation copy = new Animation(frames, delays, tickSource);
		copy.width = width;
		copy.height = height;
		copy.anchorX = anchorX;
		copy.anchorY = anchorY;
		copy.animOffset = offset;
		return copy;
	}

	// Drawing

	private Drawable getCurrentFrame()
	{
		int n = (int) ((tickSource.getTicks() + animOffset) % duration);

		for (int i = 0; i < delays.length; i++)
			if (delays[i] > n)
				return frames[i];

		return frames[0];
	}

	@Override
	protected void draw(int x, int y)
	{
		getCurrentFrame().drawAt(x, y);
	}

	@Override
	protected void draw(int x, int y, int w, int h)
	{
		Drawable f = getCurrentFrame();
		f.drawAt(x, y, Math.min(f.width, w), Math.min(f.height, h));
	}

	@Override
	protected void drawMH(int x, int y)
	{
		Drawable f = getCurrentFrame();
		f.drawMHAt(x + width - f.width, y); // Компенсация для Mirrored
	}

	@Override
	protected void fill(int x, int y, int w, int h)
	{
		Drawable f = getCurrentFrame();
		f.fillAt(x, y, Math.min(f.width, w), Math.min(f.height, h));
	}

	@Override
	protected void draw(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		getCurrentFrame().drawAt(tx, ty, tw, th, fx, fy, w, h);
	}

	@Override
	protected void fill(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		getCurrentFrame().fillAt(tx, ty, tw, th, fx, fy, w, h);
	}

	protected boolean contains(int x, int y)
	{
		return getCurrentFrame().containsAt(x, y);
	}

	// Loading

	static int[] toIntArray(ArrayList<Integer> integerList)
	{
		int[] intArray = new int[integerList.size()];
		for (int i = 0; i < integerList.size(); i++)
			intArray[i] = integerList.get(i);
		return intArray;
	}

	@Override
	public Drawable subpic(int x, int y, int width, int height)
	{
		Drawable[] subFrames = new Drawable[frames.length];
		for (int i = 0; i < frames.length; i++)
			subFrames[i] = frames[i].subpic(x, y, width, height);

		Animation sub = new Animation(subFrames, delays, tickSource);
		return sub;
	}

	@Override
	public Drawable getMirrored(int shift)
	{
		Drawable[] f = new Drawable[frames.length];
		for (int i = 0; i < frames.length; i++)
			f[i] = new Mirrored(frames[i], shift);
		Animation copy = new Animation(f, delays, tickSource);
		copy.width = width;
		copy.height = height;
		copy.anchorX = anchorX;
		copy.anchorY = anchorY;
		copy.animOffset = animOffset;
		return copy;
	}

	@Override
	public Drawable replaceColors(int[] from, int[] to)
	{
		Drawable[] subFrames = new Drawable[frames.length];
		for (int i = 0; i < frames.length; i++)
			subFrames[i] = frames[i].replaceColors(from, to);

		Animation copy = new Animation(subFrames, delays, tickSource);
		copy.width = width;
		copy.height = height;
		copy.anchorX = anchorX;
		copy.anchorY = anchorY;
		copy.animOffset = animOffset;		
		return copy;
	}
}
