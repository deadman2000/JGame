package com.deadman.dh.isometric;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.deadman.dh.Game;
import com.deadman.dh.battle.BattleSide;
import com.deadman.dh.model.Rectangle;
import com.deadman.dh.model.items.Item;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.RelativeLayout;

public class IsoViewer extends Control
{
	IsoMap map;

	public boolean focusInBounds = false;
	public MapCell focusedCell, prevFocused; // Ячейка, на которую навели мышь

	public MapCell selectedCell; // Выделенная ячейка (например в редакторе карт)

	public IsoCursor cursor;
	public boolean showCursor = true;
	public boolean drawGrid;

	public int viewX = -150, viewY = 0, viewZ = 0;
	protected int d_viewX, d_viewY, d_viewZ;

	int minDrawX, maxDrawX, minDrawY, maxDrawY;

	public boolean wallBlending = false;
	public boolean allLevels = false;
	public boolean allowDrag = true; // Перетаскивание левой кнопкой мыши
	public boolean showAll = false;

	private static float BLEND_ALPHA = 0.6f;
	private static float BLEND_RGB = 0.7f;

	public static Drawable pic_floor, pic_trace;

	public BattleSide currentSide;

	private ArrayList<IsoControl> isoControls = new ArrayList<>();

	public ICellDrawer customDrawer;

	public IsoViewer()
	{
		clip = true;
		setLayout(new RelativeLayout());
	}

	public static void showMap(IsoMap m)
	{
		GameEngine eng = new GameEngine();
		IsoViewer viewer = new IsoViewer();
		eng.addControl(viewer);

		viewer.cursor = IsoCursor.CURSOR_RECT;

		RelativeLayout	.settings(viewer)
						.fill();
		viewer.setMap(m);
		viewer.centerView();
		eng.show();
	}

	public static void showMap(Point pt)
	{
		IsoMap map = Game.map	.getSubTerrain(pt.x - 1, pt.y - 1, 3, 3)
								.getIsoMap();
		showMap(map);
	}

	public void setMap(IsoMap m)
	{
		map = m;
		if (map != null)
			setViewXY(viewX, viewY);
	}

	@Override
	protected void onResize()
	{
		super.onResize();
		setViewXY(viewX, viewY);
	}

	private int maxdz;
	private int px, py;
	private MapCell drawCell;
	private boolean discovered;
	private boolean blendAll;

	@Override
	public void onDraw()
	{
		if (map == null) return;

		d_viewX = viewX;
		d_viewY = viewY;
		d_viewZ = viewZ;

		int mindx = minDrawX;
		int maxdx = maxDrawX;
		int mindy = minDrawY;
		int maxdy = maxDrawY;
		maxdz = allLevels ? map.zheight - 1 : d_viewZ;

		// Рисуем все снизу вверх
		for (int z = 0; z <= maxdz; z++)
		{
			int dz = z * MapCell.LEVEL_HEIGHT;
			MapCell[][] zLevel = map.cells[z];

			blendAll = wallBlending && z == viewZ;

			// !! Вариант "рисовать весь пол на уровне, потом все объекты" не работает. персонаж между этажами рисуется поверх пола 
			for (int x = mindx; x <= maxdx; x++)
			{
				int px1 = scrX + x * MapCell.CELL_WIDTH - d_viewX;
				int py1 = scrY + x * MapCell.CELL_HEIGHT - d_viewY - dz;

				for (int y = mindy; y <= maxdy; y++)
				{
					px = px1 - y * MapCell.CELL_WIDTH;
					py = py1 + y * MapCell.CELL_HEIGHT;

					drawCell = zLevel[x][y];
					discovered = isDiscovered(drawCell);

					drawFloor();
					drawWalls();
					drawObjects();

					if (customDrawer != null)
						customDrawer.drawCell(drawCell, px, py);
				}
			}
		}

		for (IsoControl c : isoControls)
		{
			c.scrX = c.cellX * MapCell.CELL_WIDTH - c.cellY * MapCell.CELL_WIDTH - d_viewX + 18 + c.x;
			c.scrY = c.cellX * MapCell.CELL_HEIGHT + c.cellY * MapCell.CELL_HEIGHT - d_viewY + 44 + c.y;
			c.draw();
		}

		GameScreen.screen.resetColorFull();
	}

	boolean isDiscovered(MapCell cell)
	{
		return showAll || currentSide == null || currentSide.isDiscovered(cell);
	}

	public static Drawable cellGrid;

	private void drawFloor()
	{
		if (drawCell.floor != null && !drawCell.floor.sprite.drawOnTop)
		{
			if (discovered)
				GameScreen.screen.setBrightness(drawCell.light);
			else
				GameScreen.screen.setBrightness(0); // Рисуем тень пола
			drawCell.floor.drawAt(px, py);
		}

		if (drawGrid && drawCell.z == viewZ)
			cellGrid.drawAt(px, py);
	}

	private void drawWalls()
	{
		if (discovered)
		{
			GameScreen.screen.setBrightness(drawCell.light);

			// Стены
			if (drawCell.wall_l != null)
			{
				if (blendAll || (drawCell.state & MapCell.BLEND_WALL_L_FLAG) > 0)
					GameScreen.screen.setColorMask(BLEND_RGB, BLEND_RGB, BLEND_RGB, BLEND_ALPHA);
				else
					GameScreen.screen.resetColorFull();

				Drawable d = ((IsoSimpleObject) drawCell.wall_l).getDrawable();
				if (drawCell.x > 0 && drawCell.y < map.height - 1 && map.cells[drawCell.z][drawCell.x - 1][drawCell.y + 1].wall_r != null)
				{
					int w;
					if (drawCell.wall_r != null)
						w = 16;
					else
						w = 34;

					if (w > d.width - 2) w = d.width - 2;
					d.drawAt(px + 2, py, 2, 0, w, d.height);
				}
				else
				{
					int w;
					if (drawCell.wall_r != null)
						w = 18;
					else
						w = 36;
					if (w > d.width) w = d.width;
					d.drawAt(px, py, w, d.height);
				}
			}

			if (drawCell.wall_r != null)
			{
				if (blendAll || (drawCell.state & MapCell.BLEND_WALL_R_FLAG) > 0)
					GameScreen.screen.setColorMask(BLEND_RGB, BLEND_RGB, BLEND_RGB, BLEND_ALPHA);
				else
					GameScreen.screen.resetColorFull();

				drawCell.wall_r.drawAt(px, py);
			}

			if (drawCell.wall_l != null && drawCell.wall_r != null)
			{
				Drawable d = ((IsoSimpleObject) drawCell.wall_l).sprite.getState(2, 0);
				if (d != null)
				{
					if (blendAll || (drawCell.state & MapCell.BLEND_WALL_L_FLAG) > 0 || (drawCell.state & MapCell.BLEND_WALL_R_FLAG) > 0)
						GameScreen.screen.setColorMask(BLEND_RGB, BLEND_RGB, BLEND_RGB, BLEND_ALPHA);
					else
						GameScreen.screen.resetColorFull();

					d.drawAt(px, py); // Стык стен
				}
			}
		}
		else // Ячейка невидима
		{
			if (drawCell.wall_r != null)
			{
				MapCell c = map.getCell(drawCell.x, drawCell.y - 1, drawCell.z);
				if (c != null && isDiscovered(c)) // Соседняя открыта
				{
					if (drawCell.wall_r.isDoor())
						GameScreen.screen.setBrightness(drawCell.light); // Рисуем дверь
					else
						GameScreen.screen.setBrightness(0); // Рисуем тень стены

					if (blendAll || (drawCell.state & MapCell.BLEND_WALL_R_FLAG) > 0)
						GameScreen.screen.setColorMask(BLEND_RGB, BLEND_RGB, BLEND_RGB, BLEND_ALPHA);
					else
						GameScreen.screen.resetColorFull();

					drawCell.wall_r.drawAt(px, py);
				}
			}
			if (drawCell.wall_l != null)
			{
				MapCell c = map.getCell(drawCell.x - 1, drawCell.y, drawCell.z);
				if (c != null && isDiscovered(c)) // Соседняя открыта
				{
					if (drawCell.wall_l.isDoor())
						GameScreen.screen.setBrightness(drawCell.light); // Рисуем дверь
					else
						GameScreen.screen.setBrightness(0); // Рисуем тень стены

					if (blendAll || (drawCell.state & MapCell.BLEND_WALL_L_FLAG) > 0)
						GameScreen.screen.setColorMask(BLEND_RGB, BLEND_RGB, BLEND_RGB, BLEND_ALPHA);
					else
						GameScreen.screen.resetColorFull();

					drawCell.wall_l.drawAt(px, py);
				}
			}
		}

		GameScreen.screen.resetColorFull();
	}// TODO Объединить

	void drawObjects()
	{
		if (drawCell.floor != null && drawCell.floor.sprite.drawOnTop)
		{
			if (discovered)
				GameScreen.screen.setBrightness(drawCell.light);
			else
				GameScreen.screen.setBrightness(0); // Рисуем тень пола
			drawCell.floor.drawAt(px, py);
		}

		GameScreen.screen.resetColorFull();

		if (discovered && (drawCell.state & MapCell.FLOOR_FLAG) > 0) // Область передвижения
			pic_floor.drawAt(px, py);

		// Курсор (задник)
		if (showCursor && focusedCell != null && cursor != null)
		{
			if (drawCell == focusedCell)
			{
				if (cursor.back != null) cursor.back.drawAt(px, py);
			}
			else if (cursor.bottom_back != null && drawCell.x == focusedCell.x && drawCell.y == focusedCell.y && drawCell.z < focusedCell.z)
			{
				if (cursor.bottom_back != null) cursor.bottom_back.drawAt(px, py);
			}
		}

		if (drawCell == selectedCell)
		{
			IsoCursor.CURSOR_RECT.back.drawAt(px, py);
		}

		if (discovered)
		{
			if (blendAll || (drawCell.state & MapCell.BLEND_OBJ_FLAG) > 0)
				GameScreen.screen.setColorMask(BLEND_RGB, BLEND_RGB, BLEND_RGB, BLEND_ALPHA);
			GameScreen.screen.setBrightness(drawCell.light);
			// http://stackoverflow.com/questions/19284065/isometric-engine-drawing-issue

			// Объекты на стенах
			if (drawCell.obj_l != null) drawCell.obj_l.drawAt(px, py);
			if (drawCell.obj_r != null) drawCell.obj_r.drawAt(px, py);

			int zshift = drawCell.getZShift();

			if (zshift == 0 && drawCell.items != null) // Предметы на полу
				for (Item it : drawCell.items)
				it.drawIsoAt(px, py);

			if (drawCell.obj != null)
				drawCell.obj.drawAt(px, py);

			if (zshift > 0 && drawCell.items != null) // Предметы на объекте
				for (Item it : drawCell.items)
				it.drawIsoAt(px, py - zshift);

			if (drawCell.ch != null)
			{
				if (showAll || currentSide == null || currentSide.isVisible(drawCell))
				{
					drawCell.ch.drawAt(maxdz, drawCell, px, py);
				}

				if (drawCell.ch.hasSound())
				{
					float fx = ((float) (px + 17) / GameScreen.GAME_WIDTH - 0.5f); // -0.5 ... 0.5
					float fy = ((float) (py + 23) / GameScreen.GAME_HEIGHT - 0.5f);
					drawCell.ch.updateSounds(fx, fy);
				}
			}

			// Частицы
			if (drawCell.particle != null)
			{
				Particle p = drawCell.particle;
				while (p != null)
				{
					p.drawAt(px, py);
					p = p.next;
				}
			}

			if ((drawCell.state & MapCell.TRACE_FLAG) > 0) // Траектория передвижения
			{
				GameScreen.screen.resetColorFull();
				pic_trace.drawAt(px, py - drawCell.getZShift());
			}
		}

		GameScreen.screen.resetColorFull();

		// Курсор (передник)
		if (showCursor && focusedCell != null && cursor != null)
		{
			if (drawCell == focusedCell)
			{
				if (cursor.front != null)
				{
					cursor.front.drawAt(px, py);
				}
			}
			else if (cursor.bottom_front != null && drawCell.x == focusedCell.x && drawCell.y == focusedCell.y && drawCell.z < focusedCell.z)
			{
				if (cursor.bottom_front != null) cursor.bottom_front.drawAt(px, py);
			}
		}

		if (drawCell == selectedCell)
		{
			IsoCursor.CURSOR_RECT.front.drawAt(px, py);
		}
	}

	void setViewXY(int x, int y)
	{
		viewX = x;
		viewY = y;

		if (map != null)
		{
			minDrawX = screenToCell(0, 0, 0, true).x;
			maxDrawX = screenToCell(width + MapCell.CELL_WIDTH, height + MapCell.LEVEL_HEIGHT, map.zheight - 1, true).x;
			minDrawY = screenToCell(width + MapCell.CELL_WIDTH, 0, 0, true).y;
			maxDrawY = screenToCell(0, height + MapCell.LEVEL_HEIGHT, map.zheight - 1, true).y;
		}
	}

	void setViewZ(int z)
	{
		if (z < 0 || z >= map.zheight) return;

		viewZ = z;
		if (focusedCell != null)
			focusCell(focusedCell.x, focusedCell.y, viewZ);
	}

	public void centerView()
	{
		if (map == null) return;

		int vx = map.height * MapCell.CELL_WIDTH - map.width * MapCell.CELL_WIDTH - width + 35;
		int vy = map.height * MapCell.CELL_HEIGHT + map.width * MapCell.CELL_HEIGHT - height + 60;

		setViewXY(vx / 2, vy / 2);
	}

	public void setCenter(Rectangle rect)
	{
		int x = rect.x + rect.width / 2;
		int y = rect.y + rect.height / 2;

		int px = x * MapCell.CELL_WIDTH - y * MapCell.CELL_WIDTH - width / 2;
		int py = x * MapCell.CELL_HEIGHT + y * MapCell.CELL_HEIGHT - height / 2;
		setViewXY(px, py);
	}

	private MapCell screenToCell(int scrX, int scrY, boolean boundsLimit)
	{
		return screenToCell(scrX, scrY, viewZ, boundsLimit);
	}

	private MapCell screenToCell(int scrX, int scrY, int scrZ, boolean boundsLimit)
	{
		// Координаты относительно точки отсчета карты
		int x = scrX + viewX - MapCell.CELL_WIDTH;
		int y = scrY + viewY + MapCell.LEVEL_HEIGHT * scrZ - MapCell.LEVEL_HEIGHT;

		int mapX = (y << 1) + x;
		int mapY = (y << 1) - x;
		if (!boundsLimit && (mapX < 0 || mapY < 0)) return null;

		mapX /= MapCell.CELL_WIDTH2;
		mapY /= MapCell.CELL_WIDTH2;

		if (boundsLimit)
		{
			if (mapX < 0) mapX = 0;
			if (mapX >= map.width) mapX = map.width - 1;
			if (mapY < 0) mapY = 0;
			if (mapY >= map.height) mapY = map.height - 1;
		}
		else
		{
			if (mapX < 0 || mapX >= map.width || mapY < 0 || mapY >= map.height) return null;
		}

		return map.cells[scrZ][mapX][mapY];
	}

	private void focusCell(int x, int y, int z)
	{
		focusCell(map.cells[z][x][y]);
	}

	private void focusCell(MapCell cell)
	{
		prevFocused = focusedCell;
		focusedCell = cell;

		onCellFocused();
	}

	protected void onCellFocused()
	{
		onAction(ACTION_ITEM_SELECTED, focusedCell);
	}

	Point drag_begin;

	@Override
	public void pressMouse(Point p, MouseEvent e)
	{
		super.pressMouse(p, e);
		if (!intersectLocal(p)) return;

		if (allowDrag || e.getButton() > 1)
		{
			drag_begin = p.getLocation();
			drag_begin.translate(viewX, viewY);
			e.consume();
		}
	}

	@Override
	public void moveMouse(Point p, MouseEvent e)
	{
		super.moveMouse(p, e);
		if (drag_begin != null)
		{
			setViewXY(drag_begin.x - p.x, drag_begin.y - p.y);
			e.consume();
			return;
		}

		if (mouseFocused)
		{
			MapCell cell = screenToCell(p.x, p.y, focusInBounds);
			if (focusedCell != cell)
				focusCell(cell);
		}
	}

	@Override
	public void releaseMouse(Point p, MouseEvent e)
	{
		super.releaseMouse(p, e);
		drag_begin = null;
	}

	@Override
	public void onKeyPressed(KeyEvent e)
	{
		super.onKeyPressed(e);
		if (e.isConsumed()) return;

		//System.out.println(e);
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				close();
				break;

			case KeyEvent.VK_LEFT:
				viewX -= MapCell.CELL_WIDTH2;
				break;
			case KeyEvent.VK_RIGHT:
				viewX += MapCell.CELL_WIDTH2;
				break;
			case KeyEvent.VK_UP:
				viewY -= MapCell.CELL_HEIGHT2;
				break;
			case KeyEvent.VK_DOWN:
				viewY += MapCell.CELL_HEIGHT2;
				break;

			case KeyEvent.VK_A:
				allLevels = !allLevels;
				break;
			case KeyEvent.VK_B:
				wallBlending = !wallBlending;
				break;

			case KeyEvent.VK_PAGE_UP:
				setViewZ(viewZ + 1);
				break;

			case KeyEvent.VK_PAGE_DOWN:
				setViewZ(viewZ - 1);
				break;

			default:
				return;
		}

		e.consume();
	}

	public HitResult screenToObject(int scrX, int scrY)
	{
		int maxz = allLevels ? map.zheight - 1 : viewZ;
		for (int z = maxz; z >= 0; z--)
		{
			MapCell cell = screenToCell(scrX, scrY, z, false);
			if (cell == null) return null;

			int minx = Math.max(cell.x - 2, 0);
			int maxx = Math.min(cell.x + 2, map.width - 1);

			int miny = Math.max(cell.y - 2, 0);
			int maxy = Math.min(cell.y + 2, map.height - 1);

			for (int y = maxy; y >= miny; y--)
				for (int x = maxx; x >= minx; x--)
				{
					cell = map.cells[z][x][y];
					HitResult result = getObjectFromCell(cell, scrX, scrY);
					if (result != null) return result;
				}
		}

		return null;
	}

	private HitResult getObjectFromCell(MapCell cell, int scrX, int scrY)
	{
		// Координаты относительно ячейки
		int px = scrX - (cell.x * MapCell.CELL_WIDTH - cell.y * MapCell.CELL_WIDTH - d_viewX);
		int py = scrY - (cell.x * MapCell.CELL_HEIGHT + cell.y * MapCell.CELL_HEIGHT - d_viewY - cell.z * MapCell.LEVEL_HEIGHT);

		return cell.getObjectAt(px, py);
	}

	public void addIsoControl(IsoControl ctrl)
	{
		isoControls.add(ctrl);
		ctrl.viewer = this;
	}

	public void removeIsoControl(IsoControl ctrl)
	{
		isoControls.remove(ctrl);
		ctrl.viewer = null;
	}

	public void clearIsoControls()
	{
		isoControls.clear();
	}
}
