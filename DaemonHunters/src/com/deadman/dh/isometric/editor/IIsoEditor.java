package com.deadman.dh.isometric.editor;

import com.deadman.dh.isometric.IsoMap;

// Временный интерфейс. потом удалить!!!
public interface IIsoEditor
{
	IsoMap getMap();
	
	void createMap(String fileName, int w, int h, int l, int zl);

	void loadMap(String fileName);
}
