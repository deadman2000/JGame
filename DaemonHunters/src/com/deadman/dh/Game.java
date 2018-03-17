package com.deadman.dh;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import com.deadman.dh.battle.MissionEngine;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.editor.IsoEditor;
import com.deadman.dh.model.Element;
import com.deadman.dh.model.GlobalMap;
import com.deadman.dh.model.Mission;
import com.deadman.dh.model.Squad;
import com.deadman.dh.model.items.HealEffect;
import com.deadman.dh.model.items.ItemCursor;
import com.deadman.dh.model.items.ItemSlotType;
import com.deadman.dh.model.items.LightEffect;
import com.deadman.dh.model.itemtypes.ItemType;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.resources.ResourceManager;
import com.deadman.jgame.sound.Sound;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ScrollBarTheme;
import com.deadman.jgame.uibuilder.UIBuilderEngine;

public class Game
{
	public static GlobalEngine global;

	public static GlobalMap map;
	public static long seed;

	public static int dark_points = 0; // Темная энергия. Возврастает со временем и при провале миссии
	public static long gold = 50000;

	public static int level = 1; // Стадия игры. Отвечает за сложность врагов

	public static ArrayList<Guild> guilds = new ArrayList<>();
	public static ArrayList<Squad> squads = new ArrayList<>();

	private static ArrayList<Mission> missions = new ArrayList<>();

	public static Random rnd = new Random();

	public static Drawable ItemCursor;

	// Читы
	public static boolean FAST_BUILD = false; // Быстрое строительство
	public static boolean VIEW_ALL = false; // Просмотр всей карты
	public static boolean VISION_AREA = false; // На карте видно только те ячейки, которые в области видимости
	public static boolean AI_CONTROL = false; // Контроллирование врага
	public static boolean FREE_MOVINGS = false; // Бесконечное перемещение
	public static boolean NO_EVENTS = false; // Отключение событий на глобальной карте 

	public static int PLAYER_UNITS = 3;
	public static int ENEMY_UNITS = 3;

	public static void main(String[] args)
	{
		try
		{
			GameScreen.init("--- Deamon Hunters v0.1 - programmed by dead_man ---");
			ResourceManager.init();
			Control.disabled_bgr = Drawable.get(R.ui.disabled_bgr);
			Sound.init();
			GameScreen.defaultCursor = Drawable.get(R.cursors._default);
			ItemCursor = new ItemCursor(GameScreen.defaultCursor);

			if (args.length > 0)
			{
				if (args[0].equals("-mapeditor"))
					new IsoEditor().show();
				if (args[0].equals("-uibuilder"))
					new UIBuilderEngine().show();
				else if (args[0].equals("-test"))
					new TestEngine().show();
				else if (args[0].equals("-battle"))
					initBattle();
			}
			else
			{
				//new Intro().show();
				initGlobal();
			}

			GameLoop.run();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	static void convertMaps()
	{
		GameResources.init();

		File folder = new File("res/maps/");
		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++)
		{
			System.out.println("Upgrade " + files[i].getAbsolutePath());

			IsoMap map = IsoMap.loadMap(files[i].getAbsolutePath());
			map.upgrade();
			map.saveMap();
		}

		System.out.println("Upgrade completed");
	}

	static void initGlobal()
	{
		global = new GlobalEngine();
		global.beginCraete();
	}

	static void initBattle()
	{
		GameResources.init();
		MissionEngine eng = new MissionEngine();
		eng.show();
		eng.beginBattle();
	}

	public static void addMission(Mission mission)
	{
		synchronized (missions)
		{
			missions.add(mission);
		}
	}

	public static ArrayList<Mission> missions()
	{
		synchronized (missions)
		{
			return new ArrayList<Mission>(missions);
		}
	}

	public static void removeMission(Mission mission)
	{
		synchronized (missions)
		{
			missions.remove(mission);
		}
	}

	public static void checkMissions()
	{
		synchronized (missions)
		{
			for (int i = 0; i < missions.size(); i++)
			{
				Mission m = missions.get(i);
				if (m.endTime < GlobalEngine.time)
				{
					missions.remove(i);
					i--;
					m.onUncompleted();
				}
			}
		}
	}

	private static ScrollBarTheme _scrollTYellow;
	public static ScrollBarTheme getScrollThemeYellow()
	{
		if (_scrollTYellow == null)
		{
			_scrollTYellow = new ScrollBarTheme();
			_scrollTYellow.setUp(R.ui.vscroll_info_up, R.ui.vscroll_info_up_pr);
			_scrollTYellow.setDown(R.ui.vscroll_info_down, R.ui.vscroll_info_down_pr);
			_scrollTYellow.setVPos(R.ui.vscroll_info, R.ui.vscroll_info_pr);
		}
		return _scrollTYellow;
	}

	private static ScrollBarTheme _scrollTGray;
	public static ScrollBarTheme getScrollThemeGray()
	{
		if (_scrollTGray == null)
		{
			_scrollTGray = new ScrollBarTheme();
			_scrollTGray.setHPos(R.ui.hscroll_gray, R.ui.hscroll_gray_pr);
			_scrollTGray.setLeft(R.ui.hscroll_gray_left, R.ui.hscroll_gray_left_pr);
			_scrollTGray.setRight(R.ui.hscroll_gray_right, R.ui.hscroll_gray_right_pr);
		}
		return _scrollTGray;
	}

	private static ScrollBarTheme _scrollTPaper;
	public static ScrollBarTheme getScrollThemePaper()
	{
		if (_scrollTPaper == null)
		{
			_scrollTPaper = new ScrollBarTheme();
			_scrollTPaper.setVPos(R.ui.scroll_paper_vpos);
			_scrollTPaper.setUp(R.ui.scroll_paper_up);
			_scrollTPaper.setDown(R.ui.scroll_paper_down);
			_scrollTPaper.setVBgr(R.ui.scroll_paper_vbgr);
		}
		return _scrollTPaper;
	}

	public static final class ItemTypes
	{
		static
		{
			System.out.println("Init item types");
		}

		public static final ItemType sword = ItemType	.create(0)
														.name("Sword")
														.icon(R.items.sword)
														.equipSprite("sword")
														.meele(Element.PHYSICAL, 10, 5)
														.slots(ItemSlotType.RIGHTHAND)
														.attackTime(30)
														.end();

		public static final ItemType bow = ItemType	.create(1)
													.name("Bow")
													.icon(R.items.bow)
													.slots(ItemSlotType.RIGHTHAND)
													.twoHanded()
													.ranged()
													.attackTime(120)
													.end();

		public static final ItemType red_potion = ItemType	.create(2)
															.name("Healing potion")
															.icon(R.items.red_potion)
															.consumable()
															.effect(new HealEffect(50))
															.end();

		public static final ItemType armor = ItemType	.create(3)
														.name("Armor")
														.icon(R.items.armor)
														.armor(ItemSlotType.BODY)
														.defence(Element.PHYSICAL, 5)
														.end();

		public static final ItemType shield = ItemType	.create(4)
														.name("Shield")
														.icon(R.items.shield)
														.armor(ItemSlotType.LEFTHAND)
														.defence(Element.PHYSICAL, 1)
														.end();

		public static final ItemType arrow = ItemType	.create(5)
														.name("Arrow")
														.icon(R.items.arrow)
														.stack(100)
														.end();

		public static final ItemType bottle = ItemType	.create(6)
														.name("Empty bottle")
														.icon(R.items.bottle)
														.end();

		public static final ItemType book = ItemType.create(7)
													.name("Book")
													.icon(R.items.book)
													.end();

		public static final ItemType torch = ItemType	.create(8)
														.name("Torch")
														.icon(R.items.torch)
														.iso(R.iso.SmallTorh)
														.effect(new LightEffect(10))
														.handsEquip()
														.end();

		public static final ItemType dead_unit = ItemType	.create(9)
															.name("Dead man")
															.icon(R.items.dead_unit)
															.end();

		public static final ItemType player1 = ItemType	.create(10001)
														.name("Player1")
														.icon(R.items.player1)
														.iso(R.iso.Player1)
														.end();

		public static final ItemType player2 = ItemType	.create(10002)
														.name("Player2")
														.icon(R.items.player2)
														.iso(R.iso.Player2)
														.end();
	}
}
