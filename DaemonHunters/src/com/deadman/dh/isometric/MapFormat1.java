package com.deadman.dh.isometric;

import java.io.IOException;
import java.util.HashMap;

import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.itemtypes.ItemType;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.io.DataInputStreamBE;
import com.deadman.jgame.io.DataOutputStream;

public class MapFormat1 extends MapFormat
{
	@Override
	protected int version()
	{
		return 1;
	}

	/**
	 * Заголовок
	 *   размер по y
	 *   размер по x
	 *   размер по z
	 *   уровень-z
	 *   резерв
	 *   резерв
	 * Ячейки
	 *   количество объектов в ячейке
	 *   объекты
	 *     id
	 *     состояние
	 */
	@Override
	protected void saveMap(IsoMap map, DataOutputStream out) throws IOException
	{
		out.writeIntPack(map.height);
		out.writeIntPack(map.width);
		out.writeIntPack(map.zheight);
		out.writeIntPack(map.zeroLevel);

		for (int z = 0; z < map.zheight; z++)
			for (int y = 0; y < map.height; y++)
				for (int x = 0; x < map.width; x++)
					writeCell(map.cells[z][x][y], out);
	}

	protected static final byte T_OBJECT = 0;
	protected static final byte T_UNIT = 1;
	protected static final byte T_ITEM = 2;

	protected static final byte P_FLOOR = 0;
	protected static final byte P_WALL = 1;
	protected static final byte P_OBJECT = 2;
	protected static final byte P_WOBJECT = 3;

	protected void writeCell(MapCell c, DataOutputStream out) throws IOException
	{
		int cnt = 0;
		boolean writeObj = c.ownObject();

		if (c.floor != null) cnt++;
		if (c.wall_l != null) cnt++;
		if (c.wall_r != null) cnt++;
		if (writeObj) cnt++;
		if (c.obj_l != null) cnt++;
		if (c.obj_r != null) cnt++;
		if (c.hasItems()) cnt += c.items.size();

		out.writeIntPack(cnt);

		if (c.floor != null) writeObject(P_FLOOR, c.floor, out);
		if (c.wall_l != null) writeObject(P_WALL, c.wall_l, out);
		if (c.wall_r != null) writeObject(P_WALL, c.wall_r, out);
		if (writeObj) writeObject(P_OBJECT, c.obj, out);
		if (c.obj_l != null) writeObject(P_WOBJECT, c.obj_l, out);
		if (c.obj_r != null) writeObject(P_WOBJECT, c.obj_r, out);

		if (c.hasItems())
		{
			for (Item i : c.items)
				writeItem(i, out);
		}
	}

	protected void writeObject(byte place, IsoObject o, DataOutputStream out) throws IOException
	{
		if (o instanceof GameCharacter)
			writeUnit((GameCharacter) o, out);
		else if (o instanceof IsoSimpleObject)
			writeSimpleObject(place, (IsoSimpleObject) o, out);
		else
			throw new IOException("Wrong object type: " + o.getClass()
					.getName());
	}

	protected void writeSimpleObject(byte place, IsoSimpleObject o, DataOutputStream out) throws IOException
	{
		out.writeByte(T_OBJECT);
		out.writeByte(place);
		out.writeIntPack(o.sprite.id);
		out.writeByte(o.getFullState());
	}

	protected void writeItem(Item item, DataOutputStream out) throws IOException
	{
		out.writeByte(T_ITEM);
		out.writeIntPack(item.type.id);
		out.writeIntPack(item.count);
	}

	protected void writeUnit(GameCharacter unit, DataOutputStream out) throws IOException
	{
		out.writeByte(T_UNIT);
	}

	@Override
	protected IsoMap loadMap(DataInputStreamBE in) throws IOException
	{
		int h = in.readIntPack();
		int w = in.readIntPack();
		int l = in.readIntPack();
		int zl = in.readIntPack();

		IsoMap map = new IsoMap(w, h, l, zl);

		for (int z = 0; z < l; z++)
			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++)
					readCell(map.cells[z][x][y], in);
		in.close();

		return map;
	}

	protected void readCell(MapCell c, DataInputStreamBE in) throws IOException
	{
		int cnt = in.readIntPack();
		for (int i = 0; i < cnt; i++)
			readObject(c, in);
	}

	protected void readObject(MapCell cell, DataInputStreamBE in) throws IOException
	{
		byte type = in.readByte();
		switch (type)
		{
			case T_OBJECT:
				byte place = in.readByte();
				int sprite_id = in.readIntPack();
				byte state = in.readByte();

				HashMap<Integer, IsoSprite> map = getSpriteMap(place);
				IsoSprite s = map.get(sprite_id);
				s.setTo(cell, state);
				break;

			case T_ITEM:
				int tid = in.readIntPack();
				int count = in.readIntPack();
				ItemType t = ItemType.getItemType(tid);
				if (t != null)
					cell.items.add(t.generate()
							.setCount(count));
				break;

			case T_UNIT:
				break;

			default:
				throw new IOException("Invalid object type " + type);
		}
	}

	private HashMap<Integer, IsoSprite> getSpriteMap(byte place) throws IOException
	{
		switch (place)
		{
			case P_FLOOR:
				return GameResources.main.floors;
			case P_WALL:
				return GameResources.main.walls;
			case P_OBJECT:
				return GameResources.main.objects;
			case P_WOBJECT:
				return GameResources.main.wobjects;
			default:
				throw new IOException("Invalid place " + place);
		}
	}
}
