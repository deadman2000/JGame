package com.deadman.dh.model.items;

public enum AmmunitionSlot
{
	BACKPACK(0),
	HELM(1),
	AMULET(2),
	CLOAK(3),
	RIGHTHAND(4),
	BODY(5),
	LEFTHAND(6),
	GLOVES(7),
	LEGGINS(8),
	BRACERS(9),
	RING1(10),
	RING2(11),
	BOOTS(12),
	RING3(13),
	RING4(14);

	public final short id;

	AmmunitionSlot(int id)
	{
		this.id = (short) id;
	}

	public static final int COUNT = 15;
}
