package com.deadman.dh.battle;

import com.deadman.dh.Game;
import com.deadman.dh.isometric.IsoWay;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;

public class MoveAction extends Action
{
	private IsoWay way;
	private MapCell[] cells;

	private int currP = -1;
	private byte dir;
	private int frames, frames2;
	private int counter;
	private float sx, sy; // Смещение в ячейке (без z)
	private float dx, dy; // Сдвиг на tick
	private float zfloat, dz, dz2;
	boolean stopped = false;

	public MoveAction(MissionEngine eng, GameCharacter c, IsoWay way)
	{
		super(eng, c);

		this.way = way;
		cells = way.cells;

		System.out.println("Moving " + c + " : " + cells[0] + " => " + cells[cells.length - 1]);
	}

	@Override
	public boolean isValid()
	{
		return ch.apCount >= way.cost;
	}

	/*
	 *           /\
	 *          /0 \
	 *     Y   /\  /\  X
	 *    /   /7 \/1 \  \
	 *  |_   /\  /\  /\  _|
	 *      /6 \/fr\/2 \ 
	 *      \  /\om/\  /
	 *       \/5 \/3 \/
	 *        \  /\  /
	 *         \/4 \/
	 *          \  /
	 *           \/
	 */
	void setTarget(int ind)
	{
		currP = ind;

		MapCell curr = cells[ind];
		if (ch.cell != curr)
		{
			curr.setUnit(ch);
			checkBlocked();
			if (stopped) return;
		}

		byte zs = curr.getZShift();
		ch.setShift(0, 0, zs);

		if (ind == cells.length - 1)
			return;

		MapCell next = cells[ind + 1];
		dir = ch.rotateTo(next);
		checkBlocked();
		if (stopped)
		{
			ch.useAP(GameCharacter.ROTATE_COST);
			return;
		}

		switch (dir)
		{
			case 0:
				dx = 0;
				dy = -1;
				frames = 16;
				break;
			case 1:
				dx = 1;
				dy = -0.5f;
				frames = 16;
				break;
			case 2:
				dx = 1;
				dy = 0;
				frames = 32;
				break;
			case 3:
				dx = 1;
				dy = 0.5f;
				frames = 16;
				break;
			case 4:
				dx = 0;
				dy = 1;
				frames = 16;
				break;
			case 5:
				dx = -1;
				dy = 0.5f;
				frames = 16;
				break;
			case 6:
				dx = -1;
				dy = 0;
				frames = 32;
				break;
			case 7:
				dx = -1;
				dy = -0.5f;
				frames = 16;
				break;
		}

		frames2 = frames / 2;
		counter = frames;
		sx = 0;
		sy = 0;

		zfloat = zs;
		if (curr.z == next.z)
		{
			dz = (float) (next.getZShift() - curr.getZShift()) / frames;
			dz2 = dz;
		}
		else if (curr.z < next.z) // Подъем
		{
			dz = (float) (MapCell.LEVEL_HEIGHT - curr.getZShift()) / frames2; // Поднимаемся до края ячейки
			dz2 = (float) next.getZShift() / frames2; // Поднимаемся до высоты ячейки
		}
		else // Спуск
		{
			dz = (float) curr.getZShift() / frames2; // Опускаемся до края ячейки
			dz2 = (float) (next.getZShift() - MapCell.LEVEL_HEIGHT) / frames2; // Опускаемся до высоты
		}
	}

	@Override
	public Action tick()
	{
		if (stopped) return null;

		if (currP == -1)
		{
			setTarget(0);
			ch.setState(GameCharacter.STATE_MOVE);
		}

		counter--;
		if (counter <= 0)
		{
			int i = currP + 1;
			setTarget(i);

			if (i >= cells.length - 1)
			{
				stop();
				return null; // Если это последняя точка в пути, то мы пришли
			}
		}
		else
		{
			zfloat += dz;

			if (counter == frames2 - 1) // Перемещение на следующую ячейку
			{
				MapCell next = cells[currP + 1];

				sx = -frames2 * dx;
				sy = -frames2 * dy;

				if (next.z != ch.cell.z)
				{
					dz = dz2;
					if (next.z > ch.cell.z) // Поднимаемся
						zfloat = next.getZShift();
					else // Спускаемся
						zfloat = MapCell.LEVEL_HEIGHT;
				}

				next.setUnit(ch);
				ch.setShift((int) sx, (int) sy, (int) zfloat);
				checkBlocked();
				if (stopped) return null;
			}
			else
			{
				sx += dx;
				sy += dy;
				ch.setShift((int) sx, (int) sy, (int) zfloat);
			}
		}

		return this;
	}

	private boolean wayBlocked()
	{
		for (int i = currP; i < cells.length; i++)
		{
			MapCell c = cells[i];
			if (c.ch == ch)
				continue;

			if (c.ch != null && ch.isVisible(c))
				return true;
		}
		return false;
	}

	private void stop()
	{
		if (stopped) return;
		stopped = true;
		int c = way.costs[currP];
		if (!Game.FREE_MOVINGS) ch.useAP(c);
		ch.setShift(0, 0, ch.cell.getZShift());
		ch.setState(GameCharacter.STATE_STAND);
	}

	private void checkBlocked()
	{
		if (wayBlocked())
			stop();
	}

	@Override
	public String toString()
	{
		return "moving " + ch + " to " + cells[cells.length - 1];
	}
}
