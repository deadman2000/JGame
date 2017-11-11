package com.deadman.dh.global;

import java.awt.event.MouseEvent;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.dialogs.Book;
import com.deadman.dh.dialogs.InGameMenu;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.info.GuildInfoEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;

public class TopMenu extends Control
{
	Label laGold;

	public Guild guild;

	public static final int MODE_MENU = 0;
	public static final int MODE_CLOSE = 1;

	public TopMenu(int mode)
	{
		super(R.ui.top_menu);
		setPosition(GameScreen.GAME_WIDTH - width, 0, Control.ANCHOR_RIGHT_TOP);

		clickOnBgr = true;

		laGold = new Label(getFont(R.fonts.font4x7, 0xff3a2717).shadow(0xff82825b), 28, 4);
		addControl(laGold);

		Button btRed = new Button(R.ui.bt_red_arms, R.ui.bt_red_arms_pr);
		btRed.clickOnBgr = true;
		btRed.setPosition(9, 17);
		addControl(btRed);
		btRed.addControlListener(new ControlListener()
		{
			@Override
			public void onControlPressed(Control control, MouseEvent e)
			{
				Book.inst().show();
			}
		});

		Button btBlue = new Button(R.ui.bt_blue_arms, R.ui.bt_blue_arms_pr);
		btBlue.clickOnBgr = true;
		btBlue.setPosition(27, 20);
		addControl(btBlue);

		if (mode == MODE_MENU)
		{
			Button btMenu = new Button(R.ui.bt_menu, R.ui.bt_menu_pr);
			btMenu.clickOnBgr = true;
			btMenu.setPosition(111, 9);
			addControl(btMenu);
			btMenu.addControlListener(new ControlListener()
			{
				@Override
				public void onControlPressed(Control control, MouseEvent e)
				{
					InGameMenu.show();
				}
			});
		}
		else if (mode == MODE_CLOSE)
		{
			Button btBack = new Button(R.ui.bt_back);
			btBack.clickOnBgr = true;
			btBack.setPosition(111, 7);
			addControl(btBack);
			btBack.addControlListener(new ControlListener()
			{
				@Override
				public void onControlPressed(Control control, MouseEvent e)
				{
					GameLoop.engine.close();
				}
			});
		}

		Button btInfo = new Button(R.ui.bt_info, R.ui.bt_info_pr);
		btInfo.clickOnBgr = true;
		btInfo.setPosition(89, 4);
		addControl(btInfo);
		btInfo.addControlListener(new ControlListener()
		{
			@Override
			public void onControlPressed(Control control, MouseEvent e)
			{
				new GuildInfoEngine(guild).show();
			}
		});

		updateGold();
	}

	private long currGold = -1;

	private void updateGold()
	{
		if (Game.gold != currGold)
		{
			currGold = Game.gold;
			laGold.setText(Game.gold);
		}
	}

	@Override
	public void draw()
	{
		updateGold();
		super.draw();
	}
}
