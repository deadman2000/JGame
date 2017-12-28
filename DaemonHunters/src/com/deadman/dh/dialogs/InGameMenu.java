package com.deadman.dh.dialogs;

public class InGameMenu extends IGMPanel
{
	private static InGameMenu menu;

	public static void showMenu()
	{
		if (menu == null)
			menu = new InGameMenu();
		menu.showModal();
	}

	static final int TAG_LOAD = 0;
	static final int TAG_SAVE = 1;
	static final int TAG_REMOVE = 2;
	static final int TAG_SETTINGS = 3;
	static final int TAG_QUIT = 4;
	static final int TAG_RETURN = 5;

	private static int hor_pad = 15;

	private InGameMenu()
	{
		super(160, 145);

		setTitle("Deamon Hunters v0.1");

		int bt_w = width - hor_pad * 2 - 3;
		int cy = 11;
		addButton(TAG_LOAD, "Загрузить игру", hor_pad, cy += 17, bt_w);
		addButton(TAG_SAVE, "Сохранить игру", hor_pad, cy += 17, bt_w);
		addButton(TAG_REMOVE, "Удалить игру", hor_pad, cy += 17, bt_w);
		addButton(TAG_SETTINGS, "Настройки", hor_pad, cy += 17, bt_w);
		addButton(TAG_QUIT, "Выход", hor_pad, cy += 17, bt_w);
		addButton(TAG_RETURN, "Продолжить", 40, cy += 20, width - 80);
	}

	@Override
	protected void onButtonPressed(int tag)
	{
		switch (tag)
		{
			case TAG_QUIT:
				new CloseConfirm().showModal();
				break;
			case TAG_RETURN:
				close();
				break;
		}
	}

}
