package com.deadman.dh.model;

import java.awt.Point;
import java.util.ArrayList;

import com.deadman.jgame.drawing.GameScreen;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

public class Way
{
	public Poi from;
	public Poi to;
	public Mission mission;
	public Poi[] points;
	private Point[] _pixels;

	public Way(Poi from, Poi to, Mission m, Poi[] pts)
	{
		this.from = from;
		this.to = to;
		mission = m;
		points = pts;
	}

	public void setFrom(Poi f)
	{
		Poi[] pts = new Poi[points.length + 1];
		System.arraycopy(points, 0, pts, 1, points.length);
		pts[0] = f;
		from = f;
		points = pts;
	}

	private void buildLines()
	{
		if (points == null) return;
		ArrayList<Point> pixels = new ArrayList<>();
		for (int i = 0; i < points.length - 1; i++)
			createLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, pixels, i == points.length - 2);

		int c = pixels.size() / 4;
		int l = c * 2;
		_pixels = new Point[l];
		for (int i = 0; i < c; i++)
		{
			_pixels[i * 2] = pixels.get(i * 4);
			_pixels[i * 2 + 1] = pixels.get(i * 4 + 1);
		}
	}

	public void draw()
	{
		if (_pixels == null) buildLines();
		
		GameScreen.screen.setDrawColor(0xff5f3d14);
		
		GL2 gl = GameScreen.gl;
		gl.glPointSize(GameScreen.SCALE_FACTOR);
		gl.glBegin(GL.GL_POINTS);
		for (int i = 0; i < _pixels.length; i++)
		{
			Point p = _pixels[i];
			gl.glVertex2f(p.x + 0.5f, p.y + 0.5f);
		}
		gl.glEnd();

		GameScreen.screen.resetDrawColor();
	}

	private void createLine(int x0, int y0, int x1, int y1, ArrayList<Point> pixels, boolean inclusive)
	{
		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep)
		{
			int s = x0;
			x0 = y0;
			y0 = s;
			s = x1;
			x1 = y1;
			y1 = s;
		}
		if (x0 > x1)
		{
			int s = x0;
			x0 = x1;
			x1 = s;
			s = y0;
			y0 = y1;
			y1 = s;
		}
		int deltax = x1 - x0;
		int deltay = Math.abs(y1 - y0);
		int error = deltax / 2;
		int ystep;
		int y = y0;
		if (y0 < y1)
			ystep = 1;
		else
			ystep = -1;

		if (inclusive) x1++;
		for (int x = x0; x < x1; x++)
		{
			if (steep)
				pixels.add(new Point(y, x));
			else
				pixels.add(new Point(x, y));
			error -= deltay;
			if (error < 0)
			{
				y += ystep;
				error += deltax;
			}
		}
	}

}
