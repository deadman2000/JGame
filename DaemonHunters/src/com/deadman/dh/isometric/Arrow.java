package com.deadman.dh.isometric;

import com.deadman.dh.R;
import com.deadman.dh.model.Element;
import com.deadman.dh.model.GameCharacter;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.resources.ResourceManager;

public class Arrow extends Particle
{
	private double cmx, cmy; // Точные координаты. Одна ячейка - [16х16]
	private double sX, sY; // Смещение в единицу времени по x и y (скорость)
	private int dirInd; // Направление движения

	private IsoMap _map;

	protected int id;
	private static int _id;

	private int dmg;
	
	private GameCharacter _owner; // Кто выпустил стрелу

	private static Drawable[] pics = ResourceManager.getParts(R.iso.particles.arrow_ppr);

	public Arrow(IsoMap map, GameCharacter owner, MapCell to, int damage)
	{
		this(map, owner, (byte) 8, (byte) 8, to, 8, 8, damage);
	}

	public Arrow(IsoMap map, GameCharacter owner, byte fcx, byte fcy, MapCell to, int tcx, int tcy, int damage)
	{
		_map = map;
		id = _id++;
		dmg = damage;

		_owner = owner; 
		MapCell from = owner.cell;

		int fx = from.x * 16 + fcx; // Абсолютные координаты
		int fy = from.y * 16 + fcy;
		cmx = fx;
		cmy = fy;

		int tx = to.x * 16 + tcx;
		int ty = to.y * 16 + tcy;

		double a = Math.atan2(ty - fy, tx - fx);
		sX = Math.cos(a);
		sY = Math.sin(a);

		a -= Math.PI / 8;
		if (a < 0) a += Math.PI * 2;
		dirInd = (int) (a / (Math.PI / 4));
		dirInd += 5;
		dirInd = dirInd % 8;

		moveTo(from, fcx, fcy, (byte) 20);
	}

	@Override
	public void tick()
	{
		cmx += sX;
		cmy += sY;

		// Координаты клетки
		int mx = (int) (cmx / 16);
		int my = (int) (cmy / 16);

		// Координаты внутри клетки
		byte cx = (byte) (cmx - mx * 16);
		byte cy = (byte) (cmy - my * 16);

		if (cell.x != mx || cell.y != my)
		{
			MapCell c = _map.getCell(mx, my, cell.z);
			if (c == null) // Улетели за край карты
			{
				System.out.println(this + " died");
				kill();
				return;
			}

			if (c.obj != null)
			{
				System.out.println(this + " damage to " + c.obj);
				c.obj.hitDamage(_owner, Element.PHYSICAL, dmg);
				kill();
				return;
			}

			moveTo(c, cx, cy, z);
		}
		else
			setCellCoords(cx, cy, z);
	}

	@Override
	protected void draw(int sx, int sy)
	{
		pics[dirInd + 8].drawAt(sx, sy);
		pics[dirInd].drawAt(sx, sy - z);
	}
}
