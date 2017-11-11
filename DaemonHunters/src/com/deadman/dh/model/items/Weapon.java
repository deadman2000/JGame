package com.deadman.dh.model.items;

import com.deadman.dh.battle.MissionEngine;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.itemtypes.WeaponType;

public abstract class Weapon extends Item
{
	public Weapon(WeaponType type)
	{
		super(type);
	}

	public WeaponType wtype()
	{
		return (WeaponType) type;
	}

	public abstract void hit(MissionEngine engine, GameCharacter unit, MapCell to);
	
	public boolean canAttack(GameCharacter ch, GameCharacter another)
	{
		if (canAttack(ch, another.cell))
			return true;

		if (another.nearCell != null)
			return canAttack(ch, another.nearCell);
		return false;
	}

	public abstract boolean canAttack(GameCharacter ch, MapCell c);

	public boolean isMeele()
	{
		return false;
	}
}
