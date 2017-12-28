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
import com.deadman.jgame.ui.FillLayout;
import com.deadman.jgame.ui.Layout;
import com.deadman.jgame.ui.LoadingScreen;
import com.deadman.jgame.ui.RelativeLayout;

public class GameEngine
{
	protected Random rnd = new Random();
	protected GameScreen screen = GameScreen.screen;
	public GameEngine parent;
	public Drawable cursor;

	public long ticks = 0;

	private Control content;
	private Control root;

	public GameEngine()
	{
		root = content = new Control();
		root.name = "Root " + this;
		setLayout(new RelativeLayout());
		onSizeChanged();
	}

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
		root.setBounds(0, 0, GameScreen.GAME_WIDTH, GameScreen.GAME_HEIGHT);
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

	public void setLayout(Layout layout)
	{
		content.setLayout(layout);
	}

	public void addControl(Control c)
	{
		content.addControl(c);
	}

	public boolean containsControl(Control control)
	{
		return content.containsControl(control);
	}
	
	// Overlay

	private Control _overlay;
	private IOverlayListener _overlay_listener;

	public void showOverlay()
	{
		showOverlay(null);
	}

	public void showOverlay(IOverlayListener listener)
	{
		if (_overlay == null)
		{
			_overlay = new Control();
			_overlay.bgrColor = 0x7f000000;
			//_overlay.setLayout(new RelativeLayout());
			_overlay.setLayout(new RelativeLayout());
			_overlay.name = "overlay";
			_overlay.addControlListener(new ControlListener()
			{
				public void onPressed(Object sender, Point p, MouseEvent e)
				{
					if (_overlay_listener != null)
						_overlay_listener.overlayPressed();
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
			
			root = new Control();
			onSizeChanged();
			
			root.setLayout(new FillLayout());
			root.addControl(content);
			root.addControl(_overlay);
		}

		_overlay_listener = listener;
		_overlay.visible = true;
	}

	public void hideOverlay()
	{
		if (_overlay != null)
		{
			_overlay.visible = false;
			_overlay_listener = null;
		}
	}
	
	public Control overlay()
	{
		return _overlay;
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
