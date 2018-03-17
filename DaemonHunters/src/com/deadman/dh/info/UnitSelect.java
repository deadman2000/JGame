package com.deadman.dh.info;

import com.deadman.dh.Game;
import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.model.Squad;
import com.deadman.dh.model.Unit;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.VListView;

public class UnitSelect extends IGMPanel
{
	static final int TOP_PAD = 20;
	static final int BOTTOM_PAD = 27;

	VListView lv;

	Squad squad;
	int position;

	public UnitSelect(Squad s, int pos)
	{
		super(240, TOP_PAD + s.guild.squadFreeUnits.size() * RecruitRow.HEIGHT + BOTTOM_PAD);

		squad = s;
		position = pos;
		Guild g = squad.guild;

		addControl(new Label(fnt_igm, 7, 7, "Имя"));
		addControl(new Label(fnt_igm, 140, 7, "Пол"));
		addControl(new Label(fnt_igm, 163, 7, "Ур"));
		addControl(new Label(fnt_igm, 180, 7, "СЛ"));
		addControl(new Label(fnt_igm, 195, 7, "ЛВ"));
		addControl(new Label(fnt_igm, 210, 7, "ИН"));

		addControl(lv = new VListView());
		lv.setBounds(5, TOP_PAD, width - 13, g.squadFreeUnits.size() * RecruitRow.HEIGHT);
		lv.setScrollBar(Game.getScrollThemeYellow());
		lv.bgrColor = 0xFF686c51;
		lv.addControlListener(lv_listener);

		for (int i = 0; i < g.squadFreeUnits.size(); i++)
			lv.addItem(new RecruitRow(i, g.squadFreeUnits.get(i)));
		if (lv.itemsCount() > 0)
			lv.selectFirst();

		int by = TOP_PAD + g.squadFreeUnits.size() * RecruitRow.HEIGHT + 2;
		addButton(0, "Очистить", 12, by, 70);
		addButton(1, "Выбрать", 86, by, 70);
		addButton(TAG_CLOSE, "Закрыть", 160, by, 70);
	}

	private ControlListener lv_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == ACTION_ITEM_DBLCLICK)
				selectUnit();
		};
	};

	protected void onButtonPressed(int tag)
	{
		switch (tag)
		{
			case 0:
				clearUnit();
				break;
			case 1:
				selectUnit();
				break;
		}
	}

	private void selectUnit()
	{
		if (lv.selectedItem() == null) return;
		Unit u = (Unit) lv.selectedItem().tag;

		squad.setUnit(u, position);
		close();
	}

	private void clearUnit()
	{
		squad.setUnit(null, position);
		close();
	}
}
