package com.deadman.dh.isometric;

import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;

public class IsoCursor
{
	public Drawable back, front, bottom_back, bottom_front;

	public IsoCursor()
	{
	}
	
	public IsoCursor(int res)
	{
		this.front = Drawable.get(res);
	}
	
	public IsoCursor(Drawable back, Drawable top, Drawable b_back, Drawable b_top)
	{
		this.back = back;
		this.front = top;
		this.bottom_back = b_back;
		this.bottom_front = b_top;
	}

	public static IsoCursor CURSOR_RECT;
	public static IsoCursor CURSOR_ATTACK;

	static
	{
		Drawable p_rect_back = Drawable.get(R.iso.rect_back);
		Drawable p_rect_front = Drawable.get(R.iso.rect_front);
		Drawable p_attack_back = Drawable.get(R.iso.attack_back);
		Drawable p_attack_front = Drawable.get(R.iso.attack_front);

		CURSOR_RECT = new IsoCursor(p_rect_back, p_rect_front, p_rect_back, p_rect_front);
		CURSOR_ATTACK = new IsoCursor(p_attack_back, p_attack_front, p_rect_back, p_rect_front);
	}
}
