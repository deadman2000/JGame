package com.deadman.dh.model.items;

public enum ItemSlotType
{
	ALL(AmmunitionSlot.BACKPACK, AmmunitionSlot.HELM, AmmunitionSlot.AMULET, AmmunitionSlot.CLOAK, AmmunitionSlot.RIGHTHAND, AmmunitionSlot.BODY, AmmunitionSlot.LEFTHAND, AmmunitionSlot.GLOVES, AmmunitionSlot.LEGGINS, AmmunitionSlot.BRACERS, AmmunitionSlot.RING1, AmmunitionSlot.RING2, AmmunitionSlot.BOOTS, AmmunitionSlot.RING3, AmmunitionSlot.RING4),
	HEAD(AmmunitionSlot.HELM),
	AMULET(AmmunitionSlot.AMULET),
	CLOAK(AmmunitionSlot.CLOAK),
	BODY(AmmunitionSlot.BODY),
	LEFTHAND(AmmunitionSlot.LEFTHAND),
	RIGHTHAND(AmmunitionSlot.RIGHTHAND),
	GLOVE(AmmunitionSlot.GLOVES),
	LEGGIN(AmmunitionSlot.LEGGINS),
	BRACER(AmmunitionSlot.BRACERS),
	RING(AmmunitionSlot.RING1, AmmunitionSlot.RING2, AmmunitionSlot.RING3, AmmunitionSlot.RING4),
	BOOT(AmmunitionSlot.BOOTS),
	BACKPACK(AmmunitionSlot.BACKPACK);

	AmmunitionSlot[] cells;

	ItemSlotType(AmmunitionSlot... cells)
	{
		this.cells = cells;
	}
}
