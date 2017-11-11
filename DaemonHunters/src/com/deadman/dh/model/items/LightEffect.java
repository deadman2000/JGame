package com.deadman.dh.model.items;

import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;

public class LightEffect extends ItemEffect
{
	private final float value;

	public LightEffect(float value)
	{
		this.value = value;
	}
	
	@Override
	public boolean hasPassive()
	{
		return true;
	}
	
	@Override
	public void applyPassive(MapCell cell)
	{
		cell.lightPassive += value;
	}
	
	@Override
	public void applyPassive(GameCharacter unit)
	{
		unit.lightPassive += value;
	}
}
