package com.deadman.dh.battle;

import com.deadman.dh.isometric.IsoWay;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;

/**
 * Класс для поиска врага
 */
public class EnemySearch
{
	private static final int ST_LOOK = 0; // Первый шаг - крутимся 3 раза
	private static final int ST_TARGET = 1; // Второй шаг - идем до цели
	private static final int ST_LOOK2 = 2; // Третий шаг - крутимся 3 раза

	private final MissionEngine eng;
	private final GameCharacter ch;
	private MapCell target;
	private int step = ST_LOOK;
	private int rotation = 0;

	public boolean invalid = false; // Поиск невозможен

	public EnemySearch(MissionEngine eng, GameCharacter ch, MapCell target)
	{
		this.eng = eng;
		this.ch = ch;
		this.target = target;
	}

	public Action getAction()
	{
		switch (step)
		{
			case ST_LOOK:
				rotation++;
				if (rotation >= 3) next();
				return nextRotation();

			case ST_TARGET:
				if (ch.cell == target) // Пришли
				{
					next();
					return getAction();
				}

				IsoWay way = ch.trace(target);
				if (way == null)
				{
					// Нет пути. Возможно там кто-то уже стоит
					// TODO Проверить на расстояние до цели. Если небольшое, перейти к следующему шагу
					System.err.println("No way to target");
					invalid = true;
					return null;
				}

				MoveAction a = new MoveAction(eng, ch, way);
				if (a.isValid())
					return a;
				return null;

			case ST_LOOK2:
				rotation++;
				if (rotation >= 3) next();
				return nextRotation();

			default:
				return null;
		}
	}

	private void next()
	{
		step++;
		rotation = 0;
		System.out.println("AI> Search step " + step);
	}

	private RotateAction nextRotation()
	{
		return new RotateAction(ch, (byte) ((ch.getRotation() + 2) % 8), false);
	}
}
