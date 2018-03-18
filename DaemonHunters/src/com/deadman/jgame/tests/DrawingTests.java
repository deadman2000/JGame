package com.deadman.jgame.tests;

import org.junit.BeforeClass;
import org.junit.Test;

import com.deadman.jgame.drawing.GameScreen;

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
		new EngineTestBigSprite().show();
		TestTools.compareImages(GameScreen.screen.getScreenshot(), "test/sample_bigsprite.png");
	}
}
