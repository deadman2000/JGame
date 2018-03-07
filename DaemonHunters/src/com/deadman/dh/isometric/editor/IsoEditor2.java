package com.deadman.dh.isometric.editor;

import com.deadman.dh.IsoEditor_ui;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.systemui.IMainMenuListener;
import com.deadman.jgame.systemui.MainMenuItem;

public class IsoEditor2 extends GameEngine implements IMainMenuListener, IIsoEditor
{
	private IsoMap map;
	private IsoEditor_ui ui;
	private MapCell[][] buff;

	public IsoEditor2()
	{
		GameResources.init();
		
		ui = new IsoEditor_ui();
		setContent(ui);
		onSizeChanged();
		ui.mainMenu.listener = this;
	}

	@Override
	public void onMainMenuPressed(MainMenuItem item)
	{
		if (item == ui.mmiNew)
			newFile();
		else if (item == ui.mmiOpen)
			openFile();
		else if (item == ui.mmiSave)
			map.saveMap();
		else if (item == ui.mmiSaveAs)
			saveAs();
	}

	private void newFile()
	{
		new CreateMapForm(this).showModal();
	}

	private void openFile()
	{
		new OpenMapForm(this).showModal();
	}

	private void saveAs()
	{
		new SaveMapForm(this).showModal();
	}

	private void setStatus(String text)
	{
		//if (statusBar == null) return;
		ui.statusBar.setText(text.toUpperCase());
	}

	@Override
	public IsoMap getMap()
	{
		return map;
	}

	@Override
	public void createMap(String fileName, int w, int h, int l, int zl)
	{
		IsoMap m = new IsoMap(w, h, l, zl);
		m.fileName = fileName;
		setMap(m);
	}

	@Override
	public void loadMap(String fileName)
	{
		setMap(IsoMap.loadMap(fileName));
	}

	void setMap(IsoMap m)
	{
		if (m == null)
		{
			System.err.println("Can't load map");
			return;
		}
		map = m;
		ui.mapViewer.setMap(m);
		ui.mapViewer.centerView();

		GameScreen.screen.setTitle(m.fileName);

		buff = new MapCell[map.width][map.height];
	}
}
