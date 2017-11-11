package com.deadman.dh.model;

import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;

public class Horse
{
	public final int speed;
	public final Drawable icon;
	public final int price;
	public final String name;

	public Horse(String name, int type, int speed, int price)
	{
		this.name = name;
		icon = Drawable.get(R.ui.horse);
		this.speed = speed;
		this.price = price;
	}

	public static Horse generate()
	{
		return new Horse("Лошадка", 0, 10, 1000);
	}
}
