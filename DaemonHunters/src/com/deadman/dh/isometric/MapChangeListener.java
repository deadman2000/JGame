package com.deadman.dh.isometric;

public interface MapChangeListener
{
	public void onMapChanged();
	
	public void onMoving(MapCell cell);
}
