package com.deadman.dh;

import java.util.ArrayList;

import com.deadman.dh.model.MapObject;
import com.deadman.jgame.drawing.Drawable;

public class GameEvent
{
	public final Drawable icon;
	public final String message;
	public final int type;
	public final MapObject location;
	public final Object target;

	private GameEvent(int icon, String msg, int eventType, MapObject location, Object target)
	{
		if (icon > 0)
			this.icon = Drawable.get(icon);
		else
			this.icon = null;
		message = msg;
		type = eventType;
		this.location = location;
		this.target = target;
	}

	public static final int TYPE_INFO = 0;
	public static final int TYPE_BUILDING_COMPLETE = 1;
	public static final int TYPE_SQUAD_MOVE_COMPLETE = 2;
	public static final int TYPE_SQUAD_MISSION_BEGIN = 3;
	public static final int TYPE_NEW_DARK_FORCES = 4;

	public static ArrayList<GameEvent> events = new ArrayList<>();

	public static void add(int icon, String msg, int eventType, MapObject location)
	{
		add(icon, msg, eventType, location, null);
	}

	public static void add(int icon, String msg, int eventType, MapObject location, Object target)
	{
		events.add(new GameEvent(icon, msg, eventType, location, target));
	}
}
