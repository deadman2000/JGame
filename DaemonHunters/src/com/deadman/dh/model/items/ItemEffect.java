package com.deadman.dh.model.items;

import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;

public abstract class ItemEffect
{
	// Активируемые эффекты
	public boolean canActivate()
	{
		return false;
	}
	
	public boolean activate(GameCharacter owner, IsoObject target)
	{
		return false;
	}
	
	// Пассивные эффекты
	public boolean hasPassive()
	{
		return false;
	}
	
	public void applyPassive(MapCell cell)
	{
	}
	
	public void applyPassive(GameCharacter unit)
	{
	}
	
	// Описание
	public void appendDescription(StringBuilder sb)
	{
	}
}
