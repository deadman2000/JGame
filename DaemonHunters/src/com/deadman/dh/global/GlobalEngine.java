package com.deadman.dh.global;

import java.awt.event.KeyEvent;
import java.util.Random;

import com.deadman.dh.Game;
import com.deadman.dh.GameEvent;
import com.deadman.dh.R;
import com.deadman.dh.city.City;
import com.deadman.dh.city.CityEngine;
import com.deadman.dh.dialogs.Book;
import com.deadman.dh.dialogs.InGameMenu;
import com.deadman.dh.dialogs.EventBox;
import com.deadman.dh.dialogs.MenuAction;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.model.MapObject;
import com.deadman.dh.model.Mission;
import com.deadman.dh.model.MissionScenario;
import com.deadman.dh.model.Poi;
import com.deadman.dh.model.Squad;
import com.deadman.dh.model.generation.GlobalMapGenerator;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.drawing.Picture;
import com.deadman.jgame.sound.Music;
import com.deadman.jgame.ui.LoadingScreen;
import com.deadman.jgame.ui.ProgressStatus;
import com.deadman.jgame.ui.RelativeLayout;

public class GlobalEngine extends GameEngine
{
	public static final GameFont fnt3x5_white = getFont(R.fonts.font3x5, 0xFFFFFFFF);
	public static final GameFont fnt3x5_brown = getFont(R.fonts.font3x5, 0xFF3A2717);
	public static final GameFont fnt4x7_brown = getFont(R.fonts.font4x7, 0xFF3A2717);
	public static final GameFont fnt4x7_brown_sh = getFont(R.fonts.font4x7, 0xFF3a2717).shadow(0xFF9f9a8a);

	private GlobalMapView mapView;
	private TimePanel timePanel;
	private TopMenu topMenu;
	public GuildsPanel guildsPanel;
	public SquadsPanel squadsPanel;

	// TODO: Control для миникарты

	public GlobalEngine()
	{
		addControl(mapView = new GlobalMapView());
		RelativeLayout.settings(mapView).fill();

		addControl(guildsPanel = new GuildsPanel());
		RelativeLayout.settings(guildsPanel).alignRight(2).alignTop(22);
		addControl(squadsPanel = new SquadsPanel());
		addControl(topMenu = new TopMenu(TopMenu.MODE_MENU));
		addControl(timePanel = new TimePanel());

		setTimeSpeedLevel(1);

		//showFixMessage("Выберите город для основания гильдии");
	}

	@Override
	public void show()
	{
		super.show();

		if (Game.map == null)
		{
			beginCraete();
		}
	}

	public GlobalEngine beginCraete()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				generate(new Random().nextLong());
			}
		}).start();
		return this;
	}

	public void reCraete()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				generate(Game.seed);
			}
		}).start();
	}

	Random rnd;

	private void generate(long seed)
	{
		Game.seed = seed;
		ProgressStatus status = new ProgressStatus();
		status.max = 110;
		LoadingScreen.showLoading(status, this);

		GameResources.init();

		System.out.println("Game Seed = " + seed);
		rnd = new Random(seed);

		GlobalMapGenerator gen = new GlobalMapGenerator(GameLoop.GLOBAL_MAP_WIDTH, GameLoop.GLOBAL_MAP_HEIGHT, rnd.nextLong());
		Game.map = gen.generate(status);
		//mapView = new Picture(gen.getHeightsMap());
		Picture map = new Picture(Game.map.fillBackground(MapStyle.old, status));
		map.preload();
		mapView.setMap(map);

		createGuid();
		createGuid();
		
		System.out.println("Load completed");

		time = SECONDS_IN_HOUR * 12;
		mapView.start();
		genNextEventTime();

		status.progress = status.max;

		//Game.guilds.get(0).show();

		Music.playOGG(R.music.GAME8_ogg);
	}

	private void createGuid()
	{
		ProgressStatus st = new ProgressStatus();
		Random rnd = new Random();
		while (true)
		{
			int n = rnd.nextInt(Game.map.cities.length);
			City c = Game.map.cities[n];
			if (c.guild != null) continue;
			
			c.generate(st);
			if (c.houses.size() == 0)
			{
				System.err.println("City " + c + " without houses");
				continue;
			}

			Guild g = c.createGuild();
			g.generateUnits();
			topMenu.guild = c.guild;
			break;
		}
		guildsPanel.update();
	}

	// Messages

	private MsgScroll msgScroll;

	private void prepareMsgScroll()
	{
		if (msgScroll != null) return;
		msgScroll = new MsgScroll();
		addControl(msgScroll);
		msgScroll.setBounds(GameScreen.GAME_WIDTH / 2 - 200 / 2, 10, 200, msgScroll.height);
		RelativeLayout.settings(msgScroll).alignTop();
		msgScroll.visible = false;
	}

	public void showHideMessage(String text)
	{
		prepareMsgScroll();
		msgScroll.setText(text);
		msgScroll.visible = true;
		msgScroll.hideOnClick = true;
	}

	public void showFixMessage(String text)
	{
		prepareMsgScroll();
		msgScroll.setText(text);
		msgScroll.visible = true;
		msgScroll.hideOnClick = false;
	}

	public void showMessage(String text, String buttonText, MenuAction buttonAction)
	{
		prepareMsgScroll();
		msgScroll.setText(text, buttonText, buttonAction);
		msgScroll.visible = true;
		msgScroll.hideOnClick = false;
	}

	public void hideMessage()
	{
		msgScroll.visible = false;
	}

	// TIME

	private static final int[] speeds = { 0, 5, 60, 60 * 5, 60 * 60, 60 * 60 * 24, 60 * 60 * 24 * 30 };
	public static int timeSpeedLvl = 0;
	private static int timeSpeed = 0;

	public static final int SECONDS_IN_HOUR = 60 * 60;
	public static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
	public static final int SECONDS_IN_MONTH = SECONDS_IN_DAY * 30;
	public static final int SECONDS_IN_YEAR = SECONDS_IN_MONTH * 12;

	public static int time = 12 * SECONDS_IN_HOUR; // Текущее время в секундах

	public static int getTimeFromDays(double days)
	{
		return (int) (SECONDS_IN_DAY * days);
	}
	
	public static int getTimeFromHours(double hours)
	{
		return (int) (SECONDS_IN_HOUR * hours);
	}

	public static int getDaysFromTime(int time)
	{
		return (int)Math.round((double)time / SECONDS_IN_DAY);
	}

	private int _m_tick;
	private long next_event_time;

	private static void setTimeSpeedLevel(int lvl)
	{
		timeSpeedLvl = lvl;
		timeSpeed = speeds[lvl];
	}

	@Override
	public void tick()
	{
		if (GameEvent.events.size() > 0)
		{
			EventBox.showEvents();
			return;
		}

		_m_tick += timeSpeed;
		if (_m_tick >= GameLoop.FPS)
		{
			int dt = _m_tick / GameLoop.FPS;
			_m_tick = _m_tick % GameLoop.FPS;

			time += dt;

			if (time >= next_event_time)
				genEvent();
			
			Game.checkMissions();

			for (Guild g : Game.guilds)
				g.tick(time, dt);

			for (Squad s : Game.squads)
				s.tick(dt);

			if (GameEvent.events.size() > 0)
			{
				EventBox.showEvents();
				return;
			}

			mapView.tickWorld(dt);
			timePanel.update();
		}

		mapView.tick(ticks);
	}

	static final float DAWN_BEGIN = 5; // Начало рассвета в часах
	static final float DAWN_END = 9;   // Конец рассвета
	static final float DAWN_DURATION = DAWN_END - DAWN_BEGIN;
	static final float SUNSET_BEGIN = 18;  // Начало заката
	static final float SUNSET_END = 22;    // Конец заката
	static final float SUNSET_DURATION = SUNSET_END - SUNSET_BEGIN;
	static final float NIGHT_BRIGHTNESS = 0.1f;  // Светимость ночью
	static final float DAY_AMP = 1.f - NIGHT_BRIGHTNESS;

	public static float currentBrightness()
	{
		float worldBrightness;
		float hours = (float) time / SECONDS_IN_HOUR;
		hours = hours - (int) (hours / 24) * 24;
		if (hours > DAWN_BEGIN)
		{
			if (hours < DAWN_END)
				worldBrightness = NIGHT_BRIGHTNESS + DAY_AMP * (hours - DAWN_BEGIN) / DAWN_DURATION;
			else if (hours < SUNSET_BEGIN)
				worldBrightness = 1.f;
			else if (hours < SUNSET_END)
				worldBrightness = NIGHT_BRIGHTNESS + DAY_AMP * (SUNSET_END - hours) / SUNSET_DURATION;
			else
				worldBrightness = NIGHT_BRIGHTNESS;
		}
		else
			worldBrightness = NIGHT_BRIGHTNESS;
		return worldBrightness;
	}

	// Global Events

	static final int MIN_SCENARIO_DURATION = SECONDS_IN_DAY;

	private void genEvent()
	{
		if (Game.NO_EVENTS) return;
		
		// Выбираем сценарий миссии
		MissionScenario scenario = MissionScenario.getScenario(Game.level);

		// Выбираем место. TODO в зависимости от сценария (лес/горы/болото/город)

		// Выбираем гильдию, рядом с которой будет событие
		Guild g = Game.guilds.get(rnd.nextInt(Game.guilds.size()));

		// Получаем все точки в окрестности и выбираем случайную
		Poi[] pois = Game.map.getPois(g.city.x, g.city.y, 100);
		Poi poi = pois[rnd.nextInt(pois.length)];

		int end = time + MIN_SCENARIO_DURATION + rnd.nextInt(SECONDS_IN_DAY);

		// Создаем миссию
		Mission mission = new Mission(poi, Game.level, scenario, end);
		Game.addMission(mission);
		GameEvent.add(R.ui.event_satan, "Милорд,\nСилы зла пробудились!", GameEvent.TYPE_NEW_DARK_FORCES, mission);

		// Определяем время следующего события
		genNextEventTime();
	}

	private int event_delay = getTimeFromDays(15);
	private int event_dt = getTimeFromDays(5);

	private void genNextEventTime()
	{
		// TODO Интервал между событиями в зависимости от уровня игры
		next_event_time = time + event_delay + rnd.nextInt(event_dt);
		System.out.println("Next event time: " + next_event_time);
	}

	@Override
	public void onKeyPressed(KeyEvent e)
	{
		//System.out.println(e.getKeyCode());
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				InGameMenu.showMenu();
				break;

			case KeyEvent.VK_SPACE:
				GameLoop.paused = !GameLoop.paused;
				break;
			case KeyEvent.VK_1:
				setTimeSpeedLevel(1);
				break;
			case KeyEvent.VK_2:
				setTimeSpeedLevel(2);
				break;
			case KeyEvent.VK_3:
				setTimeSpeedLevel(3);
				break;
			case KeyEvent.VK_4:
				setTimeSpeedLevel(4);
				break;
			case KeyEvent.VK_5:
				setTimeSpeedLevel(5);
				break;
			case KeyEvent.VK_6:
				setTimeSpeedLevel(6);
				break;

			case KeyEvent.VK_N:
				beginCraete();
				break;
			case KeyEvent.VK_R:
				reCraete();
				break;

			case KeyEvent.VK_F2:
				Book.inst().show();
				break;
				
			default:
				super.onKeyPressed(e);
				return;
		}

		e.consume();
	}

	@Override
	protected void onChildClosed(GameEngine child)
	{
		if (child instanceof CityEngine)
		{
			guildsPanel.update();
		}
	}

	public void setCenter(MapObject p)
	{
		mapView.setCenter(p.x, p.y);
	}

	public void setCenter(int x, int y)
	{
		mapView.setCenter(x, y);
	}
}
