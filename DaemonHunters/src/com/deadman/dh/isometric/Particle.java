package com.deadman.dh.isometric;

import java.util.ArrayList;

public abstract class Particle implements Comparable<Particle>
{
	public Particle prev;
	public Particle next;

	private int dx, dy; // Смещение при прорисовке

	public MapCell cell; // Ячейка
	public byte x, y, z; // Координаты в ячейке

	public boolean isDead;

	public void drawAt(int cx, int cy)
	{
		if (isDead) return;
		draw(cx + dx, cy + dy);
	}

	protected abstract void draw(int sx, int sy);

	public void kill()
	{
		isDead = true;
		remove();
	}

	public void moveTo(MapCell to)
	{
		if (cell != null)
			remove();

		prev = null;
		if (to.particle != null)
			to.particle.prev = this;
		next = to.particle;
		to.particle = this;
		cell = to;
	}

	public void moveTo(MapCell to, byte cx, byte cy, byte cz)
	{
		if (cell != to)
			moveTo(to);
		setCellCoords(cx, cy, cz);
	}

	public void remove()
	{
		if (cell == null) return;

		if (prev != null)
			prev.next = next;
		else
			cell.particle = next;

		if (next != null)
			next.prev = prev;

		prev = null;
		next = null;
	}

	public void setCellCoords(byte cx, byte cy, byte cz)
	{
		x = cx;
		y = cy;
		z = cz;

		dx = cx - cy + 18;
		dy = cx / 2 + cy / 2 + ((cx % 2) + (cy % 2)) / 2 + 37;
		cell.neesSortParticles = true;
	}

	public void tick()
	{
	}

	public int compareTo(Particle o)
	{
		int d = x + y;
		int od = o.x + o.y;
		if (d > od)
			return 1;
		if (d < od)
			return -1;

		if (this.z > o.z)
			return 1;
		if (this.z < o.z)
			return -1;

		return 0;
	}

	public static ArrayList<Particle> normalize(ArrayList<Particle> list)
	{
		// TODO Оптимизировать. избавится от частого выделения памяти
		ArrayList<Particle> newList = new ArrayList<>(list.size());
		for (Particle p : list)
		{
			if (!p.isDead)
			{
				if (p.cell.neesSortParticles)
					p.cell.sortParticles();
				newList.add(p);
			}
		}
		return newList;
	}
}
