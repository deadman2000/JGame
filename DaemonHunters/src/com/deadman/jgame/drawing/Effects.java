package com.deadman.jgame.drawing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Effects
{
	static Random random = new Random();

	public static void randomizeColors(BufferedImage img)
	{
		int width = img.getWidth();
		int height = img.getHeight();
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				img.setRGB(x, y, getRandomColorRGB(img.getRGB(x, y), 20));
	}

	static int getGaussian(double u, double m)
	{
		double d = random.nextGaussian() * m / u;
		if (d > m || d < -m) return getGaussian(u, m);
		return (int) d;
	}

	static int getRandomColorRGB(int rgb, int m)
	{
		int gauss = getGaussian(3.0, m);
		int r = ((rgb >> 16) & 0xFF) + gauss;
		if (r < 0) r = 0;
		if (r > 255) r = 255;

		int g = ((rgb >> 8) & 0xFF) + gauss;
		if (g < 0) g = 0;
		if (g > 255) g = 255;

		int b = (rgb & 0xFF) + gauss;
		if (b < 0) b = 0;
		if (b > 255) b = 255;

		return (b & 0xFF) | ((g & 0xFF) << 8) | ((r & 0xFF) << 16) | 0xff000000;
	}

	static int getRandomColorRGB(Color c, int m)
	{
		int gauss = getGaussian(3.0, m);
		int r = c.getRed() + gauss;
		if (r < 0) r = 0;
		if (r > 255) r = 255;

		int g = c.getGreen() + gauss;
		if (g < 0) g = 0;
		if (g > 255) g = 255;

		int b = c.getBlue() + gauss;
		if (b < 0) b = 0;
		if (b > 255) b = 255;

		return (b & 0xFF) | ((g & 0xFF) << 8) | ((r & 0xFF) << 16) | 0xff000000;
	}
}
