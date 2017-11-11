package com.deadman.dh.model.items;

import com.deadman.dh.model.Element;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.itemtypes.ArmorType;

public class Armor extends Item
{
	public int[] armor = new int[Element.MAX_INDEX];
	
	public double apRate; // Штраф на AP 
	
	public Armor(ArmorType type)
	{
		super(type);
	}

	public void setArmor(Element el, int value)
	{
		armor[el.index] = value;
	}
	
	@Override
	public void appendDescription(StringBuilder sb)
	{
		for (int i = 0; i < Element.MAX_INDEX; i++)
		{
			int a = armor[i];
			if (a > 0)
			{
				switch (Element.valueOf(i))
				{
					case PHYSICAL:
						sb.append("Защита: ");
						break;
					case COLD:
						sb.append("Защита от холода: ");
						break;
					case FIRE:
						sb.append("Защита от огня: ");
						break;
					case POTION:
						sb.append("Защита от яда: ");
						break;
					default:
						System.err.println("Unknown element damage description");
						return;
				}
				
				sb.append(a).append("\n");
			}
		}
	}
	
	@Override
	public void applyPassive(GameCharacter unit)
	{
		super.applyPassive(unit);

		for (int i = 0; i < Element.MAX_INDEX; i++)
			unit.armors[i] += armor[i];
	}
}
