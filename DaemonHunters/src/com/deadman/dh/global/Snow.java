package com.deadman.dh.global;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Random;

import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Control;
import com.jogamp.opengl.GL2;

public class Snow extends Control
{
	private Random _rnd;
	private Particle[] _particles;
	public boolean interactive = false;

	private int _dens; // Плотность. меньше dens - больше снега

	public Snow()
	{
		this(200);
	}

	public Snow(int dens)
	{
		_rnd = new Random();
		_particles = new Particle[0];
		_dens = dens;
	}

	@Override
	protected void onResize()
	{
		init();
	}

	public void init()
	{
		int cnt = width * height / _dens;
		if (cnt < 0) cnt = 0;
		Particle[] arr = new Particle[cnt];
		for (int i = 0; i < cnt; i++)
			arr[i] = new Particle();
		_particles = arr;
	}

	public void tick(long ticks)
	{
		Particle[] arr = _particles;
		int l = arr.length;
		for (int i = 0; i < l; i++)
			arr[i].move(ticks);
	}

	public void draw()
	{
		//long t = System.currentTimeMillis();
		GL2 gl = GameScreen.gl;

		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		GameScreen.screen.resetColorMask();

		//float br = Game.screen.br;
		//Game.screen.setBrightness(Math.min(br + 0.4f, 1f));

		int l = _particles.length;
		for (int i = 0; i < l; i++)
		{
			Particle p = _particles[i];
			int x = scrX + (int) p.px;
			int y = scrY + (int) p.py;
			switch (p.color)
			{
				case 0:
					gl.glColor4f(1, 1, 1, 1);
					break;
				case 1: // TODO Подобрать нормальные цвета. Слишком серый не очень
					gl.glColor4f(0.84f, 0.84f, 0.8f, 1);
					break;
				case 2:
					gl.glColor4f(0.9f, 0.9f, 0.976f, 1); // ededf9
					break;
				default:
					gl.glColor4f(0.8f, 0.8f, 0.9f, 1); // ededf9
					break;
			}
			gl.glRecti(x, y, x + p.s, y + p.s);
		}
		gl.glColor4f(1, 1, 1, 1);

		//Game.screen.setBrightness(br);

		//System.out.println(System.currentTimeMillis() - t);
	}

	@Override
	protected void onClick(Point p, MouseEvent e)
	{
		if (interactive)
			moveAround(p, 50.0, 9999);
	}

	@Override
	protected void onMouseMove(Point p, MouseEvent e)
	{
		if (interactive)
			moveAround(p, 20.0, 6);
	}

	void moveAround(Point p, double rad, int count)
	{
		//long t = System.nanoTime(); //230 000 dens=30
		int minx = (int) (p.x - rad);
		int maxx = (int) (p.x + rad);
		int miny = (int) (p.y - rad);
		int maxy = (int) (p.y + rad);
		double rad2 = rad * rad;

		Particle[] arr = _particles;
		int l = arr.length;
		int c = 0;
		for (int i = 0; i < l; i++)
		{
			Particle pr = arr[i];
			float prx = pr.px, pry = pr.py;
			if (prx >= minx && prx <= maxx && pry >= miny && pry <= maxy &&
					p.distanceSq(prx, pry) < rad2)
			{
				double a = _rnd.nextDouble() * Math.PI * 2.0;
				double d = rad + _rnd.nextDouble() * 3.0;
				pr.px = p.x + (float) (Math.cos(a) * d);
				pr.py = p.y + (float) (Math.sin(a) * d);
				pr.cx = pr.px;
				c++;
				if (c >= count) break;
			}
		}
		// System.out.println(System.nanoTime() - t);
	}

	class Particle
	{
		public float cx;
		public float px, py;
		public byte s = 1;
		float dy;
		float dxs, shift_t, ampl;
		public byte color;

		public Particle()
		{
			genAdd();
			py = y + _rnd.nextInt(height);
			//y = _y + rnd.nextInt(_h) - _h - 10;
		}

		private void genAdd()
		{
			cx = x + _rnd.nextInt(width);
			px = cx;
			py = y - 1;

			//color = (byte) _rnd.nextInt(4);
			dy = _rnd.nextFloat() / 2.f + 0.25f;
			dxs = _rnd.nextFloat() * 100.f + 20.f;
			shift_t = _rnd.nextFloat() * 100;
			ampl = _rnd.nextFloat() * 10.f + 3.f;
			s = _rnd.nextFloat() < 0.9f ? (byte) 1 : (byte) 2;
		}

		public void move(long ticks)
		{
			if (py >= height + y)
			{
				genAdd();
			}
			else
			{
				float dx = (float) (Math.sin(ticks / dxs + shift_t) * ampl);
				px = cx + dx;
				py += dy;
			}
		}
	}
}
