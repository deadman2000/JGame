package com.deadman.dh.info;

import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MenuRow;
import com.deadman.dh.model.Cart;
import com.deadman.jgame.ui.Label;

public class CartRow extends MenuRow
{
	public CartRow(int ind, Cart cart)
	{
		super(ind, cart.name, cart);

		addControl(new Label(IGMPanel.fnt_igm, 140, 2, "" + cart.unitsCapacity));
		addControl(new Label(IGMPanel.fnt_igm, 160, 2, "" + cart.price));
	}
}
