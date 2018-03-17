package com.deadman.dh.isometric.editor;

import java.awt.event.KeyEvent;

import com.deadman.dh.R;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.ui.Column;
import com.deadman.jgame.ui.RelativeLayout;

public class IEWindow extends Column
{
	private GameEngine _eng;

	protected final int BTN_W = 38;
	protected final int BTN_H = 11;

	public IEWindow()
	{
		background = getDrawable(R.editor.ie_panel_9p);
		layout.heightByContent = true;
		layout.leftPadding = 4;
		layout.topPadding = 4;
		layout.rightPadding = 8;
		layout.bottomPadding = 8;
		fillContent();
		setSpacing(4);
	}

	@Override
	public void showModal()
	{
		_eng = GameLoop.engine;
		_eng.showOverlay();
		
		RelativeLayout.settings(this).center();
		
		_eng.overlay().addControl(this);
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
