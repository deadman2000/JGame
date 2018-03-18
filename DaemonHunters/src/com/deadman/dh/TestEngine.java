package com.deadman.dh;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.deadman.dh.isometric.IsoBigSprite;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.dh.isometric.IsoViewer;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.drawing.Picture;

public class TestEngine extends GameEngine
{
	Drawable pic;
	IsoViewer isoViewer;
	
	public TestEngine()
	{
		pic = Picture.load("test/cabinet.xcf?Cabinet");
		
		IsoBigSprite sprite = new IsoBigSprite();
		sprite.type = IsoSprite.OBJECT;
		sprite.width = 2;
		sprite.height = 1;
		sprite.mirrorRotation = true;
		sprite.initStates();
		sprite.pics.put((byte) 0, pic);
		sprite.completeStates();
		sprite.createSubPics();
		
		IsoMap map = new IsoMap(4, 4, 1, 0);
		sprite.setTo(map.cells[0][1][0]);
		sprite.setTo(map.cells[0][3][3], IsoSprite.getFullState(1, 0));

		isoViewer = new IsoViewer();
		isoViewer.drawGrid = true;
		isoViewer.setBounds(0, 0, 200, 200);
		isoViewer.setMap(map);
		isoViewer.centerView();
		addControl(isoViewer);
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
