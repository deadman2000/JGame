package com.deadman.dh.isometric.editor;

import java.awt.event.KeyEvent;

import com.deadman.dh.R;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Control;

public class IEWindow extends Control
{
	private GameEngine _eng;

	protected final int BTN_W = 38;
	protected final int BTN_H = 11;

	public IEWindow()
	{
		background = getDrawable(R.editor.ie_panel_9p);
	}

	@Override
	public void showModal()
	{
		_eng = GameLoop.engine;
		_eng.showOverlay();

		setBounds(GameScreen.GAME_WIDTH / 2 - width / 2, GameScreen.GAME_HEIGHT / 2 - height / 2, width, height, ANCHOR_NONE);

		_eng.addControl(this);
	}

	@Override
	public void close()
	{
		remove();
		_eng.hideOverlay();
		_eng = null;
	}
	
	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		super.onKeyPressed(e);
		
		if (e.isConsumed()) return;
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			close();
			e.consume();
		}
	}
}
