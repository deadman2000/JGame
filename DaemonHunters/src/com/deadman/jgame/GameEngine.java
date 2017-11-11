package com.deadman.jgame;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Random;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.LoadingScreen;

public class GameEngine
{
	protected Random rnd = new Random();
	protected GameScreen screen = GameScreen.screen;
	public GameEngine parent;
	public Drawable cursor;

	public long ticks = 0;

	public Control root = new Control().fillParent();

	public void tick()
	{
	}

	public void show()
	{
		if (GameLoop.engine instanceof LoadingScreen)
			parent = GameLoop.engine.parent;
		else
			parent = GameLoop.engine;

		onSizeChanged();
		GameLoop.engine = this;
	}

	public void close()
	{
		if (parent != null)
		{
			GameLoop.engine = parent;
			parent.onSizeChanged();
			parent.onChildClosed(this);
		}
		//else Game.quit();

		destroy();
	}

	protected void onChildClosed(GameEngine child)
	{
	}

	public final void destroy()
	{
		root.destroy();
		onDestroy();
	}

	protected void onDestroy()
	{
	}

	protected void delay(int millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
		}
	}

	// Drawing

	public void draw()
	{
		root.draw();
		root.submitChilds();
	}

	// Resize

	public void onSizeChanged()
	{
		root.onScreenChanged();
	}

	// Keyboard

	public void onKeyPressed(KeyEvent e)
	{
		root.pressKey(e);
	}

	public void onKeyReleased(KeyEvent e)
	{
	}

	// Mouse

	public void onMousePressed(Point p, MouseEvent e)
	{
		root.pressMouse(p, e);
	}

	public void onMouseMoved(Point p, MouseEvent e)
	{
		root.moveMouse(p, e);
	}

	public void onMouseReleased(Point p, MouseEvent e)
	{
		root.releaseMouse(p, e);
	}

	/*public void onMouseClicked(Point p, MouseEvent e)
	{
		root.clickMouse(p, e);
	}*/

	public void onMouseWheel(Point p, MouseWheelEvent e)
	{
		root.onMouseWheel(p, e);
	}

	// UI

	public void addControl(Control c)
	{
		root.addControl(c);
	}

	// Overlay

	private Control overlay;
	private IOverlayListener overlay_listener;

	public void showOverlay()
	{
		showOverlay(null);
	}

	public void showOverlay(IOverlayListener listener)
	{
		if (overlay == null)
		{
			overlay = new Control();
			overlay.bgrColor = 0x7f000000;
			overlay.name = "overlay";
			overlay.addControlListener(new ControlListener()
			{
				public void onPressed(Object sender, Point p, MouseEvent e)
				{
					if (overlay_listener != null)
						overlay_listener.overlayPressed();
					e.consume();
				};

				public void onReleased(Object sender, Point p, MouseEvent e)
				{
					e.consume();
				};

				public void onMouseMove(Control control, Point p, MouseEvent e)
				{
					e.consume();
				};

				public void onClick(Object sender, Point p, MouseEvent e)
				{
					e.consume();
				};
			});
		}

		overlay_listener = listener;

		overlay.setBounds(0, 0, GameScreen.GAME_WIDTH, GameScreen.GAME_HEIGHT, Control.ANCHOR_ALL);
		addControl(overlay);
	}

	public void hideOverlay()
	{
		overlay.remove();
		overlay_listener = null;
	}

	// Resources

	public static Drawable getDrawable(int id)
	{
		return Drawable.get(id);
	}

	public static GameFont getFont(int id)
	{
		return GameFont.get(id);
	}
	
	public static GameFont getFont(int id, int... pal)
	{
		return GameFont.get(id, pal);
	}
}
