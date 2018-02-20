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

	public MainMenuItem(MainMenu menu, String text, boolean root)
	{
		this.menu = menu;
		label = new Label(menu.font, 2, 1, text);
		addControl(label);

		if (root)
		{
			height = label.height + 2;
			width = label.width + 4;
		}
		else
		{
			width = 42;
			height = label.height + 4;
			label.y = 2;
		}
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
			menu.listener.onMainMenuPressed(this);
		}

		e.consume();
	}

	private void closeMenu()
	{
		menu.closeSubMenu();
	}

	public MainMenuItem addItem(String text, int id)
	{
		if (subItemsPanel == null)
		{
			subItemsPanel = menu.createSubMenuPanel();
		}

		MainMenuItem it = new MainMenuItem(menu, text, false);
		it.x = 2;
		it.tag = id;

		subItemsPanel.addControl(it);

		if (subItemsPanel.width < it.width + 4)
			subItemsPanel.width = it.width + 4;

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
