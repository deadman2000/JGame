package com.deadman.dh.model.items;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;

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
		if (_item == null)
		{
			hide();
			return;
		}
		_item.type.icon.drawAt(scrX + 11, scrY + 11);
	}

	private Timer timer;

	public void show(ItemSlot slot, Item item)
	{
		_item = item;
		if (item == null)
		{
			hide();
			return;
		}

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
		Point p = slot.screenPos();

		int x = p.x + slot.width / 2 - w / 2;
		int y = p.y - h - 2;

		// Укладываем в границы экрана
		if (y + h > GameScreen.GAME_HEIGHT)
			y = GameScreen.GAME_HEIGHT - h;
		else if (y < 0)
			y = 0;

		if (x + w > GameScreen.GAME_WIDTH) // Сдвигаем справа от края экрана
			x = GameScreen.GAME_WIDTH - w;
		else if (x < 0) // Сдвигаем слева от края экрана
			x = 0;

		setBounds(x, y, w, h);

		if (!visible && timer == null)
		{
			timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					visible = true;
					timer.cancel();
					timer = null;
				}
			}, 200);
		}
	}

	@Override
	public void hide()
	{
		super.hide();
		if (timer != null)
		{
			Timer t = timer;
			timer = null;
			try
			{
				t.cancel();
			}
			catch (Exception e)
			{
			}
		}
	}

	private static ItemInfoPanel _panel;

	public static ItemInfoPanel panel()
	{
		if (_panel == null)
		{
			_panel = new ItemInfoPanel();
			if (!GameLoop.engine.containsControl(_panel)) GameLoop.engine.addControl(_panel);
		}
		return _panel;
	}

	public static void hidePanel()
	{
		if (_panel == null) return;
		_panel.hide();
		_panel._item = null;
	}
}
