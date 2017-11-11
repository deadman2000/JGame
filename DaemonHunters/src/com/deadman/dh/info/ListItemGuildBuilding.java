package com.deadman.dh.info;

import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MenuRow;
import com.deadman.dh.guild.GuildBuilding;
import com.deadman.jgame.ui.Label;

public class ListItemGuildBuilding extends MenuRow
{
	private Label laSquare;

	public ListItemGuildBuilding(int ind, GuildBuilding gb)
	{
		super(ind, gb.type.name, gb);

		addControl(laSquare = new Label(IGMPanel.fnt_igm));
		laSquare.autosize = false;
		laSquare.halign = Label.ALIGN_RIGHT;
		laSquare.setBounds(width - 40 - 2, 2, 40, 0, ANCHOR_RIGHT_TOP);
		laSquare.setText(gb.units.size() + "/" + gb.getMaxUnits());
	}

	public GuildBuilding getBuilding()
	{
		return (GuildBuilding)tag;
	}
}
