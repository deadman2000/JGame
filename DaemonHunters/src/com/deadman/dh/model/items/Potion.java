package com.deadman.dh.model.items;

import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.itemtypes.PotionType;

public class Potion extends Item
{
	public Potion(PotionType type)
	{
		super(type);
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
			GameCharacter ch = (GameCharacter)target;
			if (ch.hpCount == ch.hpMax) return false;
			
			super.activate(owner, target); // Используем эффекты
			count = 0; // Потребляем
			return true;
		}
		return false;
	}
}
