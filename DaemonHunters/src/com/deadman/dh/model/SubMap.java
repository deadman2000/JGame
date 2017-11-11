package com.deadman.dh.model;

import java.util.Random;

import com.deadman.dh.Game;
import com.deadman.dh.city.City;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.isometric.IsoSprite;

public class SubMap
{
	public byte[][] map;
	public double x, y, width, height;

	public int mapWidth, mapHeight;

	public SubMap(byte[][] arr, double tx, double ty, double tw, double th)
	{
		map = arr;
		mapWidth = map.length;
		mapHeight = map[0].length;

		x = tx;
		y = ty;
		width = tw;
		height = th;
	}

	private IsoMap iso;
	private Random rnd;

	IsoSprite bigTree, bigTreeL2, smallTree, smallTreeL2, smallTree2;

	public IsoMap getIsoMap()
	{
		if (iso != null) return iso;

		IsoSprite sea = IsoSprite.byName("Sea");
		IsoSprite dirt = IsoSprite.byName("Dirt");
		IsoSprite grass = IsoSprite.byName("Grass");
		IsoSprite bigGrass = IsoSprite.byName("BigGrass");
		IsoSprite sand = IsoSprite.byName("Sand");

		bigTree = IsoSprite.byName("BigTree");
		bigTreeL2 = IsoSprite.byName("BigTree-L2");
		smallTree = IsoSprite.byName("SmallTree");
		smallTreeL2 = IsoSprite.byName("SmallTree-L2");
		smallTree2 = IsoSprite.byName("SmallTree2");

		// Пропорции заполнения деревьями в лесу
		byte[] fp = new byte[] { 8, 12, 80 }; // TODO Перенести в константы
		int fs = fp[0] + fp[1] + fp[2];

		// Пропорции заполнения деревьями на полянах
		byte[] tp = new byte[] { 1, 5, 100 }; // TODO Перенести в константы
		int ts = tp[0] + tp[1] + tp[2];

		iso = new IsoMap(mapWidth, mapHeight, 5, 0);
		iso.fillFloor(0);

		rnd = new Random();

		for (int x = 0; x < mapWidth; x++)
		{
			for (int y = 0; y < mapHeight; y++)
			{
				byte t = map[x][y];
				MapCell cell = iso.getCell(x, y, iso.zeroLevel);

				if (GlobalMap.isFlag(t, GlobalMap.TYPE_RIVER) || GlobalMap.isFlag(t, GlobalMap.TYPE_SEA))
					sea.setTo(cell);
				else if (GlobalMap.isFlag(t, GlobalMap.TYPE_MOUNTAIN))
					dirt.setTo(cell);
				else if (t == GlobalMap.FOREST_ID)
				{
					int floorRnd = rnd.nextInt(100);
					if (floorRnd < 20)
						grass.setTo(cell);
					else if (floorRnd < 40)
						bigGrass.setTo(cell);
					else
						dirt.setTo(cell);

					int k = rnd.nextInt(fs);
					if (k < fp[0])
						setBigTree(x, y);
					else if (k < fp[0] + fp[1])
						setSmallTree(x, y);
				}
				else if (t == GlobalMap.TERRAIN_ID)
				{
					int floorRnd = rnd.nextInt(100);

					if (floorRnd < 40)
						grass.setTo(cell);
					else if (floorRnd < 80)
						bigGrass.setTo(cell);
					else
						dirt.setTo(cell);

					int k = rnd.nextInt(ts);
					if (k < tp[0])
						setBigTree(x, y);
					else if (k < tp[0] + tp[1])
						setSmallTree(x, y);
				}
				else if (t == GlobalMap.COAST_ID || t == GlobalMap.COASTLINE_ID)
				{
					sand.setTo(cell);
				}
				else
					System.err.println("Not implemented terr type " + t);
			}
		}

		int l = (int) x;
		int r = (int) Math.round(x + width + 0.5);
		int t = (int) y;
		int b = (int) Math.round(y + height + 0.5);

		// Города
		for (City city : Game.map.cities)
		{
			if (city.x >= l && city.x <= r && city.y >= t && city.y <= b)
			{
				System.out.println(city);
				//city.generate();
			}
		}

		return iso;
	}

	private void setBigTree(int x, int y)
	{
		bigTree.setTo(iso.cells[iso.zeroLevel][x][y]);
		for (int z = iso.zeroLevel + 1; z < iso.zheight; z++)
			bigTreeL2.setTo(iso.cells[z][x][y]);
	}

	private void setSmallTree(int x, int y)
	{
		if (rnd.nextInt() > 50)
		{
			smallTree.setTo(iso.cells[iso.zeroLevel][x][y]);
			smallTreeL2.setTo(iso.cells[iso.zeroLevel + 1][x][y]);
		}
		else
		{
			smallTree2.setTo(iso.cells[iso.zeroLevel][x][y]);
		}
	}
}
