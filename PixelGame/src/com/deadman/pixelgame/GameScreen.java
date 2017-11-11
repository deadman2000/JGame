package com.deadman.pixelgame;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class GameScreen extends JPanel
{
	public static final int GAME_WIDTH = 320;
	public static final int GAME_HEIGHT = 200;
	public static final int SCENE_HEIGHT = 145;
	public static final int SCALE_FACTOR = 4;

	public static final int PALETTE_SIZE = 256;

	public int[] palette = new int[PALETTE_SIZE];
	public byte[] pixels = new byte[GAME_WIDTH * GAME_HEIGHT];
	private byte[] background = new byte[GAME_WIDTH * GAME_HEIGHT];
	private ArrayList<Palette> loadedPal = new ArrayList<Palette>();

	public Scene scene;
	public Sprite font;

	public List<IDrawable> drawable = new ArrayList<IDrawable>();
	public GameCursor cursor;

	public GameScreen()
	{
		for (int i = 0; i < palette.length; i++)
		{
			palette[i] = 0xFF000000;
		}
	}

	public void init()
	{
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseMotion);
		addMouseWheelListener(mouseWheelListener);
	}

	public static boolean T_TOP;
	public static float T_XSCALE = 1.f, T_YSCALE = 1.f;

	public void setPixel(int wx, int wy, int lx, int ly, int ax, int ay, byte color)
	{
		int x = (int) (wx + (lx - ax) * T_XSCALE);
		int y = (int) (wy + (ly - ay) * T_YSCALE);
		setPixel(x, y, color);
	}

	public void setPixel(int x, int y, byte color)
	{
		if (x < 0 || x >= GAME_WIDTH || y < 0 || y >= GAME_HEIGHT) return;

		if (T_TOP || !scene.mask.isOnTop(x, y))
			pixels[x + y * GAME_WIDTH] = color;
	}

	public void loadPalette(Palette pal)
	{
		if (loadedPal.contains(pal)) return;

		if (loadedPal.size() == 0)
			pal.offset = 0;
		else
			pal.offset = loadedPal.get(loadedPal.size() - 1).getNextOffset();

		loadedPal.add(pal);
		for (int i = 0; i < pal._colors.length; i++)
			this.palette[pal.offset + i] = pal._colors[i];
	}

	public void unloadPalette(Palette pal)
	{
		int ind = loadedPal.indexOf(pal);
		if (ind == -1) return;

		loadedPal.remove(ind);

		for (int i = ind; i < loadedPal.size(); i++)
		{
			int newOff;
			if (i == 0)
				newOff = 0;
			else
				newOff = loadedPal.get(i - 1).getNextOffset();

			loadedPal.get(i).offset = newOff;
		}
	}

	private void loadBackground(byte[] bgr, int offset)
	{
		System.arraycopy(bgr, 0, background, offset, bgr.length);
	}

	public void loadGui(Background bgr)
	{
		loadBackground(bgr.pixels, GAME_WIDTH * SCENE_HEIGHT);
	}

	public void loadScene(Scene s)
	{
		scene = s;
		loadBackground(scene.background.pixels, 0);
	}

	// Paint

	private Image imgScreen = null;
	private int[] buff = new int[GAME_WIDTH * GAME_HEIGHT];

	protected void paintComponent(Graphics g)
	{
		if (imgScreen != null) g.drawImage(imgScreen, 0, 0, GAME_WIDTH * SCALE_FACTOR, GAME_HEIGHT * SCALE_FACTOR, this);
	}

	public void paint()
	{
		if (background != null)
		{
			System.arraycopy(background, 0, pixels, 0, background.length);
		}

		T_TOP = false;

		for (IDrawable o : drawable)
			o.draw();

		T_TOP = true;

		for (int i = 0; i < _speaks.size(); i++)
		{
			SpeakText t = _speaks.get(i);
			t.draw();
			if (t.elapsed())
			{
				_speaks.remove(i);
				i--;
			}
		}

		if (cursor != null) cursor.draw();

		for (int y = 0; y < GAME_HEIGHT; y++)
			for (int x = 0; x < GAME_WIDTH; x++)
				buff[x + y * GAME_WIDTH] = palette[pixels[x + y * GAME_WIDTH] & 0xFF];

		imgScreen = createImage(new MemoryImageSource(GAME_WIDTH, GAME_HEIGHT, buff, 0, GAME_WIDTH));
		repaint();
	}

	// Speak

	ArrayList<SpeakText> _speaks = new ArrayList<SpeakText>();

	public SpeakText showText(String text, int x, int y)
	{
		SpeakText t = new SpeakText(text, x, y);
		_speaks.add(t);
		return t;
	}

	public void drawChar(Character c, int x, int y)
	{
		font.frames[c - 'A'].drawAt(x, y);
	}

	// Animation

	private List<Animation> _animations = new ArrayList<Animation>();

	public void playAnimation(Animation anim)
	{
		_animations.add(anim);
	}

	public void stopAnimation(Animation anim)
	{
		_animations.remove(anim);
	}

	// Mouse Events

	Point screenToGame(Point p)
	{
		p.x /= SCALE_FACTOR;
		p.y /= SCALE_FACTOR;
		return p;
	}

	MouseListener mouseListener = new MouseListener()
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			onMouseClicked(screenToGame(e.getPoint()), e.getButton());
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}
	};

	private void onMouseClicked(Point p, int button)
	{
		if (button == 1)
		{
			// GUI
			if (isGUI(p))
			{
			}
			else
			{
				// Hit sprites
				for (IDrawable d : drawable)
				{
					if (d.contains(p) && (d instanceof IHitable))
					{
						if (((IHitable) d).hit(p))
							return;
					}
				}

				// Hit scene actions

				// Moving
				if (!scene.mask.isMovable(p))
					p = findMovable(p);
				if (p != null)
					Game.mainCharacter.moveTo(p);
			}
		}
	}

	private boolean isGUI(Point p)
	{
		return p.y >= GameScreen.SCENE_HEIGHT;
	}

	private Point findMovable(Point p)
	{
		int yT = -1;
		for (int y = p.y - 1; y >= 0; y--)
		{
			if (scene.mask.isMovable(p.x, y))
			{
				yT = y;
				break;
			}
		}

		int yB = -1;
		for (int y = p.y + 1; y < GameScreen.SCENE_HEIGHT; y++)
		{
			if (scene.mask.isMovable(p.x, y))
			{
				yB = y;
				break;
			}
		}

		if (yB == -1) return new Point(p.x, yT);
		if (yT == -1) return new Point(p.x, yB);

		if (p.y - yT < yB - p.y)
			return new Point(p.x, yT);
		else
			return new Point(p.x, yB);
	}

	MouseMotionListener mouseMotion = new MouseMotionListener()
	{
		@Override
		public void mouseMoved(MouseEvent e)
		{
			Point p = screenToGame(e.getPoint());
			if (p.x < 0)
				p.x = 0;
			else if (p.x >= GAME_WIDTH)
				p.x = GAME_WIDTH - 1;
			if (p.y < 0)
				p.y = 0;
			else if (p.y >= GAME_HEIGHT)
				p.y = GAME_HEIGHT - 1;

			if (cursor != null)
			{
				cursor.setPosition(p.x, p.y);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			mouseMoved(e);
		}
	};

	MouseWheelListener mouseWheelListener = new MouseWheelListener()
	{
		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			cursor.setState(1 - cursor.getState());
		}
	};

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(GAME_WIDTH * SCALE_FACTOR, GAME_HEIGHT * SCALE_FACTOR);
	}

	private static final long serialVersionUID = -7561471385790738136L;
}
