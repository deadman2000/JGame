package com.deadman.dh.model.items;

import com.deadman.dh.model.GameCharacter;

public class UnitItemsPage extends ItemsPage
{
	public GameCharacter unit;
	
	public UnitItemsPage(GameCharacter unit, String name, int w, int h)
	{
		super(name, w, h);
		this.unit = unit;
	}
}
