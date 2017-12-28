package com.deadman.dh.isometric.editor;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.deadman.dh.R;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.IOverlayListener;
import com.deadman.jgame.ui.Control;

public class MainMenu extends Control implements IOverlayListener
{
	private GameEngine engine;

	private int nextX = 4;
	private int spacing = 4;
	private Control openedMenu;
	public IMainMenuListener listener;

	public MainMenu(GameEngine eng, IMainMenuListener listener)
	{
		engine = eng;
		this.listener = listener;

		background = getDrawable(R.editor.ie_menu_bgr);
		height = background.height;
	}

	public MainMenuItem addItem(String text)
	{
		MainMenuItem it = new MainMenuItem(this, text, true);
		it.x = nextX;
		nextX += spacing + it.width;
		addControl(it);
		return it;
	}

	@Override
	protected void onPressed(Point p, MouseEvent e)
	{
		super.onPressed(p, e);
		if (!e.isConsumed())
			closeSubMenu();
	}
	
	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			if (openedMenu != null)
			{
				closeSubMenu();
				e.consume();
			}
		}
	}

	public final int SUBMENU_WIDTH = 48;

	public Control createSubMenuPanel()
	{
		Control panel = new Control();
		panel.width = SUBMENU_WIDTH;
		panel.height = 10;
		panel.background = getDrawable(R.editor.ie_panel_9p);
		return panel;
	}

	public void closeSubMenu()
	{
		if (openedMenu != null)
		{
			openedMenu.remove();
			openedMenu = null;

			engine.hideOverlay();
		}
	}

	public void openSubMenu(Control control)
	{
		close();

		engine.showOverlay(this);
		engine.overlay().addControl(control);
		openedMenu = control;
	}

	@Override
	public void overlayPressed()
	{
		closeSubMenu();
	}
}
