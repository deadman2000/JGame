package com.deadman.dh.isometric;

import java.io.IOException;
import java.util.HashMap;

import com.deadman.dh.model.itemtypes.ItemType;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.io.DataInputStreamBE;
import com.deadman.jgame.io.DataOutputStream;

public class MapFormat0
{
	/**
	 * Заголовок
	 *   размер по y
	 *   размер по x
	 *   размер по z
	 *   уровень-z
	 *   резерв
	 *   резерв
	 * Ячейки
	 *   битовые флаги наличия объектов
	 *     0 - пол
	 *     1 - левая стена
	 *     2 - правая стена
	 *     3 - основной объект
	 *     4 - левый объект
	 *     5 - правый объект
	 *     6 - объекты на полу
	 *   объекты
	 *     id
	 *     состояние
	 */
	public void saveMap(IsoMap map, DataOutputStream out) throws IOException
	{
		out.writeShort(map.height);
		out.writeShort(map.width);
		out.writeShort(map.zheight);
		out.writeShort(map.zeroLevel);
		out.writeShort(0);
		out.writeShort(0);

		for (int z = 0; z < map.zheight; z++)
			for (int y = 0; y < map.height; y++)
				for (int x = 0; x < map.width; x++)
				{
					writeCell(map.cells[z][x][y], out);
				}
	}

	protected void writeCell(MapCell c, DataOutputStream out) throws IOException
	{
		boolean writeObj = c.ownObject();

		int flags = 0;
		if (c.floor != null) flags |= 1 << 0;
		if (c.wall_l != null) flags |= 1 << 1;
		if (c.wall_r != null) flags |= 1 << 2;
		if (writeObj) flags |= 1 << 3;
		if (c.obj_l != null) flags |= 1 << 4;
		if (c.obj_r != null) flags |= 1 << 5;
		out.write(flags);

		if (c.floor != null) writeObject(c.floor, out);
		if (c.wall_l != null) writeObject(c.wall_l, out);
		if (c.wall_r != null) writeObject(c.wall_r, out);
		if (writeObj) writeObject(c.obj, out);
		if (c.obj_l != null) writeObject(c.obj_l, out);
		if (c.obj_r != null) writeObject(c.obj_r, out);
	}

	public IsoMap loadMap(int head, DataInputStreamBE in) throws IOException
	{
		int h = head & 0xFFFF;
		int w = head >>> 16;
		int l = in.readShort() & 0xFFFF;
		int zl = in.readShort() & 0xFFFF;

		IsoMap map = new IsoMap(w, h, l, zl);
		in.readShort();
		in.readShort();

		for (int z = 0; z < l; z++)
			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++)
					readCell(map.cells[z][x][y], in);
		in.close();

		return map;
	}

	private void set(HashMap<Integer, IsoSprite> map, int id, MapCell c, byte state)
	{
		IsoSprite s = map.get(id);
		if (s != null)
			s.setTo(c, state);
		else
		{
			System.err.println("Sprite " + id + " not found");
			if (id == 10001)
				c.putOnFloor(ItemType.getItemType(10001).generate());
			else if (id == 10002)
				c.putOnFloor(ItemType.getItemType(10002).generate());
		}
	}

	protected void readCell(MapCell c, DataInputStreamBE in) throws IOException
	{
		int flags = in.read();

		if ((flags & 1 << 0) != 0)
			set(GameResources.main.floors, in.readInt(), c, in.readByte());

		if ((flags & 1 << 1) != 0)
			set(GameResources.main.walls, in.readInt(), c, in.readByte());

		if ((flags & 1 << 2) != 0)
			set(GameResources.main.walls, in.readInt(), c, in.readByte());

		if ((flags & 1 << 3) != 0)
			set(GameResources.main.objects, in.readInt(), c, in.readByte());

		if ((flags & 1 << 4) != 0)
			set(GameResources.main.wobjects, in.readInt(), c, in.readByte());

		if ((flags & 1 << 5) != 0)
			set(GameResources.main.wobjects, in.readInt(), c, in.readByte());
	}

	// Сериализация
	protected void writeObject(IsoSimpleObject o, DataOutputStream out) throws IOException
	{
		out.writeInt(o.sprite.id);
		out.writeByte(o.getFullState());
	}
}
