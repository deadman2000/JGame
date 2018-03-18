package com.deadman.dh.isometric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.drawing.Drawable;

/**
 * Класс спрайта изометрии
 * @author dead_man
 *
 */
public class IsoSprite
{
	public byte type; // Тип
	public int id; // Идентификатор
	public String name; // Название
	public String owner; // Для оружия, название тела
	public boolean mirrorRotation;
	public byte rotating; // Количество поворотов (для редактора)

	public float light_rate; // Пропускание света
	public float light_source; // Свечение

	public Hashtable<Byte, Drawable> pics; // Состояния спрайта (поворот, вариация)
	private byte[] randomStatesCount; // Число рандомных состояний (для редактора)
	public byte subType;

	public byte zheight; // Высота подъема (для лестниц и полов)

	public boolean drawOnTop; // отрисовывать во вторую очередь (пол с травой после стен)

	private static final String[] weapon_types = new String[] { "sword" };
	public HashMap<String, IsoSprite> weapons;

	private ArrayList<SpritePal> colors; // Кастомные цвета

	public byte material; // Материал (для звуков хотьбы / удара)

	public IsoSprite() // Конструктор для тестов
	{
	}

	public IsoSprite(Node node) // Конструктор для ресурсов
	{
		type = getObjClass(node.getNodeName());

		id = GameResources.getInt(node, "id");
		name = GameResources.getString(node, "name");

		mirrorRotation = GameResources.getBool(node, "mirror");

		if (type == OBJ_ON_WALL)
			mirrorRotation = true;

		if (type == UNIT)
			loadWeapons();

		light_rate = GameResources.getFloat(node, "lightrate");
		light_source = GameResources.getFloat(node, "lightsource");

		subType = (byte) GameResources.getInt(node, "subtype");
		zheight = (byte) GameResources.getInt(node, "zheight");

		String matName = GameResources.getString(node, "material");
		if (matName != null)
			material = getMaterial(matName);

		drawOnTop = GameResources.getBool(node, "drawOnTop");

		if (node.hasChildNodes())
		{
			NodeList childs = node.getChildNodes();
			for (int j = 0; j < childs.getLength(); j++)
			{
				Node n = childs.item(j);
				String nodeName = n.getNodeName();

				if (nodeName.equalsIgnoreCase("#text"))
					continue;

				if (nodeName.equals("pal"))
				{
					SpritePal pal = SpritePal.parse(n);
					if (colors == null)
						colors = new ArrayList<>();
					colors.add(pal);
				}
			}
		}

		loadStates();
	}

	public IsoSprite(byte type, int id, String owner, String name, boolean mirrorRotation)
	{
		this.type = type;
		this.id = id;
		this.owner = owner;
		this.name = name;
		this.mirrorRotation = mirrorRotation;

		loadStates();
	}

	public IsoSprite(IsoSprite spr)
	{
		type = spr.type;
		id = spr.id;
		name = spr.name;
		mirrorRotation = spr.mirrorRotation;
		pics = spr.pics;

		weapons = spr.weapons;

		light_rate = spr.light_rate;
		light_source = spr.light_source;

		material = spr.material;

		subType = spr.subType;
		zheight = spr.zheight;

		drawOnTop = spr.drawOnTop;

		pics = spr.pics;
	}

	public void initStates()
	{
		pics = new Hashtable<>();
		randomStatesCount = new byte[16];
	}

	private void loadStates()
	{
		initStates();

		String path;
		if (type == UNIT)
		{
			if (owner != null)
				path = "R.units." + owner + "." + name;
			else
				path = "R.units." + name + "." + name;
		}
		else
			path = "R.iso." + name;

		byte r, s, lastS;
		for (r = 0; r < 16; r++)
		{
			lastS = 0;
			for (s = 0; s < 16; s++)
			{
				Drawable d = Drawable.get(path + "_" + r + "_" + s);
				if (d == null && s == 0)
				{
					d = Drawable.get(path + "_" + r);
					if (d == null && r == 0)
						d = Drawable.get(path);
				}

				if (d != null)
				{
					pics.put(getFullState(r, s), d);
					lastS = (byte) (s + 1);
				}
			}
			randomStatesCount[r] = lastS;
		}

		completeStates();
	}

	public void completeStates()
	{
		byte r, s, maxR = 0;
		for (s = 0; s < 16; s++)
			for (r = maxR; r < 16; r++)
			{
				if (pics.get(getFullState(r, s)) != null)
					maxR = r;
			}

		if (type == UNIT) // Добавляем отзеркаленные для юнитов
		{
			for (r = 5; r < 8; r++)
				for (s = 0; s < 3; s++) // 0-стоя, 1-движение
				{
					byte fs = getFullState(r, s);
					Drawable d = pics.get(fs);
					if (d == null)
					{
						Drawable original = pics.get(getFullState(8 - r, s)); // Противоположное
						if (original != null)
							pics.put(fs, original.getMirrored(-35));
					}
				}
		}
		else if (mirrorRotation && maxR == 0)
		{
			for (s = 0; s < 16; s++)
			{
				Drawable d = pics.get(getFullState(0, s));
				if (d != null)
					pics.put(getFullState(1, s), d.getMirrored(-35));
				else
					break;
			}
			randomStatesCount[1] = s;
			maxR++;
		}

		maxR++;
		randomStatesCount = Arrays.copyOf(randomStatesCount, maxR);
		rotating = maxR;
	}

	protected void loadWeapons() // TODO свой метод для больших (?)
	{
		weapons = new HashMap<>();
		for (String t : weapon_types)
		{
			IsoSprite s = new IsoSprite(type, -1, name, t, mirrorRotation);
			weapons.put(t, s);
		}
	}

	@Override
	public String toString()
	{
		return "[" + id + ", " + name + "]";
	}

	static Random _rnd = new Random();

	public byte getRandomState(int rotation)
	{
		byte r = randomStatesCount[rotation];
		if (r <= 1)
			return getFullState(rotation, 0);
		else
			return getFullState(rotation, _rnd.nextInt(r));
	}

	public byte getStateFromSeed(int rotation, int s)
	{
		return getFullState(rotation, s % randomStatesCount[rotation]);
	}

	public byte getStateFromSeed(int s)
	{
		return (byte) (s % randomStatesCount[0]);
	}

	public Drawable getState(int rotation, int sid)
	{
		return pics.get(getFullState(rotation, sid));
	}

	public byte getNextState(byte state)
	{
		int rotation = (state >> 4) & 0x0F;
		byte rs = (byte) (state & 0x0F);
		rs++;
		if (rs >= randomStatesCount[rotation])
			return 0;
		return (byte) (rs | (state & 0xF0));
	}

	public static byte getFullState(int rotation, int state)
	{
		return (byte) ((rotation << 4) | (state & 0xF));
	}

	public Drawable getPic()
	{
		return pics.get((byte) 0);
	}

	public Drawable getPic(byte state)
	{
		return pics.get(state);
	}

	public void drawAt(int state, int direction, int x, int y)
	{
		Drawable pic = getState(direction, state);
		if (pic != null)
		{
			pic.drawAt(x, y);
		}
	}

	public void drawAt(byte fullState, int x, int y)
	{
		Drawable pic = pics.get(fullState);
		if (pic != null)
		{
			pic.drawAt(x, y);
		}
	}

	public void setTo(MapCell cell)
	{
		setTo(cell, getRandomState(0));
	}

	public void setTo(MapCell cell, byte state)
	{
		IsoSimpleObject obj = new IsoSimpleObject(this, state);
		cell.setObject(obj, type);
	}

	public void setToL(MapCell cell)
	{
		if (type == WALL)
			setTo(cell, getRandomState(0));
		else
			setToL(cell, getRandomState(0));
	}

	public void setToL(MapCell cell, byte state)
	{
		if (type == WALL)
			setTo(cell, getFullState(0, state));
		else
		{
			IsoSimpleObject obj = new IsoSimpleObject(this, state);
			cell.setObjectL(obj, type);
		}
	}

	public void setToR(MapCell cell)
	{
		if (type == WALL)
			setTo(cell, getRandomState(1));
		else
			setToR(cell, getRandomState(0));
	}

	public void setToR(MapCell cell, byte state)
	{
		if (type == WALL)
			setTo(cell, getFullState(1, state));
		else
		{
			IsoSimpleObject obj = new IsoSimpleObject(this, state);
			cell.setObjectR(obj, type);
		}
	}

	// Типы
	public static final byte FLOOR = 0;
	public static final byte WALL = 1;
	public static final byte OBJECT = 2;
	public static final byte OBJ_ON_WALL = 3;
	public static final byte OBJ_ON_FLOOR = 4;
	public static final byte UNIT = 50;
	public static final byte HELPERS = 100;

	// Под-типы
	public static final byte ST_WALL_DOOR = 1;

	// Материалы
	public static final byte MT_OTHER = 0;
	public static final byte MT_WOOD = 1;
	public static final byte MT_GRASS = 2;
	public static final byte MT_DIRT = 3;
	public static final byte MT_ROCK = 4;

	private static byte getObjClass(String name)
	{
		if (name.equals("floor"))
			return FLOOR;
		if (name.equals("wall"))
			return WALL;
		if (name.equals("object"))
			return OBJECT;
		if (name.equals("wallobject"))
			return OBJ_ON_WALL;
		if (name.equals("unit"))
			return UNIT;
		if (name.equals("helper"))
			return HELPERS;

		System.err.println("Unknowon iso-object class " + name);
		return -1;
	}

	private static byte getMaterial(String name)
	{
		if (name.equals("wood"))
			return MT_WOOD;
		if (name.equals("grass"))
			return MT_GRASS;
		if (name.equals("dirt"))
			return MT_DIRT;
		if (name.equals("rock"))
			return MT_ROCK;
		return MT_OTHER;
	}

	public static IsoSprite parse(Node node)
	{
		byte w = (byte) GameResources.getInt(node, "width", 1);
		byte h = (byte) GameResources.getInt(node, "height", 1);

		if (w != 1 || h != 1)
			return new IsoBigSprite(node);
		else
			return new IsoSprite(node);
	}

	public static IsoSprite byName(String name)
	{
		return GameResources.main.isoSprites.get(name);
	}

	public IsoSprite colorize(int val)
	{
		if (colors == null) return this;

		val = Math.abs(val);

		int[] from = null, to = null;

		int c = 0;
		for (int i = 0; i < colors.size(); i++)
		{
			SpritePal pal = colors.get(i);

			int k = (val + c) % (pal.customs.length + 1);
			c += pal.customs.length;
			if (k == pal.customs.length)
				continue; // Оставляем оригинальный цвет

			int[] colors = pal.customs[k];

			if (from == null)
			{
				from = pal.colors;
				to = colors;
			}
			else
			{
				from = concat(from, pal.colors);
				to = concat(to, colors);
			}
		}
		if (from == null) return this;

		return replaceColors(from, to);
	}

	public int[] concat(int[] a, int[] b)
	{
		int aLen = a.length;
		int bLen = b.length;
		int[] c = new int[aLen + bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}

	public IsoSprite replaceColors(int[] from, int[] to)
	{
		Hashtable<Byte, Drawable> colorStates = new Hashtable<>();

		for (Byte k : pics.keySet())
		{
			Drawable d = pics.get(k);
			d = d.replaceColors(from, to);
			colorStates.put(k, d);
		}

		IsoSprite res = new IsoSprite(this);
		res.pics = colorStates;
		return res;
	}

	// http://www.pixeljoint.com/pixelart/49116.htm
}
