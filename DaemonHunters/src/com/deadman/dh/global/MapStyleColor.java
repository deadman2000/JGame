package com.deadman.dh.global;

import java.awt.Color;

import com.deadman.dh.model.GlobalMap;

public class MapStyleColor extends MapStyle
{

	private static final Color cTerrain = new Color(160, 224, 132);
	private static final Color cCoast = new Color(208, 195, 170);
	private static final Color cCoastline = new Color(149, 138, 116);
	private static final Color cMountain = new Color(149, 138, 116);
	private static final Color cMountain2 = new Color(106, 90, 76);
	private static final Color cForest = new Color(28, 84, 8);
	private static final Color cSeaLight = new Color(148, 188, 224);
	private static final Color cSeaMiddle = new Color(140, 184, 225);
	private static final Color cSeaDark = new Color(128, 172, 212);

	@Override
	public Color getColor(byte type)
	{
		switch (type)
		{
			case GlobalMap.TERRAIN_ID:
				return cTerrain;
			case GlobalMap.COAST_ID:
				return cCoast;
			case GlobalMap.COASTLINE_ID:
				return cCoastline;

			case GlobalMap.MOUNTAIN_ID:
				return cMountain;
			case GlobalMap.MOUNTAIN2_ID:
				return cMountain2;

			case GlobalMap.FOREST_ID:
				return cForest;

			case GlobalMap.SEA_LIGHT_ID:
				return cSeaLight;
			case GlobalMap.SEA_MIDDLE_ID:
				return cSeaMiddle;
			case GlobalMap.SEA_DARK_ID:
				return cSeaDark;

			case GlobalMap.RIVER_ID:
				return cSeaMiddle;
			case GlobalMap.RIVER2_ID:
				return cSeaDark;

			case GlobalMap.CITY_ID:
				return new Color(0xFFD0D0D0);
				//return Color.WHITE;// cCoast;

			case GlobalMap.INK_ID:
				return cMountain2;
			case GlobalMap.LABEL_ID:
				return Color.BLACK;

			default:
				return Color.BLACK;
		}
	}

}
