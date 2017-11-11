package com.deadman.walker;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.resources.XCF;
import com.deadman.jgame.ui.Control;

public class Compas extends Control
{
	private Drawable foreground, text;
	public static float direction;

	public Compas(XCF xcf)
	{
		super(xcf.getDrawable("compas_bgr"));
		setPosition((GameScreen.GAME_WIDTH - width) / 2, 0);
		anchor = ANCHOR_TOP;

		foreground = xcf.getDrawable("compas_fore");
		text = xcf.getDrawable("compas_text");
	}

	@Override
	public void draw()
	{
		background.drawAt(scrX, scrY, width, height);
		text.drawAt(scrX + 6, scrY + 3, (int) (direction * 45) + 10, 0, width - 9, text.height);
		foreground.drawAt(scrX + 2, scrY + 2);
	}
}
