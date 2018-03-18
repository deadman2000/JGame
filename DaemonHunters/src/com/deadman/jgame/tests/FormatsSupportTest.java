package com.deadman.jgame.tests;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.deadman.jgame.drawing.GameScreen;

@RunWith(Parameterized.class)
public class FormatsSupportTest
{
	@Parameter
	public File file;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		GameScreen.initTesting();
	}

	@Test
	public void test() throws Exception
	{
		GameScreen.screen.engine = new EngineTestImage(file.getPath());
		TestTools.compareSubImage(file, GameScreen.screen.getScreenshot());
	}

	@Parameters
	public static Collection<Object[]> data()
	{
		Collection<Object[]> data = new ArrayList<Object[]>();

		File dir = new File("test/pngs");
		File[] files = dir.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String filename)
			{
				return filename.endsWith(".png");
			}
		});

		for (File f : files)
			data.add(new Object[] { f });

		return data;
	}
}
