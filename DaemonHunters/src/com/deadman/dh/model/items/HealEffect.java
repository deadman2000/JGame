package com.deadman.dh.model.items;

import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.model.GameCharacter;

public class HealEffect extends ItemEffect
{
	private int val;

	public HealEffect(int value)
	{
		val = value;
	}

	@Override
	public boolean canActivate()
	{
		return true;
	}

	@Override
	public boolean activate(GameCharacter owner, IsoObject target)
	{
		if (target instanceof GameCharacter)
		{
			GameCharacter ch = (GameCharacter) target;
			if (ch.hpCount >= ch.hpMax) return false;
			ch.heal(val);
			return true;
		}
		else
			return false;
	}
}
