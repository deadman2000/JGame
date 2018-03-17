package com.deadman.jgame.ui;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.drawing.ScreenEffect;

public class ModalEngine extends GameEngine
{
	private final GameEngine _engine;
	private final boolean _isModalEngine;

	public final Control topControl;

	public ModalEngine(Control control)
	{
		assert(GameLoop.engine != null);
		
		_engine = GameLoop.engine;
		_isModalEngine = _engine instanceof ModalEngine;

		topControl = control;
		addControl(control);
		RelativeLayout.settings(control).center();
		control.isFocused = true;
	}

	@Override
	public void draw()
	{
		if (!_isModalEngine)
		{
			GameScreen.screen.beginFX();
			_engine.draw();
			GameScreen.screen.endFX(ScreenEffect.DECOLORIZE);
		}
		else
		{
			_engine.draw();
		}
		super.draw();
	}

	@Override
	public void onSizeChanged()
	{
		super.onSizeChanged();
		if (_engine != null)
			_engine.onSizeChanged();
	}
}
