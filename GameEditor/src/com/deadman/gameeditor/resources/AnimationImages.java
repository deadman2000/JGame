package com.deadman.gameeditor.resources;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Класс анимации на изображениях
 */
public class AnimationImages extends AnimatedDrawable {
	private ArrayList<Frame> _frames; // Кадры
	private int _duration = 0; // Общая продолжительность

	public AnimationImages(ArrayList<Frame> frames) {
		_frames = frames;

		_duration = 0;
		for (Frame f : frames)
		{
			_duration += f.delay;
			f.totalDelay = _duration;
		}
	}

	@Override
	public String getLabel() {
		return getCurrentFrameDrawable().getLabel();
	}

	// Tick

	private int _currentFrame;
	private int _ticks;

	public void tick() {
		_ticks++;
		_currentFrame = calcCurrentFrame();
	}

	private int calcCurrentFrame() {
		int n = _ticks % _duration;

		for (int i = 0; i < _frames.size(); i++)
			if (_frames.get(i).totalDelay > n)
				return i;

		return 0;
	}

	public void setCurrentFrame(int value) {
		_currentFrame = value;
		_ticks = _frames.get(value).totalDelay;
	}

	public int getFramesCount() {
		return _frames.size();
	}

	@Override
	public int getCurrentFrame() {
		return _currentFrame;
	}

	private Drawable getCurrentFrameDrawable() {
		return _frames.get(_currentFrame).picture;
	}

	// Drawable

	@Override
	public Rectangle getBounds() {
		Drawable pic = getCurrentFrameDrawable();
		return pic.getBounds();
	}
}
