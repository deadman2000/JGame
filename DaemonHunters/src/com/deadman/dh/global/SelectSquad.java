package com.deadman.dh.global;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.deadman.dh.Game;
import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MenuAction;
import com.deadman.dh.dialogs.MenuRow;
import com.deadman.dh.model.Squad;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.VListView;

public class SelectSquad extends IGMPanel
{
	static final int TOP_PAD = 20;
	static final int BOTTOM_PAD = 27;

	VListView lv;
	MenuAction action;

	public Squad squad;

	public SelectSquad(ArrayList<Squad> squads, MenuAction action)
	{
		super(240, 0);
		this.action = action;

		// TODO Ограничение по размеру
		height = TOP_PAD + squads.size() * MenuRow.HEIGHT + BOTTOM_PAD;

		setTitle("Выберите отряд");

		addControl(lv = new VListView());
		lv.setBounds(5, TOP_PAD, width - 13, squads.size() * MenuRow.HEIGHT); // TODO Переделать
		lv.setScrollBar(Game.getScrollThemeYellow());
		lv.bgrColor = 0xFF686c51;
		lv.addControlListener(lv_listener);

		for (Squad s : squads)
			lv.addItem(new MenuRow(s.name, s));
		if (lv.itemsCount() > 0)
			lv.selectFirst();

		int by = TOP_PAD + squads.size() * MenuRow.HEIGHT + 2;
		addButton(0, "Отправить", 86, by, 70);
		addButton(TAG_CLOSE, "Закрыть", 160, by, 70);
	}

	private ControlListener lv_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == ACTION_ITEM_DBLCLICK)
				selectSquad();
		}
	};

	protected void onButtonPressed(int tag)
	{
		switch (tag)
		{
			case 0:
				selectSquad();
				break;
		}
	}

	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			selectSquad();
	}

	private void selectSquad()
	{
		if (lv.selectedItem() != null)
			squad = (Squad) lv.selectedItem().tag;
		if (action != null)
			action.onAction(this, 0);
		close();
	}
}
