package com.deadman.jgame.drawing;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;

import com.deadman.dh.R;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.resources.ResourceManager;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.GLBuffers;

public class GameScreen extends JFrame implements GLEventListener
{
	public static int SCALE_FACTOR = 3;
	public static int GAME_WIDTH, GAME_HEIGHT;
	public static int START_WIDTH = 1200, START_HEIGHT = 900;
	private int screen_width, screen_height;

	private boolean isScreenInit = false;

	private GraphicsDevice gd;
	private GLCanvas canvas;
	public Point cursorPos;
	public static Drawable defaultCursor;

	public static GL2 gl;
	public static GameScreen screen;

	private GLU glu = new GLU();

	public static void init(String title)
	{
		screen = new GameScreen(title, false);
	}

	public static void initTesting()
	{
		if (screen != null) return;

		SCALE_FACTOR = 1;
		START_WIDTH = 200;
		START_HEIGHT = 200;
		screen = new GameScreen("Unit testing", true);
	}

	private GameScreen(String title, boolean testMode)
	{
		super(title);

		gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
								.getDefaultScreenDevice();

		createCanvas();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(START_WIDTH, START_HEIGHT));

		if (!testMode)
		{
			setIgnoreRepaint(true);
			try
			{
				Image image = ImageIO.read(new File("res/icon.png"));
				setIconImage(image);
			}
			catch (Exception e)
			{
			}

			BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Cursor blankCursor = Toolkit.getDefaultToolkit()
										.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
			setCursor(blankCursor);

			pack();
			setLocationRelativeTo(null);
			setVisible(true);

			requestFocus();

			addKeyListener(keyListener);
			canvas.addMouseListener(mouseListener);
			canvas.addMouseMotionListener(mouseListener);
			canvas.addMouseWheelListener(mouseListener);
		}
		else
		{
			setUndecorated(true);
			setResizable(false);
			pack();
			setVisible(true);
		}

		//cursor = CURSOR_DEFAULT;

		calcSize();
	}

	@Override
	public void doLayout()
	{
		super.doLayout();
		canvas.repaint();
	}

	void createCanvas()
	{
		if (canvas != null)
		{
			remove(canvas);
			canvas = null;
		}

		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		//caps.setStencilBits(8);
		canvas = new GLCanvas(caps);
		add(canvas);
		canvas.addGLEventListener(this);
		canvas.setFocusable(false);
	}

	private void calcSize()
	{
		if (screen_width == 0)
		{
			screen_width = canvas.getWidth();
			screen_height = canvas.getHeight();
		}
		GAME_WIDTH = screen_width / SCALE_FACTOR;
		GAME_HEIGHT = screen_height / SCALE_FACTOR;
		if (GameEngine.current != null)
			GameEngine.current.onSizeChanged();
	}

	// OpenGL

	public void redraw()
	{
		canvas.repaint();
	}

	@Override
	public void init(GLAutoDrawable drawable)
	{
		screen_width = canvas.getWidth();
		screen_height = canvas.getHeight();
		gl = drawable	.getGL()
						.getGL2();
		gl.setSwapInterval(1); // v-sync
	}

	private void initScreen()
	{
		gl.glViewport(0, 0, GAME_WIDTH * SCALE_FACTOR, GAME_HEIGHT * SCALE_FACTOR);

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glOrtho(0.0f, GAME_WIDTH, GAME_HEIGHT, 0.0f, 0.0f, 1.0f);

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		isScreenInit = true;
	}

	public GameEngine engine;

	@Override
	public void display(GLAutoDrawable drawable)
	{
		//long t = System.currentTimeMillis();
		boolean ts = _takeScreenshot;

		gl = drawable	.getGL()
						.getGL2();
		if (!isScreenInit) initScreen();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

		if (engine != null)
			engine.draw();
		else if (GameEngine.current != null)
			GameEngine.current.draw();

		drawCursor();

		if (ts)
			saveScreenshot();

		Picture.clean(gl);
		gl = null;

		//t = System.currentTimeMillis() - t;
		//if (t > Game.TICK_DELAY)			System.out.println("Big draw time: " + t);
		//setTitle("" + t);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		height = (height == 0) ? 1 : height;
		screen_width = width;
		screen_height = height;
		calcSize();
		gl = drawable	.getGL()
						.getGL2();
		disposeFX();
		initScreen();
	}

	@Override
	public void dispose(GLAutoDrawable drawable)
	{
		gl = drawable	.getGL()
						.getGL2();
		disposeFX();
		disposeShaders();
	}

	// Drawing

	public float br = 1f; // Яркость
	private float red = 1f; // Цвет
	private float green = 1f;
	private float blue = 1f;
	private float alpha = 1f; // Прозрачность

	public void resetColorFull()
	{
		br = 1f;
		red = 1f;
		green = 1f;
		blue = 1f;
		alpha = 1f;
		gl.glColor4f(1f, 1f, 1f, 1f);
	}

	public void resetColorMask()
	{
		red = 1f;
		green = 1f;
		blue = 1f;
		gl.glColor4f(br * red, br * green, br * blue, alpha);
	}

	public void setBrightness(float brightness)
	{
		br = brightness;
		gl.glColor4f(br * red, br * green, br * blue, alpha);
	}

	public void setColorMask(int c)
	{
		red = ((c >> 16) & 0xFF) / 255.f;
		green = ((c >> 8) & 0xFF) / 255.f;
		blue = (c & 0xFF) / 255.f;
		gl.glColor4f(br * red, br * green, br * blue, alpha);
	}

	public void setColorMask(float r, float g, float b, float a)
	{
		red = r;
		green = g;
		blue = b;
		alpha = a;
		gl.glColor4f(br * r, br * g, br * b, a);
	}

	public void setAlpha(float a)
	{
		if (alpha == a) return;
		alpha = a;
		gl.glColor4f(br * red, br * green, br * blue, alpha);
	}

	public void setDrawColor(int c)
	{
		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		gl.glColor4f(((c >> 16) & 0xFF) / 255.f, ((c >> 8) & 0xFF) / 255.f, (c & 0xFF) / 255.f, ((c >> 24) & 0xFF) / 255.f);
	}

	public void resetDrawColor()
	{
		gl.glColor4f(br * red, br * green, br * blue, alpha);
	}

	public void drawRect(int x, int y, int w, int h, int c)
	{
		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		gl.glColor4f(((c >> 16) & 0xFF) / 255.f, ((c >> 8) & 0xFF) / 255.f, (c & 0xFF) / 255.f, ((c >> 24) & 0xFF) / 255.f);
		gl.glRecti(x, y, x + w, y + h);
		gl.glColor4f(br * red, br * green, br * blue, alpha);
	}

	private boolean _clipped = false;

	public void enableClipping(int x, int y, int w, int h)
	{
		if (w <= 0 || h <= 0) return;
		if (_clipped) System.err.println("Multiple scissor test!!!");

		x *= SCALE_FACTOR;
		y *= SCALE_FACTOR;
		w *= SCALE_FACTOR;
		h *= SCALE_FACTOR;
		y += h;

		y = canvas.getHeight() / SCALE_FACTOR * SCALE_FACTOR - y;

		gl.glEnable(GL.GL_SCISSOR_TEST);
		gl.glScissor(x, y, w, h);
		_clipped = true;
	}

	public void disableClipping()
	{
		gl.glDisable(GL.GL_SCISSOR_TEST);
		_clipped = false;
	}

	public void drawCursor()
	{
		if (cursorPos != null)
		{
			if (GameEngine.current != null && GameEngine.current.cursor != null)
				GameEngine.current.cursor.drawAt(cursorPos);
			else if (defaultCursor != null)
				defaultCursor.drawAt(cursorPos);
		}
	}

	// FX

	private int colorTexture = 0;
	private int renderFBO = 0;

	private void initFX()
	{
		disposeFX();

		renderFBO = genFrameBuffer();
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, renderFBO);

		colorTexture = genTexture();
		gl.glBindTexture(GL2.GL_TEXTURE_2D, colorTexture);

		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, GAME_WIDTH * SCALE_FACTOR, GAME_HEIGHT * SCALE_FACTOR, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, colorTexture, 0);

		// Чтобы добавить stencil: http://stackoverflow.com/questions/26519463/opengl-es-2-0-gl-points-and-stencil-buffer

		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);

		int status = gl.glCheckFramebufferStatus(GL2.GL_DRAW_FRAMEBUFFER);
		if (status != GL2.GL_FRAMEBUFFER_COMPLETE)
			System.err.println("FBO status: " + status);

		printError();
	}

	public void beginFX()
	{
		if (colorTexture == 0) initFX();

		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, renderFBO);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
	}

	public void endFX(ScreenEffect effect)
	{
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

		if (shaderProgram == 0) initShaders();
		gl.glUseProgram(shaderProgram);

		gl.glBindTexture(GL2.GL_TEXTURE_2D, colorTexture);

		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "colorTexture"), 0);
		//gl.glUniform1f(gl.glGetUniformLocation(shaderprogram, "time"), (float) (System.nanoTime() & 0xFFFFFF) / 0xFFFFFF);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "width"), GAME_WIDTH);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "height"), GAME_HEIGHT);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "effect"), effect.id);

		gl.glRecti(-1, -1, 1, 1);

		gl.glUseProgram(0);

		printError();
	}

	public int genFrameBuffer()
	{
		int[] ids = new int[1];
		gl.glGenFramebuffers(1, ids, 0);
		return ids[0];
	}

	public int genRenderBuffer()
	{
		int[] ids = new int[1];
		gl.glGenRenderbuffers(1, ids, 0);
		return ids[0];
	}

	public int genTexture()
	{
		int[] ids = new int[1];
		gl.glGenTextures(1, ids, 0);
		return ids[0];
	}

	private void disposeFX()
	{
		if (colorTexture != 0)
		{
			gl.glDeleteTextures(1, new int[] { colorTexture }, 0);
			colorTexture = 0;
		}
		if (renderFBO != 0)
		{
			gl.glDeleteFramebuffers(1, new int[] { renderFBO }, 0);
			renderFBO = 0;
		}
	}

	public void printError()
	{
		int errorCode = gl.glGetError();
		if (errorCode == 0) return;

		String errorStr = glu.gluErrorString(errorCode);
		System.err.println("#" + errorCode + " : " + errorStr);
	}

	// Shaders

	private int vertexShader;
	private int fragmentShader;
	private int shaderProgram;

	private void initShaders()
	{
		disposeShaders();

		try
		{
			attachShaders();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void disposeShaders()
	{
		if (shaderProgram != 0)
		{
			gl.glDeleteProgram(shaderProgram);
			gl.glDeleteShader(vertexShader);
			gl.glDeleteShader(fragmentShader);
			shaderProgram = 0;
			vertexShader = 0;
			fragmentShader = 0;
		}
	}

	public String[] loadShader(int id)
	{
		ArrayList<String> lines = new ArrayList<String>();
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(ResourceManager.getInputStream(id)));
			String line;
			while ((line = br.readLine()) != null)
			{
				lines.add(line);
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return lines.toArray(new String[lines.size()]);
	}

	void attachShaders() throws Exception
	{
		shaderProgram = gl.glCreateProgram();

		vertexShader = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
		String[] shaderSrc = loadShader(R.glsl.vertex_glsl);
		gl.glShaderSource(vertexShader, shaderSrc.length, shaderSrc, null, 0);
		gl.glCompileShader(vertexShader);
		gl.glAttachShader(shaderProgram, vertexShader);

		fragmentShader = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		shaderSrc = loadShader(R.glsl.fragment_glsl);
		gl.glShaderSource(fragmentShader, shaderSrc.length, shaderSrc, null, 0);
		gl.glCompileShader(fragmentShader);
		gl.glAttachShader(shaderProgram, fragmentShader);

		gl.glLinkProgram(shaderProgram);
		gl.glValidateProgram(shaderProgram);

		int[] result = new int[1];
		gl.glGetProgramiv(shaderProgram, GL2.GL_LINK_STATUS, result, 0);
		if (result[0] != 1)
		{
			IntBuffer intBuffer = IntBuffer.allocate(1);
			gl.glGetProgramiv(shaderProgram, GL2.GL_INFO_LOG_LENGTH, intBuffer);
			int size = intBuffer.get(0);
			System.err.print("Program link error: ");
			if (size > 0)
			{
				ByteBuffer byteBuffer = ByteBuffer.allocate(size);
				gl.glGetProgramInfoLog(shaderProgram, size, intBuffer, byteBuffer);
				for (byte b : byteBuffer.array())
					System.err.print((char) b);
			}
			else
				System.err.println("Shader Unknown error");
			//System.exit(1);
		}
	}

	// Mouse Events

	private Point screenToGame(Point p)
	{
		p.x /= SCALE_FACTOR;
		p.y /= SCALE_FACTOR;
		return p;
	}

	//public static boolean KEY_SHIFT = false;

	private KeyListener keyListener = new KeyAdapter()
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			//System.out.println("Key pressed: " + e.getKeyCode() + " " + e.getKeyChar());
			//if (e.getKeyCode() == KeyEvent.VK_SHIFT) KEY_SHIFT = true;

			if (GameEngine.current != null)
				GameEngine.current.onKeyPressed(e);

			if (e.isConsumed()) return;

			int mod = e.getModifiers();
			if (mod != 0) return;

			switch (e.getKeyCode())
			{
				case KeyEvent.VK_F1:
					Picture.reload();
					//GameScreen.sprites.clear();
					break;
				case KeyEvent.VK_F2:
					System.gc();
					break;

				case KeyEvent.VK_F4:
					if (isFullscreen)
						setWindowed();
					else
						setFullscreen();
					break;
				case KeyEvent.VK_ESCAPE: // Esc
					if (GameEngine.current != null)
						GameEngine.current.close();
					break;

				case KeyEvent.VK_SUBTRACT:
					int ws = getWidth() / SCALE_FACTOR;
					int hs = getHeight() / SCALE_FACTOR;
					SCALE_FACTOR = Math.max(1, SCALE_FACTOR - 1);
					if (!isFullscreen && getExtendedState() != MAXIMIZED_BOTH)
						setSize(ws * SCALE_FACTOR, hs * SCALE_FACTOR);
					else
						calcSize();
					isScreenInit = false;
					break;
				case KeyEvent.VK_ADD:
					int wa = getWidth() / SCALE_FACTOR;
					int ha = getHeight() / SCALE_FACTOR;
					SCALE_FACTOR++;
					if (!isFullscreen && getExtendedState() != MAXIMIZED_BOTH)
						setSize(wa * SCALE_FACTOR, ha * SCALE_FACTOR);
					else
						calcSize();
					isScreenInit = false;
					break;

				case KeyEvent.VK_F12:
					takeScreenshot();
					break;

				default:
					break;
			}
		}

		public void keyReleased(KeyEvent e)
		{
			//if (e.getKeyCode() == KeyEvent.VK_SHIFT) KEY_SHIFT = false;

			if (GameEngine.current != null)
				GameEngine.current.onKeyReleased(e);

			if (e.isConsumed()) return;
		}
	};

	private boolean _takeScreenshot;
	private String _screenshotPath;
	private Object _screenWaiting;
	private BufferedImage _lastScreenshot;

	public void takeScreenshot()
	{
		_takeScreenshot = true;
		_screenshotPath = "D:\\Download\\texture.png";
	}

	public void waitScreenshot(String path) throws Exception
	{
		BufferedImage image = getScreenshot();
		saveImage(image, path);
	}

	public BufferedImage getScreenshot() throws Exception
	{
		Object w = new Object();
		_screenWaiting = w;
		_screenshotPath = null;
		_lastScreenshot = null;

		_takeScreenshot = true;
		canvas.repaint();

		BufferedImage result = null;
		try
		{
			synchronized (w)
			{
				w.wait(5000);
			}
			if (_lastScreenshot == null) throw new Exception("Timeout");

			result = _lastScreenshot;
		}
		finally
		{
			_screenWaiting = null;
			_lastScreenshot = null;
		}

		return result;
	}

	private BufferedImage getScreen()
	{
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		try
		{
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			ByteBuffer buffer = GLBuffers.newDirectByteBuffer(width * height * 4);
			gl.glReadBuffer(GL.GL_BACK);
			gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);

			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int r = buffer.get() & 0xff;
					int g = buffer.get() & 0xff;
					int b = buffer.get() & 0xff;
					buffer.get(); // consume alpha

					image.setRGB(x, height - y - 1, (r << 16) | (g << 8) | b);
				}
			}
			return image;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	private void saveScreenshot()
	{
		try
		{
			BufferedImage image = getScreen();

			if (_screenshotPath != null)
			{
				saveImage(image, _screenshotPath);
				System.out.println("Screenshot in " + _screenshotPath);
			}
			else
			{
				_lastScreenshot = image;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		_takeScreenshot = false;
		if (_screenWaiting != null)
		{
			synchronized (_screenWaiting)
			{
				_screenWaiting.notify();
			}
		}
	}

	private void saveImage(BufferedImage image, String path) throws IOException
	{
		ImageIO.write(image, "png", new File(path));
	}

	private MouseInputAdapter mouseListener = new MouseInputAdapter()
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			if (GameEngine.current != null)
				GameEngine.current.onMousePressed(screenToGame(e.getPoint()), e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (GameEngine.current != null)
				GameEngine.current.onMouseReleased(screenToGame(e.getPoint()), e);
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			Point p = screenToGame(e.getPoint());
			/*if (p.x < 0)
				p.x = 0;
			else if (p.x >= GAME_WIDTH)
				p.x = GAME_WIDTH - 1;
			if (p.y < 0)
				p.y = 0;
			else if (p.y >= GAME_HEIGHT)
				p.y = GAME_HEIGHT - 1;*/

			if (cursorPos != null && cursorPos.equals(p))
				return;

			cursorPos = p;

			if (GameEngine.current != null)
				GameEngine.current.onMouseMoved(p, e);
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			mouseMoved(e);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			if (GameEngine.current != null)
				GameEngine.current.onMouseWheel(screenToGame(e.getPoint()), e);
		}
	};

	// Fullscreen

	public boolean isFullscreen = false;
	private int oldWidth, oldHeight;

	public void setWindowed()
	{
		// http://gpsnippets.blogspot.ru/2007/08/toggle-fullscreen-mode.html
		isFullscreen = false;

		setVisible(false);
		dispose();

		setUndecorated(false);
		gd.setFullScreenWindow(null);
		setSize(oldWidth, oldHeight);
		setLocationRelativeTo(null);

		setVisible(true);
		Picture.reload();
		requestFocus();
		canvas.requestFocus();
	}

	public void setFullscreen()
	{
		isFullscreen = true;

		oldWidth = getWidth();
		oldHeight = getHeight();

		setVisible(false);
		dispose();

		setUndecorated(true);
		//setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		gd.setFullScreenWindow(this);

		setVisible(true);
		Picture.reload();
		requestFocus();
		canvas.requestFocus();
	}

	private static final long serialVersionUID = -7561471385790738136L;

	static
	{
		System.setProperty("sun.java2d.d3d", "False"); // To fix a black-fullscreen-bug
	}

}
