package com.deadman.dh.city;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.dialogs.Book;
import com.deadman.dh.global.TopMenu;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoViewer;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;

public class CityEngine extends GameEngine
{
	public final City city;

	Control houseInfo;
	Label laHouseInfo;
	Button btnBuy;

	TopMenu menu;

	static final GameFont fntHouseInfo = getFont(R.fonts.font3x5, 0xFF5A3F24);

	public CityEngine(City c)
	{
		city = c;

		onSizeChanged();

		houseInfo = new Control(R.ui.building_info);
		houseInfo.visible = false;
		addControl(houseInfo);

		laHouseInfo = new Label(fntHouseInfo);
		laHouseInfo.setPosition(14, 26);
		laHouseInfo.line_interval = 4;
		houseInfo.addControl(laHouseInfo);

		addControl(new CityTop(c));

		addControl(menu = new TopMenu(TopMenu.MODE_CLOSE));
		menu.guild = city.guild;
	}

	@Override
	public void draw()
	{
		if (city.picMap != null)
			city.picMap.drawAt(-viewX, -viewY);

		if (lightedHouse != null)
			drawHouse(lightedHouse);

		if (selectedHouse != null && selectedHouse != lightedHouse)
			drawHouse(selectedHouse);

		super.draw();
	}

	private void drawHouse(Building h)
	{
		drawHouse(h, h.type.color);
	}

	private void drawHouse(Building h, int color)
	{
		int tx = h.x - viewX;
		int ty = h.y - viewY;
		if (tx + h.type.width < 0 || tx >= GameScreen.GAME_WIDTH || ty + h.type.height < 0 || ty >= GameScreen.GAME_HEIGHT) return;

		screen.drawRect(tx, ty, h.type.width, h.type.height, color);
	}

	Point drag_begin;
	int viewX, viewY;

	Building lightedHouse = null;
	Building selectedHouse = null;

	public void select(Building house)
	{
		//new IsometricEngine(selectedHouse.type.map).show();
		selectedHouse = house;

		if (house != null)
		{
			laHouseInfo.setText("SIZE: " + house.type.width + "x" + house.type.height + "\nSQUARE: " + house.type.square + "\nPRICE: " + house.type.getPrice());
			btnBuy.visible = house.type.getPrice() <= Game.gold;
			calcHouseInfoPos();
			houseInfo.visible = true;
		}
		else
			houseInfo.visible = false;
	}

	private void calcHouseInfoPos()
	{
		int x = selectedHouse.x - viewX - houseInfo.width + selectedHouse.type.width;
		int y = selectedHouse.y - viewY - 2 - houseInfo.height;
		houseInfo.setPosition(x, y);
	}

	Point pressPos;

	@Override
	public void onMousePressed(Point p, MouseEvent e)
	{
		super.onMousePressed(p, e);
		if (e.isConsumed()) return;

		pressPos = p.getLocation();

		// Перетягивание карты
		drag_begin = p.getLocation();
		drag_begin.translate(viewX, viewY);
	}

	@Override
	public void onMouseMoved(Point p, MouseEvent e)
	{
		super.onMouseMoved(p, e);

		if (drag_begin != null)
		{
			setViewPos(drag_begin.x - p.x, drag_begin.y - p.y);
		}
		else
		{
			if (city.housesMap != null)
			{
				p = screenToMap(p);
				if (p.x >= 0 && p.x < city.width && p.y >= 0 && p.y < city.height)
				{
					int id = city.housesMap[p.x][p.y] & 0xFFFF;
					if (id == 0)
						lightedHouse = null;
					else
						lightedHouse = city.houses.get(id - 1);
				}
				else
					lightedHouse = null;
			}
		}
	}

	@Override
	public void onMouseReleased(Point p, MouseEvent e)
	{
		super.onMouseReleased(p, e);
		if (e.isConsumed()) return;

		if (e.getButton() == 1 && pressPos != null && pressPos.distance(p) < 4)
		{
			select(lightedHouse);
		}

		drag_begin = null;
	}

	private Point screenToMap(Point p)
	{
		p = p.getLocation();
		p.translate(viewX, viewY);
		return p;
	}

	int min_view_x, max_view_x, min_view_y, max_view_y;

	private void setViewPos(int x, int y)
	{
		viewX = Math.min(Math.max(x, min_view_x), max_view_x);
		viewY = Math.min(Math.max(y, min_view_y), max_view_y);

		if (selectedHouse != null)
		{
			calcHouseInfoPos();
		}
	}

	public void setCenter(int x, int y)
	{
		setViewPos(x - GameScreen.GAME_WIDTH / 2, y - GameScreen.GAME_HEIGHT / 2);
	}

	public void setCenter(Building b)
	{
		setCenter(b.x, b.y);
	}

	@Override
	public void onSizeChanged()
	{
		super.onSizeChanged();

		min_view_x = -GameScreen.GAME_WIDTH / 2;
		min_view_y = -GameScreen.GAME_HEIGHT / 2;
		max_view_x = city.width - GameScreen.GAME_WIDTH / 2;
		max_view_y = city.height - GameScreen.GAME_HEIGHT / 2;

		if (max_view_x < 0)
		{
			max_view_x /= 2;
			min_view_x = Math.abs(max_view_x);
		}
		if (max_view_y < 0)
		{
			max_view_y /= 2;
			min_view_y = Math.abs(max_view_y);
		}

		setViewPos(viewX, viewY);
	}

	@Override
	public void onKeyPressed(KeyEvent e)
	{
		super.onKeyPressed(e);
		if (e.isConsumed()) return;

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_F2:
				Book.inst().show();
				break;
			case KeyEvent.VK_ESCAPE: // Esc
				close();
				break;
			case KeyEvent.VK_O:
				openIso();
				break;
			default:
				return;
		}
		e.consume();
	}

	private void openIso()
	{
		IsoMap map = city.createIsoMap();
		IsoViewer.showMap(map);
		//new IsometricEngine(map).show();
	}

	ControlListener btnBuyHouse_listener = new ControlListener()
	{
			/*if (selectedHouse == null) return;
			Guild g = selectedHouse.buyHouse(city);
			g.generateUnits();
			menu.guild = g;
			buildingsPanel.guild = g;
			buildingsPanel.update();
			select(null);*/
	};

	@Override
	public void show()
	{
		super.show();

		if (city.houses.size() > 0)
			setCenter(city.houses.get(0));
	}
}
