package com.deadman.dh.city;

import java.util.ArrayList;
import java.util.Random;

import com.deadman.dh.R;
import com.deadman.dh.isometric.IsoMap;

public class BuildingType
{
	public final int square;

	public final int width, height;
	public final int color;
	public final IsoMap map;

	public byte category = HOUSE;

	public BuildingType(IsoMap m, int c)
	{
		map = m;
		width = m.width;
		height = m.height;
		color = c;

		int s = 0;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				for (int z = 0; z < map.zheight; z++)
					if (m.cells[z][x][y].floor != null)
						s++;
		square = s;
	}

	public long getPrice()
	{
		return square * 10;
	}

	// Типы домов по расположению входа
	public static BuildingType[] N, S, W, E;
	public static ArrayList<BuildingType> all;

	public static void initHouseTypes()
	{
		if (all != null) return;

		all = new ArrayList<>();

		N = findHouseTypes(R.maps.houseN);
		S = findHouseTypes(R.maps.houseS);
		W = findHouseTypes(R.maps.houseW);
		E = findHouseTypes(R.maps.houseE);
	}

	private static BuildingType[] findHouseTypes(int[] ids)
	{
		BuildingType[] arr = new BuildingType[ids.length];
		Random rnd = new Random();
		for (int i = 0; i < ids.length; i++)
		{
			IsoMap m = IsoMap.loadMap(ids[i]);
			BuildingType t = new BuildingType(m, rnd.nextInt() | 0xFF000000);
			arr[i] = t;
			all.add(t);
		}
		return arr;
	}

	public static final byte HOUSE = 0x0;
}
