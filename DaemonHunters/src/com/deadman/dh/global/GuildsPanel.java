package com.deadman.dh.global;

import java.util.ArrayList;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.guild.Guild;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;

public class GuildsPanel extends Control
{
	private ArrayList<GuildCard> guildCards = new ArrayList<>();

	private static final int top_pad = 12;

	private static final Drawable img_bgr = getDrawable(R.ui.scroll_bgr);
	private static final Drawable img_bottom = getDrawable(R.ui.scroll_bottom_2);
	
	public GuildsPanel()
	{
		width = img_bgr.width;
		calcSize();
	}

	private void calcSize()
	{
		height = top_pad + guildCards.size() * 28 + 2 + img_bottom.height - 2;
	}

	public void addGuild(Guild g)
	{
		GuildCard card = new GuildCard(g, GlobalEngine.fnt3x5_brown);
		card.setPosition(3, top_pad + guildCards.size() * 28);
		addControl(card);
		guildCards.add(card);
		calcSize();
	}

	public void removeGuild(Guild g)
	{
		for (GuildCard gc : guildCards)
		{
			if (gc.guild == g)
			{
				gc.remove();
				guildCards.remove(gc);
				break;
			}
		}
		calcSize();
	}

	public void clear()
	{
		for (GuildCard gc : guildCards)
		{
			gc.remove();
		}
		guildCards.clear();
	}

	@Override
	public void draw()
	{
		int h = top_pad + guildCards.size() * 28 + 2;
		img_bgr.drawAt(scrX, scrY, img_bgr.width, h);
		img_bottom.drawAt(scrX, scrY + h);
		super.draw();
	}

	public void update()
	{
		clear();
		for (Guild g : Game.guilds)
			addGuild(g);
	}
}
