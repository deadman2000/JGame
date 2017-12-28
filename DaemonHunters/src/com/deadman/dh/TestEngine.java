package com.deadman.dh;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.ColumnLayout;
import com.deadman.jgame.ui.Control;

public class TestEngine extends GameEngine
{
	public TestEngine()
	{
		//GameResources.init();

		setLayout(new ColumnLayout());
		
		{
			Control i1 = new Control();
			i1.height = 20;
			i1.bgrColor = 0xff0000;
			addControl(i1);
		}

		{
			Control i2 = new Control();
			ColumnLayout.settings(i2).fillHeight();
			i2.bgrColor = 0x00ff00;
			addControl(i2);
		}

		{
			Control i3 = new Control();
			i3.height = 20;
			i3.bgrColor = 0x0000ff;
			addControl(i3);
		}
	}

	@Override
	public void draw()
	{
		super.draw();
	}

	@Override
	public void onKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			/*case KeyEvent.VK_LEFT:
				cx--;
				updateSubpic();
				break;
			case KeyEvent.VK_RIGHT:
				cx++;
				updateSubpic();
				break;
			case KeyEvent.VK_UP:
				cy--;
				updateSubpic();
				break;
			case KeyEvent.VK_DOWN:
				cy++;
				updateSubpic();
				break;*/
		}

		super.onKeyPressed(e);
	}

	@Override
	public void onMouseMoved(Point p, MouseEvent e)
	{
		super.onMouseMoved(p, e);
		GameScreen.screen.setTitle(p.toString());
	}
}
