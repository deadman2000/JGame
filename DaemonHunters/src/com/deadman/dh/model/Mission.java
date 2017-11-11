package com.deadman.dh.model;

import com.deadman.dh.Game;

public class Mission extends MapObject
{
	public Poi point;
	public int level; // Уровень миссии
	public MissionScenario scenario;
	public int endTime; // Время окончания действия в секундах глобального времени

	public Mission(Poi p, int lvl, MissionScenario sc, int end)
	{
		point = p;
		x = p.x;
		y = p.y;
		level = lvl;
		scenario = sc;
		endTime = end;
	}

	@Override
	public String getName()
	{
		return this.toString();
	}

	public void onUncompleted()
	{
		System.out.println("Uncompleted " + this);
		Game.dark_points += level * 10;
		// TODO Может быть отправлять уведомление?
	}
}
