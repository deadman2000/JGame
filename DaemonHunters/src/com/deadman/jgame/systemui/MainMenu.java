package com.deadman.jgame.systemui;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.IOverlayListener;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Column;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Row;

public class MainMenu extends Row implements IOverlayListener
{
	private GameEngine engine;

	private Control openedMenu;
	public IMainMenuListener listener;
	public GameFont font;
	public Drawable subMenuBackground;
	
	public MainMenu()
	{
		setSpacing(4);
		layout.leftPadding = 1;
	}
	
	public MainMenu(GameEngine eng, IMainMenuListener listener)
	{
		this();
		engine = eng;
		this.listener = listener;
	}

	public MainMenuItem addItem(String text)
	{
		MainMenuItem it = new MainMenuItem(this, text);
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
		Column panel = new Column();
		panel.setSpacing(1);
		panel.width = SUBMENU_WIDTH;
		panel.background = subMenuBackground;
		panel.columnLayout.heightByContent = true;
		panel.columnLayout.topPadding = 4;
		panel.columnLayout.bottomPadding = 6;
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

		engine = GameLoop.engine;
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
