package com.deadman.dh.model.items;

import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.itemtypes.ItemType;

/**
 * Мертвый персонаж на полу
 */
public class Corpse extends Item
{
	public final GameCharacter character;
	
	public Corpse(GameCharacter character)
	{
		super(ItemType.dead_unit);
		
		this.character = character;
	}

	@Override
	public void drawIsoAt(int x, int y)
	{
		character.drawAt(x, y);
	}
}
