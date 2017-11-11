package com.deadman.dh;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.deadman.dh.isometric.IsoBigSprite;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.drawing.Mirrored;
import com.deadman.jgame.drawing.PicPart;

public class TestEngine extends GameEngine
{
	IsoBigSprite s;
	Drawable d1, d2, d3;
	private static final int red = 0x5f0000;

	public TestEngine()
	{
		GameResources.init();
		s = (IsoBigSprite) GameResources.main.getIso("Cabinet");
		byte st = 17;
		d1 = s.getPart((byte) 0, (byte) 0, st);
		d2 = s.getPart((byte) 1, (byte) 0, st);
		d3 = Drawable.get(R.iso.Cabinet_0_1);
	}

	@Override
	public void draw()
	{
		super.draw();

		drawPic(10, 50, d1);

		Mirrored m = (Mirrored) d2;
		drawPic(100, 50, m);

		PicPart p = (PicPart) m.original;
		drawPic(150, 50, p);

		p.original.drawAt(80, 50, 12, 44, -p.original.width + 12, 0, -12, 44);

		drawPic(170, 50, p.original);

		//drawPic(250, 50, d3);

		//System.out.println(d2.toString());
	}

	private void drawPic(int x, int y, Drawable d)
	{
		screen.drawRect(x - d.anchorX, y - d.anchorY, d.width, d.height, red);
		d.drawAt(x, y);
		screen.drawRect(x, y, 1, 1, 0xffff0000);
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
