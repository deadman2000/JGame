package com.deadman.dh.global;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.model.Squad;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Label;

public class SquadCard extends Control
{
	private Label laName;
	public final Squad squad;

	private static final Drawable img_bgr = getDrawable(R.ui.squad_card);
	private static final Drawable ic_move = getDrawable(R.ui.ic_squad_move);
	private static final Drawable ic_stand = getDrawable(R.ui.ic_squad_stand);

	public static final int HEIGHT = img_bgr.height + 2;

	public SquadCard(Squad s)
	{
		squad = s;

		width = img_bgr.width;
		height = HEIGHT;
		laName = new Label(GlobalEngine.fnt3x5_brown, 0, 4, s.name);
		addControl(laName);
	}

	@Override
	public void draw()
	{
		super.draw();

		img_bgr.drawAt(scrX, scrY);
		if (squad.location == null)
			ic_move.drawAt(scrX + 73, scrY + 4);
		else
			ic_stand.drawAt(scrX + 73, scrY + 4);
	}

	@Override
	protected void onClick(Point p, MouseEvent e)
	{
		Game.global.setCenter(squad.x, squad.y);
	}
}
