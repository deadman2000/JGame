package com.deadman.dh.model.items;

import com.deadman.dh.battle.MissionEngine;
import com.deadman.dh.isometric.Arrow;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.itemtypes.RangedWeaponType;

public class RangedWeapon extends Weapon
{
	public RangedWeapon(RangedWeaponType type)
	{
		super(type);
	}

	@Override
	public Item cloneOne()
	{
		return new RangedWeapon((RangedWeaponType) type);
	}

	@Override
	public void hit(MissionEngine engine, GameCharacter unit, MapCell to)
	{
		engine.putParticle(new Arrow(engine.map, unit, to, /* Damage */ 10)); // TODO Брать значение урона у стрелы
	}

	@Override
	public boolean canAttack(GameCharacter attack, MapCell c)
	{
		return true;
	}
}
