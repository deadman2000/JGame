package com.deadman.dh.model.items;

import com.deadman.dh.Game;
import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.model.Element;
import com.deadman.dh.model.GameCharacter;

public class MagicalDamage extends ItemEffect
{
	public Element element;
	public int value;
	public int amp;

	public MagicalDamage(int level)
	{
		// TODO Generate
		element = Element.FIRE;
		value = 5;
		amp = 3;
	}
	
	@Override
	public boolean canActivate()
	{
		return true;
	}

	@Override
	public void activate(GameCharacter owner, IsoObject target)
	{
		target.hitDamage(owner, element, value + Game.rnd.nextInt(amp + 1));
	}

	@Override
	public void appendDescription(StringBuilder sb)
	{
		super.appendDescription(sb);
		sb.append(element.damageName())
				.append(": ")
				.append(value)
				.append(" ... ")
				.append(amp)
				.append("\n");
	}
}
