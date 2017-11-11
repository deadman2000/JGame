package com.deadman.dh.global;

import java.awt.Color;

public abstract class MapStyle
{
	public static final MapStyle color = new MapStyleColor();
	public static final MapStyle old = new MapStyleOld();
	public static final MapStyle city = new MapStyleCity();

	public abstract Color getColor(byte type);
}
