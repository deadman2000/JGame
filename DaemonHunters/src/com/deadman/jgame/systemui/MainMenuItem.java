package com.deadman.jgame.systemui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Label;

public class MainMenuItem extends Control
{
	public MainMenu menu;
	private Label label;

	private Control subItemsPanel;

	private final int FOCUSED_COLOR = 0x30ffffff;

	public MainMenuItem(MainMenu menu, String text)
	{
		this.menu = menu;
		label = new Label(menu.font, 2, 1, text);
		addControl(label);

		height = label.height + 2;
		width = label.width + 4;
	}

	public MainMenuItem(MainMenuItem parent, String text)
	{
		menu = parent.menu;
		label = new Label(menu.font, 2, 1, text);
		addControl(label);

		width = 42;
		height = label.height + 4;
		label.y = 2;
	}

	public int id()
	{
		if (tag == null) return 0;
		return (int) tag;
	}

	@Override
	protected void onPressed(Point p, MouseEvent e)
	{
		if (subItemsPanel != null)
		{
			subItemsPanel.y = height;
			subItemsPanel.x = x;
			menu.openSubMenu(subItemsPanel);
		}
		else
		{
			closeMenu();
			if (menu.listener != null)
				menu.listener.onMainMenuPressed(this);
		}

		e.consume();
	}

	private void closeMenu()
	{
		menu.closeSubMenu();
	}

	@Override
	public void addControl(Control c)
	{
		if (c instanceof MainMenuItem)
			addItem((MainMenuItem) c);
		else
			super.addControl(c);
	}

	public void addControl(MainMenuItem item)
	{
		addItem(item);
	}

	public void addItem(MainMenuItem item)
	{
		if (subItemsPanel == null)
			subItemsPanel = menu.createSubMenuPanel();

		item.x = 2;
		subItemsPanel.addControl(item);

		if (subItemsPanel.width < item.width + 4)
			subItemsPanel.width = item.width + 4;
	}

	public MainMenuItem addItem(String text, int id)
	{
		MainMenuItem it = new MainMenuItem(this, text);
		addItem(it);

		it.tag = id;

		return it;
	}

	public MainMenuItem addItem(String text)
	{
		return addItem(text, -1);
	}

	@Override
	protected void onMouseEnter()
	{
		super.onMouseEnter();
		bgrColor = FOCUSED_COLOR;
	}

	@Override
	protected void onMouseLeave()
	{
		super.onMouseLeave();
		bgrColor = 0;
	}
}
