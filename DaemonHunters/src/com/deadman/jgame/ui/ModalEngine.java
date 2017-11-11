package com.deadman.jgame.ui;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameScreen;

public class ModalEngine extends GameEngine
{
	private final GameEngine _engine;
	private final boolean _isModalEngine;

	public final Control topControl;

	public ModalEngine(Control control)
	{
		if (GameLoop.engine == null)
			throw new NullPointerException("Current engine is null");
		
		_engine = GameLoop.engine;
		_isModalEngine = _engine instanceof ModalEngine;

		topControl = control;
		addControl(control);
		control.centerParent();
		control.isFocused = true;
	}

	@Override
	public void draw()
	{
		if (!_isModalEngine)
		{
			GameScreen.screen.beforeDrawFX();
			_engine.draw();
			GameScreen.screen.afterDrawFX();
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
