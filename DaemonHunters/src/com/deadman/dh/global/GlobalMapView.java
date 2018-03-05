package com.deadman.dh.global;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.city.City;
import com.deadman.dh.dialogs.MenuAction;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.isometric.IsoViewer;
import com.deadman.dh.model.MapObject;
import com.deadman.dh.model.Mission;
import com.deadman.dh.model.Poi;
import com.deadman.dh.model.Squad;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.drawing.Picture;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.RelativeLayout;
import com.jogamp.opengl.GL2;

public class GlobalMapView extends Control
{
	private Drawable bgr;
	private Drawable map;
	//static final int BRD_WIDTH = 14;
	//private Drawable mapbrd; 
	private Drawable brd_vl, brd_ht, brd_vr, brd_hb;
	private Drawable brd_lt, brd_lb, brd_rt, brd_rb;
	private Drawable picSquad, picDarkForces;
	private Drawable cursor_lens;

	public int viewX, viewY;
	public int centerX, centerY;

	private int min_view_x, min_view_y, max_view_x, max_view_y;

	private MapContextMenu mapContext;

	private Weather weather;

	//private Label laPos;
	
	public GlobalMapView()
	{
		bgr = getDrawable(R.ui.map_bgr);
		
		//mapbrd = getDrawable(R.ui.mapbrd_9p);
		brd_lt = getDrawable(R.ui.mapbrd_lt); // TODO Заменить на NinePanel
		brd_lb = getDrawable(R.ui.mapbrd_lb);
		brd_rt = getDrawable(R.ui.mapbrd_rt);
		brd_rb = getDrawable(R.ui.mapbrd_rb);

		brd_vl = getDrawable(R.ui.mapbrd_vl);

		brd_ht = getDrawable(R.ui.mapbrd_ht);

		brd_vr = getDrawable(R.ui.mapbrd_vr);
		brd_hb = getDrawable(R.ui.mapbrd_hb);

		addControl(mapContext = new MapContextMenu(this));

		picSquad = getDrawable(R.ui.marker_squad);
		picSquad.setAnchorCenter();

		picDarkForces = getDrawable(R.ui.marker_darkforces);
		picDarkForces.setAnchor(12, 11);

		weather = new Weather(this);
		addControl(weather);
		RelativeLayout.settings(weather).fill();

		/*addControl(laPos = new Label(GlobalEngine.fnt3x5_brown));
		laPos.bgrColor = -1;
		laPos.setPosition(0, 0);*/

		cursor_lens = getDrawable(R.cursors.lens);
		cursor_lens.setAnchor(8, 8);
	}

	@Override
	protected void onResize()
	{
		super.onResize();

		if (map == null) return;

		min_view_x = width / 2;
		min_view_y = height / 2;
		max_view_x = width / 2 - map.width;
		max_view_y = height / 2 - map.height;

		setViewPos(viewX, viewY);
	}

	public void setMap(Picture picture)
	{
		map = picture;
	}

	long currTime;
	float worldBrightness;

	@Override
	public void draw()
	{
		// Фон
		bgr.drawAt(scrX, scrY, width, height);

		GL2 gl = GameScreen.gl;
		gl.glPushMatrix();
		gl.glTranslatef(scrX + viewX, scrY + viewY, 0);

		// Яркость
		if (!GameLoop.paused && GlobalEngine.timeSpeedLvl == 6)
		{
			currTime = 0;
			worldBrightness = 1.f;
		}
		else if (currTime != GlobalEngine.time)
		{
			currTime = GlobalEngine.time;
			worldBrightness = GlobalEngine.currentBrightness();
		}
		GameScreen.screen.setBrightness(worldBrightness);

		// Границы карты
		//mapbrd.drawAt(-BRD_WIDTH, -BRD_WIDTH, map.width + BRD_WIDTH, map.height + BRD_WIDTH);
		brd_lt.drawAt(-brd_lt.width, -brd_lt.height);
		brd_lb.drawAt(-brd_vl.width, map.height);
		brd_rt.drawAt(map.width, -brd_rt.height);
		brd_rb.drawAt(map.width, map.height);

		brd_vl.drawAt(-brd_vl.width, 0, brd_vl.width, map.height);
		brd_ht.drawAt(0, -brd_ht.height, map.width, brd_ht.height);
		brd_vr.drawAt(map.width, 0, brd_vr.width, map.height);
		brd_hb.drawAt(0, map.height, map.width, brd_hb.height);

		GameScreen.screen.enableClipping(scrX + viewX, scrY + viewY, map.width, map.height);
		map.drawAt(0, 0);

		// Названия городов
		for (City c : Game.map.cities)
		{
			if (c.imgLabel == null) continue;
			if (mapContext.visible && mapContext.target == c) continue;
			c.imgLabel.drawAt(c.labelRect.x, c.labelRect.y);
		}

		GameScreen.screen.setBrightness(1f);

		// Ночные огни
		if (worldBrightness < 0.6f)
		{
			for (City c : Game.map.cities)
			{
				if (c.level == City.LVL_BIG)
				{
					int color;
					switch (c.id % 4)
					{
						case 0:
							color = 0xffffd257;
							break;
						case 1:
							color = 0xffedea0f;
							break;
						case 2:
							color = 0xffffd496;
							break;
						default:
							color = 0xffe5aa0b;
							break;
					}

					GameScreen.screen.drawRect(c.x - 1, c.y, 3, 1, 0xffff7e00);
					GameScreen.screen.drawRect(c.x, c.y - 1, 1, 3, 0xffff7e00);
					GameScreen.screen.drawRect(c.x, c.y, 1, 1, color);
				}
				else
				{
					int color;
					switch (c.id % 4)
					{
						case 0:
							color = 0xffffd257;
							break;
						case 1:
							color = 0xffff7e00;
							break;
						case 2:
							color = 0xffffd496;
							break;
						default:
							color = 0xffe5aa0b;
							break;
					}

					GameScreen.screen.drawRect(c.x, c.y, 1, 1, color);
				}
			}
		}

		// Задания
		for (Mission p : Game.missions())
			picDarkForces.drawAt(p.x, p.y);

		// Гильдии
		for (Guild g : Game.guilds)
			g.marker.drawAt(g.city.x, g.city.y);

		// Отряды
		for (Squad s : Game.squads)
		{
			if (s.way != null)
				s.way.draw();

			if (s.location != s.guild.city)
				picSquad.drawAt(s.x, s.y);
		}

		weather.drawWorld();

		GameScreen.screen.disableClipping();
		gl.glPopMatrix();

		weather.drawView();

		GameScreen.screen.setBrightness(1f);

		/*for (Poi p : Game.map.points)
		{
			Point s = mapToScreen(p.x, p.y);
			Game.screen.drawRect(s.x, s.y, 1, 1, 0xff00ff00);
		}*/

		super.draw();
	}

	private Point screenToMap(Point p)
	{
		return new Point(p.x - viewX, p.y - viewY);
	}

	public void setCenter(int x, int y)
	{
		setViewPos(-x + width / 2, -y + height / 2);
	}

	private void setViewPos(int x, int y)
	{
		int dx = viewX;
		int dy = viewY;
		viewX = Math.max(Math.min(x, min_view_x), max_view_x);
		viewY = Math.max(Math.min(y, min_view_y), max_view_y);
		dx -= viewX;
		dy -= viewY;
		mapContext.setPosition(mapContext.x - dx, mapContext.y - dy);

		centerX = viewX - width / 2;
		centerY = viewY - height / 2;

		onAction(ACTION_VIEW_CHANGED);
	}

	public static final int ACTION_VIEW_CHANGED = 0x100;

	Point drag_begin;
	Point pressPos;
	boolean _dragged;

	@Override
	public void pressMouse(Point p, MouseEvent e)
	{
		pressPos = p.getLocation();
		_dragged = false;

		if (e.getButton() == 1 || e.getButton() == 3)
		{
			// Перетягивание карты
			drag_begin = p.getLocation();
			drag_begin.translate(-viewX, -viewY);
		}

		super.pressMouse(p, e);
	}

	@Override
	public void releaseMouse(Point p, MouseEvent e)
	{
		super.releaseMouse(p, e);
		drag_begin = null;
	}

	@Override
	protected void onClick(Point p, MouseEvent e)
	{
		super.onClick(p, e);
		if (e.isConsumed()) return;

		if (e.getButton() == 1 && !_dragged)
		{
			Point pm = screenToMap(p);
			if (Game.map.rect.contains(pm))
			{
				if (!mapContext.attached && mapContext.visible)
					mapContext.attach();
				else if (mapContext.attached) mapContext.hide();

				hitMap(pm);
			}
		}

		if (e.getButton() == 3 && _traceSquad != null)
			_traceSquad = null;

		if (e.getButton() == 2)
		{
			IsoViewer.showMap(screenToMap(p));
		}
	}

	@Override
	public void moveMouse(Point p, MouseEvent e)
	{
		Point mp = screenToMap(p);

		//if (laPos != null) laPos.setText(mp.x + " x " + mp.y);
		if (pressPos != null && pressPos.distanceSq(p.x, p.y) > 4)
			pressPos = null;

		if (drag_begin != null)
		{
			setViewPos(p.x - drag_begin.x, p.y - drag_begin.y);
			_dragged = true;
		}
		else
		{
			showContext(mp);
		}

		super.moveMouse(p, e);
	}

	void showContext(Point mp)
	{
		if (mapContext.visible && mapContext.attached) return;

		MapObject obj = getMapObject(mp);
		if (obj != null)
			mapContext.show(obj);
		else
			mapContext.visible = false;
	}

	private void hitMap(Point point)
	{
		if (_traceSquad != null) // Режим выбора направления отряда
		{
			if (mapContext.target == null) return;

			if (mapContext.target instanceof Poi)
			{
				_traceSquad.moveTo((Poi) mapContext.target);
				endMoveSquad();
			}
			else if (mapContext.target instanceof Mission)
			{
				_traceSquad.moveTo((Mission) mapContext.target);
				endMoveSquad();
			}
		}
		else
		{
			/*mapContext.attached = false;
			mapContext.visible = false;
			System.out.println("Context menu deactivated");*/
		}
	}

	public MapObject getMapObject(Point point)
	{
		double mind2 = 4 * 4;
		MapObject res = null;

		for (Squad s : Game.squads)
		{
			//if (s.location == null) continue;

			int dx = s.x - point.x;
			int dy = s.y - point.y;
			if (dx < -4 || dx > 4 || dy < -4 || dy > 4) continue;

			double d = dx * dx + dy * dy;
			if (d < mind2)
			{
				res = s;
				mind2 = d;
			}
		}

		for (City c : Game.map.cities)
		{
			int dx = c.x - point.x;
			int dy = c.y - point.y;
			if (dx < -4 || dx > 4 || dy < -4 || dy > 4) continue;

			double d = dx * dx + dy * dy;
			if (d <= mind2)
			{
				res = c;
				mind2 = d;
			}
		}

		if (res == null)
		{
			mind2 = 8 * 8;
			for (Mission p : Game.missions())
			{
				int dx = p.x - point.x;
				int dy = p.y - point.y;
				if (dx < -8 || dx > 8 || dy < -8 || dy > 8) continue;

				double d = dx * dx + dy * dy;
				if (d < mind2)
				{
					res = p;
					mind2 = d;
				}
			}
		}

		return res;
	}

	public void tickWorld(int dt)
	{
		weather.tickWorld(dt);
	}

	public void tick(long ticks)
	{
		weather.tick(ticks);
	}

	Squad _traceSquad;

	public void sendSquad(Squad squad)
	{
		Game.global.showMessage("Выберите пункт назначения", "Отмена", ma_sendSquadCancel);
		_traceSquad = squad;
	}

	MenuAction ma_sendSquadCancel = new MenuAction()
	{
		@Override
		public void onAction(Object sender, int action)
		{
			_traceSquad = null;
			Game.global.hideMessage();
		}
	};

	void endMoveSquad()
	{
		_traceSquad = null;
		//mapContext.detach(); // ?? скрытие фокуса по клику
		mapContext.attached = false;
		Game.global.hideMessage();
	}

	public void start()
	{
		weather.start();
	}
}
