package com.deadman.dh.global;

import java.awt.Color;

import com.deadman.dh.model.GlobalMap;

public class MapStyleCity extends MapStyle
{
	private static final Color cGray = new Color(208, 208, 208);

	private static final Color cDark = new Color(149, 138, 116);

	private static final Color cSea = new Color(193, 234, 234);

	@Override
	public Color getColor(byte type)
	{
		switch (type)
		{
			case GlobalMap.MOUNTAIN2_ID:
			case GlobalMap.MOUNTAIN_ID:
			case GlobalMap.FOREST_ID:
			case GlobalMap.TERRAIN_ID:
			case GlobalMap.COAST_ID:
				return cGray;

			case GlobalMap.COASTLINE_ID:
				return cDark;

			case GlobalMap.SEA_DARK_ID:
			case GlobalMap.SEA_MIDDLE_ID:
			case GlobalMap.SEA_LIGHT_ID:
				return cSea;

			case GlobalMap.RIVER_ID:
			case GlobalMap.RIVER2_ID:
			case GlobalMap.RIVER3_ID:
			default:
				return Color.BLACK;
		}
	}
}
