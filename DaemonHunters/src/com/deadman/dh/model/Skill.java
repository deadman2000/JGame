package com.deadman.dh.model;

/**
 * Скилл конкретного юнита
 * @author ivamar
 *
 */
public class Skill
{
	private final Unit unit;
	private final SkillType type;
	public int value; // Уровень прокачки в % (100 на каждый уровень)

	public Skill(Unit owner, SkillType type)
	{
		unit = owner;
		this.type = type;
	}

	/**
	 * Прокачка скилла в тренировочной комнате
	 * @param dt Время
	 * @param square Площадь комнаты
	 */
	public void learn(int dt, int square)
	{
		int lvl100 = unit.lvl * 100;
		if (value > lvl100) return;

		double ds = dt * ((unit.str * type.fs + unit.dex * type.fd + unit.intl * type.fi + square / 100.) / 10000.);
		if (ds > lvl100 - value)
			ds = lvl100 - value;

		int s = (int) (value / 100);
		value += ds;

		if ((int) (value / 100) != s)
		{
			System.out.println(unit.name + " " + type.name + " => " + (int) value / 100);
		}
	}
}
