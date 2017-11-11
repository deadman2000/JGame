package com.deadman.dh.model.items;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.isometric.MapCell;
import com.deadman.jgame.ui.Control;

public class ItemsGridList extends Control
{
	public static int PAD_X = 3;
	public static int PAD_Y = 3;

	private MapCell _cell;
	private ItemMovingValidator validator;

	public ItemsGridList(int x, int y, int w, int h)
	{
		setBounds(x, y, w, h);
		bgrColor = 0xff000000;
	}

	public void setCell(MapCell cell)
	{
		_cell = cell;
	}

	@Override
	public void draw()
	{
		super.draw();

		if (_cell == null || !_cell.hasItems()) return;

		for (int i = 0; i < _cell.items.size(); i++)
		{
			Item it = _cell.items.get(i);
			if (it == null)
				continue;

			int x = scrX + PAD_X + ItemSlot.ITEM_WIDTH / 2 + (ItemSlot.ITEM_WIDTH + PAD_X) * i;
			int y = scrY + PAD_Y + ItemSlot.ITEM_HEIGHT / 2;

			it.drawAt(x, y);
		}
	}

	@Override
	protected void onClick(Point p, MouseEvent e)
	{
		super.onClick(p, e);

		int cx = (p.x - PAD_X) / (ItemSlot.ITEM_WIDTH + PAD_X);
		int cy = (p.y - PAD_Y) / (ItemSlot.ITEM_HEIGHT + PAD_Y);
		int w = (width - PAD_X) / (ItemSlot.ITEM_WIDTH + PAD_X);

		int i = cy * w + cx;

		Item it = null;
		if (_cell.hasItems() && i < _cell.items.size()) // Нажали на предмет
			it = _cell.items.get(i);
		Item pick = ItemSlot.pickedItem();

		if (pick != null) // В руках предмет. 
		{
			if (validator != null && !validator.canDrop(pick, this))
				return;

			if (it != null) // Меняем местами
			{
				_cell.items.set(i, pick);
				ItemSlot.pick(it, this);
			}
			else // Кладем
			{
				_cell.items.add(pick);
				ItemSlot.drop();
			}
		}
		else // В руках ничего. Берем
		{
			if (it == null) return;

			if (validator != null && !validator.canPick(it, this))
				return;

			ItemSlot.pick(it, this);
			_cell.items.remove(i);
		}
		
		_cell.calcPassive();
	}

	public void setValidator(ItemMovingValidator validator)
	{
		this.validator = validator;
	}
}
