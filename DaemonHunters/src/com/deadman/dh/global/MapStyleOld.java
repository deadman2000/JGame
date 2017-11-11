package com.deadman.dh.global;

import java.awt.Color;

import com.deadman.dh.model.GlobalMap;

public class MapStyleOld extends MapStyle
{
	private static final Color cLight = new Color(237, 231, 213);
	private static final Color cNormal = new Color(222, 213, 196);

	private static final Color cDark = new Color(149, 138, 116);
	private static final Color cDark2 = new Color(106, 90, 76);
	private static final Color cDark3 = new Color(73, 65, 57);

	private static final Color cSeaMiddle = new Color(222, 217, 200);
	private static final Color cSeaDark = new Color(208, 195, 170);
	private static final Color cLabel = new Color(49, 35, 19);

	@Override
	public Color getColor(byte type)
	{
		switch (type)
		{
			case GlobalMap.MOUNTAIN2_ID:
				return cNormal;
			case GlobalMap.MOUNTAIN_ID:
				return cNormal;

			case GlobalMap.FOREST_ID:
				return cNormal;
			case GlobalMap.TERRAIN_ID:
				return cNormal;
			case GlobalMap.COAST_ID:
				return cLight;
			case GlobalMap.COASTLINE_ID:
				return cDark;

			case GlobalMap.SEA_DARK_ID:
				return cSeaDark;
			case GlobalMap.SEA_MIDDLE_ID:
				return cSeaMiddle;
			case GlobalMap.SEA_LIGHT_ID:
				return cLight;

			case GlobalMap.RIVER_ID:
				return cDark;
			case GlobalMap.RIVER2_ID:
				return cDark2;
			case GlobalMap.RIVER3_ID:
				return cDark3;

			case GlobalMap.CITY_ID:
				return cLight;

			case GlobalMap.INK_ID:
				return cDark2;
			case GlobalMap.LABEL_ID:
				return cLabel;

			default:
				return Color.BLACK;
		}
	}

}
