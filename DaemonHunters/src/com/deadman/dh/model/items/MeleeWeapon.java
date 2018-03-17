package com.deadman.dh.model.items;

import com.deadman.dh.Game;
import com.deadman.dh.battle.MissionEngine;
import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.itemtypes.MeleeWeaponType;

public class MeleeWeapon extends Weapon
{
	private MeleeWeaponType wtype;
	
	public MeleeWeapon(MeleeWeaponType type)
	{
		super(type);
		wtype = type;
	}
	
	@Override
	public Item cloneOne()
	{
		return new MeleeWeapon(wtype);
	}

	@Override
	public boolean isMeele()
	{
		return true;
	}

	@Override
	public void hit(MissionEngine engine, GameCharacter unit, MapCell to)
	{
		IsoObject target = to.ch != null ? to.ch : to.obj; // TODO Ломать стены
		if (target == null) return;

		target.hitDamage(unit, wtype.element, wtype.value + Game.rnd.nextInt(wtype.amp + 1));
	}

	@Override
	public void appendDescription(StringBuilder sb)
	{
		sb.append(wtype.element.damageName())
				.append(": ")
				.append(wtype.value)
				.append(" ... ")
				.append(wtype.amp)
				.append("\n");
	}

	@Override
	public boolean canAttack(GameCharacter attack, MapCell c)
	{
		if (attack.cell.isNear(c))
			return true;
		if (attack.nearCell != null)
		{
			if (attack.nearCell.isNear(c))
				return true;
		}
		return false;
	}
}
