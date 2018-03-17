package com.deadman.dh.isometric.editor;

import com.deadman.dh.isometric.IsoSimpleObject;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.items.Item;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;

public class CellView extends Control
{
	private MapCell _cell;

	public CellView()
	{
		bgrColor = 0xff000000;
		setSize(40, 56);
	}

	public void setCell(MapCell cell)
	{
		_cell = cell;
	}

	@Override
	protected void onDraw()
	{
		if (_cell == null) return;

		int px = scrX + 2;
		int py = scrY + 2;

		if (_cell.floor != null)
			_cell.floor.drawAt(px, py);
		if (_cell.wall_l != null)
			_cell.wall_l.drawAt(px, py);
		if (_cell.wall_r != null)
			_cell.wall_r.drawAt(px, py);

		if (_cell.wall_l != null && _cell.wall_r != null)
		{
			Drawable d = ((IsoSimpleObject) _cell.wall_l).sprite.getState(2, 0);
			if (d != null)
				d.drawAt(px, py); // Стык стен
		}

		if (_cell.obj_l != null)
			_cell.obj_l.drawAt(px, py);
		if (_cell.obj_r != null)
			_cell.obj_r.drawAt(px, py);

		int zshift = _cell.getZShift();

		if (zshift == 0 && _cell.items != null) // Предметы на полу
			for (Item it : _cell.items)
			it.drawIsoAt(px, py);

		if (_cell.obj != null)
			_cell.obj.drawAt(px, py);

		if (zshift > 0 && _cell.items != null) // Предметы на объекте
			for (Item it : _cell.items)
			it.drawIsoAt(px, py - zshift);
	}
}
