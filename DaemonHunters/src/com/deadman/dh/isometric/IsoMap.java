package com.deadman.dh.isometric;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.io.DataInputStreamBE;
import com.deadman.jgame.io.DataOutputStream;
import com.deadman.jgame.resources.ResourceManager;

public class IsoMap
{
	public final MapCell[][][] cells; // [z][x][y]
	public final int width, height, zheight;
	public int zeroLevel;
	public String fileName;
	private float light = 1.0f;

	public IsoMap(int w, int h, int l, int zl)
	{
		width = w;
		height = h;
		zheight = l;
		zeroLevel = zl;
		cells = new MapCell[l][w][h];

		for (short z = 0; z < l; z++)
		{
			MapCell[][] rowZ = cells[z];
			for (short x = 0; x < w; x++)
			{
				MapCell[] rowX = rowZ[x];
				for (short y = 0; y < h; y++)
					rowX[y] = new MapCell(this, x, y, z);
			}
		}
	}

	public IsoMap(IsoMapInfo info)
	{
		this(info.width, info.height, info.zheight, info.zerolevel);
	}

	public MapCell getCell(int x, int y, int z)
	{
		if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= zheight) return null;
		return cells[z][x][y];
	}

	public void fillFloor(int id)
	{
		fillFloor(GameResources.main.floors.get(id));
	}

	public void fillFloor(IsoSprite spr)
	{
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				MapCell c = cells[zeroLevel][x][y];
				spr.setTo(c);
			}
	}

	public void copyTo(IsoMap m, int tx, int ty)
	{
		for (short z = 0; z < zheight; z++)
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++)
				{
					int cx = tx + x;
					int cy = ty + y;
					MapCell c = cells[z][x][y].copy(m, cx, cy, z);
					m.cells[z][cx][cy] = c;
				}
	}

	public void mixTo(IsoMap m, int tx, int ty)
	{
		for (short z = 0; z < zheight; z++)
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++)
				{
					int cx = tx + x;
					int cy = ty + y;
					cells[z][x][y].mixTo(m.cells[z][cx][cy]);
				}
	}

	public void mixToRandom(IsoMap m, int tx, int ty)
	{
		for (short z = 0; z < zheight; z++)
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++)
				{
					int cx = tx + x;
					int cy = ty + y;
					cells[z][x][y].mixToRandom(m.cells[z][cx][cy]);
				}
	}
	
	//region Save/Load

	public void saveMap()
	{
		saveMap(fileName);
	}

	public void saveMap(String fileName)
	{
		this.fileName = fileName;

		try
		{
			DataOutputStream out = DataOutputStream.createZip(fileName);
			MapFormat.save(this, out);
			out.close();
			System.out.println("Map saved to '" + fileName + "'");
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, ex.toString());
			ex.printStackTrace();
		}
	}

	public static IsoMap loadMap(String fileName)
	{
		System.out.println("Load map " + fileName);

		File f = new File(fileName);
		if (!f.exists()) return null;

		try
		{
			IsoMap map = loadMap(DataInputStreamBE.readZip(fileName));
			map.fileName = f.getAbsolutePath();
			return map;
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, e.toString());
			e.printStackTrace();
			return null;
		}
	}

	public static IsoMap loadMap(int id)
	{
		return loadMap(ResourceManager.getResource(id)
				.getGZip());
	}

	public static IsoMap loadMap(DataInputStreamBE in)
	{
		try	
		{
			return MapFormat.load(in);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, ex.toString());
			ex.printStackTrace();
			return null;
		}
	}

	//endregion

	//region Light

	private static boolean DEBUG_LIGHT = false;

	public void setLight(float l)
	{
		light = l;
		calcLights();
	}

	public float getLight()
	{
		return light;
	}

	public void calcLights()
	{
		ArrayList<MapCell> lights = new ArrayList<>();

		for (int z = 0; z < zheight; z++)
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++)
				{
					MapCell cell = cells[z][x][y];
					if (cell.getLightSource() != 0.f)
						lights.add(cell);

					cell.light = light;
				}

		long __t = System.currentTimeMillis();
		for (MapCell s : lights)
		{
			new CalcFOV(this, null, LightMode.LIGHT, (int) s.getLightSource(), s).calculate();
			//new CalcFOV2D(this, LightMode.LIGHT, (int) s.getLightStrength(), s).calculate();
		}
		if (DEBUG_LIGHT && lights.size() > 0) System.out.println("Lights count: " + lights.size() + " Calc time: " + (System.currentTimeMillis() - __t));
	}

	//endregion

	// События
	private ArrayList<MapChangeListener> mapChangeListeners;

	public void addChangeListener(MapChangeListener l)
	{
		if (mapChangeListeners == null)
			mapChangeListeners = new ArrayList<>();
		mapChangeListeners.add(l);
	}

	public void removeChangeListener(MapChangeListener l)
	{
		mapChangeListeners.remove(l);
		if (mapChangeListeners.size() == 0)
			mapChangeListeners = null;
	}

	public void onChanged()
	{
		if (mapChangeListeners != null)
			for (MapChangeListener l : mapChangeListeners)
			l.onMapChanged();
	}

	public void onMoving(MapCell cell)
	{
		if (mapChangeListeners != null)
			for (MapChangeListener l : mapChangeListeners)
			l.onMoving(cell);
	}

	public void upgrade()
	{
		for (int z = 0; z < zheight; z++)
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++)
				{
					MapCell cell = cells[z][x][y];
					if (cell.obj_l != null)
						upgradeWallO(cell.obj_l);
					if (cell.obj_r != null)
						upgradeWallO(cell.obj_r);
				}
	}

	private void upgradeWallO(IsoSimpleObject o)
	{
		if (o.sprite.id == 2)
		{
			IsoSprite newSprite = null;

			byte r = o.getRotation();
			if (r >= 2 && r <= 3)
			{
				newSprite = GameResources.main.getIso("Balk2");
			}
			else if (r >= 4 && r <= 5)
			{
				newSprite = GameResources.main.getIso("Balk3");
			}
			else if (r >= 6 && r <= 7)
			{
				newSprite = GameResources.main.getIso("Balk4");
			}

			if (newSprite != null)
				o.sprite = newSprite;
		}

		o.setRotation((byte) 0);
		//System.out.println("  " + o + " R:" + o.getRotation());
	}
}
