package com.deadman.dh.guild;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.R;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ListViewItem;

public class BuildingItem extends ListViewItem
{
	private static final Drawable picCard = getDrawable(R.ui.building_card);
	private static final Drawable picCardProgr = getDrawable(R.ui.building_card_progr);
	private static final Drawable pbBuilding = getDrawable(R.ui.guild_pb_green);
	public static final int WIDTH = 80;
	public static final int HEIGHT = 17;

	private final GuildBuilding building;
	private final GuildEngine engine;

	public BuildingItem(GuildEngine eng, GuildBuilding gb)
	{
		engine = eng;
		building = gb;
		height = 17;

		int dy = 0;
		if (gb.isBuild())
			dy = -2;

		setSize(WIDTH, HEIGHT);
		bgrMode = BGR_ONE;
		if (gb.isBuild())
			background = picCardProgr;
		else
			background = picCard;

		Label laType = new Label(GlobalEngine.fnt4x7_brown, 0, 5 + dy, gb.type.name);
		addControl(laType);

		if (!gb.isBuild() && gb.getMaxUnits() > 0)
		{
			Label laS = new Label(GlobalEngine.fnt3x5_brown);
			laS.setText(gb.units.size() + "/" + gb.getMaxUnits());
			laS.setPosition(picCard.width - laS.width - 2, 7 + dy);
			addControl(laS);
		}
	}

	@Override
	public void draw()
	{
		super.draw();

		if (building.isBuild())
		{
			pbBuilding.drawAt(scrX + 1, scrY + 13, 0, 0, (int) (building.buildingPerc() * pbBuilding.width), 2);
		}
	}

	@Override
	protected void onClick(Point p, MouseEvent e)
	{
		engine.setCenter(building);
	}
}
