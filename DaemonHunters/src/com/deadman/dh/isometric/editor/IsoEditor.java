package com.deadman.dh.isometric.editor;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.isometric.HitResult;
import com.deadman.dh.isometric.IsoCursor;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.isometric.IsoSimpleObject;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.dh.isometric.IsoViewer;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.systemui.IMainMenuListener;
import com.deadman.jgame.systemui.MainMenu;
import com.deadman.jgame.systemui.MainMenuItem;
import com.deadman.jgame.systemui.StatusBar;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.ColumnLayout;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.HListView;
import com.deadman.jgame.ui.ListViewItem;
import com.deadman.jgame.ui.RelativeLayout;
import com.deadman.jgame.ui.Row;
import com.deadman.jgame.ui.RowLayout;
import com.deadman.jgame.ui.TextBox;

public class IsoEditor extends GameEngine implements IMainMenuListener, IIsoEditor
{
	private IsoMap map;
	private IsoViewer mapViewer;

	private MapCell[][] buff;

	private IsoCursor wall_currsor, floor_cursor, obj_cursror;

	public static GameFont fnt_light_3x5 = getFont(R.fonts.font3x5, 0xFFd7ab34);
	public static GameFont fnt_medium_3x5 = getFont(R.fonts.font3x5, 0xFFa47801);
	public static GameFont fnt_dark_3x5 = getFont(R.fonts.font3x5, 0xFF7c5b03);

	private static final int TAB_COUNT = 4;
	private Button[] tabButtons = new Button[TAB_COUNT];
	private ListViewItem[][] items = new ListViewItem[TAB_COUNT][];
	private HListView lvItems;
	private StatusBar statusBar;
	private TextBox tbSearch;
	private EditorButton btSelect, btBrush;

	private MapCell bound_beg, bound_end;

	private int initW = 4, initH = 4, initZH = 1;

	private Drawable CURSOR_MOVE_X = getDrawable(R.cursors.move_x);
	private Drawable CURSOR_MOVE_Y = getDrawable(R.cursors.move_y);
	private Drawable CURSOR_MOVE_XY = getDrawable(R.cursors.move_xy);
	private boolean resizing = false;

	enum EditorMode
	{
		SELECTION,
		PAINT
	};

	private EditorMode mode;

	public IsoEditor()
	{
		init();
		createMap("res/maps/temp.map", initW, initH, initZH, 0);
	}

	public IsoEditor(String fileName)
	{
		init();
		loadMap(fileName);
	}

	private void init()
	{
		GameResources.init();

		initialiseControls();

		floor_cursor = new IsoCursor(R.iso.cursor_floor);
		wall_currsor = new IsoCursor(R.iso.cursor_wall);
		obj_cursror = new IsoCursor();

		selectPage(0);
	}

	private final int MM_FILE_NEW = 1;
	private final int MM_FILE_OPEN = 2;
	private final int MM_FILE_SAVE = 3;
	private final int MM_FILE_SAVEAS = 4;

	private void initialiseControls()
	{
		setLayout(new ColumnLayout(ColumnLayout.H_FILL));

		{
			MainMenu mm = new MainMenu(this, this);
			mm.background = getDrawable(R.editor.ie_menu_bgr);
			mm.subMenuBackground = getDrawable(R.editor.ie_panel_9p);
			mm.font = fnt_light_3x5;
			mm.height = mm.background.height;
			addControl(mm);

			MainMenuItem mmFile = mm.addItem("FILE");
			mmFile.addItem("NEW", MM_FILE_NEW);
			mmFile.addItem("OPEN", MM_FILE_OPEN);
			mmFile.addItem("SAVE", MM_FILE_SAVE);
			mmFile.addItem("SAVE AS", MM_FILE_SAVEAS);

			MainMenuItem mmView = mm.addItem("VIEW");
			mmView.addItem("GRID");
			MainMenuItem mmMap = mm.addItem("MAP");
			mmMap.addItem("RESIZE");
		}

		{
			Row row = new Row();
			ColumnLayout.settings(row).fillHeight();
			row.fillContent();
			addControl(row);

			{
				row.addControl(mapViewer = new IsoViewer());
				RowLayout.settings(mapViewer).fillWidth();
				mapViewer.allow_drag = false;
				mapViewer.show_all = true;
				mapViewer.wall_blending = false;
				mapViewer.addControlListener(mapViewer_listener);
				mapViewer.selectionInBounds = true;
			}

			{ // Right panel
				Control ctRight = new Control();
				ctRight.setWidth(100);
				ctRight.bgrColor = 0x916a02;
				row.addControl(ctRight);

				btSelect = new EditorButton();
				btSelect.setBounds(10, 2, 14, 14);
				btSelect.addControl(new Control(Drawable.get(R.editor.ic_select), 2, 2));
				ctRight.addControl(btSelect);
				btSelect.addControlListener(new ControlListener()
				{
					@Override
					public void onClick(Object sender, Point p, MouseEvent e)
					{
						setMode(EditorMode.SELECTION);
					}
				});

				btBrush = new EditorButton();
				btBrush.setBounds(26, 2, 14, 14);
				btBrush.addControl(new Control(Drawable.get(R.editor.ic_brush), 2, 2));
				ctRight.addControl(btBrush);
				btBrush.addControlListener(new ControlListener()
				{
					@Override
					public void onClick(Object sender, Point p, MouseEvent e)
					{
						setMode(EditorMode.PAINT);
					}
				});

				setMode(EditorMode.SELECTION);
			}
		}

		{
			Row rowTabs = new Row();
			rowTabs.setHeight(20);
			rowTabs.background = getDrawable(R.editor.ie_tabs_bgr);
			rowTabs.setSpacing(-3);
			addControl(rowTabs);

			rowTabs.addControl(new Control(R.editor.ie_tabs_left));

			for (int i = 0; i < TAB_COUNT; i++)
			{
				Button bt = new Button(R.editor.ie_tab_button_bgr, R.editor.ie_tab_button_pressed);
				bt.y = 3;
				bt.check_on_click = true;
				rowTabs.addControl(bt);
				tabButtons[i] = bt;
				bt.tag = i;
				bt.addControlListener(tab_listener);

				bt.image = getDrawable(R.editor.ie_tab_button[i]);
			}

			Control rowfill = new Control();
			RowLayout.settings(rowfill).fillWidth();
			rowTabs.addControl(rowfill);

			Control ctSearchBgr = new Control(R.editor.ie_search_bgr);
			ctSearchBgr.setLayout(new RelativeLayout());
			ctSearchBgr.setWidth(100);
			ctSearchBgr.y = 4;
			rowTabs.addControl(ctSearchBgr);

			{
				tbSearch = new TextBox(fnt_dark_3x5); // , 0, 2, ctSearchBgr.width - 13
				RelativeLayout.settings(tbSearch).alignLeft(0).alignTop(2).alignRight(13);
				tbSearch.addControlListener(search_listener);
				ctSearchBgr.addControl(tbSearch);
	
				Button btSearchCancel = new Button(R.editor.ie_search_cancel);
				RelativeLayout.settings(btSearchCancel).alignTop(3).alignRight(2);
				btSearchCancel.addControlListener(search_cancel_listener);
				ctSearchBgr.addControl(btSearchCancel);
			}

			rowTabs.addControl(new Control(0, 0, 6, 0));
		}

		{
			Row rowItems = new Row();
			rowItems.setHeight(70);
			addControl(rowItems);

			Control left = new Control(R.editor.ie_items_left);
			left.addControlListener(new ControlListener()
			{
				public void onPressed(Object sender, Point p, MouseEvent e)
				{
					lvItems.scrollLeft();
				};
			});
			rowItems.addControl(left);

			Control bgr = new Control(R.editor.ie_items_bgr);
			bgr.setLayout(new RelativeLayout());
			RowLayout.settings(bgr).fillWidth();
			rowItems.addControl(bgr);
			{
				lvItems = new HListView(); // 10, ctBottomLeft.y + 21, GameScreen.GAME_WIDTH - ctBottomLeft.width - ctBottomRight.width, 57 + 11
				lvItems.y = 1;
				lvItems.setHeight(68);
				RelativeLayout.settings(lvItems).fill(0, 1, 0, 1);
				lvItems.setScrollBar(Game.createHScrollGray());
				lvItems.addControlListener(lvInstr_listener);
				bgr.addControl(lvItems);
			}

			Control right = new Control(R.editor.ie_items_right);
			right.addControlListener(new ControlListener()
			{
				public void onPressed(Object sender, Point p, MouseEvent e)
				{
					lvItems.scrollRight();
				};
			});
			rowItems.addControl(right);

			items[0] = makeItems(GameResources.main.floors.values());
			items[1] = makeItems(GameResources.main.walls.values());
			items[2] = makeItems(GameResources.main.objects.values());
			items[3] = makeItems(GameResources.main.wobjects.values());
		}

		{
			addControl(statusBar = new StatusBar());
			statusBar.bgrColor = 0x916a02;
			statusBar.setLeftBackground(getDrawable(R.editor.ie_status_left));
			statusBar.setFont(fnt_light_3x5);
		}

		submitChilds();
	}

	@Override
	public void onMainMenuPressed(MainMenuItem item)
	{
		switch (item.id())
		{
			case MM_FILE_NEW:
				newFile();
				break;
			case MM_FILE_OPEN:
				openFile();
				break;
			case MM_FILE_SAVE:
				map.saveMap();
				break;
			case MM_FILE_SAVEAS:
				saveAs();
				break;
		}
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
		//if (statusBar == null) return;
		statusBar.setText(text.toUpperCase());
	}

	@SuppressWarnings("unused")
	private void setStatus(String format, Object... args)
	{
		setStatus(String.format(format, args));
	}

	private ListViewItem[] makeItems(Collection<IsoSprite> values)
	{
		ListViewItem[] arr = new ListViewItem[values.size()];
		int i = 0;
		for (IsoSprite s : values)
			arr[i++] = new InstrItem(s);
		return arr;
	}

	ControlListener tab_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			selectPage((int) ((Button) sender).tag);
		}
	};

	ControlListener search_cancel_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			tbSearch.clear();
		};
	};

	ControlListener search_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_VALUE_CHANGED)
			{
				filterBy(tbSearch.text);
			}
		};
	};

	void filterBy(String pattern)
	{
		if (pattern.isEmpty())
		{
			ListViewItem[] arr = items[current_page];
			lvItems.setItems(arr);
			for (int i = 0; i < arr.length; i++)
			{
				if (arr[i].tag == curr_instr)
				{
					lvItems.selectIndex(i);
					return;
				}
			}
			lvItems.selectIndex(0);
			return;
		}

		pattern = pattern.toLowerCase();
		ArrayList<ListViewItem> list = new ArrayList<>();
		for (int i = 0; i < TAB_COUNT; i++)
		{
			ListViewItem[] arr = items[i];
			for (int j = 0; j < arr.length; j++)
			{
				ListViewItem it = arr[j];
				IsoSprite spr = (IsoSprite) it.tag;
				if (isMatch(spr, pattern))
					list.add(it);
			}
		}

		lvItems.setItems(list);
		lvItems.selectIndex(0);
	}

	private boolean isMatch(IsoSprite spr, String pattern)
	{
		return spr.name.toLowerCase().indexOf(pattern) != -1;
	}

	private int current_page = -1;

	protected void selectPage(int ind)
	{
		if (current_page == ind) return;

		tbSearch.isFocused = false;
		current_page = ind;
		for (int i = 0; i < TAB_COUNT; i++)
		{
			if (i != ind)
				tabButtons[i].setChecked(false);
		}

		tabButtons[ind].setChecked(true);
		if (tbSearch.text.length() == 0) // Обновляем lv только если фильтр пустой
		{
			lvItems.setItems(items[ind]);
			lvItems.selectIndex(0);
		}
	};

	void setMap(IsoMap m)
	{
		if (m == null)
		{
			System.err.println("Can't load map");
			return;
		}
		map = m;
		mapViewer.setMap(m);
		mapViewer.centerView();

		GameScreen.screen.setTitle(m.fileName);

		buff = new MapCell[map.width][map.height];
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

	public IsoMap getMap()
	{
		return map;
	}

	public void setMode(EditorMode newMode)
	{
		mode = newMode;
		switch (mode)
		{
			case SELECTION:
				btBrush.setChecked(false);
				btSelect.setChecked(true);

				break;

			case PAINT:
				btBrush.setChecked(true);
				btSelect.setChecked(false);
				break;
		}
	}

	private ControlListener lvInstr_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_ITEM_SELECTED)
			{
				if (tag != null)
				{
					selectInstr((IsoSprite) lvItems.selectedItem().tag);
				}
			}
		}
	};

	IsoSprite curr_instr;
	byte rotation;

	private void selectInstr(IsoSprite instr)
	{
		if (instr != null)
			setStatus(instr.toString());
		curr_instr = instr;
		if (rotation >= instrRotating()) rotation = (byte) (instrRotating() - 1);

		if (curr_instr == null)
		{
			mapViewer.cursor = floor_cursor;
		}
		else
			switch (curr_instr.type)
			{
				case IsoSprite.WALL:
					mapViewer.cursor = wall_currsor;
					break;
				case IsoSprite.FLOOR:
					mapViewer.cursor = floor_cursor;
					break;
				case IsoSprite.OBJECT:
					obj_cursror.front = curr_instr.getState(rotation, 0);
					mapViewer.cursor = obj_cursror;
					break;
				case IsoSprite.OBJ_ON_WALL:
					obj_cursror.front = curr_instr.getState(rotation, 0);
					mapViewer.cursor = obj_cursror;
					break;
				case IsoSprite.HELPERS:
					if (curr_instr.id == 0)
					{
						obj_cursror.front = curr_instr.getState(rotation, 0);
						mapViewer.cursor = obj_cursror;
					}
					else if (curr_instr.id == 1) mapViewer.cursor = null;
					break;
				default:
					return;
			}

		if (instr == null)
			lvItems.selectItem(null);
		else if (lvItems.selectedItem() != null && lvItems.selectedItem().tag != instr)
			lvItems.selectByTag(instr);

		selectPage(curr_instr.type); // Меняем выбранную вкладку
	}

	boolean clear;

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
				rotation--;
				if (rotation < 0) rotation = (byte) (instrRotating() - 1);
				selectInstr(curr_instr);
				break;
			case KeyEvent.VK_CLOSE_BRACKET:
				rotation++;
				if (rotation >= instrRotating()) rotation = 0;
				selectInstr(curr_instr);
				break;

			case KeyEvent.VK_F5:
				loadPrevMap();
				break;
			case KeyEvent.VK_F6:
				loadNextMap();
				break;

			case KeyEvent.VK_G:
				mapViewer.drawGrid = !mapViewer.drawGrid;
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

	private int instrRotating()
	{
		if (curr_instr == null) return 0;
		if (curr_instr.type == IsoSprite.OBJ_ON_WALL) return 2;
		return curr_instr.rotating;
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

	private ControlListener mapViewer_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			if (curr_instr != null && mapViewer.selected_cell != null && e.getButton() == 1)
			{
				if (curr_instr.type != IsoSprite.HELPERS)
				{
					bound_beg = mapViewer.selected_cell;
					setOverlay(bound_beg, bound_beg);
				}
				else
				{
					if (curr_instr.id == 1)
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
				if (bound_beg != null && (bound_end == null || !bound_end.equals(mapViewer.selected_cell)))
				{
					if (mapViewer.prev_selected != null) unsetOverlay(bound_beg, mapViewer.prev_selected);
					if (mapViewer.selected_cell != null) setOverlay(bound_beg, mapViewer.selected_cell);

					bound_end = mapViewer.selected_cell;
				}
			}
		}

		public void onReleased(Object sender, Point p, MouseEvent e)
		{
			if (bound_beg != null && mapViewer.selected_cell != null)
			{
				for (int x = 0; x < map.width; x++)
					for (int y = 0; y < map.height; y++)
						buff[x][y] = null;
				map.calcLights();
			}
			bound_beg = null;
			bound_end = null;
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

	private void setResizing(boolean enabled)
	{
		resizing = enabled;
		if (enabled)
			selectInstr(null);
		else
			cursor = null;
	}

	private void setNextState(Point p)
	{
		HitResult result = mapViewer.screenToObject(p.x, p.y);
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

	private void setOverlay(MapCell c1, MapCell c2)
	{
		int x1 = Math.min(c1.x, c2.x);
		int y1 = Math.min(c1.y, c2.y);
		int x2 = Math.max(c1.x, c2.x);
		int y2 = Math.max(c1.y, c2.y);

		if (clear)
		{
			if (curr_instr.type == IsoSprite.WALL)
			{
				if (x1 == x2 && y1 == y2) return;

				if (x1 != x2)
				{
					for (int x = x1; x <= x2 - 1; x++) // Right wall  \
					{
						setOverlay(x, y1);
						map.cells[mapViewer.viewZ][x][y1].wall_r = null;

						if (y1 != y2 && y2 < map.height)
						{
							setOverlay(x, y2);
							map.cells[mapViewer.viewZ][x][y2].wall_r = null;
						}
					}
				}

				if (y1 != y2)
				{
					for (int y = y1; y <= y2 - 1; y++) //  Left wall /
					{
						setOverlay(x1, y);
						map.cells[mapViewer.viewZ][x1][y].wall_l = null;

						if (x1 != x2 && x2 < map.width)
						{
							setOverlay(x2, y);
							map.cells[mapViewer.viewZ][x2][y].wall_l = null;
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
						MapCell c = map.cells[mapViewer.viewZ][x][y];
						switch (curr_instr.type)
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
								System.err.println("Wrong usage IsoEditor.setOverlay with type: " + curr_instr.type);
								break;
						}
					}
			}
		}
		else // !clear
		{
			if (curr_instr.type == IsoSprite.WALL)
			{
				if (x1 == x2 && y1 == y2) return;

				if (x1 != x2)
				{
					for (int x = x1; x <= x2 - 1; x++) //   \
					{
						setOverlay(x, y1);
						curr_instr.setTo(map.cells[mapViewer.viewZ][x][y1], curr_instr.getRandomState(1));

						if (y1 != y2 && y2 < map.height)
						{
							setOverlay(x, y2);
							curr_instr.setTo(map.cells[mapViewer.viewZ][x][y2], curr_instr.getRandomState(1));
						}
					}
				}

				if (y1 != y2)
				{
					for (int y = y1; y <= y2 - 1; y++) //   /
					{
						setOverlay(x1, y);
						curr_instr.setTo(map.cells[mapViewer.viewZ][x1][y], curr_instr.getRandomState(0));

						if (x1 != x2 && x2 < map.width)
						{
							setOverlay(x2, y);
							curr_instr.setTo(map.cells[mapViewer.viewZ][x2][y], curr_instr.getRandomState(0));
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
						if (curr_instr.type == IsoSprite.OBJ_ON_WALL)
						{
							if (rotation % 2 == 1)
								curr_instr.setToL(map.cells[mapViewer.viewZ][x][y]);
							else
								curr_instr.setToR(map.cells[mapViewer.viewZ][x][y]);
						}
						else
							curr_instr.setTo(map.cells[mapViewer.viewZ][x][y], curr_instr.getRandomState(rotation));
					}
			}
		}
	}

	private void setOverlay(int x, int y)
	{
		if (buff[x][y] == null)
		{
			MapCell c = map.cells[mapViewer.viewZ][x][y];
			buff[x][y] = c;
			map.cells[mapViewer.viewZ][x][y] = c.copy(map, x, y, mapViewer.viewZ);
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
					map.cells[mapViewer.viewZ][x][y] = c;
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
}
