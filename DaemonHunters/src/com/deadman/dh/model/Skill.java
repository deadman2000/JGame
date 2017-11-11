package com.deadman.dh.model;

public class Skill
{
	private final Unit unit;
	private final double _fs, _fd, _fi;
	public final String name;
	public int value;

	public Skill(String name, Unit owner, double fs, double fd, double fi)
	{
		this.name = name;
		unit = owner;
		_fs = fs;
		_fd = fd;
		_fi = fi;
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

		double ds = dt * ((unit.str * _fs + unit.dex * _fd + unit.intl * _fi + square / 100.) / 10000.);
		if (ds > lvl100 - value)
			ds = lvl100 - value;

		int s = (int) (value / 100);
		value += ds;

		if ((int) (value / 100) != s)
		{
			System.out.println(unit.name + " " + name + " => " + (int) value / 100);
		}
	}
}
