package com.deadman.jgame.ui;

import com.deadman.jgame.drawing.Drawable;

public class ScrollBarTheme
{
	public Drawable v_pos, v_pos_pr;
	public Drawable up, up_pr;
	public Drawable down, down_pr;
	public Drawable v_bgr;

	public Drawable h_pos, h_pos_pr;
	public Drawable left, left_pr;
	public Drawable right, right_pr;
	public Drawable h_bgr;

	public void setUp(int id_up, int id_up_pr)
	{
		up = Drawable.get(id_up);
		up_pr = Drawable.get(id_up_pr);
	}

	public void setUp(int id_up)
	{
		up = up_pr = Drawable.get(id_up);
	}

	public void setDown(int id_down, int id_down_pr)
	{
		down = Drawable.get(id_down);
		down_pr = Drawable.get(id_down_pr);
	}

	public void setDown(int id_down)
	{
		down = down_pr = Drawable.get(id_down);
	}

	public void setVPos(int id_v_pos, int id_v_pos_pr)
	{
		v_pos = Drawable.get(id_v_pos);
		v_pos_pr = Drawable.get(id_v_pos_pr);
	}

	public void setVPos(int id_v_pos)
	{
		v_pos = v_pos_pr = Drawable.get(id_v_pos);
	}

	public void setVBgr(int id_v_bgr)
	{
		v_bgr = Drawable.get(id_v_bgr);
	}

	public void setHPos(int id_h_pos, int id_h_pos_pr)
	{
		h_pos = Drawable.get(id_h_pos);
		h_pos_pr = Drawable.get(id_h_pos_pr);
	}

	public void setLeft(int id_left, int id_left_pr)
	{
		left = Drawable.get(id_left);
		left_pr = Drawable.get(id_left_pr);
	}

	public void setRight(int id_right, int id_right_pr)
	{
		right = Drawable.get(id_right);
		right_pr = Drawable.get(id_right_pr);
	}
}
