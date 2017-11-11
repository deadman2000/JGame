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
	public void activate(GameCharacter owner, IsoObject target)
	{
		int dmg = target.hitDamage(owner, Element.PHYSICAL, value + Game.rnd.nextInt(amp + 1));
		owner.heal(dmg);
	}
}
