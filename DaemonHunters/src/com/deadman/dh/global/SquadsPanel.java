package com.deadman.dh.global;

import java.util.ArrayList;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.model.Squad;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;

public class SquadsPanel extends Control
{
	private ArrayList<SquadCard> squadCards = new ArrayList<>();

	private static final Drawable img_top = getDrawable(R.ui.scroll_top);
	private static final Drawable img_bgr = getDrawable(R.ui.scroll_bgr);
	private static final Drawable img_bottom = getDrawable(R.ui.scroll_bottom_2);

	private static final int top_pad = 12;

	public SquadsPanel()
	{
		width = img_bgr.width;
		setPosition(5, 5);
		calcSize();
	}

	@Override
	public void draw()
	{
		int h = top_pad + squadCards.size() * SquadCard.HEIGHT - 5;
		img_top.drawAt(scrX, scrY);
		img_bgr.drawAt(scrX + 2, scrY + img_top.height - 1, img_bgr.width, h);
		img_bottom.drawAt(scrX + 2, scrY + img_top.height - 1 + h);
		super.draw();
	}

	private void calcSize()
	{
		if (squadCards.size() == 0)
			visible = false;
		else
		{
			visible = true;
			height = top_pad + squadCards.size() * SquadCard.HEIGHT + 2 + img_bottom.height - 2;
		}
	}

	public void clear()
	{
		for (SquadCard sc : squadCards)
		{
			sc.remove();
		}
		squadCards.clear();
	}

	public void addSquad(Squad s)
	{
		SquadCard card = new SquadCard(s);
		card.setPosition(3 + 2, top_pad + squadCards.size() * SquadCard.HEIGHT);
		addControl(card);
		squadCards.add(card);
		calcSize();
	}

	public void update()
	{
		clear();
		for (Squad s : Game.squads)
			addSquad(s);
	}
}
