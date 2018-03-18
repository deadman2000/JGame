package com.deadman.dh.global;

import java.awt.image.BufferedImage;

import com.deadman.dh.R;
import com.deadman.dh.model.generation.PerlinNoise;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.Picture;

public class Clouds
{
	private static final float Z_FACTOR = 1.3f;
	private static final double RECT_SIZE = 200.; // Чем больше, тем крупнее облака
	private static final double DEPTH = 64.;
	private static final int TILE_SIZE = 100; // Размер тайла

	private PerlinNoise perlin;

	private double level = 0.3; // Облачность
	private double opacity = 0.9; // Максимальная прозрачность

	private int tw, th; // Размер массива с тайлами
	private CloudTile[][] tiles;

	private double _dx, _dy; // Смещение тайлов
	private double sx = 0.06, sy = 0.03; // Скорость перемещения облаков. Пикселей в секунду мира

	private GlobalMapView view;

	public Clouds(GlobalMapView view)
	{
		this.view = view;

		perlin = PerlinNoise.create();

		tw = (int) Math.round((GlobalEngine.GLOBAL_MAP_WIDTH * Z_FACTOR) / TILE_SIZE + 0.5) + 1;
		th = (int) Math.round((GlobalEngine.GLOBAL_MAP_HEIGHT * Z_FACTOR) / TILE_SIZE + 0.5) + 1;
		tiles = new CloudTile[tw][th];
		for (int x = 0; x < tw; x++)
			for (int y = 0; y < th; y++)
				tiles[x][y] = new CloudTile(x, y);
	}

	public void start()
	{
		Thread t = new Thread(new CloudUpdater());
		t.setName("Cloud updater");
		t.start();
	}

	class CloudUpdater implements Runnable
	{
		long lastTime;

		@Override
		public void run()
		{
			lastTime = GlobalEngine.time;
			while (true)
			{
				try
				{
					Thread.sleep(GameLoop.TICK_DELAY);
				}
				catch (InterruptedException e)
				{
					break;
				}

				if (GlobalEngine.time == lastTime) continue;

				long dt = GlobalEngine.time - lastTime;
				lastTime = GlobalEngine.time;
				move(dt);

				//System.out.printf("%d  %f %f\n", dt, _dx, _dy);
			}
		}
	};

	private void move(long dt)
	{
		double ndx = _dx - dt * sx;
		double ndy = _dy - dt * sy;
		if (ndx > TILE_SIZE || ndy > TILE_SIZE || ndx < -TILE_SIZE || ndy < -TILE_SIZE)
		{
			int sx = (int) (ndx / TILE_SIZE); // Количество тайлов для сдвига по X
			int sy = (int) (ndy / TILE_SIZE); // Количество тайлов для сдвига по Y

			ndx -= sx * TILE_SIZE;
			ndy -= sy * TILE_SIZE;

			CloudTile[][] newMap = new CloudTile[tw][th];
			CloudTile t0 = tiles[0][0];

			int t0x = t0.cx; // Координаты левого верхнего тайла на старой сетке
			int t0y = t0.cy;

			int nt0x = t0x - sx; // Координаты левого верхнего тайла на новой сетке
			int nt0y = t0y - sy;

			for (int x = 0; x < tw; x++)
			{
				int nx = nt0x + x;
				int ox = x - sx;
				for (int y = 0; y < th; y++)
				{
					int ny = nt0y + y;
					int oy = y - sy;

					if (ox >= 0 && ox < tw && oy >= 0 && oy < th)
						newMap[x][y] = tiles[ox][oy];
					else
						newMap[x][y] = new CloudTile(nx, ny);
				}
			}

			tiles = newMap;
		}

		_dx = ndx;
		_dy = ndy;
	}

	Drawable mask = GameEngine.getDrawable(R.cursors._default);

	public void draw()
	{
		int x = view.centerX;
		int y = view.centerY;

		x *= Z_FACTOR - 1; // Псевдо Z
		y *= Z_FACTOR - 1;

		x += _dx;
		y += _dy;
		CloudTile[][] currTiles = tiles;

		int x0 = (-x - view.viewX) / TILE_SIZE;
		int y0 = (-y - view.viewY) / TILE_SIZE;
		if (x0 < 0) x0 = 0;
		if (y0 < 0) y0 = 0;

		int x1 = (int) Math.round(x0 + view.width / TILE_SIZE + 0.5);
		int y1 = (int) Math.round(y0 + view.height / TILE_SIZE + 0.5);
		if (x1 >= tw) x1 = tw - 1;
		if (y1 >= th) y1 = th - 1;

		for (int tx = x0; tx <= x1; tx++)
		{
			int sx = x + tx * TILE_SIZE;
			CloudTile[] tileRow = currTiles[tx];
			for (int ty = y0; ty <= y1; ty++)
			{
				int sy = y + ty * TILE_SIZE;
				tileRow[ty].drawAt(sx, sy);
			}
		}
	}

	//GameFont fnt = GameEngine.getFont(R.fonts.font3x5, 0xFF101010);

	class CloudTile
	{
		public int cx, cy; // Координаты в сетке

		public Picture picture;
		private BufferedImage img = Picture.createImage(TILE_SIZE, TILE_SIZE);

		public CloudTile(int x, int y)
		{
			cx = x;
			cy = y;
			build();
		}

		@Override
		protected void finalize() throws Throwable
		{
			if (picture != null)
				picture.delete();
			super.finalize();
		}

		public void drawAt(int sx, int sy)
		{
			//if ((cx + cy) % 2 == 0) GameScreen.screen.drawRect(sx, sy, picture.width, picture.height, 0x7fff0000);
			picture.drawAt(sx, sy);
			//fnt.drawAt(sx + TILE_SIZE / 2, sy + TILE_SIZE / 2, this.toString());
		}

		public void build()
		{
			if (picture != null) return;

			for (int x = 0; x < TILE_SIZE; x++)
			{
				double dx = (cx * TILE_SIZE + x) / RECT_SIZE;
				for (int y = 0; y < TILE_SIZE; y++)
				{
					double dy = (cy * TILE_SIZE + y) / RECT_SIZE;
					double v = perlin.turbulence2(dx, dy, DEPTH);
					v = (v + 0.5 - level) / (1 - level);
					v = Math.max(0, Math.min(v, 1.0));
					v = v * v * v * opacity;
					int a = (int) (0xff * v);
					int argb = (a << 24) | 0xffffff;
					img.setRGB(x, y, argb);
				}
			}

			picture = new Picture(img);
		}

		@Override
		public String toString()
		{
			return cx + ":" + cy;
		}
	}
}
