package com.deadman.dh.info;

import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MenuRow;
import com.deadman.dh.model.Unit;
import com.deadman.jgame.ui.Label;

public class RecruitRow extends MenuRow
{
	public RecruitRow(int ind, Unit unit)
	{
		super(ind, unit.name, unit);;

		addControl(new Label(IGMPanel.fnt_igm, 140, 2, unit.male ? "м" : "ж"));
		addControl(new Label(IGMPanel.fnt_igm, 160, 2, "" + unit.lvl));
		addControl(new Label(IGMPanel.fnt_igm, 178, 2, "" + unit.str));
		addControl(new Label(IGMPanel.fnt_igm, 193, 2, "" + unit.dex));
		addControl(new Label(IGMPanel.fnt_igm, 208, 2, "" + unit.intl));
	}

}
