package com.deadman.dh.info;

import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MessageBox;
import com.deadman.dh.guild.GuildBuilding;
import com.deadman.dh.model.Unit;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.VListView;

public class BuildingSelect extends IGMPanel
{
	VListView lv;

	Unit unit;

	public BuildingSelect(Unit u)
	{
		super(150, 100);

		unit = u;

		lv = new VListView();
		lv.setBounds(5, 5, width - 13, 100);
		addControl(lv);
		lv.addControlListener(lv_listener);

		for (int i = 0; i < u.guild.buildings.size(); i++)
		{
			GuildBuilding gb = u.guild.buildings.get(i);
			if (!gb.isBuild() && gb.getMaxUnits() > 0)
				lv.addItem(new ListItemGuildBuilding(i, gb));
		}
		
		//lv.autoHeight();
		if (u.building != null)
			lv.selectByTag(u.building);

		int pad = 6;

		height = 5 + lv.height + pad + 15 + 7;

		int w = (width - 13 - 5) / 2;
		addButton(TAG_CLOSE, "Отмена", 5, 5 + lv.height + pad, w);
		addButton(0, "Выбрать", 5 + w + 5, 5 + lv.height + pad, w);
	}

	private ControlListener lv_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == ACTION_ITEM_DBLCLICK)
				accept();
		};
	};

	protected void onButtonPressed(int tag)
	{
		if (tag == 0)
			accept();
	};

	void accept()
	{
		if (lv.selectedItem() == null) return;

		GuildBuilding gb = ((ListItemGuildBuilding) lv.selectedItem()).getBuilding();
		if (gb.isUnitsFull())
		{
			MessageBox.show("Недостаточно места");
			return;
		}

		unit.setInBuilding(gb);
		close();
	}

}
