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
	public void activate(GameCharacter owner, IsoObject target)
	{
		GameCharacter ch = (GameCharacter)target;
		ch.heal(val);
	}
}
