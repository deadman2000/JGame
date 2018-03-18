package com.deadman.dh;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.drawing.Picture;

public class TestEngine extends GameEngine
{
	Drawable pic;
	
	public TestEngine()
	{
		pic = Picture.load("test\\pngs\\basi0g04.png");
	}

	@Override
	public void draw()
	{
		super.draw();
		pic.drawAt(10, 10);
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
