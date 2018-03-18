package com.deadman.jgame.tests;

import com.deadman.dh.isometric.IsoBigSprite;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.dh.isometric.IsoViewer;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.Picture;

public class EngineTestBigSprite extends GameEngine
{
	public EngineTestBigSprite()
	{
		Drawable pic = Picture.load("test/cabinet.xcf?Cabinet");

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

		IsoViewer isoViewer = new IsoViewer();
		isoViewer.setBounds(0, 0, 200, 200);
		isoViewer.setMap(map);
		isoViewer.centerView();
		addControl(isoViewer);
	}
}
