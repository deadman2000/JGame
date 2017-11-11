package com.deadman.walker;

import java.awt.event.KeyEvent;

import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemInfoPanel;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.dh.model.items.ItemsGrid;
import com.deadman.dh.model.items.ItemsPage;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.resources.XCF;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;

public class WalkerEngine extends GameEngine
{
	public WorldView scene;
	private Compas compas;
	private Control charControl1;
	private PaperMap miniMap;

	private ItemsPage inventory;
	private ItemsGrid itemsGrid;

	public static boolean fog = true;

	public static Position position;

	public static WalkerEngine inst;

	public WalkerEngine()
	{
		inst = this;
		XCF xcfMain = XCF.loadFile("res/main.xcf");
		XCF xcfScene = XCF.loadFile("res/scene.xcf");
		XCF xcfItems = XCF.loadFile("res/items.xcf");

		// Top
		Control top_bgr = new Control(xcfMain.getDrawable("top_bgr"));
		top_bgr.setBounds(0, 0, GameScreen.GAME_WIDTH, top_bgr.height);
		top_bgr.anchor = Control.ANCHOR_LEFT_TOP | Control.ANCHOR_RIGHT;
		addControl(top_bgr);

		Control top = new Control(xcfMain.getDrawable("top"));
		addControl(top);
		top.setPosition((GameScreen.GAME_WIDTH - top.width) / 2, 0);
		top.anchor = Control.ANCHOR_TOP;

		compas = new Compas(xcfMain);
		addControl(compas);

		// Inventory
		Control slotsBgr = new Control(xcfMain.getDrawable("bgr_item_slots"));
		slotsBgr.setPosition(12, 26, Control.ANCHOR_TOP);
		addControl(slotsBgr);

		inventory = new ItemsPage("Inventory", 2, 20);

		ItemInfoPanel.ENABLED = false;
		ItemSlot.ITEM_HEIGHT = 17;
		ItemSlot.ITEM_WIDTH = 17;
		ItemsGrid.PAD_X = 1;
		ItemsGrid.PAD_Y = 1;
		itemsGrid = new ItemsGrid(15, 37, xcfMain.getDrawable("bgr_itemslot"));
		itemsGrid.anchor = Control.ANCHOR_TOP;
		addControl(itemsGrid);
		itemsGrid.setPage(inventory);

		Button btnSlotsUp = new Button(xcfMain.getDrawable("btn_slots_up"), xcfMain.getDrawable("btn_slots_up_pressed"));
		btnSlotsUp.setPosition(15, 29, Control.ANCHOR_TOP);
		addControl(btnSlotsUp);
		//itemsGrid.bindButtonUp(btnSlotsUp);

		Button btnSlotsDown = new Button(xcfMain.getDrawable("btn_slots_down"), xcfMain.getDrawable("btn_slots_down_pressed"));
		btnSlotsDown.setPosition(15, 163, Control.ANCHOR_TOP);
		addControl(btnSlotsDown);
		//itemsGrid.bindButtonDown(btnSlotsDown);

		//Scene
		Control sceneFrame = new Control(xcfMain.getDrawable("scene_frame"));
		addControl(sceneFrame);
		sceneFrame.setPosition((GameScreen.GAME_WIDTH - sceneFrame.width) / 2, 16);
		sceneFrame.anchor = Control.ANCHOR_TOP;

		scene = new WorldView();
		addControl(scene);

		// Character
		charControl1 = new Control(xcfMain.getDrawable("player1-slot"));
		charControl1.setPosition(GameScreen.GAME_WIDTH / 2 - charControl1.width / 2, 164);
		charControl1.anchor = Control.ANCHOR_TOP;
		addControl(charControl1);

		// Map control
		Control mapBgr = new Control(xcfMain.getDrawable("map_bgr"));
		mapBgr.setPosition(299, 75);
		addControl(mapBgr);

		PaperMap.init(xcfMain);
		miniMap = new PaperMap(4, 4, 63, 63);
		miniMap.anchor = Control.ANCHOR_RIGHT_TOP;
		mapBgr.addControl(miniMap);

		// Map
		Map map = new Map(10, 10);

		scene.background = xcfScene.getDrawable("city");
		WallType w0 = new WallType(xcfScene, "wall0");

		WItemType rock = new WItemType("rock", xcfItems);
		WItemType potion = new WItemType("potion", xcfItems, "bottle");

		new Item(potion).moveTo(inventory, 0, 0);

		for (int x = 0; x < 10; x++)
			for (int y = 0; y < 10; y++)
			{
				Cell c = new Cell();
				map.cells[x][y] = c;
				if (y == 0)
					c.setWall(0, w0);
				if (y == 9)
					c.setWall(2, w0);
				if (x == 0)
					c.setWall(3, w0);
				if (x == 9)
					c.setWall(1, w0);
			}

		map.cells[5][5].setWall(Direction.NORTH.index, w0);
		map.cells[5][5].setWall(Direction.SOUTH.index, w0);
		map.cells[5][5].setWall(Direction.WEST.index, w0);
		map.cells[5][5].setWall(Direction.EAST.index, w0);
		map.cells[5][5].addItem(0, new Item(rock));
		map.cells[5][5].addItem(1, new Item(rock));
		map.cells[5][5].addItem(2, new Item(rock));
		map.cells[5][5].addItem(3, new Item(rock));

		setMap(map);
		moveTo(new Position(5, 5, Direction.NORTH));
	}

	private void setMap(Map map)
	{
		scene.map = map;
		miniMap.setMap(map);
	}

	@Override
	public void draw()
	{
		GameScreen.screen.drawRect(0, 0, GameScreen.GAME_WIDTH, GameScreen.GAME_HEIGHT, 0xff7e6a47);
		super.draw();
	}

	@Override
	public void tick()
	{
		scene.tick(ticks);
	}

	@Override
	public void onKeyPressed(KeyEvent e)
	{
		//System.out.println(e.getKeyCode());
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				GameLoop.quit();
				break;

			case KeyEvent.VK_NUMPAD8:
				scene.move(Moving.FORWARD);
				break;
			case KeyEvent.VK_NUMPAD2:
			case KeyEvent.VK_NUMPAD5:
				scene.move(Moving.BACKWARD);
				break;
			case KeyEvent.VK_NUMPAD4:
				scene.move(Moving.SHIFT_LEFT);
				break;
			case KeyEvent.VK_NUMPAD7:
				scene.move(Moving.ROTATE_LEFT);
				break;
			case KeyEvent.VK_NUMPAD9:
				scene.move(Moving.ROTATE_RIGHT);
				break;
			case KeyEvent.VK_NUMPAD6:
				scene.move(Moving.SHIFT_RIGHT);
				break;

			default:
				return;
		}

		e.consume();
	}

	public static void moveTo(Position p)
	{
		position = p;

		inst.miniMap.explore(p.x, p.y);
		inst.miniMap.explore(p.x + 1, p.y);
		inst.miniMap.explore(p.x - 1, p.y);
		inst.miniMap.explore(p.x, p.y - 1);
		inst.miniMap.explore(p.x, p.y + 1);
	}
}
