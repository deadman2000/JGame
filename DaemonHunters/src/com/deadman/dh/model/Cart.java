package com.deadman.dh.model;

import com.deadman.dh.R;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.jgame.drawing.Drawable;

public class Cart
{
	private Cart(String name, int price, int unitCap, int pic, int map)
	{
		this.name = name;
		this.price = price;
		this.map = IsoMap.loadMap(map);
		unitsCapacity = unitCap;
		icon = Drawable.get(pic);
	}

	public final String name;
	public final int price;
	public final int unitsCapacity;
	public final Drawable icon;
	public boolean accessible = true;
	public final IsoMap map;

	public static Cart[] carts;

	static
	{
		carts = new Cart[] { 
			new Cart("Открытая телега", 1000, 4, R.ui.cart, R.maps.cart_map),
			new Cart("Накрытая телега", 1500, 8, R.ui.cart, R.maps.cart_map)
		};
	}
}
