package com.deadman.pixelgame;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Main
{
	public static void main(String[] args)
	{
		String pack = "pack.xml";
		if (args.length > 0)
		{
			pack = args[0];
		}

		try
		{
			Game.run(createWindow(), pack);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	public static JFrame window;
	
	private static GameScreen createWindow()
	{
		window = new JFrame(Game.TITLE);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		try
		{
			Image image = ImageIO.read(Main.class.getResource("/res/pixelize.png"));
			window.setIconImage(image);
		}
		catch (Exception e)
		{
		}

		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		window.setCursor(blankCursor);

		GameScreen gamePanel = new GameScreen();
		window.add(gamePanel);

		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);

		return gamePanel;
	}
}
