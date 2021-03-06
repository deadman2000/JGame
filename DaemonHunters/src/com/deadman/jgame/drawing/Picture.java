package com.deadman.jgame.drawing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.resources.XCF;
import com.jogamp.nativewindow.util.PixelFormat;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLPixelBuffer.GLPixelAttributes;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

public class Picture extends Drawable
{
	private String _fileName;
	private BufferedImage _img;
	private Texture t;
	private boolean notFound = false;

	public Picture()
	{
	}

	public Picture(BufferedImage image)
	{
		this();

		_img = image;
		width = image.getWidth();
		height = image.getHeight();
	}

	public Picture(BufferedImage image, int anchorX, int anchorY)
	{
		this(image);
		this.anchorX = anchorX;
		this.anchorY = anchorY;
	}

	@Override
	public String toString()
	{
		if (_fileName != null)
			return String.format("PIC %s (%d:%d)", _fileName, anchorX, anchorY);
		return String.format("PIC (%d:%d)", anchorX, anchorY);
	}

	// Drawing

	@Override
	protected void draw(int x, int y)
	{
		GL2 gl = GameScreen.gl;
		int x2 = x + width;
		int y2 = y + height;

		getTexture().bind(gl);

		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2i(x, y2);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2i(x2, y2);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2i(x2, y);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2i(x, y);
		gl.glEnd();
	}

	@Override
	protected void drawMH(int x, int y)
	{
		GL2 gl = GameScreen.gl;
		int x2 = x + width;
		int y2 = y + height;

		getTexture().bind(gl);

		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2i(x2, y2);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2i(x, y2);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2i(x, y);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2i(x2, y);
		gl.glEnd();
	}

	@Override
	protected void drawMV(int x, int y)
	{
		GL2 gl = GameScreen.gl;
		int x2 = x + width;
		int y2 = y + height;

		getTexture().bind(gl);

		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2i(x, y2);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2i(x2, y2);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2i(x2, y);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2i(x, y);
		gl.glEnd();
	}

	@Override
	protected void drawRR(int x, int y)
	{
		GL2 gl = GameScreen.gl;
		int x2 = x + width;
		int y2 = y + height;

		getTexture().bind(gl);

		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2i(x, y2);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2i(x2, y2);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2i(x2, y);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2i(x, y);
		gl.glEnd();
	}

	@Override
	protected void drawRL(int x, int y)
	{
		GL2 gl = GameScreen.gl;
		int x2 = x + width;
		int y2 = y + height;

		getTexture().bind(gl);

		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2i(x, y2);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2i(x2, y2);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2i(x2, y);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2i(x, y);
		gl.glEnd();
	}

	@Override
	protected void draw(int x, int y, int w, int h)
	{
		GL2 gl = GameScreen.gl;
		int x2 = x + w;
		int y2 = y + h;

		getTexture().bind(gl);

		float tr = (float) w / width;
		float tb = (float) h / height;

		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, tb);
		gl.glVertex2i(x, y2);
		gl.glTexCoord2f(tr, tb);
		gl.glVertex2i(x2, y2);
		gl.glTexCoord2f(tr, 0);
		gl.glVertex2i(x2, y);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2i(x, y);
		gl.glEnd();
	}

	@Override
	protected void fill(int x, int y, int w, int h)
	{
		GL2 gl = GameScreen.gl;
		int x2 = x + w;
		int y2 = y + h;

		float tr = (float) w / width;
		float tb = (float) h / height;

		getTexture().bind(gl);

		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, tb);
		gl.glVertex2i(x, y2);
		gl.glTexCoord2f(tr, tb);
		gl.glVertex2i(x2, y2);
		gl.glTexCoord2f(tr, 0);
		gl.glVertex2i(x2, y);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2i(x, y);
		gl.glEnd();
	}

	@Override
	protected void draw(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		GL2 gl = GameScreen.gl;
		int tx2 = tx + tw;
		int ty2 = ty + th;

		// Координаты текстуры
		float tl = (float) fx / width;
		float tr = (float) (fx + w) / width;
		float tt = (float) fy / height;
		float tb = (float) (fy + h) / height;

		getTexture().bind(gl);

		gl.glBegin(GL2.GL_QUADS);

		gl.glTexCoord2f(tl, tb);
		gl.glVertex2i(tx, ty2);

		gl.glTexCoord2f(tr, tb);
		gl.glVertex2i(tx2, ty2);

		gl.glTexCoord2f(tr, tt);
		gl.glVertex2i(tx2, ty);

		gl.glTexCoord2f(tl, tt);
		gl.glVertex2i(tx, ty);

		gl.glEnd();
	}

	@Override
	protected void fill(int tx, int ty, int tw, int th, int fx, int fy, int w, int h)
	{
		GL2 gl = GameScreen.gl;
		int mx = tx + tw;
		int my = ty + th;
		int tty, tty2, tx2;

		// Координаты текстуры
		float tl = (float) fx / width;
		float tr = (float) (fx + w) / width;
		float tt = (float) fy / height;
		float tb = (float) (fy + h) / height;

		getTexture().bind(gl);

		gl.glBegin(GL2.GL_QUADS);

		while (tx < mx)
		{
			tx2 = tx + w;
			if (tx2 > mx)
			{
				tr = (float) (fx + mx - tx) / width;
				tx2 = mx;
			}

			tty = ty;
			while (tty < my)
			{
				tty2 = tty + h;
				if (tty2 > my)
				{
					tb = (float) (fy + my - tty) / height;
					tty2 = my;
				}

				gl.glTexCoord2f(tl, tb);
				gl.glVertex2i(tx, tty2);

				gl.glTexCoord2f(tr, tb);
				gl.glVertex2i(tx2, tty2);

				gl.glTexCoord2f(tr, tt);
				gl.glVertex2i(tx2, tty);

				gl.glTexCoord2f(tl, tt);
				gl.glVertex2i(tx, tty);

				tty += h;
			}
			tb = (float) (fy + h) / height;

			tx += w;
		}

		gl.glEnd();
	}

	protected boolean contains(int x, int y)
	{
		if (_img == null || x < 0 || y < 0 || x >= width || y >= height) return false;
		return (_img.getRGB(x, y) >>> 24) > 0;
	}

	// Resources

	private static BufferedImage loadImage(String fileName, boolean warnings) throws IOException
	{
		File f = new File(fileName);
		if (!f.exists())
			throw new FileNotFoundException();
		BufferedImage img = ImageIO.read(f);
		if (img == null)
			throw new IOException("File not opened");
		if (img.getType() != BufferedImage.TYPE_4BYTE_ABGR)
		{
			BufferedImage replaced = createImage(img.getWidth(), img.getHeight());
			Graphics2D g = replaced.createGraphics();
			g.drawImage(img, 0, 0, null);
			return replaced;
		}
		return img;
	}

	private Buffer wrapImageDataBuffer(final BufferedImage image)
	{
		final DataBuffer data = image	.getRaster()
										.getDataBuffer();

		if (data instanceof DataBufferInt)
			return IntBuffer.wrap(((DataBufferInt) data).getData());

		if (data instanceof DataBufferByte)
			return ByteBuffer.wrap(((DataBufferByte) data).getData());

		if (data instanceof DataBufferDouble)
			throw new RuntimeException("DataBufferDouble rasters not supported by OpenGL");

		if (data instanceof DataBufferFloat)
			return FloatBuffer.wrap(((DataBufferFloat) data).getData());

		if (data instanceof DataBufferShort)
			return ShortBuffer.wrap(((DataBufferShort) data).getData());

		if (data instanceof DataBufferUShort)
			return ShortBuffer.wrap(((DataBufferUShort) data).getData());

		throw new RuntimeException("Unexpected DataBuffer type?");
	}

	protected Texture getTexture()
	{
		if (notFound) return null;

		if (t == null)
		{
			GL2 gl = GameScreen.gl;

			_buff.add(this);

			if (_img == null)
			{
				if (_fileName != null)
				{
					try
					{
						_img = loadImage(_fileName, false);
					}
					catch (IOException e)
					{
						System.err.println("Error load image " + _fileName);
						e.printStackTrace();
						notFound = true;
						return null;
					}
				}
				else
				{
					notFound = true;
					return null;
				}
			}

			boolean alpha = _img.getColorModel()
								.hasAlpha();
			//System.out.println(_fileName + " : " + _img);
			PixelFormat fmt = alpha ? PixelFormat.ABGR8888 : PixelFormat.BGR888;
			int internalFormat = alpha ? GL.GL_RGBA8 : GL.GL_RGB8;
			int pixelFormat = alpha ? GL.GL_RGBA : GL.GL_RGB;
			final GLPixelAttributes glpa = new GLPixelAttributes(gl.getGLProfile(), fmt, false);
			int pixelType = glpa.type;
			Buffer buff = wrapImageDataBuffer(_img);
			TextureData d = new TextureData(gl.getGLProfile(), internalFormat, _img.getWidth(), _img.getHeight(), 0, pixelFormat, pixelType, false, false, false, buff, null);
			t = TextureIO.newTexture(d);

			t.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			t.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

			t.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
			t.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);

			t.enable(gl);

			//if (_fileName != null) _img = null;
		}
		return t;
	}

	private boolean isDeleted = false;

	public void delete()
	{
		isDeleted = true;
		needClean = true;
	}

	private void destroy(GL2 gl)
	{
		if (t != null)
		{
			notFound = true;

			t.destroy(gl);
			t = null;
			_img = null;
		}
	}

	public static Drawable load(String path)
	{
		if (path.contains("?"))
		{
			String[] parts = path.split("\\?", 2);
			XCF xcf = GameResources.loadXCF(parts[0]);
			return xcf.getDrawable(parts[1]);
		}

		synchronized (_buff)
		{
			for (Picture p : _buff)
			{
				if (p._fileName != null && p._fileName.equals(path))
					return p;
			}
		}

		File f = new File(path);
		if (!f.exists())
		{
			System.err.println("Resource not exists " + path);
			System.exit(1);
			return null;
		}

		try
		{
			BufferedImage img = loadImage(path, false);
			Picture pic = new Picture(img);
			pic._fileName = path;
			return pic;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	// Buffer
	private static ArrayList<Picture> _buff = new ArrayList<>();

	public static void reload()
	{
		synchronized (_buff)
		{
			for (Picture p : _buff)
			{
				//if (p._fileName != null) p._img = null; // TODO доделать. Не имеет смысла
				p.t = null;
			}
		}
	}

	private static boolean needClean = false;

	public static void clean(GL2 gl)
	{
		if (!needClean) return;

		needClean = false;

		synchronized (_buff)
		{
			for (int i = 0; i < _buff.size(); i++)
			{
				Picture p = _buff.get(i);
				if (p.isDeleted)
				{
					_buff.remove(i);
					p.destroy(gl);
					i--;
				}
			}
		}
	}

	public void drawAt(Graphics2D g2d, int x, int y)
	{
		g2d.drawImage(_img, x - anchorX, y - anchorY, null);
	}

	/**
	 * Автокадрирование
	 */
	public void crop() // TODO Убрать из релиза
	{
		int top = 0, left = 0, bottom = height - 1, right = width - 1;

		// Find top
		boolean isBreak = false;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if ((_img.getRGB(x, y) & 0xFF000000) != 0)
				{
					top = y;
					isBreak = true;
					break;
				}
			}

			if (isBreak) break;
		}
		if (!isBreak)
		{
			clear();
			return; // is full transparent image
		}

		// Find bottom
		isBreak = false;
		for (int y = height - 1; y > top; y--)
		{
			for (int x = 0; x < width; x++)
			{
				if ((_img.getRGB(x, y) & 0xFF000000) != 0)
				{
					bottom = y;
					isBreak = true;
					break;
				}
			}

			if (isBreak) break;
		}
		if (!isBreak)
			bottom = top;

		// Find left
		isBreak = false;
		for (int x = 0; x < width; x++)
		{
			for (int y = top; y <= bottom; y++)
			{
				if ((_img.getRGB(x, y) & 0xFF000000) != 0)
				{
					left = x;
					isBreak = true;
					break;
				}
			}

			if (isBreak) break;
		}

		// Find right
		isBreak = false;
		for (int x = width - 1; x > left; x--)
		{
			for (int y = top; y <= bottom; y++)
			{
				if ((_img.getRGB(x, y) & 0xFF000000) != 0)
				{
					right = x;
					isBreak = true;
					break;
				}
			}

			if (isBreak) break;
		}
		if (!isBreak)
			right = left;

		width = right - left + 1;
		height = bottom - top + 1;

		BufferedImage cropped = createImage(width, height);
		cropped	.getGraphics()
				.drawImage(_img, 0, 0, width, height, left, top, right + 1, bottom + 1, null);
		_img = cropped;
		anchorX -= left;
		anchorY -= top;
	}

	private void clear()
	{
		width = 1;
		height = 1;
		_img = createImage(1, 1);
	}

	/*@Override
	public void maskImage(int baseColor) // TODO Убрать из релиза
	{
		float r = ((baseColor >> 16) & 0xff) / 255f;
		float g = ((baseColor >> 8) & 0xff) / 255f;
		float b = (baseColor & 0xff) / 255f;
	
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				int c = _img.getRGB(x, y);
				if (c == 0) continue;
				int tr = (int) (((c >> 16) & 0xff) / r);
				int tg = (int) (((c >> 8) & 0xff) / g);
				int tb = (int) ((c & 0xff) / b);
				int tc = (c & 0xff000000) | (tr << 16) | (tg << 8) | tb;
				_img.setRGB(x, y, tc);
			}
		}
	}*/

	@Override
	public Drawable replaceColors(int[] from, int[] to)
	{
		BufferedImage img = createImage(width, height);
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				int c = _img.getRGB(x, y);
				int ind = indexOf(from, c);
				if (ind >= 0)
					img.setRGB(x, y, to[ind]);
				else
					img.setRGB(x, y, c);
			}
		}

		Picture pic = new Picture(img);
		pic.anchorX = anchorX;
		pic.anchorY = anchorY;
		return pic;
	}

	private int indexOf(int[] array, int value) // TODO в Utils
	{
		for (int i = 0; i < array.length; i++)
			if (array[i] == value) return i;
		return -1;
	}

	public BufferedImage getImage()
	{
		return _img;
	}

	public Drawable subpic2(int x, int y, int w, int h)
	{
		BufferedImage sub = createImage(w, h);
		Graphics g = sub.getGraphics();
		int dx = x + anchorX;
		int dy = y + anchorY;
		g.drawImage(_img, 0, 0, w, h, dx, dy, dx + w, dy + h, null);
		Picture pic = new Picture(sub);
		pic.anchorX = -x;
		pic.anchorY = -y;
		pic.crop();
		return pic;
	}

	public Drawable subpic(int x, int y, int w, int h)
	{
		int ax = -x;
		int ay = -y;

		x += anchorX;
		y += anchorY;

		if (x < 0)
		{
			ax += x;
			w += x;
			x = 0;
		}
		if (x + w > width)
		{
			w = width - x;
		}

		if (y < 0)
		{
			ay += y;
			h += y;
			y = 0;
		}
		if (y + h > height)
		{
			h = height - y;
		}

		if (w <= 0 || h <= 0)
		{
			w = 0;
			h = 0;
		}

		Picture orig = new Picture(_img);
		PicPart pp = new PicPart(orig, x, y, w, h);
		pp.anchorX = ax;
		pp.anchorY = ay;
		return pp;
	}

	private void resize(int w, int h, int dx, int dy)
	{
		BufferedImage sub = createImage(w, h);
		Graphics g = sub.getGraphics();
		g.drawImage(_img, dx, dy, null);
		_img = sub;
		width = w;
		height = h;
	}

	private Picture copy()
	{
		return new Picture(_img, anchorX, anchorY);
	}

	// Эффекты

	public Picture outline(int color)
	{
		if (_img == null) return null;

		Picture copy = copy();
		copy.makeOutline(color);
		return copy;
	}

	public void makeOutline(int color)
	{
		resize(width + 2, height + 2, 1, 1);

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int c = _img.getRGB(x, y);
				if (c != color && (c & 0xFF000000) != 0) // Непрозрачный пиксель
				{
					setIfEmpty(x - 1, y - 1, color);
					setIfEmpty(x - 1, y, color);
					setIfEmpty(x - 1, y + 1, color);

					setIfEmpty(x, y - 1, color);
					setIfEmpty(x, y + 1, color);

					setIfEmpty(x + 1, y - 1, color);
					setIfEmpty(x + 1, y, color);
					setIfEmpty(x + 1, y + 1, color);
				}
			}
		}
	}

	@Override
	public Picture shadow(int color, int sx, int sy)
	{
		if (_img == null) return null;

		Picture copy = copy();
		copy.makeShadow(color, sx, sy);
		return copy;
	}

	private void makeShadow(int color, int sx, int sy)
	{
		resize(width + Math.abs(sx), height + Math.abs(sy), sx < 0 ? -sx : 0, sy < 0 ? -sy : 0);
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int c = _img.getRGB(x, y);
				if (c != color && (c & 0xFF000000) != 0) // Непрозрачный пиксель
					setIfEmpty(x + sx, y + sy, color);
			}
		}
	}

	// Окрашивает пиксель, если он пустой
	private void setIfEmpty(int x, int y, int color)
	{
		if (x < 0 || x >= width || y < 0 || y >= height) return;
		int c = _img.getRGB(x, y);
		if (c != color && (c & 0xFF000000) == 0) // Прозрачный пиксель
			_img.setRGB(x, y, color);
	}

	public static BufferedImage createImage(int width, int height)
	{
		WritableRaster raster = colorModelRGBA.createCompatibleWritableRaster(width, height);
		return new BufferedImage(colorModelRGBA, raster, false, null);
	}

	static final ColorModel colorModelRGBA = new DirectColorModel(32, 0xff000000, 0xff0000, 0xff00, 0xff);

}
