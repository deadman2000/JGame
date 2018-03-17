package com.deadman.dh.isometric.editor;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;

import com.deadman.dh.Game;
import com.deadman.dh.IsoEditor_ui;
import com.deadman.dh.R;
import com.deadman.dh.isometric.HitResult;
import com.deadman.dh.isometric.IsoCursor;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.isometric.IsoSimpleObject;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.systemui.IMainMenuListener;
import com.deadman.jgame.systemui.MainMenuItem;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.CheckGroup;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ListViewItem;

public class IsoEditor extends GameEngine implements IMainMenuListener, ItemSelectForm.IItemSelected
{
	private IsoMap map;
	private IsoEditor_ui ui;

	private MapCell[][] buff;

	private IsoCursor curWall, curFloor, curObject;

	private int currentPage = -1;
	private Button[] tabButtons;
	private IsoSprite currentInstrument;
	private byte rotation;
	private MapCell boundBeg, boundEnd;
	private boolean resizing = false;
	private boolean clear;

	private Drawable CURSOR_MOVE_X = getDrawable(R.cursors.move_x);
	private Drawable CURSOR_MOVE_Y = getDrawable(R.cursors.move_y);
	private Drawable CURSOR_MOVE_XY = getDrawable(R.cursors.move_xy);

	// TODO Убрать
	public static GameFont fnt_light_3x5 = getFont(R.fonts.font3x5, 0xFFd7ab34);
	public static GameFont fnt_medium_3x5 = getFont(R.fonts.font3x5, 0xFFa47801);
	public static GameFont fnt_dark_3x5 = getFont(R.fonts.font3x5, 0xFF7c5b03);

	enum EditMode
	{
		SELECTION,
		PAINT
	};

	public IsoEditor()
	{
		init();
		loadMap("res/maps/scenario.map");
		//createMap("res/maps/temp.map", 4, 4, 1, 0);
	}

	public IsoEditor(String fileName)
	{
		init();
		loadMap(fileName);
	}

	private void init()
	{
		GameResources.init();

		ui = new IsoEditor_ui();
		setContent(ui);

		createItems(GameResources.main.floors);
		createItems(GameResources.main.walls);
		createItems(GameResources.main.objects);
		createItems(GameResources.main.wobjects);

		ui.mainMenu.listener = this;

		ui.btSelect.addControlListener(modes_listener);
		ui.btBrush.addControlListener(modes_listener);
		new CheckGroup(ui.btSelect, ui.btBrush);

		ui.rciObject.addControlListener(rowCellItem_listener);
		ui.rciWObjectLeft.addControlListener(rowCellItem_listener);
		ui.rciWObjectRight.addControlListener(rowCellItem_listener);
		ui.rciWallLeft.addControlListener(rowCellItem_listener);
		ui.rciWallRight.addControlListener(rowCellItem_listener);
		ui.rciFloor.addControlListener(rowCellItem_listener);
		ui.rciUnit.addControlListener(rowCellItem_listener);
		ui.lvCellItems.setScrollBar(Game.createVScrollInfo());
		ui.btCellItemAdd.addControlListener(cellItemAdd_listener);
		ui.btCellItemRemove.addControlListener(cellItemRemove_listener);

		ui.lvItems.setScrollBar(Game.createHScrollGray());
		ui.lvItems.addControlListener(lvInstr_listener);
		ui.tbSearch.addControlListener(search_listener);
		ui.btSearchCancel.addControlListener(searchCancel_listener);
		ui.ctlItemsLeft.addControlListener(itemsScroll_listener);
		ui.ctlItemsRight.addControlListener(itemsScroll_listener);

		tabButtons = new Button[4];
		tabButtons[0] = ui.btTab0;
		tabButtons[1] = ui.btTab1;
		tabButtons[2] = ui.btTab2;
		tabButtons[3] = ui.btTab3;
		new CheckGroup(tabButtons);

		for (int i = 0; i < tabButtons.length; i++)
		{
			Button b = tabButtons[i];
			b.tag = i;
			b.addControlListener(tab_listener);
		}

		curFloor = new IsoCursor(R.iso.cursor_floor);
		curWall = new IsoCursor(R.iso.cursor_wall);
		curObject = new IsoCursor();

		ui.btTab0.setChecked(true);
		ui.btSelect.setChecked(true);

		submitChilds();
	}

	private void createItems(HashMap<Integer, IsoSprite> map)
	{
		for (IsoSprite s : map.values())
			ui.lvItems.addItem(new InstrItem(s));
	}

	public void updateMode(EditMode mode)
	{
		switch (mode)
		{
			case SELECTION:
				ui.colPaint.hide();
				ui.rowSelect.show();
				ui.mapViewer.cursor = IsoCursor.CURSOR_RECT;
				ui.mapViewer.removeControlListener(mapViewerPaint_listener);
				ui.mapViewer.addControlListener(mapViewerSelect_listener);
				break;
			case PAINT:
				ui.colPaint.show();
				ui.rowSelect.hide();
				selectInstrument(currentInstrument);
				ui.mapViewer.removeControlListener(mapViewerSelect_listener);
				ui.mapViewer.addControlListener(mapViewerPaint_listener);
				ui.mapViewer.selectedCell = null;
				break;
		}
	}

	@Override
	public void onMainMenuPressed(MainMenuItem item)
	{
		if (item == ui.mmiNew)
			newFile();
		else if (item == ui.mmiOpen)
			openFile();
		else if (item == ui.mmiSave)
			map.saveMap();
		else if (item == ui.mmiSaveAs)
			saveAs();
	}

	private void newFile()
	{
		new CreateMapForm(this).showModal();
	}

	private void openFile()
	{
		new OpenMapForm(this).showModal();
	}

	private void saveAs()
	{
		new SaveMapForm(this).showModal();
	}

	private void setStatus(String text)
	{
		ui.statusBar.setText(text);
	}

	public IsoMap getMap()
	{
		return map;
	}

	public void createMap(String fileName, int w, int h, int l, int zl)
	{
		IsoMap m = new IsoMap(w, h, l, zl);
		m.fileName = fileName;
		setMap(m);
	}

	public void loadMap(String fileName)
	{
		setMap(IsoMap.loadMap(fileName));
	}

	void setMap(IsoMap m)
	{
		if (m == null)
		{
			System.err.println("Can't load map");
			return;
		}
		map = m;
		ui.mapViewer.setMap(m);
		ui.mapViewer.centerView();

		GameScreen.screen.setTitle(m.fileName);

		buff = new MapCell[map.width][map.height];
	}

	protected void selectPage(int ind)
	{
		if (currentPage == ind) return;

		ui.tbSearch.isFocused = false;
		currentPage = ind;

		tabButtons[ind].setChecked(true);
		if (ui.tbSearch.text.length() == 0) // Обновляем lv только если фильтр пустой
		{
			filterInstrByType(ind);
		}
	};

	// Events

	private ControlListener mapViewerSelect_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			if (e.getButton() == 1) selectCell(ui.mapViewer.focusedCell);
		}
	};

	private ControlListener mapViewerPaint_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			if (currentInstrument != null && ui.mapViewer.focusedCell != null && e.getButton() == 1)
			{
				if (currentInstrument.type != IsoSprite.HELPERS)
				{
					boundBeg = ui.mapViewer.focusedCell;
					setOverlay(boundBeg, boundBeg);
				}
				else
				{
					if (currentInstrument.id == 1)
					{
						setNextState(p);
					}
				}

				e.consume();
				return;
			}
		}

		public void onMouseMove(Control control, Point p, MouseEvent e)
		{
			if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) > 0)
			{
				if (boundBeg != null && (boundEnd == null || !boundEnd.equals(ui.mapViewer.focusedCell)))
				{
					if (ui.mapViewer.prevFocused != null) unsetOverlay(boundBeg, ui.mapViewer.prevFocused);
					if (ui.mapViewer.focusedCell != null) setOverlay(boundBeg, ui.mapViewer.focusedCell);

					boundEnd = ui.mapViewer.focusedCell;
				}
			}
		}

		public void onReleased(Object sender, Point p, MouseEvent e)
		{
			if (boundBeg != null && ui.mapViewer.focusedCell != null)
			{
				for (int x = 0; x < map.width; x++)
					for (int y = 0; y < map.height; y++)
						buff[x][y] = null;
				map.calcLights();
			}
			boundBeg = null;
			boundEnd = null;
		}

		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_ITEM_SELECTED)
			{
				MapCell c = (MapCell) tag;
				if (c == null) return;
				setStatus(c + " L:" + c.obj_l + "  R:" + c.obj_r);

				if (resizing)
				{
					if (c.x == 0 || c.x == map.width - 1)
					{
						if (c.y > 0 && c.y < map.height - 1)
							cursor = CURSOR_MOVE_Y;
						else
							cursor = CURSOR_MOVE_XY;
					}
					else if (c.y == 0 || c.y == map.height - 1)
					{
						if (c.x > 0 && c.x < map.width - 1)
							cursor = CURSOR_MOVE_X;
						else
							cursor = CURSOR_MOVE_XY;
					}
					else
						cursor = null;
				}
			}
		}
	};

	private ControlListener lvInstr_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_ITEM_SELECTED)
			{
				if (tag != null)
				{
					selectInstrument((IsoSprite) ui.lvItems.selectedItem().tag);
				}
			}
		}
	};

	ControlListener search_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_VALUE_CHANGED)
			{
				filterInstrByPattern(ui.tbSearch.text);
				ui.tbSearch.isFocused = true;
			}
		};
	};

	ControlListener searchCancel_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			ui.tbSearch.clear();
		};
	};

	ControlListener tab_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_CHECKED)
			{
				selectPage((int) ((Button) sender).tag);
			}
		};
	};

	ControlListener modes_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_CHECKED)
			{
				EditMode mode;
				if (sender == ui.btSelect)
					mode = EditMode.SELECTION;
				else if (sender == ui.btBrush)
					mode = EditMode.PAINT;
				else
					return;

				updateMode(mode);
			}
		};
	};

	ControlListener itemsScroll_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			if (sender == ui.ctlItemsLeft)
				ui.lvItems.scrollLeft();
			else if (sender == ui.ctlItemsRight) ui.lvItems.scrollRight();
		}
	};

	ControlListener rowCellItem_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			if (e.getButton() == 1)
				selectCellProperty((PropertyRow) sender);
			else if (e.getButton() == 3)
				clearCellProperty((PropertyRow) sender);
		}
	};

	ControlListener cellItemAdd_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			addCellItem();
		}
	};

	ControlListener cellItemRemove_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			removeCellItem();
		}
	};

	// Keys

	@Override
	public void onKeyPressed(KeyEvent e)
	{
		super.onKeyPressed(e);
		if (e.isConsumed()) return;

		if (e.isShiftDown())
		{
			clear = true;
		}

		if (e.isControlDown())
		{
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_N:
					newFile();
					break;
				case KeyEvent.VK_S:
					if (e.isShiftDown())
						saveAs();
					else
						map.saveMap();
					break;
				case KeyEvent.VK_O:
					openFile();
					break;

				default:
					super.onKeyPressed(e);
					return;
			}

			e.consume();
			return;
		}

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_OPEN_BRACKET:
				if (ui.btBrush.checked)
				{
					rotation--;
					if (rotation < 0) rotation = (byte) (instrRotating() - 1);
					selectInstrument(currentInstrument);
				}
				break;
			case KeyEvent.VK_CLOSE_BRACKET:
				if (ui.btBrush.checked)
				{
					rotation++;
					if (rotation >= instrRotating()) rotation = 0;
					selectInstrument(currentInstrument);
				}
				break;

			case KeyEvent.VK_F5:
				loadPrevMap();
				break;
			case KeyEvent.VK_F6:
				loadNextMap();
				break;

			case KeyEvent.VK_G:
				ui.mapViewer.drawGrid = !ui.mapViewer.drawGrid;
				break;
			case KeyEvent.VK_B:
				ui.mapViewer.wallBlending = !ui.mapViewer.wallBlending;
				break;
			case KeyEvent.VK_R:
				setResizing(!resizing);
				break;

			default:
				super.onKeyPressed(e);
				return;
		}

		e.consume();
	}

	@Override
	public void onKeyReleased(KeyEvent e)
	{
		super.onKeyReleased(e);

		if (!e.isShiftDown() && clear)
		{
			clear = false;
		}
	}

	// Instruments

	private void setNextState(Point p)
	{
		HitResult result = ui.mapViewer.screenToObject(p.x, p.y);
		if (result == null)
			return;

		System.out.println("Hit on " + result.object);

		if (result.object instanceof IsoObject)
		{
			IsoObject obj = (IsoObject) result.object;
			if (obj instanceof IsoSimpleObject) ((IsoSimpleObject) obj).setNextState();
		}
		else
			System.err.println("Unknown hit result type " + result.object);
	}

	private int instrRotating()
	{
		if (currentInstrument == null) return 0;
		if (currentInstrument.type == IsoSprite.OBJ_ON_WALL) return 2;
		return currentInstrument.rotating;
	}

	private void selectInstrument(IsoSprite instr)
	{
		if (instr != null)
			setStatus(instr.toString());
		currentInstrument = instr;
		if (rotation >= instrRotating()) rotation = (byte) (instrRotating() - 1);

		if (currentInstrument == null)
		{
			ui.mapViewer.cursor = curFloor;
		}
		else
			switch (currentInstrument.type)
			{
				case IsoSprite.WALL:
					ui.mapViewer.cursor = curWall;
					break;
				case IsoSprite.FLOOR:
					ui.mapViewer.cursor = curFloor;
					break;
				case IsoSprite.OBJECT:
					curObject.front = currentInstrument.getState(rotation, 0);
					ui.mapViewer.cursor = curObject;
					break;
				case IsoSprite.OBJ_ON_WALL:
					curObject.front = currentInstrument.getState(rotation, 0);
					ui.mapViewer.cursor = curObject;
					break;
				case IsoSprite.HELPERS:
					if (currentInstrument.id == 0)
					{
						curObject.front = currentInstrument.getState(rotation, 0);
						ui.mapViewer.cursor = curObject;
					}
					else if (currentInstrument.id == 1) ui.mapViewer.cursor = null;
					break;
				default:
					return;
			}

		if (instr == null)
			ui.lvItems.selectItem(null);
		else if (ui.lvItems.selectedItem() != null && ui.lvItems.selectedItem().tag != instr)
			ui.lvItems.selectByTag(instr);

		selectPage(currentInstrument.type); // Меняем выбранную вкладку
	}

	private void setResizing(boolean enabled)
	{
		resizing = enabled;
		if (enabled)
			selectInstrument(null);
		else
			cursor = null;
	}

	private void filterInstrByPattern(String pattern)
	{
		if (pattern.isEmpty())
		{
			filterInstrByType(currentPage);
			return;
		}

		pattern = pattern.toLowerCase();

		for (ListViewItem lvi : ui.lvItems.items)
		{
			InstrItem ii = (InstrItem) lvi;
			ii.visible = isMatch(ii.sprite, pattern);
		}

		ui.lvItems.update(true);
		ui.lvItems.selectFirst();
	}

	private void filterInstrByType(int type)
	{
		boolean isSelected = false;
		for (ListViewItem lvi : ui.lvItems.items)
		{
			InstrItem ii = (InstrItem) lvi;
			ii.visible = ii.sprite.type == type;

			if (ii.sprite == currentInstrument)
			{
				ui.lvItems.selectItem(lvi);
				isSelected = true;
			}
		}
		ui.lvItems.update(true);
		if (!isSelected)
			ui.lvItems.selectFirst();
	}

	private boolean isMatch(IsoSprite spr, String pattern)
	{
		return spr.name.toLowerCase().indexOf(pattern) != -1;
	}

	// Overlay

	private void setOverlay(MapCell c1, MapCell c2)
	{
		int x1 = Math.min(c1.x, c2.x);
		int y1 = Math.min(c1.y, c2.y);
		int x2 = Math.max(c1.x, c2.x);
		int y2 = Math.max(c1.y, c2.y);

		if (clear)
		{
			if (currentInstrument.type == IsoSprite.WALL)
			{
				if (x1 == x2 && y1 == y2) return;

				if (x1 != x2)
				{
					for (int x = x1; x <= x2 - 1; x++) // Right wall  \
					{
						setOverlay(x, y1);
						map.cells[ui.mapViewer.viewZ][x][y1].wall_r = null;

						if (y1 != y2 && y2 < map.height)
						{
							setOverlay(x, y2);
							map.cells[ui.mapViewer.viewZ][x][y2].wall_r = null;
						}
					}
				}

				if (y1 != y2)
				{
					for (int y = y1; y <= y2 - 1; y++) //  Left wall /
					{
						setOverlay(x1, y);
						map.cells[ui.mapViewer.viewZ][x1][y].wall_l = null;

						if (x1 != x2 && x2 < map.width)
						{
							setOverlay(x2, y);
							map.cells[ui.mapViewer.viewZ][x2][y].wall_l = null;
						}
					}
				}
			}
			else
			{
				for (int x = x1; x <= x2; x++)
					for (int y = y1; y <= y2; y++)
					{
						setOverlay(x, y);
						MapCell c = map.cells[ui.mapViewer.viewZ][x][y];
						switch (currentInstrument.type)
						{
							case IsoSprite.FLOOR:
								c.floor = null;
								break;
							case IsoSprite.OBJECT:
								c.obj = null;
								break;
							case IsoSprite.OBJ_ON_WALL:
								if (rotation % 2 == 1)
									c.obj_l = null;
								else
									c.obj_r = null;
								break;
							default:
								System.err.println("Wrong usage IsoEditor.setOverlay with type: " + currentInstrument.type);
								break;
						}
					}
			}
		}
		else // !clear
		{
			if (currentInstrument.type == IsoSprite.WALL)
			{
				if (x1 == x2 && y1 == y2) return;

				if (x1 != x2)
				{
					for (int x = x1; x <= x2 - 1; x++) //   \
					{
						setOverlay(x, y1);
						currentInstrument.setTo(map.cells[ui.mapViewer.viewZ][x][y1], currentInstrument.getRandomState(1));

						if (y1 != y2 && y2 < map.height)
						{
							setOverlay(x, y2);
							currentInstrument.setTo(map.cells[ui.mapViewer.viewZ][x][y2], currentInstrument.getRandomState(1));
						}
					}
				}

				if (y1 != y2)
				{
					for (int y = y1; y <= y2 - 1; y++) //   /
					{
						setOverlay(x1, y);
						currentInstrument.setTo(map.cells[ui.mapViewer.viewZ][x1][y], currentInstrument.getRandomState(0));

						if (x1 != x2 && x2 < map.width)
						{
							setOverlay(x2, y);
							currentInstrument.setTo(map.cells[ui.mapViewer.viewZ][x2][y], currentInstrument.getRandomState(0));
						}
					}
				}
			}
			else
			{
				for (int x = x1; x <= x2; x++)
					for (int y = y1; y <= y2; y++)
					{
						setOverlay(x, y);
						if (currentInstrument.type == IsoSprite.OBJ_ON_WALL)
						{
							if (rotation % 2 == 1)
								currentInstrument.setToL(map.cells[ui.mapViewer.viewZ][x][y]);
							else
								currentInstrument.setToR(map.cells[ui.mapViewer.viewZ][x][y]);
						}
						else
							currentInstrument.setTo(map.cells[ui.mapViewer.viewZ][x][y], currentInstrument.getRandomState(rotation));
					}
			}
		}
	}

	private void setOverlay(int x, int y)
	{
		if (buff[x][y] == null)
		{
			MapCell c = map.cells[ui.mapViewer.viewZ][x][y];
			buff[x][y] = c;
			map.cells[ui.mapViewer.viewZ][x][y] = c.copy(map, x, y, ui.mapViewer.viewZ);
		}
	}

	private void unsetOverlay(MapCell c1, MapCell c2)
	{
		for (int x = 0; x < map.width; x++)
			for (int y = 0; y < map.height; y++)
			{
				MapCell c = buff[x][y];
				if (c != null)
				{
					map.cells[ui.mapViewer.viewZ][x][y] = c;
					buff[x][y] = null;
				}
			}
	}

	private void loadPrevMap()
	{
		File curr = new File(map.fileName).getAbsoluteFile();
		File folder = curr.getParentFile();
		File[] files = folder.listFiles(mapsFilter);
		if (files.length == 1) return;

		boolean found = false;
		for (int i = files.length - 1; i >= 0; i--)
		{
			File f = files[i];
			if (f.getAbsoluteFile().equals(curr))
			{
				found = true;
				continue;
			}

			if (found)
			{
				loadMap(f.getAbsolutePath());
				return;
			}
		}

		loadMap(files[files.length - 1].getAbsolutePath());
	}

	private void loadNextMap()
	{
		File curr = new File(map.fileName).getAbsoluteFile();
		File folder = curr.getParentFile();
		File[] files = folder.listFiles(mapsFilter);
		if (files.length == 1) return;

		boolean found = false;
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if (f.getAbsoluteFile().equals(curr))
			{
				found = true;
				continue;
			}

			if (found)
			{
				loadMap(f.getAbsolutePath());
				return;
			}
		}

		loadMap(files[0].getAbsolutePath());
	}

	FileFilter mapsFilter = new FileFilter()
	{
		@Override
		public boolean accept(File f)
		{
			return !f.isDirectory() && f.getName().endsWith(".map");
		}
	};

	// Selection

	private PropertyRow currentCellItem;

	private void selectCell(MapCell cell)
	{
		selectCellProperty(null);

		ui.mapViewer.selectedCell = cell;
		ui.cellView.setCell(cell);
		ui.laCellAddress.setText(cell);

		ui.rciObject.setValue(cell.obj);
		ui.rciWObjectLeft.setValue(cell.obj_l);
		ui.rciWObjectRight.setValue(cell.obj_r);
		ui.rciWallLeft.setValue(cell.wall_l);
		ui.rciWallRight.setValue(cell.wall_r);
		ui.rciFloor.setValue(cell.floor);
		ui.rciUnit.setValue(cell.ch);

		refreshCellItems();
	}

	private void refreshCellItems()
	{
		ui.lvCellItems.clear();

		MapCell cell = ui.mapViewer.selectedCell;
		if (cell == null || cell.items == null) return;

		for (Item it : cell.items)
			ui.lvCellItems.addItem(new CellItemItem(it));
	}

	private void selectCellProperty(PropertyRow it)
	{
		if (currentCellItem != null)
			currentCellItem.unselect();
		currentCellItem = it;
		if (it != null)
		{
			it.select();
			showProperties((IsoObject) it.value);
		}
		else
			hideProperties();
	}

	private void clearCellProperty(PropertyRow it)
	{
		((IsoObject) it.value).remove();
		selectCell(ui.mapViewer.selectedCell);
	}

	private void hideProperties()
	{
	}

	private void showProperties(IsoObject value)
	{
	}

	private void addCellItem()
	{
		if (ui.mapViewer.selectedCell == null) return;
		new ItemSelectForm(this).showModal();
	}

	@Override
	public void onItemSelected(Item it)
	{
		ui.mapViewer.selectedCell.putOnFloor(it);
		refreshCellItems();
	}

	protected void removeCellItem()
	{
		CellItemItem cii = (CellItemItem) ui.lvCellItems.selectedItem();
		if (cii == null) return;

		if (ui.mapViewer.selectedCell == null) return;
		ui.mapViewer.selectedCell.removeFromFloor(cii.item);
		ui.lvCellItems.removeItem(cii);
	}

	class CellItemItem extends ListViewItem
	{
		public final Item item;

		public CellItemItem(Item it)
		{
			item = it;

			height = ItemSlot.ITEM_HEIGHT + 2;
			addControl(new Label(getFont(R.fonts.font3x5, 0xffffffff), ItemSlot.ITEM_WIDTH + 4, 6, it.type.name + " x" + it.count));
		}

		@Override
		protected void onDraw()
		{
			item.type.icon.drawAt(scrX + ItemSlot.ITEM_WIDTH / 2 + 1, scrY + ItemSlot.ITEM_HEIGHT / 2 + 1);
			super.onDraw();
		}

		@Override
		public void onSelected()
		{
			bgrColor = 0x40ffffff;
		}

		@Override
		public void onDeselected()
		{
			bgrColor = 0;
		}
	}
}
