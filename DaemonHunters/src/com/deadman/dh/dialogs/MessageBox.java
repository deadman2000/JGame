package com.deadman.dh.dialogs;

import java.awt.event.KeyEvent;

import com.deadman.jgame.ui.Label;

public class MessageBox extends IGMPanel
{
	private static final int LEFT_PAD = 10;
	private static final int RIGHT_PAD = 13;

	private int _mode;

	private MessageBox(String txt, int mode)
	{
		super(100, 50);

		Label laMessage = new Label(IGMPanel.fnt_igm, LEFT_PAD, 10, txt);
		addControl(laMessage);
		width = Math.max(LEFT_PAD + laMessage.width + RIGHT_PAD, 125);

		_mode = mode;
		if (mode == MBMODE_OK)
			addButton(MBACTION_OK, "ОК", width / 2 - 25, 25, 50);
		else if (mode == MBMODE_YESNO)
		{
			addButton(MBACTION_YES, "Да", width / 2 - 51, 25, 50);
			addButton(MBACTION_NO, "Нет", width / 2 + 1, 25, 50);
		}
	}

	private MenuAction action;

	@Override
	protected void onButtonPressed(int tag)
	{
		if (action != null)
			action.onAction(this, tag);

		close();
	}

	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			switch (_mode)
			{
				case MBMODE_OK:
					onButtonPressed(MBACTION_OK);
					break;
				case MBMODE_YESNO:
					onButtonPressed(MBACTION_YES);
					break;
			}
		}
	}

	private static final int MBMODE_OK = 0;
	private static final int MBMODE_YESNO = 1;

	public static final int MBACTION_OK = 0;
	public static final int MBACTION_NO = 0;
	public static final int MBACTION_YES = 1;

	public static void show(String message)
	{
		MessageBox mb = new MessageBox(message, MBMODE_OK);
		mb.showModal();
	}

	public static void showYesNo(String message, MenuAction action)
	{
		MessageBox mb = new MessageBox(message, MBMODE_YESNO);
		mb.action = action;
		mb.showModal();
	}
}
