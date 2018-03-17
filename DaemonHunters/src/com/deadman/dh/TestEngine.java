package com.deadman.dh;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;

public class TestEngine extends GameEngine
{
	private Drawable pic, bgr;
	
	public TestEngine()
	{
		pic = getDrawable(R.test_alpha);
		bgr = getDrawable(R.test_bgr);
	}

	@Override
	public void draw()
	{
		super.draw();
		
		bgr.drawAt(0, 0);
		
		GameScreen.screen.drawRect(230, 10, 100, 100, 0x7fff0000);
		pic.drawAt(120, 120);
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
