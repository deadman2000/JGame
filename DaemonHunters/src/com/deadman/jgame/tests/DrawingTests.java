package com.deadman.jgame.tests;

import org.junit.BeforeClass;
import org.junit.Test;

import com.deadman.dh.isometric.IsoBigSprite;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.dh.isometric.IsoViewer;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.*;
import com.deadman.jgame.ui.*;

public class DrawingTests
{
	// https://habrahabr.ru/post/120101/

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		GameScreen.initTesting();
	}

	@Test
	public void testEmpty() throws Exception
	{
		TestTools.compareImages(GameScreen.screen.getScreenshot(), "test/sample_empty.png");
	}

	@Test
	public void testBlending() throws Exception
	{
		new EngineTestBlending().show();
		TestTools.compareImages(GameScreen.screen.getScreenshot(), "test/sample_blending.png");
	}

	@Test
	public void testNineParts() throws Exception
	{
		new EngineTestNinePart().show();
		TestTools.compareImages(GameScreen.screen.getScreenshot(), "test/sample_9p.png");
	}

	@Test
	public void testMirroring() throws Exception
	{
		new EngineTestMirroring().show();
		TestTools.compareImages(GameScreen.screen.getScreenshot(), "test/sample_mirroring.png");
	}

	@Test
	public void testBigSprite() throws Exception
	{
		GameEngine eng = new GameEngine();

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
		eng.addControl(isoViewer);

		eng.show();
		TestTools.compareImages(GameScreen.screen.getScreenshot(), "test/sample_bigsprite.png");
	}

	@Test
	public void testRowLayout() throws Exception
	{
		GameEngine eng = new GameEngine();

		Control row = new Control();
		row.setBounds(10, 10, 100, 100);
		row.setLayout(new RowLayout());
		eng.addControl(row);
		row.bgrColor = 0x7fffffff;

		{
			Control red = new Control();
			red.bgrColor = 0xffff0000;
			red.width = 10;
			RowLayout	.settings(red)
						.fillHeight();
			row.addControl(red);
		}
		{
			Control green = new Control();
			green.bgrColor = 0xff00ff00;
			green.height = 10;
			RowLayout	.settings(green)
						.fillWidth();
			row.addControl(green);
		}
		{
			Control blue = new Control();
			blue.bgrColor = 0xff0000ff;
			blue.height = 20;
			blue.width = 20;
			row.addControl(blue);
		}

		eng.show();
		TestTools.compareImages(GameScreen.screen.getScreenshot(), "test/sample_row.png");
	}

	@Test
	public void testColLayout() throws Exception
	{
		GameEngine eng = new GameEngine();

		Control row = new Control();
		row.setBounds(10, 10, 100, 100);
		row.setLayout(new ColumnLayout());
		eng.addControl(row);
		row.bgrColor = 0x7fffffff;

		{
			Control red = new Control();
			red.bgrColor = 0xffff0000;
			red.height = 10;
			ColumnLayout.settings(red)
						.fillWidth();
			row.addControl(red);
		}
		{
			Control green = new Control();
			green.bgrColor = 0xff00ff00;
			green.width = 10;
			ColumnLayout.settings(green)
						.fillHeight();
			row.addControl(green);
		}
		{
			Control blue = new Control();
			blue.bgrColor = 0xff0000ff;
			blue.height = 20;
			blue.width = 20;
			row.addControl(blue);
		}

		eng.show();
		TestTools.compareImages(GameScreen.screen.getScreenshot(), "test/sample_column.png");
	}
}
