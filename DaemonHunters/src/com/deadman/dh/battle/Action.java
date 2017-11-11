package com.deadman.dh.battle;

import com.deadman.dh.model.GameCharacter;

public abstract class Action
{
	public MissionEngine engine;
	public GameCharacter ch;

	public Action(MissionEngine eng, GameCharacter c)
	{
		engine = eng;
		ch = c;
	}

	public Action(GameCharacter c)
	{
		ch = c;
	}

	public Action()
	{
	}
	
	public abstract Action tick();
	
	public abstract boolean isValid();
}
