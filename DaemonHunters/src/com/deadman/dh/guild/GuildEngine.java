package com.deadman.dh.guild;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.global.TopMenu;
import com.deadman.dh.isometric.ICellDrawer;
import com.deadman.dh.isometric.IsoControl;
import com.deadman.dh.isometric.IsoCursor;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoViewer;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GPoint;
import com.deadman.dh.model.Rectangle;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.RelativeLayout;

public class GuildEngine extends GameEngine implements ICellDrawer
{
	public final Guild guild;

	private GameFont fnt10x14 = getFont(R.fonts.font10x14);

	private IsoViewer mapViewer;
	private IsoMap map;
	private MapCell[][] cells;
	private byte[][] buildMap; // 0 - пусто; 1 - тунель; 2 - вход; 3 - строение

	private Drawable CURSOR_MOVE_X = getDrawable(R.cursors.move_x);
	private Drawable CURSOR_MOVE_Y = getDrawable(R.cursors.move_y);
	private Drawable CURSOR_MOVE_XY = getDrawable(R.cursors.move_xy);

	private Drawable cell_select = getDrawable(R.iso.CellSelect);
	private Drawable cell_select_err = getDrawable(R.iso.CellSelect_0_1);

	private GuildBuildingType instrument;

	private BuildingsPanel buildingsPanel;

	private Control buildingBox; // Окно подтверждения постройки

	public GuildEngine(Guild g)
	{
		guild = g;

		cursor = Game.ItemCursor;

		createControls();

		map = g.getMap();
		cells = map.cells[0];
		mapViewer.setMap(map);
		mapViewer.clearIsoControls();
		mapViewer.centerView();

		buildMap = new byte[guild.width][guild.height];

		for (GPoint p : guild.tunnels)
			buildMap[p.x][p.y] = 1;

		for (GuildBuilding b : guild.buildings)
		{
			byte t = (byte) (b.type.isEntry() ? 2 : 3);
			for (int x = b.rect.x; x <= b.rect.right(); x++)
				for (int y = b.rect.y; y <= b.rect.bottom(); y++)
				{
					buildMap[x][y] = t;
				}

			if (b.isBuild())
				addBuildTime(b);
		}
	}

	private Control buildInfo;
	private Label laBuildName, laBuildInfo, laBuildingName, laBuildingInfo;

	private void createControls()
	{
		mapViewer = new IsoViewer();
		addControl(mapViewer);
		RelativeLayout.settings(mapViewer).fill();
		mapViewer.addControlListener(mapViewer_listener);
		mapViewer.allowDrag = true;
		mapViewer.showAll = true;
		mapViewer.customDrawer = this;
		//mapViewer.setLight(0f);

		addControl(buildingsPanel = new BuildingsPanel(this));
		RelativeLayout.settings(buildingsPanel).alignRightTop(2, 22);
		addControl(new TopMenu(TopMenu.MODE_CLOSE));

		addControl(buildInfo = new Control(4, 4, 120, 120));
		buildInfo.visible = false;
		buildInfo.addControl(laBuildName = new Label(GlobalEngine.fnt4x7_brown_sh, 2, 2));
		buildInfo.addControl(laBuildInfo = new Label(GlobalEngine.fnt3x5_white, 2, 17));
		laBuildInfo.line_interval = 2;

		addControl(buildingBox = new Control(R.ui.building_info));
		RelativeLayout.settings(buildingBox).alignBottom(16).alignRight(16);
		buildingBox.visible = false;

		laBuildingName = new Label(GlobalEngine.fnt4x7_brown);
		buildingBox.addControl(laBuildingName);
		laBuildingName.setPosition(11, 11);

		laBuildingInfo = new Label(GlobalEngine.fnt4x7_brown);
		buildingBox.addControl(laBuildingInfo);
		laBuildingInfo.setPosition(11, 22);

		Button buildOk = new Button(R.ui.building_ok);
		buildingBox.addControl(buildOk);
		buildOk.setPosition(62, 64);
		buildOk.addControlListener(new ControlListener()
		{
			@Override
			public void onClick(Object sender, Point p, MouseEvent e)
			{
				buildConfirm();
			}
		});

		Button buildCancel = new Button(R.ui.building_cancel);
		buildingBox.addControl(buildCancel);
		buildCancel.setPosition(15, 64);
		buildCancel.addControlListener(new ControlListener()
		{
			@Override
			public void onClick(Object sender, Point p, MouseEvent e)
			{
				buildEnd();
			}
		});

		submitChilds();
	}

	private void addBuildTime(GuildBuilding b)
	{
		IsoControl c = new IsoControl(b.rect.x + b.rect.width / 2, b.rect.y + b.rect.height / 2);

		Label l = new Label(fnt10x14);
		l.setText(Integer.toString(b.buildRemainDays()));
		l.x = -l.width / 2;
		l.y = -MapCell.LEVEL_HEIGHT / 2;
		c.addControl(l);

		mapViewer.addIsoControl(c);
	}

	boolean drag = false;
	byte dragDir = 0;
	MapCell bound_beg, bound_end;

	private ControlListener mapViewer_listener = new ControlListener()
	{
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			if (instrument == null) return;
			if (e.getButton() == 1)
			{
				if (mapViewer.focusedCell == null) return;

				MapCell c = mapViewer.focusedCell;
				dragDir = -1;

				if (bound_beg != null) // Проверяем что мы меняем уже созданный диапазон
				{
					int minX = Math.min(bound_beg.x, bound_end.x);
					int minY = Math.min(bound_beg.y, bound_end.y);
					int maxX = Math.max(bound_beg.x, bound_end.x);
					int maxY = Math.max(bound_beg.y, bound_end.y);

					if (c.x >= minX && c.x <= maxX && c.y >= minY && c.y <= maxY)
					{
						if (c.x == minX)
						{
							if (c.y == minY)
								dragDir = 0;
							else if (c.y == maxY)
								dragDir = 6;
							else
								dragDir = 7;
						}
						else if (c.x == maxX)
						{
							if (c.y == minY)
								dragDir = 2;
							else if (c.y == maxY)
								dragDir = 4;
							else
								dragDir = 3;
						}
						else
						{
							if (c.y == minY)
								dragDir = 1;
							else if (c.y == maxY)
								dragDir = 5;
						}
					}

					if (dragDir != -1)
					{
						bound_beg = cells[minX][minY];
						bound_end = cells[maxX][maxY];
					}
				}

				if (dragDir == -1)
				{
					bound_beg = c;
					bound_end = c;
					selectBound(c, c);
				}

				drag = true;

				e.consume();
			}
		}

		public void onReleased(Object sender, Point p, MouseEvent e)
		{
			if (instrument == null) return;
			if (e.getButton() == 1)
			{
				drag = false;
				if (bound_beg == bound_end)
				{
					unselectRect();
				}
			}
		}

		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_ITEM_SELECTED)
			{
				MapCell c = (MapCell) tag;
				if (instrument == null)
				{
					GuildBuilding build = guild.getBuildingAt(c.x, c.y);
					if (build != null)
						showInfo(build);
					else
						hideInfo();
					return;
				}

				GameScreen.screen.setTitle(c.toString());
				if (drag)
				{
					hideInfo();
					if (dragDir == -1)
					{
						bound_end = c;
					}
					else
					{
						switch (dragDir)
						{
							case 0:
								bound_beg = c;
								break;
							case 1:
								bound_beg = cells[bound_beg.x][c.y];
								break;
							case 2:
								bound_beg = cells[bound_beg.x][c.y];
								bound_end = cells[c.x][bound_end.y];
								break;
							case 3:
								bound_end = cells[c.x][bound_end.y];
								break;
							case 4:
								bound_end = c;
								break;
							case 5:
								bound_end = cells[bound_end.x][c.y];
								break;
							case 6:
								bound_beg = cells[c.x][bound_beg.y];
								bound_end = cells[bound_end.x][c.y];
								break;
							case 7:
								bound_beg = cells[c.x][bound_beg.y];
								break;

							default:
								break;
						}
					}

					selectBound(bound_beg, bound_end);
				}
				else if (bound_beg != null)
				{
					int minX = Math.min(bound_beg.x, bound_end.x);
					int minY = Math.min(bound_beg.y, bound_end.y);
					int maxX = Math.max(bound_beg.x, bound_end.x);
					int maxY = Math.max(bound_beg.y, bound_end.y);

					if ((c.x == bound_beg.x || c.x == bound_end.x) && (c.y == bound_beg.y || c.y == bound_end.y))
						cursor = CURSOR_MOVE_XY;
					else if ((c.x == bound_beg.x || c.x == bound_end.x) && c.y >= minY && c.y <= maxY)
						cursor = CURSOR_MOVE_Y;
					else if ((c.y == bound_beg.y || c.y == bound_end.y) && c.x >= minX && c.x <= maxX)
						cursor = CURSOR_MOVE_X;
					else
						cursor = Game.ItemCursor;
				}
			}
		}
	};

	public void onKeyPressed(java.awt.event.KeyEvent e)
	{
		super.onKeyPressed(e);

		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			buildConfirm();
		}
	}

	private boolean buildError;

	private ArrayList<MapCell> selected;

	private int minX, minY, maxX, maxY;

	private void selectBound(MapCell from, MapCell to)
	{
		minX = Math.min(from.x, to.x);
		minY = Math.min(from.y, to.y);
		maxX = Math.max(from.x, to.x);
		maxY = Math.max(from.y, to.y);

		ArrayList<MapCell> selection = new ArrayList<>();

		if (selected != null)
		{
			for (MapCell c : selected)
				//if (!selection.contains(c))
				c.userState = 0;
			selected = null;
		}

		if (instrument.isTunnel())
		{
			ArrayList<MapCell> list = new ArrayList<>();
			for (int x = minX; x <= maxX; x++)
				for (int y = minY; y <= maxY; y++)
				{
					if (buildMap[x][y] == 0)
						list.add(cells[x][y]);
				}

			for (int i = 0; i < list.size(); i++)
			{
				MapCell c = list.get(i);
				if (nearEntryOrTunnel(c))
				{
					selection.add(c);
					buildMap[c.x][c.y] = -1;
					list.set(i, null);
					c.userState = 1;
				}
			}

			if (selection.size() > 0) // есть клетки рядом с проходом. расширяем границы
			{
				boolean nothing;
				while (true)
				{
					nothing = true;
					for (int i = 0; i < list.size(); i++)
					{
						MapCell c = list.get(i);
						if (c == null) continue;

						if (nearBuildTunnel(c))
						{
							selection.add(c);
							buildMap[c.x][c.y] = -1;
							list.set(i, null);
							nothing = false;
							c.userState = 1;
						}
					}
					if (nothing) break;
				}
			}

			if (selection.size() > 0)
			{
				buildError = false;
				for (int x = minX; x <= maxX; x++)
					for (int y = minY; y <= maxY; y++)
						if (buildMap[x][y] == -1)
							buildMap[x][y] = 0;
			}
			else
			{
				buildError = true;
				selection = list;
				for (MapCell c : list)
					c.userState = 2;
			}
		}
		else
		{
			if (!instrument.isPassSize(maxX - minX + 1, maxY - minY + 1))
			{
				buildError = true;

				for (int x = minX; x <= maxX; x++)
					for (int y = minY; y <= maxY; y++)
					{
						MapCell c = cells[x][y];
						c.userState = 2;
						selection.add(c);
					}
			}
			else
			{
				buildError = false;
				byte v;
				for (int x = minX; x <= maxX; x++)
					for (int y = minY; y <= maxY; y++)
					{
						v = buildMap[x][y];
						MapCell c = cells[x][y];
						if (v == 0 || v == 1) // На тунелях строить можно
						{
							selection.add(c);
							c.userState = 1;
						}
						else
						{
							buildError = true;
							c.userState = 2;
						}
					}
			}

			if (!buildError)
			{
				// Проверяем на близость с туннелем или входом
				buildError = true;
				MapCell c;
				for (int x = minX; x <= maxX; x++)
				{
					c = cells[x][minY];
					if (nearEntryOrTunnel(c))
					{
						buildError = false;
						break;
					}

					c = cells[x][maxY];
					if (nearEntryOrTunnel(c))
					{
						buildError = false;
						break;
					}
				}
				if (buildError)
				{
					for (int y = minY; y <= maxY; y++)
					{
						c = cells[minX][y];
						if (nearEntryOrTunnel(c))
						{
							buildError = false;
							break;
						}

						c = cells[maxX][y];
						if (nearEntryOrTunnel(c))
						{
							buildError = false;
							break;
						}
					}
				}

				byte state = (byte) (buildError ? 2 : 1);
				for (MapCell cell : selection)
					cell.userState = state;
			}
		}

		selected = selection;

		int w = maxX - minX + 1;
		int h = maxY - minY + 1;
		int s = w * h;
		StringBuilder sb = new StringBuilder();
		if (!instrument.isTunnel())
		{
			sb.append(String.format("ПЛОЩАДЬ: %d\nРАЗМЕР: %dx%d\n", s, w, h));
			if (instrument.squarePerUnit > 0)
				sb.append(String.format("МЕСТ: %d\n", s / instrument.squarePerUnit));
		}

		int price = instrument.price * (instrument.isTunnel() ? selection.size() : s);
		sb.append(String.format("ЦЕНА: %d", price));
		laBuildingInfo.setText(sb.toString());
		
		if (price > Game.gold && !buildError)
		{
			buildError = true;
			for (MapCell cell : selection)
				cell.userState = 2;
		}
	}

	private boolean nearEntryOrTunnel(MapCell c)
	{
		byte v;

		if (c.x > 0)
		{
			v = buildMap[c.x - 1][c.y];
			if (v == 1 || v == 2) return true;
		}

		if (c.x < guild.width - 1)
		{
			v = buildMap[c.x + 1][c.y];
			if (v == 1 || v == 2) return true;
		}

		if (c.y > 0)
		{
			v = buildMap[c.x][c.y - 1];
			if (v == 1 || v == 2) return true;
		}

		if (c.y < guild.height - 1)
		{
			v = buildMap[c.x][c.y + 1];
			if (v == 1 || v == 2) return true;
		}

		return false;
	}

	private boolean nearBuildTunnel(MapCell c)
	{
		if (c.x > 0 && buildMap[c.x - 1][c.y] == -1) return true;
		if (c.x < guild.width - 1 && buildMap[c.x + 1][c.y] == -1) return true;
		if (c.y > 0 && buildMap[c.x][c.y - 1] == -1) return true;
		if (c.y < guild.height - 1 && buildMap[c.x][c.y + 1] == -1) return true;
		return false;
	}

	public void beginBuild(GuildBuildingType bt)
	{
		instrument = bt;
		
		hideInfo();
		unselectRect();
		mapViewer.cursor = IsoCursor.CURSOR_RECT;
		mapViewer.allowDrag = false;
		
		laBuildingName.setText(bt.name);
		laBuildingInfo.setText("");
		buildingBox.visible = true;
	}

	private void buildConfirm()
	{
		if (buildError || bound_beg == null) return;

		if (instrument.isTunnel()) // Тунель
		{
			ArrayList<GPoint> points = new ArrayList<>();
			for (MapCell c : selected)
			{
				points.add(new GPoint(c.x, c.y));
				buildMap[c.x][c.y] = 1;
			}
			guild.buildTunnel(instrument, points);
		}
		else // Комната
		{
			Rectangle rect = new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
			if (instrument.squarePerUnit > 0 && rect.square() < instrument.squarePerUnit)
			{
				// Площади не хватает даже на одного юнита
				return;
			}

			GuildBuilding b = guild.build(instrument, rect);

			byte t = (byte) (b.type.isEntry() ? 2 : 3);
			for (int x = b.rect.x; x <= b.rect.right(); x++)
				for (int y = b.rect.y; y <= b.rect.bottom(); y++)
				{
					buildMap[x][y] = t;
				}

			if (b.isBuild())
				addBuildTime(b);
		}

		buildEnd();
	}

	private void buildEnd()
	{
		unselectRect();
		buildingsPanel.update();
		buildingBox.visible = false;
		instrument = null;
		mapViewer.cursor = null;
		mapViewer.allowDrag = true;
	}

	private void unselectRect()
	{
		cursor = null;
		bound_beg = null;
		bound_end = null;
	}

	public void setCenter(GuildBuilding build)
	{
		mapViewer.setCenter(build.rect);
	}

	@Override
	public void drawCell(MapCell cell, int px, int py)
	{
		if (bound_beg == null) return;
		if (cell.x < minX || cell.x > maxX || cell.y < minY || cell.y > maxY) return;

		if (instrument.isTunnel() && buildMap[cell.x][cell.y] != 0) return;

		switch (cell.userState)
		{
			case 1:
			default:
				cell_select.drawAt(px, py);
				break;
			case 2:
				cell_select_err.drawAt(px, py);
				break;
		}
	}

	private void hideInfo()
	{
		buildInfo.visible = false;
	}

	private void showInfo(GuildBuilding build)
	{
		laBuildName.setText(build.type.name);
		laBuildInfo.setText(build.info());
		buildInfo.visible = true;
	}
}
