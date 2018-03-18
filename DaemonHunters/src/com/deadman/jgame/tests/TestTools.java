package com.deadman.jgame.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class TestTools
{
	public static void compareImages(BufferedImage img, String path) throws Exception
	{
		assertNotNull(img);
		try
		{
			compareImages(img, ImageIO.read(new File(path)));
		}
		catch (Exception e)
		{
			String fileName = getFailedPath();
			save(img, fileName);
			fail(path + " != " + fileName + " : " + e.getMessage());
		}
	}

	public static void compareImages(BufferedImage img1, BufferedImage img2) throws Exception
	{
		assertNotNull(img1);
		assertNotNull(img2);

		if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
			throw new Exception("Different image sizes");

		for (int x = 0; x < img1.getWidth(); x++)
			for (int y = 0; y < img1.getHeight(); y++)
				if (img1.getRGB(x, y) != img2.getRGB(x, y)) throw new Exception("Different images");
	}

	public static void compareSubImage(File file, BufferedImage img) throws Exception
	{
		try
		{
			compareSubImages(ImageIO.read(file), img);
		}
		catch (Exception e)
		{
			String fileName = getFailedPath();
			save(img, fileName);
			fail(file + " != " + fileName + " : " + e.getMessage());
		}
	}

	public static void compareSubImages(BufferedImage imgSmall, BufferedImage imgBig) throws Exception
	{
		if (imgSmall.getWidth() > imgBig.getWidth() || imgSmall.getHeight() > imgBig.getWidth())
			throw new Exception("Subimage is bigger");

		imgSmall = repaint(imgSmall);

		for (int x = 0; x < imgSmall.getWidth(); x++)
			for (int y = 0; y < imgSmall.getHeight(); y++)
			{
				int c1 = imgBig.getRGB(x, y);
				int c2 = imgSmall.getRGB(x, y);
				if (colorCompare(c1, c2, 1))
				{
					String smallPath = getFailedPath();
					save(imgSmall, smallPath);
					throw new Exception("Different images (" + c1 + " != " + c2 + " at " + x + ":" + y + ") " + smallPath);
				}
			}
	}

	public static boolean colorCompare(int c1, int c2, int delta)
	{
		if (Math.abs(((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff)) > delta)
			return true;

		if (Math.abs(((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff)) > delta)
			return true;

		if (Math.abs((c1 & 0xff) - (c2 & 0xff)) > delta)
			return true;

		return false;
	}

	public static void save(BufferedImage image, String path)
	{
		try
		{
			ImageIO.write(image, "png", new File(path));
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}

	private static BufferedImage repaint(BufferedImage src)
	{
		BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setComposite(AlphaComposite.SrcOver.derive(1f));
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		g.drawImage(src, 0, 0, null);
		return img;
	}

	static int _failedInd = 0;

	public static String getFailedPath()
	{
		return "test/fails/failed_" + (_failedInd++) + ".png";
	}

	static
	{
		clearFails();
	}

	private static void clearFails()
	{
		File dir = new File("test/fails/");
		for (File f : dir.listFiles())
			f.delete();
	}
}
