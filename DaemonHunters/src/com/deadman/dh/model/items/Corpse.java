package com.deadman.dh.model.items;

import com.deadman.dh.ItemTypes;
import com.deadman.dh.model.GameCharacter;

/**
 * Мертвый персонаж на полу
 */
public class Corpse extends Item
{
	public final GameCharacter character;
	
	public Corpse(GameCharacter character)
	{
		super(ItemTypes.dead_unit);
		
		this.character = character;
	}

	@Override
	public Item cloneOne()
	{
		return new Corpse(character);
	}

	@Override
	public void drawIsoAt(int x, int y)
	{
		character.drawAt(x, y);
	}
}
