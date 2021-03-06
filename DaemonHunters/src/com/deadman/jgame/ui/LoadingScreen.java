package com.deadman.jgame.ui;

import com.deadman.dh.global.Snow;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.GameScreen;

public class LoadingScreen extends GameEngine
{
	private Snow snow;
	private GameEngine _engineOnFinish;
	private ProgressStatus _status;

	public LoadingScreen(ProgressStatus status, GameEngine showOnFinish)
	{
		_engineOnFinish = showOnFinish;

		_status = status;
		
		snow = new Snow(100);
		snow.interactive = true;
		addControl(snow);
		RelativeLayout.settings(snow).fill();
	}

	@Override
	public void tick()
	{
		snow.tick(ticks);
	}

	@Override
	public void draw()
	{
		screen.drawRect(0, 0, GameScreen.GAME_WIDTH, GameScreen.GAME_HEIGHT, 0xff000000);
		
		if (_status.progress == _status.max)
		{
			_engineOnFinish.show();
			//return;
		}

		
		int w = GameScreen.GAME_WIDTH - 40;
		screen.drawRect(20, GameScreen.GAME_HEIGHT - 50, w, 30, 0xFF7F7F7F);
		screen.drawRect(20 + 2, GameScreen.GAME_HEIGHT - 50 + 2, (w - 4) * _status.progress / _status.max, 30 - 4, 0xFFFF0000);

		super.draw();
	}

	public static void showLoading(ProgressStatus st, GameEngine engineOnFinish)
	{
		new LoadingScreen(st, engineOnFinish).show();
	}
}
