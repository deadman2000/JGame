package com.deadman.dh.guild;

import com.deadman.dh.isometric.IsoMap;

public class GuildBuildPattern implements IGuildPatternSource
{
	public IsoMap map;
	public int max; // Максимально число на 1 помещение
	public int anchor = 0; // привязка к стене
	public int doorPos = GuildMapBuilder.DOOR_ALL; // Позиция входа
	private boolean enabled;

	public int used; // Число использований

	public GuildBuildPattern(int mapId)
	{
		map = IsoMap.loadMap(mapId); // TODO Грузить при первом обращении
	}

	public GuildBuildPattern(IsoMap map)
	{
		this.map = map;
	}

	public GuildBuildPattern setAnchor(int value)
	{
		anchor = value;
		return this;
	}

	public GuildBuildPattern setDoorFilter(int value)
	{
		doorPos = value;
		return this;
	}

	public boolean isDoorPass(int door)
	{
		return (door & doorPos) > 0;
	}

	public GuildBuildPattern setMax(int value)
	{
		max = value;
		return this;
	}

	@Override
	public void reset()
	{
		used = 0;
		enabled = true;
	}

	@Override
	public GuildBuildPattern getPattern()
	{
		if (enabled) return this;
		return null;
	}

	@Override
	public void disable(GuildBuildPattern m)
	{
		enabled = false;
	}
}
