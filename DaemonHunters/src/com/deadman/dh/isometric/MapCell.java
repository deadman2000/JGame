package com.deadman.dh.isometric;

import java.util.ArrayList;
import java.util.Collections;

import com.deadman.dh.fx.SoundEffect;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.Corpse;

public class MapCell
{
	public static final int CELL_WIDTH = 16;
	public static final int CELL_WIDTH2 = CELL_WIDTH * 2;
	public static final int CELL_HEIGHT = 8;
	public static final int CELL_HEIGHT2 = CELL_HEIGHT * 2;
	public static final int LEVEL_HEIGHT = 36;
	public static final int Z_HEIGHT = 2; // Соотношение ширины клетки к высоте

	public final short x, y, z;
	public final IsoMap map;

	public float light = 1.f; // Освещенность ячейки
	public byte state; // Флаги состояния ячейки (прозрачность, указатель пути и т.д.)
	public byte userState;  // Выделение ячейки и пр.

	public MapCell(IsoMap map, short x, short y, short z)
	{
		this.map = map;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// Particles
	public Particle particle;
	public boolean neesSortParticles;

	public void sortParticles()
	{
		if (particle == null || particle.next == null) return;

		ArrayList<Particle> list = new ArrayList<>();
		Particle p = particle;
		while (p != null)
		{
			list.add(p);
			p = p.next;
		}
		Collections.sort(list);

		p = list.get(0);
		p.prev = null;
		particle = p;

		for (int i = 1; i < list.size(); i++)
		{
			p.next = list.get(i);
			p.next.prev = p;
			p = p.next;
		}
		p.next = null;

		neesSortParticles = false;
	}

	// Iso objects

	public IsoSimpleObject floor = null;

	public IsoSimpleObject wall_l = null;
	public IsoSimpleObject wall_r = null;

	public IsoSimpleObject obj_l = null; // Объекты на стене
	public IsoSimpleObject obj_r = null;

	public IsoSimpleObject obj = null;

	public GameCharacter ch = null; // Юнит

	public ArrayList<Item> items = null; // Объекты на полу

	public void setObject(IsoSimpleObject o, byte type)
	{
		if (o != null)
			o.cell = this;
		switch (type)
		{
			case IsoSprite.FLOOR:
				floor = o;
				break;
			case IsoSprite.OBJECT:
				obj = o;
				break;
			case IsoSprite.WALL:
				if (o.getRotation() % 2 == 0)
					wall_l = o;
				else
					wall_r = o;
				break;
			case IsoSprite.OBJ_ON_WALL:
				if (o.getRotation() % 2 == 0)
					obj_l = o;
				else
					obj_r = o;
				break;
			default:
				System.err.println("Wrong usage MapCell.setObject with type: " + type);
				break;
		}
	}

	public void setObjectL(IsoSimpleObject o, byte type)
	{
		if (o != null)
			o.cell = this;
		obj_l = o;
		o.setRotation((byte) 1);
	}

	public void setObjectR(IsoSimpleObject o, byte type)
	{
		if (o != null)
			o.cell = this;
		obj_r = o;
	}

	public void setUnit(GameCharacter unit)
	{
		if (ch == unit) return;
		if (ch != null && unit != null)
			System.err.println("Cell already with unit: " + this + " = " + ch + " => " + unit);

		//System.out.println(this + " " + unit);
		boolean lightMoved = (ch != null && ch.getLightSource() > 0) || (unit != null && unit.getLightSource() > 0);

		ch = unit;
		if (unit != null)
		{
			MapCell from = unit.cell;

			if (unit.cell != null)
				unit.cell.ch = null;
			unit.cell = this;
			unit.setShift(0, 0, getZShift());
			
			if (from != null)
			{
				onMoving(from); // Для просчета дверей
				unit.onMoving(); // Для обновления карты видимости
			}
		}

		if (lightMoved)
			map.calcLights();
	}

	public void putOnFloor(Item item)
	{
		if (items == null)
			items = new ArrayList<>();
		items.add(item);
		calcPassive();
	}

	public void putOnFloor(GameCharacter unit)
	{
		putOnFloor(new Corpse(unit));
	}

	public void removeFromFloor(Item item) 
	{
		if (items == null) return;
		items.remove(item);
		calcPassive();
	}
	
	public boolean hasItems()
	{
		return items != null && items.size() > 0;
	}
	
	public void calcPassive() // TODO Вызывать когда меняется список
	{
		lightPassive = 0;
		
		if (items != null)
			for (Item i : items)
				i.applyPassive(this);
	}

	// Other
	public MapCell copy(IsoMap target, int x, int y, int z)
	{
		MapCell c = new MapCell(target, (short) x, (short) y, (short) z);
		c.floor = floor;
		c.wall_l = wall_l;
		c.wall_r = wall_r;
		c.obj_l = obj_l;
		c.obj_r = obj_r;
		c.obj = obj;
		c.items = items;
		c.ch = ch; // Может быть косяк, т.к. у персонажа есть указатель на ячейку
		c.light = light;
		target.cells[z][x][y] = c;
		return c;
	}

	public void mixTo(MapCell target)
	{
		if (floor != null) floor.copyTo(target);
		if (wall_l != null) wall_l.copyTo(target);
		if (wall_r != null) wall_r.copyTo(target);
		if (obj_l != null) obj_l.copyTo(target);
		if (obj_r != null) obj_r.copyTo(target);
		if (obj != null) obj.copyTo(target);
	}

	public void mixToRandom(MapCell target)
	{
		if (floor != null) floor.copyToRandom(target);
		if (wall_l != null) wall_l.copyToRandom(target);
		if (wall_r != null) wall_r.copyToRandom(target);
		if (obj_l != null) obj_l.copyToRandom(target);
		if (obj_r != null) obj_r.copyToRandom(target);
		if (obj != null) obj.copyToRandom(target);
	}

	@Override
	public String toString()
	{
		return "[" + x + ";" + y + ";" + z + "]";
	}

	public boolean equals(MapCell cell)
	{
		return x == cell.x && y == cell.y && z == cell.z;
	}

	/**
	 * Считает расстояние х16 до соседней ячейки
	 * @param cell
	 * @return
	 */
	public int distanceToNeighbor(MapCell cell)
	{
		int d;
		if (cell.x == x && cell.y == y)
			d = 0;
		else if (cell.x == x || cell.y == y)
			d = 16;
		else
			d = 23; // 22.6 (sqrt(16*16+16*16)

		d += Math.abs(getFullZ() - cell.getFullZ());

		return d;
	}

	/**
	 * Считает расстояние х1 до любой ячейки
	 * @param cell
	 * @return
	 */
	public int distanceTo(MapCell cell)
	{
		return (int) Math.sqrt(square(cell.x - x) + square(cell.y - y) + square((cell.z - z) * Z_HEIGHT));
	}

	static int square(int c)
	{
		return c * c;
	}

	public byte directionTo(MapCell dest)
	{
		if (dest == this) return -1;

		double t = Math.atan2(x - dest.x, y - dest.y) / Math.PI / 2.0 * 8.0; // Угол в относительных единицах (0..8)
		if (t > 0)
			t += 0.5;
		else
			t -= 0.5;
		byte r = (byte) (1 - (int) t);
		if (r < 0) r += 8;
		return r;
	}

	public float lightPassive = 0;

	public float getLightSource()
	{
		float r = lightPassive;
		if (obj != null) r += obj.getLightSource();
		if (obj_l != null) r += obj_l.getLightSource();
		if (obj_r != null) r += obj_r.getLightSource();
		if (ch != null) r += ch.getLightSource();
		return r;
	}

	public float getLightRate(MapCell from)
	{
		int dx = x - from.x;
		int dy = y - from.y;
		int dz = z - from.z;

		if (Math.abs(dx) > 1 || Math.abs(dy) > 1)
		{
			// Пересчитываем угол обзора
			if (dx == 0)
			{
				if (dy > 0)
					dy = 1;
				else
					dy = -1;
			}
			else if (dy == 0)
			{
				if (dx > 0)
					dx = 1;
				else
					dx = -1;
			}
			else
			{
				float a = (float) dx / dy;
				boolean strong_angles = true;

				if (!strong_angles && Math.abs(a) > 2.41f)
					dy = 0;
				else if (a < -0.41f)
					dy = -1;
				else if (a > 0.41f)
					dy = 1;
				else if (a > 0f) // 0 .. 0.41
					dy = 1;
				else // -0.41 .. 0
					dy = -1;

				if (dx < 0) dy = -dy;

				if (!strong_angles && Math.abs(a) < 0.41)
					dx = 0;
				else
				{
					if (dx > 0)
						dx = 1;
					else
						dx = -1;
				}
			}
		}

		from = map.getCell(x - dx, y - dy, z - dz);

		float r = getWF(from);
		if (from.obj != null) r *= from.obj.getLightRate();

		return r;
	}

	/*
	 *           /\
	 *          /0 \
	 *     Y   /\  /\  X
	 *    /   /7 \/1 \  \
	 *  |/_  /\  /\  /\ _\|
	 *      /6 \/fr\/2 \ 
	 *      \  /\om/\  /
	 *       \/5 \/3 \/
	 *        \  /\  /
	 *         \/4 \/
	 *          \  /
	 *           \/
	 */

	// Возвращает проходимость света из текущей ячейки в целевую
	private float getWF(MapCell from)
	{
		MapCell c;
		int dx = x - from.x;
		int dy = y - from.y;
		int dz = z - from.z;

		if (dz != 0)
		{
			float r;
			if (dz < 0) // Сверху
			{
				c = map.getCell(x, y, z + 1);
				if (c != null && c.floor != null)
					r = c.floor.getLightRate();
				else
					r = 1.f;
			}
			else // Снизу
			{
				if (floor != null)
					r = floor.getLightRate();
				else
					r = 1.f;
			}

			/*if (dx != 0 || dy != 0) // Если диагональ, половина препятствия - стены на уровне и пол
			{
				c = map.getCell(x - dx, y - dy, z);
				float wallRate = getWF(grid, c);
				
				// TO DO Добавить пол соседей
				float floorRate = 1f;
				
				r = r * 0.5f + Math.min(wallRate, floorRate) * 0.5f;
			}*/

			return r;
		}

		if (dx == 0) // <1> <5>
		{
			if (dy < 0) // <1>
			{
				c = map.getCell(x, y + 1, z);
				if (c != null && c.wall_r != null)
					return c.wall_r.getLightRate();
				else
					return 1.f;
			}
			else // <5>
			{
				if (wall_r != null)
					return wall_r.getLightRate();
				else
					return 1.f;
			}
		}

		if (dy == 0) // <7> <3>
		{
			if (dx < 0) // <7>
			{
				c = map.getCell(x + 1, y, z);
				if (c != null && c.wall_l != null)
					return c.wall_l.getLightRate();
				else
					return 1.f;
			}
			else // <3>
			{
				if (wall_l != null)
					return wall_l.getLightRate();
				else
					return 1.f;
			}
		}

		if (dx < 0) // <0> <6>
		{
			if (dy < 0) // <0>
			{
				MapCell c2 = map.getCell(x + 1, y + 1, z);

				float r1 = 1f;
				c = map.getCell(x + 1, y, z);
				if (c != null && c.wall_l != null) r1 = c.wall_l.getLightRate();
				if (c2 != null && c2.wall_r != null) r1 = Math.min(r1, c2.wall_r.getLightRate());

				float r2 = 1f;
				c = map.getCell(x, y + 1, z);

				if (c != null && c.wall_r != null) r2 = c.wall_r.getLightRate();
				if (c2 != null && c2.wall_l != null) r2 = Math.min(r2, c2.wall_l.getLightRate());

				return (r1 + r2) / 2f;
			}
			else // <6>
			{
				float r1 = 1f;
				if (wall_r != null) r1 = wall_r.getLightRate();
				c = map.getCell(x + 1, y - 1, z);
				if (c != null && c.wall_l != null) r1 = Math.min(r1, c.wall_l.getLightRate());

				float r2 = 1f;
				c = map.getCell(x + 1, y, z);
				if (c != null && c.wall_l != null) r2 = c.wall_l.getLightRate();
				if (c != null && c.wall_r != null) r2 = Math.min(r2, c.wall_r.getLightRate());

				return (r1 + r2) / 2f;
			}
		}
		else // <2> <4>
		{
			if (dy < 0) // <2>
			{
				float r1 = 1f;
				if (wall_l != null) r1 = wall_l.getLightRate();
				c = map.getCell(x - 1, y + 1, z);
				if (c != null && c.wall_r != null) r1 = Math.min(r1, c.wall_r.getLightRate());

				float r2 = 1f;
				c = map.getCell(x, y + 1, z);
				if (c != null && c.wall_r != null) r2 = c.wall_r.getLightRate();
				if (c != null && c.wall_l != null) r2 = Math.min(r2, c.wall_l.getLightRate());

				return (r1 + r2) / 2f;
			}
			else // <4>
			{
				float r1 = 1f;
				if (wall_r != null) r1 = wall_r.getLightRate();
				c = map.getCell(x, y - 1, z);
				if (c != null && c.wall_l != null) r1 = Math.min(r1, c.wall_l.getLightRate());

				float r2 = 1f;
				if (wall_l != null) r2 = wall_l.getLightRate();
				c = map.getCell(x - 1, y, z);
				if (c != null && c.wall_r != null) r2 = Math.min(r2, c.wall_r.getLightRate());

				return (r1 + r2) / 2f;
			}
		}
	}

	/**
	 * Возвращает возможность пройти в эту ячейку из заданной
	 * @param map
	 * @param from
	 * @return
	 */
	public int moveCost(MapCell from)
	{
		if (floor == null) return 0;
		//if (ch != null) return 0;
		//if (obj == null) return 0;
		//if (obj != null && !obj.isMovable()) return 0;

		int dx = x - from.x;
		int dy = y - from.y;
		int dz = z - from.z;

		if (Math.abs(dx) > 1 || Math.abs(dy) > 1 || Math.abs(dz) > 1)
		{
			System.err.println("isMovable not neighbor cell");
			return 0;
		}

		int zh = getFullZ();
		int fromZH = from.getFullZ();
		int zDiff = Math.abs(zh - fromZH);

		if (zDiff > 18) // TODO Const for ZHEIGHT move range
			return 0;

		int dist = from.distanceToNeighbor(this);

		// Проверяем на наличие стен
		if (dx == 0) // <1> <5>
		{
			if (dy < 0) // <1>
			{
				if (from.wall_r != null && !from.wall_r.isMovable())
					return 0;
			}
			else // dy > 0, <5>
			{
				if (wall_r != null && !wall_r.isMovable())
					return 0;
			}
		}
		else if (dy == 0) // <7> <3>
		{
			if (dx < 0) // <7>
			{
				if (from.wall_l != null && !from.wall_l.isMovable())
					return 0;
			}
			else // dx > 0, <3>
			{
				if (wall_l != null && !wall_l.isMovable())
					return 0;
			}
		}
		else if (dx > 0) // <2> <4>
		{
			if (dy < 0) // <2>
			{
				if (from.wall_r != null && !from.wall_r.isMovable())
					return 0;
				if (wall_l != null && !wall_l.isMovable())
					return 0;
				MapCell c3 = map.getCell(x, y + 1, z);
				if (c3.wall_r != null && !c3.wall_r.isMovable())
					return 0;
				if (c3.wall_l != null && !c3.wall_l.isMovable())
					return 0;
			}
			else // dy > 0,  <4>
			{
				if (wall_l != null && !wall_l.isMovable())
					return 0;
				if (wall_r != null && !wall_r.isMovable())
					return 0;
				MapCell c5 = map.getCell(x - 1, y, z);
				if (c5 == null)
				{
					System.err.println("error " + from + " to " + this);
					return 0;
				}
				if (c5.wall_r != null && !c5.wall_r.isMovable())
					return 0;
				MapCell c3 = map.getCell(x, y - 1, z);
				if (c3.wall_l != null && !c3.wall_l.isMovable())
					return 0;
			}
		}
		else // dx < 0   <0> <6> 
		{
			if (dy < 0) // <0>
			{
				if (from.wall_l != null && !from.wall_l.isMovable())
					return 0;
				if (from.wall_r != null && !from.wall_r.isMovable())
					return 0;
				MapCell c7 = map.getCell(x, y + 1, z);
				if (c7.wall_r != null && !c7.wall_r.isMovable())
					return 0;
				MapCell c1 = map.getCell(x + 1, y, z);
				if (c1.wall_l != null && !c1.wall_l.isMovable())
					return 0;
			}
			else // dy > 0, <6>
			{
				if (from.wall_l != null && !from.wall_l.isMovable())
					return 0;
				if (wall_r != null && !wall_r.isMovable())
					return 0;
				MapCell c5 = map.getCell(x + 1, y, z);
				if (c5.wall_l != null && !c5.wall_l.isMovable())
					return 0;
				if (c5.wall_r != null && !c5.wall_r.isMovable())
					return 0;
			}
		}

		return dist;
	}

	public int getFullZ()
	{
		return z * LEVEL_HEIGHT + getZShift();
	}

	public byte getZShift()
	{
		byte zh = 0;
		if (floor != null) zh += floor.sprite.zheight;
		if (obj != null) zh += obj.sprite.zheight;
		return zh;
	}

	void onMoving(MapCell from)
	{
		int dx = x - from.x;
		int dy = y - from.y;
		//int dz = z - from.z;

		if (dx == 0) // <1> <5>
		{
			if (dy < 0) // <1>
			{
				if (from.wall_r != null && from.wall_r.isDoor())
					from.toggleDoor();
			}
			else // dy > 0, <5>
			{
				if (wall_r != null && wall_r.isDoor())
					toggleDoor();
			}
		}
		else if (dy == 0) // <7> <3>
		{
			if (dx < 0) // <7>
			{
				if (from.wall_l != null && from.wall_l.isDoor())
					from.toggleDoor();
			}
			else // dx > 0, <3>
			{
				if (wall_l != null && wall_l.isDoor())
					toggleDoor();
			}
		}
		map.onMoving(this);
	}

	private void toggleDoor()
	{
		if (wall_l != null)
		{
			changeDoorState(wall_l);
			wall_r = wall_l;
			wall_r.setRotation((byte) 1);
			wall_l = null;
			map.onChanged();
		}
		else if (wall_r != null)
		{
			changeDoorState(wall_r);
			wall_l = wall_r;
			wall_l.setRotation((byte) 0);
			wall_r = null;
			map.onChanged();
		}
	}

	private void changeDoorState(IsoSimpleObject obj)
	{
		if (obj.subState == 0)
		{
			SoundEffect.DOOR_CLOSE.stop();
			SoundEffect.DOOR_OPEN.play();
			obj.subState = 1;
		}
		else
		{
			SoundEffect.DOOR_OPEN.stop();
			SoundEffect.DOOR_CLOSE.play();
			obj.subState = 0;
		}
	}

	public HitResult getObjectAt(int px, int py)
	{
		if (px < 0 || py < 0) return null;
		if (obj != null && obj.contains(px, py))
			return new HitResult(this, obj, false);

		if (obj_r != null && obj_r.contains(px, py)) return new HitResult(this, obj_r, false);
		if (obj_l != null && obj_l.contains(px, py)) return new HitResult(this, obj_l, true);
		if (wall_l != null && wall_l.contains(px, py)) return new HitResult(this, wall_l, true);
		if (wall_r != null && wall_r.contains(px, py)) return new HitResult(this, wall_r, false);
		if (floor != null && floor.contains(px, py)) return new HitResult(this, floor, false);
		return null;
	}

	public static final int BLEND_WALL_L_FLAG = 1;
	public static final int BLEND_WALL_R_FLAG = 2;
	public static final int BLEND_OBJ_FLAG = 4;
	public static final int NOT_BLEND_FLAG = ~(BLEND_WALL_L_FLAG | BLEND_WALL_R_FLAG | BLEND_OBJ_FLAG);
	public static final int FLOOR_FLAG = 8;
	public static final int NOT_FLOOR_FLAG = ~FLOOR_FLAG;
	public static final int TRACE_FLAG = 16;
	public static final int NOT_TRACE_FLAG = ~TRACE_FLAG;

	/**
	 * true, если ячейка рядом (включая разный z уровень) и можно пройти
	 * 
	 * @param cell
	 * @return
	 */
	public boolean isNear(MapCell cell)
	{
		return Math.abs(x - cell.x) <= 1 && Math.abs(y - cell.y) <= 1 && Math.abs(z - cell.z) <= 1 && moveCost(cell) > 0;
	}

	/**
	 * true, если в ячейке есть объект. Если это большой объект, true, если ячейка является основной для него
	 * @return
	 */
	public boolean ownObject()
	{
		return obj != null && (!(obj instanceof IsoObjectPart) || ((IsoObjectPart) obj).original.cell == this);
	}
}
