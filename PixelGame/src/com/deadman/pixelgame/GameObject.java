package com.deadman.pixelgame;

import java.awt.Point;
import java.awt.Rectangle;

public class GameObject implements IDrawable
{
	public GameObject()
	{
		_position = new Point();
	}

	public GameObject(DrawableObject drw, int px, int py)
	{
		drawable = drw;
		_position = new Point(px, py);
	}

	public DrawableObject drawable;
	protected Point _position;

	public void setPosition(int px, int py)
	{
		_position.x = px;
		_position.y = py;
	}

	public void setPosition(Point p)
	{
		_position = p;
	}

	public Point position()
	{
		return _position;
	}

	public void draw()
	{
		if (_moving != null) _moving.tick();
		drawable.drawAt(_position.x, _position.y);
		//if (_moving != null) _moving.draw();
	}

	private Moving _moving;

	public void moveTo(Point target)
	{
		//stop();
		float startTick = 0;
		if (_moving != null)
			startTick = _moving.curr_tick;
		
		if (_position.equals(target)) return;
		_moving = Moving.move(this, target);
		_moving.curr_tick = startTick;
	}

	public void onMove(int direction)
	{
	}

	public void onStop()
	{
		_moving = null;
	}

	public void stop()
	{
		_moving = null;
		onStop();
	}

	@Override
	public boolean contains(Point p)
	{
		return drawable.contains(p.x - _position.x, p.y - _position.y);
	}

	public Rectangle getBounds()
	{
		Rectangle r = drawable.getBounds();
		r.translate(_position.x, _position.y);
		return r;
	}
}
