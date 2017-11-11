package com.deadman.dh.isometric;

import java.util.Arrays;

import com.deadman.dh.battle.BattleCell;

public class CalcFOV
{
	private IsoMap map;
	private int radius;
	private MapCell source;
	private LightMode mode;
	private byte direction = -1;
	private BattleCell[][][] bcells;

	static final boolean DEBUG = false;

	/**
	 * http://www.roguebasin.com/index.php?title=FOV_using_recursive_shadowcasting
	 * http://www.robotacid.com/flash/red_rogue/shadow_casting/fov.pdf
	 */
	public CalcFOV(IsoMap map, BattleCell[][][] bcells, LightMode mode, int radius, MapCell source)
	{
		this.map = map;
		this.bcells = bcells;
		this.radius = radius;
		this.source = source;
		this.mode = mode;
	}

	public CalcFOV(IsoMap map, BattleCell[][][] bcells, LightMode mode, int radius, MapCell source, byte direction)
	{
		this.map = map;
		this.bcells = bcells;
		this.radius = radius;
		this.source = source;
		this.mode = mode;
		this.direction = direction;
	}

	public void calculate()
	{
		init();

		// Подготавливаем карту освещения
		Arrays.fill(weights, 0f);
		weights[getWInd(0, 0, 0)] = 1f; // light the starting cell

		//castLight("I", 1, 0f, 1f, 0f, 1f, LightDirection.NNET, 1f);
		if (direction == -1)
		{
			for (LightDirection dir : LightDirection.values())
				castLight(dir.toString(), 1, 0f, 1f, 0f, 1f, dir, 1f);
		}
		else
		{
			for (LightDirection dir : directions[direction])
				castLight(dir.toString(), 1, 0f, 1f, 0f, 1f, dir, 1f);
		}

		// Накладываем карту освещения на карту мира
		int minZ = source.z < radius ? -source.z : -radius;
		int maxZ = source.z + radius >= map.zheight ? map.zheight - source.z - 1 : radius;

		int minY = source.y < radius ? -source.y : -radius;
		int maxY = source.y + radius >= map.height ? map.height - source.y - 1 : radius;

		int minX = source.x < radius ? -source.x : -radius;
		int maxX = source.x + radius >= map.width ? map.width - source.x - 1 : radius;

		for (int z = minZ; z <= maxZ; z++)
		{
			int cz = source.z + z;
			int indz = (z + SIZE_Z) * W_XYLEN;
			for (int y = minY; y <= maxY; y++)
			{
				int cy = source.y + y;
				int indy = indz + (y + SIZE) * W_XLEN + SIZE;
				for (int x = minX; x <= maxX; x++)
				{
					int cx = source.x + x;

					MapCell c = map.cells[cz][cx][cy];
					float w = weights[indy + x];
					if (w > 0)
					{
						switch (mode)
						{
							case LIGHT:
								c.light += w;
								break;

							case VISION:
								BattleCell bc = bcells[cz][cx][cy];
								bc.calc_visible = true;
								break;

							default:
								break;
						}
					}
				}
			}
		}
	}

	public void castLight(String src, int startRow, float minSlopeXY, float maxSlopeXY, float minSlopeZY, float maxSlopeZY, LightDirection dir, float rate)
	{
		if (maxSlopeXY < minSlopeXY) return;
		if (maxSlopeZY < minSlopeZY) return;
		if (DEBUG) System.out.printf("%s Calc from row %d  Rate:%f   Slope-X: (%f - %f)  Slope-Z (%f - %f)\r\n", src, startRow, rate, minSlopeXY, maxSlopeXY, minSlopeZY, maxSlopeZY);

		float newMaxXY = maxSlopeXY;

		for (int row = startRow; row <= radius; row++) // Обходим строки луча. в каждой строке квадрат [row x row] 
		{
			int deltaY = -row;
			for (int deltaX = -row; deltaX <= 0; deltaX++)
			{
				// Рассчитываем угол луча по X
				float cellMinSlopeXY = (deltaX + 0.5f) / (deltaY - 0.5f);
				float cellMaxSlopeXY = (deltaX - 0.5f) / (deltaY + 0.5f);
				if (cellMinSlopeXY > maxSlopeXY) // Пропускаем если еще не дошли до своего диапазона
					continue;
				if (cellMaxSlopeXY < minSlopeXY) // Заканчиваем если вышли из своего диапазона
					break;

				float prevMaxZY = maxSlopeZY; // Ограничение по Z для нескольких препятствий в ряду

				//for (int deltaZ = 0; deltaZ <= row; deltaZ++)
				for (int deltaZ = -row; deltaZ <= 0; deltaZ++)
				{
					// Рассчитываем угол луча по Z
					float cellMinSlopeZY = (deltaZ + 0.5f) / (deltaY - 0.5f);
					float cellMaxSlopeZY = (deltaZ - 0.5f) / (deltaY + 0.5f);
					if (cellMinSlopeZY > maxSlopeZY)
						continue;
					if (cellMaxSlopeZY < minSlopeZY)
						break;

					int dx = deltaX * dir.xx + deltaY * dir.xy;
					int dy = deltaX * dir.yx + deltaY * dir.yy + deltaZ * dir.yz;
					int dz = deltaY * dir.zy + deltaZ * dir.zz;

					MapCell cell = map.getCell(source.x + dx, source.y + dy, source.z + dz);
					if (cell == null) continue;

					if (DEBUG) System.out.printf("%s %d-%d Calc %s  CellSlope: X(%f %f) Z(%f %f)\r\n", src, startRow, row, cell, cellMinSlopeXY, cellMaxSlopeXY, cellMinSlopeZY, cellMaxSlopeZY);
					float r = cell.getLightRate(source); // Пропускание света в эту ячейку

					float shadowRate = rate * r; // Итоговое пропускание света

					float dist = squares[dz + SIZE_Z][dx + SIZE][dy + SIZE];
					if (dist <= radius)
					{
						float bright = shadowRate * (1 - dist / radius);
						if (bright > 1f) bright = 1f;

						int ind = getWInd(dz, dx, dy);
						if (weights[ind] < bright)
						{
							if (DEBUG) System.out.printf("%s %d-%d Bright %s => %f   Slope-X: (%f - %f)  Slope-Z (%f - %f)\r\n", src, startRow, row, cell, bright, cellMinSlopeXY, cellMaxSlopeXY, cellMinSlopeZY, cellMaxSlopeZY);
							if (DEBUG) System.out.printf("\t\tSlope-X: (%f - %f)  Slope-Z (%f - %f)\r\n", minSlopeXY, maxSlopeXY, minSlopeZY, maxSlopeZY);
							weights[ind] = bright;
						}
					}

					if (r < 1f && row < radius)
					{
						if (DEBUG) System.out.printf("%s %d Blocked %s  Rate: %f  CellSlope: X(%f %f) Z(%f %f)\r\n", src, row, cell, r, cellMinSlopeXY, cellMaxSlopeXY, cellMinSlopeZY, cellMaxSlopeZY);
						//if (shadowRate > 0) castLight(row + 1, cellMinSlopeXY, cellMaxSlopeXY, cellMinSlopeZY, cellMaxSlopeZY, dir, shadowRate); // X

						/**
						 *  _________  <
						 * |   |2|   |  \/
						 * |   |_|   |
						 * | 0 |X| 1 |
						 * |   |0|   |
						 * |___|_|___|
						 * min       max
						 *  0 - Текущий луч
						 */

						if (cellMinSlopeXY < newMaxXY)
							castLight(src + "-R1", row + 1, Math.max(cellMaxSlopeXY, maxSlopeXY), maxSlopeXY, minSlopeZY, maxSlopeZY, dir, rate); // 1
						if (cellMaxSlopeZY < prevMaxZY)
							castLight(src + "-R2", row + 1, cellMinSlopeXY, cellMaxSlopeXY, cellMaxSlopeZY, prevMaxZY, dir, rate); // 2

						newMaxXY = cellMinSlopeXY;
						prevMaxZY = cellMinSlopeZY;
						if (DEBUG) System.out.printf("%s  %s New maxXY %f\r\n", src, cell, newMaxXY);
						if (DEBUG) System.out.printf("%s  %s New maxZY %f\r\n", src, cell, prevMaxZY);
					}
				} // deltaZ

				/**
				 *  _________  <
				 * |   |_|   |  \/
				 * |   |X|   |
				 * | 0 | |   |
				 * |   |0|   |
				 * |___|_|___|
				 *  0 - Текущий луч
				 */
				if (prevMaxZY < maxSlopeZY && prevMaxZY > minSlopeZY) // Остатки в строке
					castLight(src + "-R0", row + 1, Math.max(cellMinSlopeXY, minSlopeXY), Math.min(cellMaxSlopeXY, maxSlopeXY), minSlopeZY, prevMaxZY, dir, rate);

				maxSlopeXY = newMaxXY;
				if (maxSlopeXY < minSlopeXY)
					break;
			} // deltaX

			if (maxSlopeXY < minSlopeXY)
				break;
		} // row
	}

	private static LightDirection[][] directions = new LightDirection[][] {
			// 0 - N
			new LightDirection[] { LightDirection.NNWB, LightDirection.NNWT, LightDirection.NNEB, LightDirection.NNET, LightDirection.NEB, LightDirection.NET, LightDirection.NWB, LightDirection.NWT },
			// 1 - N-E
			new LightDirection[] { LightDirection.NNEB, LightDirection.NNET, LightDirection.NEB, LightDirection.NET, LightDirection.ENEB, LightDirection.ENET },
			// 2 - E
			new LightDirection[] { LightDirection.NEB, LightDirection.NET, LightDirection.ENEB, LightDirection.ENET, LightDirection.ESEB, LightDirection.ESET, LightDirection.SEB, LightDirection.SET },
			// 3 - S-E
			new LightDirection[] { LightDirection.ESEB, LightDirection.ESET, LightDirection.SEB, LightDirection.SET, LightDirection.SSEB, LightDirection.SSET },
			// 4 - S
			new LightDirection[] { LightDirection.SEB, LightDirection.SET, LightDirection.SSEB, LightDirection.SSET, LightDirection.SSWB, LightDirection.SSWT, LightDirection.SWB, LightDirection.SWT },
			// 5 - S-W
			new LightDirection[] { LightDirection.SSWB, LightDirection.SSWT, LightDirection.SWB, LightDirection.SWT, LightDirection.WSWB, LightDirection.WSWT },
			// 6 - W
			new LightDirection[] { LightDirection.WSWB, LightDirection.WSWT, LightDirection.WNWB, LightDirection.WNWT, LightDirection.NWB, LightDirection.NWT, LightDirection.SWB, LightDirection.SWT },
			// 7 - N-W
			new LightDirection[] { LightDirection.WNWB, LightDirection.WNWT, LightDirection.NWB, LightDirection.NWT, LightDirection.NNWB, LightDirection.NNWT }
	};

	public enum LightDirection
	{
		NNEB(1, 0, 0, 1, 0, 0, 1),
		NNET(1, 0, 0, 1, 0, 0, -1),

		NEB(-1, 0, 0, 0, 1, 1, 0),
		NET(-1, 0, 0, 0, 1, -1, 0),

		ENEB(-1, 0, 0, 1, 0, 0, 1),
		ENET(-1, 0, 0, 1, 0, 0, -1),

		ESEB(0, -1, 1, 0, 0, 0, 1),
		ESET(0, -1, 1, 0, 0, 0, -1),

		SEB(-1, 0, 0, 0, -1, 1, 0),
		SET(-1, 0, 0, 0, -1, -1, 0),

		SSEB(0, -1, -1, 0, 0, 0, 1),
		SSET(0, -1, -1, 0, 0, 0, -1),

		WSWB(1, 0, 0, -1, 0, 0, 1),
		WSWT(1, 0, 0, -1, 0, 0, -1),

		SSWB(-1, 0, 0, -1, 0, 0, 1),
		SSWT(-1, 0, 0, -1, 0, 0, -1),

		SWB(1, 0, 0, 0, -1, 1, 0),
		SWT(1, 0, 0, 0, -1, -1, 0),

		WNWB(0, 1, -1, 0, 0, 0, 1),
		WNWT(0, 1, -1, 0, 0, 0, -1),

		NWB(1, 0, 0, 0, 1, 1, 0),
		NWT(1, 0, 0, 0, 1, -1, 0),

		NNWB(0, 1, 1, 0, 0, 0, 1),
		NNWT(0, 1, 1, 0, 0, 0, -1);

		
		
		LightDirection(int xx, int xy, int yx, int yy, int yz, int zy, int zz)
		{
			this.xx = xx;
			this.xy = xy;
			this.yx = yx;
			this.yy = yy;
			this.yz = yz;
			this.zy = zy;
			this.zz = zz;
		}

		public final int xx;
		public final int xy;
		public final int yx;
		public final int yy;
		public final int yz;
		public final int zy;
		public final int zz;
	}

	private static final int SIZE = 40; // Проверить с максимальным размером свечения
	private static final int SIZE_Z = SIZE / MapCell.Z_HEIGHT;
	private static final int W_ZLEN = SIZE * 2 / MapCell.Z_HEIGHT + 1;
	private static final int W_XLEN = SIZE * 2 + 1;
	private static final int W_YLEN = SIZE * 2 + 1;
	private static final int W_XYLEN = W_XLEN * W_YLEN;

	private static final float[] weights = new float[W_ZLEN * W_XLEN * W_YLEN];

	private static int getWInd(int z, int x, int y)
	{
		return (z + SIZE_Z) * W_XYLEN + (y + SIZE) * W_XLEN + (x + SIZE);
	}

	private static final float[][][] squares = new float[W_ZLEN][W_XLEN][W_YLEN];
	static boolean inited = false;

	private static void init()
	{
		if (inited) return;
		inited = true;

		for (int x = 0; x <= SIZE * 2; x++)
			for (int y = 0; y <= SIZE * 2; y++)
				for (int z = 0; z <= SIZE_Z * 2; z++)
				{
					squares[z][x][y] = (float) Math.sqrt((x - SIZE) * (x - SIZE) + (y - SIZE) * (y - SIZE) + (z - SIZE_Z) * (z - SIZE_Z) * MapCell.Z_HEIGHT);
				}
	}
}
