package com.deadman.dh.battle;

import java.util.ArrayList;

import com.deadman.jgame.ui.Control;

public class MessageAnimation
{
	private ArrayList<Moving> _movings = new ArrayList<>();
	
	public void add(Control control)
	{
		_movings.add(new Moving(control));
	}

	public void tick()
	{
		for (int i = 0; i < _movings.size(); i++)
		{
			Moving m = _movings.get(i);
			if (!m.move())
			{
				m.control.remove();
				_movings.remove(i);
				i--;
			}
		}
	}

	class Moving
	{
		private static final int MOVE_DELAY = 2; // Двигаем контрол раз в X отрисовок
		private static final int MOVE_PERIOD = 30; // Время жизни
		public final Control control;

		private int ticks = 0;

		public Moving(Control c)
		{
			control = c;
		}

		public boolean move()
		{
			ticks++;
			if (ticks >= MOVE_PERIOD) return false;

			if (ticks % MOVE_DELAY == 0)
			{
				control.x++;
				control.y--;
			}
			return true;
		}
	}
}
