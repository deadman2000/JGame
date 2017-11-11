package com.deadman.dh.isometric;

import java.util.Arrays;

public class CalcFOV2D
{	
	private static float distance(int dx, int dy)
	{
		return squares[SIZE_Z][dx + SIZE][dy + SIZE];
		//return (float) Math.sqrt(dx * dx + dy * dy);
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
					squares[z][x][y] = (float) Math.sqrt((x - SIZE) * (x - SIZE) + (y - SIZE) * (y - SIZE) + (z - SIZE_Z) * (z - SIZE_Z));
				}
	}

	private IsoMap map;
	private int radius;
	private MapCell source;
	private LightMode mode;

	public CalcFOV2D(IsoMap map, LightMode mode, int radius, MapCell source)
	{
		this.map = map;
		this.radius = radius;
		this.source = source;
		this.mode = mode;
	}

	public void calculate()
	{
		init();

		// Подготавливаем карту освещения
		Arrays.fill(weights, 0f);
		weights[getWInd(0, 0, 0)] = 1f; // light the starting cell

		castLight(1, 1.0f, 0.0f, 0, 1, 1, 0, 1f);
		//castLight(1, 1.0f, 0.0f, 0, -1, -1, 0, 1f);

		/*for (int dx = -1; dx <= 1; dx += 2)
			for (int dy = -1; dy <= 1; dy += 2)
			{
				castLight(1, 1.0f, 0.0f, 0, dx, dy, 0, 1f);
				castLight(1, 1.0f, 0.0f, dx, 0, 0, dy, 1f);
			}*/

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
					/*MapCell c = map.getCell(cx, cy, cz);
					if (c == null)
					{
						System.err.println("CalcFOV impossible " + cx + ":" + cy + ":" + cz);
						continue;
					}*/
					//c.light += weights[indy + x];
					float w = weights[indy + x];
					if (w > 0)
					{
						switch (mode)
						{
							case LIGHT:
								c.light += w;
								break;
								
							case VISION:
								//c.calc_visible = true;
								break;

							default:
								break;
						}
					}
				}
			}
		}
	}

	public void castLight(int startRow, float start, float end, int xx, int xy, int yx, int yy, float rate)
	{
		if (start < end) return;

		float newStart = 0.0f;
		boolean blocked = false;

		//System.err.println("Calc from row " + startRow + " " + end + " - " + start + " Rate: " + rate);

		for (int row = startRow; row <= radius && !blocked; row++)
		{
			int deltaY = -row;
			for (int deltaX = -row; deltaX <= 0; deltaX++)
			{
				// Рассчитываем угол луча
				float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
				float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

				if (start <= rightSlope) continue; // TODO Может стоит добавить порог
				if (end >= leftSlope) break;

				int dx = deltaX * xx + deltaY * xy;
				int dy = deltaX * yx + deltaY * yy;
				MapCell cell = map.getCell(source.x + dx, source.y + dy, source.z);
				if (cell == null) continue;

				//System.out.printf("%d => %d : %s  CellSlope: %f %f\r\n", startRow, row, cell, leftSlope, rightSlope);
				
				float r = cell.getLightRate(source); // Пропускание света в эту ячейку

				float shadowRate = rate * r; // Итоговое пропускание света

				float dist = distance(dx, dy);
				if (dist <= radius)
				{
					float bright = shadowRate * (1 - dist / radius);
					if (bright > 1f) bright = 1f;

					int ind = getWInd(0, dx, dy);
					if (weights[ind] < bright)
						weights[ind] = bright;
				}

				if (blocked)
				{
					if (r < 1f)
					{
						if (shadowRate > 0 && row < radius) castLight(row + 1, leftSlope, rightSlope, xx, xy, yx, yy, shadowRate);

						newStart = rightSlope;
						continue;
					}
					else
					{
						blocked = false;
						start = newStart;
					}
				}
				else
				{
					if (r < 1f && row < radius)
					{
						blocked = true;

						if (shadowRate > 0) castLight(row + 1, leftSlope, rightSlope, xx, xy, yx, yy, shadowRate);

						castLight(row + 1, start, leftSlope, xx, xy, yx, yy, rate); // Продолжаем считать уже посчитанный кусок в следующем ряду
						newStart = rightSlope; // А у себя сдвигаем начало
					}
				}
			}
		}
	}
}
