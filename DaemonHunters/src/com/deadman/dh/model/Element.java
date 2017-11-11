package com.deadman.dh.model;

/**
 * Элемент урона
 * @author dead_man
 *
 */
public enum Element
{
	PHYSICAL(0),
	FIRE(1),
	COLD(2),
	POTION(3);

	public static final int MAX_INDEX = 3;

	private Element(int index)
	{
		this.index = index;
	}

	public final int index;

	public static Element valueOf(int index)
	{
		switch (index)
		{
			case 0:
				return Element.PHYSICAL;
			case 1:
				return Element.FIRE;
			case 2:
				return Element.COLD;
			case 3:
				return Element.POTION;
			default:
				System.err.println("Element(" + index + ") valueOf not implemented");
				return Element.PHYSICAL;
		}
	}
	
	public String damageName()
	{
		switch (this)
		{
			case PHYSICAL:
				return "Физический урон";
			case COLD:
				return "Урон холодом";
			case FIRE:
				return "Урон огнем";
			case POTION:
				return "Урон ядом";
			default:
				System.err.println("Unknown element damage description");
				return "";
		}
	}
}
