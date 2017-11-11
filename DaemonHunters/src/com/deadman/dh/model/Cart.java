package com.deadman.dh.model;

import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;

public class Cart
{
	private Cart(String name, int price, int unitCap, int pic)
	{
		this.name = name;
		this.price = price;
		unitsCapacity = unitCap;
		icon = Drawable.get(pic);
	}

	public final String name;
	public final int price;
	public final int unitsCapacity;
	public final Drawable icon;
	public boolean accessible = true;

	public static Cart[] carts;

	static
	{
		carts = new Cart[] { 
			new Cart("Крытая телега", 1000, 4, R.ui.cart),
			new Cart("Накрытая телега", 1500, 8, R.ui.cart)
		};
	}
}
