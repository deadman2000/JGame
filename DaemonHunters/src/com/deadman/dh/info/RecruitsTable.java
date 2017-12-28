package com.deadman.dh.info;

import java.awt.event.KeyEvent;

import com.deadman.dh.Game;
import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MessageBox;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.model.Unit;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.VListView;

public class RecruitsTable extends IGMPanel
{
	static final int SIZE = 10;
	static final int TOP_PAD = 20;
	static final int BOTTOM_PAD = 27;

	VListView lv;

	Guild guild;
	GuildInfoEngine eng;

	public RecruitsTable(GuildInfoEngine e, Guild g)
	{
		super(240, TOP_PAD + SIZE * RecruitRow.HEIGHT + BOTTOM_PAD);

		eng = e;
		guild = g;

		addControl(new Label(fnt_igm, 7, 7, "Имя"));
		addControl(new Label(fnt_igm, 140, 7, "Пол"));
		addControl(new Label(fnt_igm, 163, 7, "Ур"));
		addControl(new Label(fnt_igm, 180, 7, "СЛ"));
		addControl(new Label(fnt_igm, 195, 7, "ЛВ"));
		addControl(new Label(fnt_igm, 210, 7, "ИН"));

		addControl(lv = new VListView());
		lv.setBounds(5, TOP_PAD, width - 13, SIZE * RecruitRow.HEIGHT);
		lv.setScrollBar(Game.createVScrollInfo());
		lv.bgrColor = 0xFF686c51;
		lv.addControlListener(lv_listener);

		for (int i = 0; i < SIZE; i++)
			lv.addItem(new RecruitRow(i, Unit.generate()));
		if (lv.itemsCount() > 0)
			lv.selectIndex(0);
		
		int by = TOP_PAD + SIZE * RecruitRow.HEIGHT + 2;
		addButton(0, "Нанять", 86, by, 70);
		addButton(TAG_CLOSE, "Закрыть", 160, by, 70);
	}

	private void recruitCurrent()
	{
		if (lv.selectedItem() == null) return;

		if (!guild.canRecruit())
		{
			MessageBox.show("Недостаточно места в гильдии");
			return;
		}

		guild.addUnit((Unit) lv.selectedItem().tag);
		lv.removeItem(lv.selectedItem());
		for (int i = 0; i < lv.itemsCount(); i++)
			((RecruitRow) lv.items.get(i)).setIndex(i);

		eng.updateUnitsCount();
	}

	private ControlListener lv_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag) 
		{
			if (action == ACTION_ITEM_DBLCLICK)
				recruitCurrent();
		};
	};

	@Override
	protected void onButtonPressed(int tag)
	{
		if (tag == 0)
			recruitCurrent();
	}

	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			recruitCurrent();
	}

}
