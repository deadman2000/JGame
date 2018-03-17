package com.deadman.dh.isometric.editor;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.Game;
import com.deadman.dh.ItemSelectForm_ui;
import com.deadman.dh.R;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.dh.model.itemtypes.ItemType;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ListViewItem;

public class ItemSelectForm extends ItemSelectForm_ui
{
	private final IItemSelected _listener;

	public ItemSelectForm(IItemSelected listener)
	{
		_listener = listener;
		lvItemTypes.setScrollBar(Game.createVScrollInfo());
		for (ItemType it : ItemType.itemTypes.values())
		{
			lvItemTypes.addItem(new ItemTypeItem(it));
		}

		btAdd.addControlListener(add_listener);
		btCancel.addControlListener(cancel_listener);
	}

	ControlListener add_listener = new ControlListener()
	{
		public void onControlPressed(com.deadman.jgame.ui.Control control, java.awt.event.MouseEvent e)
		{
			commit();
		}
	};
	
	private void commit()
	{
		ItemTypeItem iti = (ItemTypeItem) lvItemTypes.selectedItem();
		if (iti == null) return;
		int count;

		try
		{
			count = Integer.parseInt(tbCount.text);
		}
		catch (Exception ex)
		{
			return;
		}
		
		if (count <= 0) return;

		Item it = iti.type.generate().setCount(count);
		_listener.onItemSelected(it);
		close();
	}

	ControlListener cancel_listener = new ControlListener()
	{
		public void onControlPressed(com.deadman.jgame.ui.Control control, java.awt.event.MouseEvent e)
		{
			close();
		}
	};

	public interface IItemSelected
	{
		void onItemSelected(Item it);
	}
	
	class ItemTypeItem extends ListViewItem
	{
		public final ItemType type;

		public ItemTypeItem(ItemType type)
		{
			this.type = type;
			height = ItemSlot.ITEM_HEIGHT + 2;
			addControl(new Label(getFont(R.fonts.font3x5, 0xffffffff), ItemSlot.ITEM_WIDTH + 4, 6, type.name));
		}

		@Override
		protected void onDraw()
		{
			type.icon.drawAt(scrX + ItemSlot.ITEM_WIDTH / 2 + 1, scrY + ItemSlot.ITEM_HEIGHT / 2 + 1);
			super.onDraw();
		}

		@Override
		public void onSelected()
		{
			bgrColor = 0x40ffffff;
		}

		@Override
		public void onDeselected()
		{
			bgrColor = 0;
		}
		
		@Override
		protected void onClick(Point p, MouseEvent e)
		{
			super.onClick(p, e);
			if (e.getButton() == 1 && e.getClickCount() == 2)
			{
				commit();
			}
		}
	}
}
