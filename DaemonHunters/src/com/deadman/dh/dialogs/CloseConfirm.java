package com.deadman.dh.dialogs;

import java.awt.event.KeyEvent;

import com.deadman.jgame.GameLoop;

public class CloseConfirm extends IGMPanel
{
	static final int TAG_YES = 0;
	static final int TAG_NO = 1;

	public CloseConfirm()
	{
		super(200, 55);

		setTitle("Вы действительно хотите выйти?");

		int cy = 28;
		addButton(TAG_YES, "Да", 15, cy, 50);
		addButton(TAG_NO, "Нет", width - 60, cy, 50);
	}

	@Override
	protected void onButtonPressed(int tag)
	{
		switch (tag)
		{
			case TAG_YES:
				//GameLoop.quit();
				System.exit(0);
				break;
			case TAG_NO:
				close();
				break;
		}
	}
	
	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			GameLoop.quit();
			e.consume();
		}
	}
}
