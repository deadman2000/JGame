package com.deadman.dh.guild;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoSprite;

public class GuildPatternGroup implements IGuildPatternSource
{
	private static final Random rnd = new Random();

	private ArrayList<GuildBuildPattern> patterns; // Все шаблоны
	private ArrayList<GuildBuildPattern> work; // Рабочий набор

	public GuildPatternGroup(GuildBuildPattern[] arr)
	{
		patterns = new ArrayList<GuildBuildPattern>(Arrays.asList(arr));
	}

	public GuildPatternGroup()
	{
		patterns = new ArrayList<>();
	}

	@Override
	public void reset()
	{
		for (GuildBuildPattern p : patterns)
			p.reset();
		work = new ArrayList<>(patterns);
	}

	@Override
	public GuildBuildPattern getPattern()
	{
		if (work.size() == 0) return null;
		int i = rnd.nextInt(work.size());
		return work.get(i);
	}

	@Override
	public void disable(GuildBuildPattern p)
	{
		work.remove(p);
	}

	public GuildPatternGroup add(GuildBuildPattern p)
	{
		patterns.add(p);
		return this;
	}

	private static HashMap<String, GuildBuildPattern> simplePatterns = new HashMap<>();

	public GuildPatternGroup generate(String spriteName)
	{
		GuildBuildPattern p;

		if (simplePatterns.containsKey(spriteName))
		{
			p = simplePatterns.get(spriteName);
			patterns.add(p);
			p = simplePatterns.get(spriteName + "_");
			patterns.add(p);
			return this;
		}

		IsoSprite spr = IsoSprite.byName(spriteName);

		IsoMap map = new IsoMap(2, 1, 1, 0);
		spr.setToR(map.cells[0][1][0]);
		p = new GuildBuildPattern(map).setAnchor(GuildMapBuilder.ANCHOR_TOP);
		patterns.add(p);
		simplePatterns.put(spriteName, p);

		map = new IsoMap(1, 2, 1, 0);
		spr.setToL(map.cells[0][0][1]);
		p = new GuildBuildPattern(map).setAnchor(GuildMapBuilder.ANCHOR_LEFT);
		patterns.add(p);
		simplePatterns.put(spriteName + "_", p);

		return this;
	}
}
