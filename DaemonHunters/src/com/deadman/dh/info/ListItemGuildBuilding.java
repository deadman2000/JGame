package com.deadman.dh.info;

import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MenuRow;
import com.deadman.dh.guild.GuildBuilding;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.RelativeLayout;

public class ListItemGuildBuilding extends MenuRow
{
	private Label laSquare;

	public ListItemGuildBuilding(int ind, GuildBuilding gb)
	{
		super(ind, gb.type.name, gb);
		
		setLayout(new RelativeLayout());

		addControl(laSquare = new Label(IGMPanel.fnt_igm));
		laSquare.autosize = false;
		laSquare.halign = Label.ALIGN_RIGHT;
		laSquare.setSize(40, 0);
		RelativeLayout.settings(laSquare).alignRightTop(2, 2);
		laSquare.setText(gb.units.size() + "/" + gb.getMaxUnits());
	}

	public GuildBuilding getBuilding()
	{
		return (GuildBuilding)tag;
	}
}
