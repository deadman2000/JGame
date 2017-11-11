package com.deadman.pixelgame;

import java.awt.Point;

public class Moving
{
	private Point[] _path;
	private GameObject _obj;

	public float curr_tick;
	private static final float SPEED = 1f;
	private static final float TICK_LIMIT = 4f;
	private static final int WAY_INCREMENT = 4;
	private int way_curr;

	public Moving(Point[] path, GameObject obj)
	{
		_path = path;
		_obj = obj;
	}

	int _dir = -1;

	public void tick()
	{
		curr_tick += SPEED;

		if (curr_tick < TICK_LIMIT) return;
		
		while (curr_tick >= TICK_LIMIT)
		{
			curr_tick -= TICK_LIMIT;
			way_curr += WAY_INCREMENT;

			if (way_curr >= _path.length)
			{
				way_curr = _path.length - 1;
				break;
			}
		}

		Point p = _path[way_curr];
		_obj.setPosition(p);

		if (way_curr < _path.length - 1)
		{
			int d = getDirection(p, _path[way_curr + 1]);
			if (_dir != d)
			{
				_obj.onMove(d);
				_dir = d;
			}
		}
		else
		{
			Game.log("End path");
			_obj.onStop();
		}
	}

	private int getDirection(Point from, Point to)
	{
		if (_dir == GameCharacter.UP && from.y > to.y) return GameCharacter.UP;
		if (_dir == GameCharacter.RIGHT && from.x < to.x) return GameCharacter.RIGHT; 
		if (_dir == GameCharacter.DOWN && from.y < to.y) return GameCharacter.DOWN;
		if (_dir == GameCharacter.LEFT && from.x > to.x) return GameCharacter.LEFT;

		if (from.x < to.x) return GameCharacter.RIGHT;
		if (from.x > to.x) return GameCharacter.LEFT;
		
		if (from.y > to.y) return GameCharacter.UP;
		if (from.y < to.y) return GameCharacter.DOWN;

		return 1;
	}
	
	public static Moving move(GameObject obj, Point target)
	{
		Game.log("move");
		
		//long time = System.nanoTime();
		Point[] path = JPS.findPath(Game.screen.scene.mask, obj.position(), target);
		//System.out.println("Path build: " + (System.nanoTime() - time));

		if (path == null) return null;
		return new Moving(path, obj);
	}

	public void draw()
	{
		//for (Point p : _path) Game.screen.setPixel(p.x, p.y, (byte) 0);
	}
}
