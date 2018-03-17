package com.deadman.dh.model.items;

import com.deadman.dh.Game;
import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.model.Element;
import com.deadman.dh.model.GameCharacter;

public class VampireEffect extends ItemEffect
{
	public int value;
	public int amp;

	@Override
	public boolean activate(GameCharacter owner, IsoObject target)
	{
		if (target instanceof GameCharacter)
		{
			// TODO Проверить на нежить и механизмы
			int dmg = target.hitDamage(owner, Element.PHYSICAL, value + Game.rnd.nextInt(amp + 1));
			owner.heal(dmg);
			return true;
		}
		else
			return false;
	}
}
