package com.deadman.pixelgame;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;

public class GameCharacter extends GameObject implements IHitable
{
	public HashMap<Integer, DrawableObject> states = new HashMap<>();
	int currState = -1;

	public void setStateDrawable(int num, DrawableObject draw)
	{
		states.put(num, draw);
	}

	public void setState(int num)
	{
		if (currState == num) return;
		
		Game.log("Set state " + num);
		DrawableObject d = states.get((Integer) num);
		if (drawable != d)
		{
			drawable = d;
			drawable.reset();
		}
		currState = num;
	}

	@Override
	public void draw()
	{
		super.draw();
		if (_speak != null && _speak.elapsed()) _speak = null;
	}

	@Override
	public void onMove(int direction)
	{
		Game.log("onMove");
		if (_speak != null) _speak.stop();
		super.onMove(direction);
		setState(ST_MOVE | direction);
	}

	@Override
	public void onStop()
	{
		Game.log("onStop");
		super.onStop();
		setState(ST_STAND | (currState & 0xFC));
	}

	public static final int ST_STAND = 0;
	public static final int ST_MOVE = 1;

	public static final int UP = 0 << 2;
	public static final int LEFT = 1 << 2;
	public static final int DOWN = 2 << 2;
	public static final int RIGHT = 3 << 2;


	@Override
	public boolean hit(Point p)
	{
		Game.log("Hit");
		stop();

		if (_speak != null) _speak.stop();

		Rectangle rect = getBounds();
		_speak = Game.screen.showText(getPhrase(), (int) rect.getCenterX(), rect.y - 5);
		return true;
	}
	
	private SpeakText _speak;

	//private String[] phrases = new String[] { "Hello.", "I am a man.", "I can speak.", "I speak english.", "И немного по русски.", "I can speak\nin two lines.", "And\nmore\nlines...", "..." };
	//private String[] phrases = new String[] { "СЪЕШЬ ЕЩЕ ЭТИХ МЯГКИХ\nФРАНЦУСКИХ БУЛОЧЕК", "ДА ВЫПЕЙ ЧАЮ" };
	//private String[] phrases = new String[] { "съешь еще этих мягких\nфранцуских булочек", "да выпей чаю" };
	private String[] phrases = new String[] { "Привет!", "Надо добавить\nанимацию речи." };

	private int ph_ind = 0;

	private String getPhrase()
	{
		String str = phrases[ph_ind];
		ph_ind++;
		if (ph_ind == phrases.length) ph_ind = 0;
		return str;
	}
}
