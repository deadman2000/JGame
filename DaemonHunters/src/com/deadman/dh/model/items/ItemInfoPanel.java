package com.deadman.dh.model.items;

import com.deadman.dh.global.GlobalEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Label;

public class ItemInfoPanel extends Control
{
	public static boolean ENABLED = true;

	private Item _item;
	private Label _itemName;
	private Label _itemDescription;

	private ItemInfoPanel()
	{
		bgrColor = 0xffb5b5b5;
		//setBounds(0, 0, 100, 100);
		visible = false;

		Control picBgr = new Control(3, 3, 16, 16);
		addControl(picBgr);
		picBgr.bgrColor = 0xff000000;

		addControl(_itemName = new Label(GlobalEngine.fnt4x7_brown_sh, 21, 7));
		addControl(_itemDescription = new Label(GlobalEngine.fnt4x7_brown_sh, 3, 22));
	}

	@Override
	public void draw()
	{
		super.draw();
		_item.type.icon.drawAt(scrX + 11, scrY + 11);
	}

	private void _show(int x, int y, Item item)
	{
		if (_item == item) return;
		_item = item;

		System.out.println(item.getName());
		_itemName.setText(item.getName());
		StringBuilder sbDescr = new StringBuilder();
		item.appendDescription(sbDescr);
		String descrition = sbDescr.toString();
		_itemDescription.setText(descrition);
		int w, h;
		if (!descrition.isEmpty())
		{
			w = Math.max(100, _itemDescription.x + _itemDescription.getTextWidth() + 3);
			h = _itemDescription.y + _itemDescription.textHeight + 4;
		}
		else
		{
			w = 100;
			h = _itemDescription.y;
		}

		if (y + h + 13 > GameScreen.GAME_HEIGHT)
			y = GameScreen.GAME_HEIGHT - h - 3;
		else
			y += 10;

		x -= w / 2;
		if (x + w + 5 > GameScreen.GAME_WIDTH) // Сдвигаем справа от края экрана
			x = GameScreen.GAME_WIDTH - w - 5;
		if (x < 5) // Сдвигаем слева от края экрана
			x = 5;

		setBounds(x, y, w, h);
		visible = true;
	}

	private static ItemInfoPanel _panel;

	public static void showPanel(int x, int y, Item item)
	{
		if (item == null)
			hidePanel();
		
		if (_panel == null)
		{
			_panel = new ItemInfoPanel();
			if (!GameLoop.engine.containsControl(_panel)) GameLoop.engine.addControl(_panel);
		}

		_panel._show(x, y, item);
	}

	public static void hidePanel()
	{
		if (_panel == null) return;
		_panel.hide();
		_panel._item = null;
	}
}
