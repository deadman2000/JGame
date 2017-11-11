package com.deadman.dh.info;

import java.awt.event.KeyEvent;

import com.deadman.dh.Game;
import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MessageBox;
import com.deadman.dh.model.Horse;
import com.deadman.dh.model.Squad;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.VListView;

public class BuyHorse extends IGMPanel
{
	static final int TOP_PAD = 20;
	static final int BOTTOM_PAD = 27;
	static final int COUNT = 10;

	private final VListView lv;

	private final Squad squad;

	public BuyHorse(Squad sq)
	{
		super(240, 0);

		squad = sq;

		addControl(new Label(IGMPanel.fnt_igm, 7, 7, "Тип"));
		addControl(new Label(IGMPanel.fnt_igm, 110, 7, "Скорость"));
		addControl(new Label(IGMPanel.fnt_igm, 163, 7, "Цена"));

		addControl(lv = new VListView(5, TOP_PAD, width - 13, COUNT * HorseRow.HEIGHT));
		lv.item_height = RecruitRow.HEIGHT;
		lv.setScrollBar(Game.createVScrollInfo());
		lv.bgrColor = 0xFF686c51;
		lv.addControlListener(lv_listener);

		for (int i = 0; i < COUNT; i++)
			lv.addItem(new HorseRow(i, Horse.generate()));
		if (lv.size() > 0)
			lv.selectIndex(0);

		height = TOP_PAD + CartRow.HEIGHT * lv.size() + BOTTOM_PAD;

		int by = TOP_PAD + lv.size() * RecruitRow.HEIGHT + 2;
		addButton(0, "Купить", 86, by, 70);
		addButton(TAG_CLOSE, "Закрыть", 160, by, 70);
	}

	private ControlListener lv_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == ACTION_ITEM_DBLCLICK)
				buyCurrent();
		};
	};

	protected void onButtonPressed(int tag)
	{
		if (tag == 0)
			buyCurrent();
	};

	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			buyCurrent();
	}

	private void buyCurrent()
	{
		if (lv.selectedItem() == null) return;

		Horse horse = (Horse) lv.selectedItem().tag;

		if (Game.gold < horse.price)
		{
			MessageBox.show("Недостаточно золота");
			return;
		}

		Game.gold -= horse.price;
		squad.horse = horse;
		close();
	}

}
