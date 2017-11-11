package com.deadman.dh.info;

import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MenuRow;
import com.deadman.dh.model.Horse;
import com.deadman.jgame.ui.Label;

public class HorseRow extends MenuRow
{
	public HorseRow(int ind, Horse horse)
	{
		super(ind, horse.name, horse);
		
		addControl(new Label(IGMPanel.fnt_igm, 140, 2, "" + horse.speed));
		addControl(new Label(IGMPanel.fnt_igm, 160, 2, "" + horse.price));
	}
}
