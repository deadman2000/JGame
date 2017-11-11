package com.deadman.dh.global;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.guild.Guild;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Label;

public class GuildCard extends Control
{
	private Label laName;
	public final Guild guild;

	private static Drawable BGR = getDrawable(R.ui.guild_card);
	private static Drawable PB = getDrawable(R.ui.guild_pb_green);

	public GuildCard(Guild g, GameFont nameFont)
	{
		guild = g;

		background = BGR;

		width = background.width;
		height = background.height;
		laName = new Label(nameFont, 0, 4, g.city.name);
		addControl(laName);
	}

	@Override
	public void draw()
	{
		super.draw();

		if (guild.buildProgress >= 0)
		{
			PB.drawAt(scrX + 1, scrY + 11, 0, 0, (int) (guild.buildProgress * PB.width), 2);
		}
	}
	
	@Override
	protected void onClick(Point p, MouseEvent e)
	{
		if (e.getClickCount() == 1)
			Game.global.setCenter(guild.city);
		else
			guild.show();
	}
}
