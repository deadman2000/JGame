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
import com.deadman.dh.model.itemtypes.AmmunitionType;
import com.deadman.dh.model.itemtypes.ArmorType;
import com.deadman.dh.model.itemtypes.ItemType;
import com.deadman.dh.model.itemtypes.MeeleWeaponType;
import com.deadman.dh.model.itemtypes.PotionType;
import com.deadman.dh.model.itemtypes.RangedWeaponType;
import com.deadman.dh.model.itemtypes.TorchType;
import com.deadman.dh.model.itemtypes.WeaponType;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.resources.ResourceManager;
import com.deadman.jgame.sound.Sound;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.HScrollBar;
import com.deadman.jgame.ui.VScrollBar;
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
			}
			else
			{
				//new Intro().show();
				initGlobal();
				//initBattle();
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

	public static VScrollBar createVScrollInfo()
	{
		return new VScrollBar(R.ui.vscroll_info_up, R.ui.vscroll_info_up_pr, R.ui.vscroll_info, R.ui.vscroll_info_pr, R.ui.vscroll_info_down, R.ui.vscroll_info_down_pr);
	}

	public static HScrollBar createHScrollGray()
	{
		return new HScrollBar(R.ui.hscroll_gray_up, R.ui.hscroll_gray_up_pr, R.ui.hscroll_gray, R.ui.hscroll_gray_pr, R.ui.hscroll_gray_down, R.ui.hscroll_gray_down_pr);
	}

	public static final class ItemTypes
	{
		public static final MeeleWeaponType sword = new MeeleWeaponType(0, R.items.sword, 30).damage(Element.PHYSICAL, 10, 5);
		public static final WeaponType bow = new RangedWeaponType(1, R.items.bow, 120).twoHanded();
		public static final ItemType red_potion = new PotionType(2, R.items.red_potion).addEffect(new HealEffect(50));
		public static final ArmorType armor = new ArmorType(3, ArmorType.BODY, R.items.armor);
		public static final ItemType shield = new ItemType(4, R.items.shield);
		public static final AmmunitionType arrow = new AmmunitionType(5, R.items.arrow);
		public static final ItemType bottle = new ItemType(6, R.items.bottle);
		public static final ItemType book = new ItemType(7, R.items.book);
		public static final ItemType torch = new TorchType(8, R.items.torch, 10).setIso(R.iso.SmallTorh);
		public static final ItemType dead_unit = new ItemType(9, R.items.dead_unit);

		public static final ItemType player1 = new ItemType(10001, R.items.player1).setIso(R.iso.Player1);
		public static final ItemType player2 = new ItemType(10002, R.items.player2).setIso(R.iso.Player2);
	}
}
