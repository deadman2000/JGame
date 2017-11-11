package com.deadman.dh.info;

import java.awt.event.KeyEvent;

import com.deadman.dh.Game;
import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MessageBox;
import com.deadman.dh.model.Cart;
import com.deadman.dh.model.Squad;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.VListView;

public class BuyCart extends IGMPanel
{
	static final int TOP_PAD = 20;
	static final int BOTTOM_PAD = 27;

	private final VListView lv;
	
	private final Squad squad;

	public BuyCart(Squad sq)
	{
		super(240, 0);
		
		squad = sq;

		addControl(new Label(fnt_igm, 7, 7, "Название"));
		addControl(new Label(fnt_igm, 120, 7, "Размер"));
		addControl(new Label(fnt_igm, 163, 7, "Цена"));

		addControl(lv = new VListView(5, TOP_PAD, width - 13, Cart.carts.length * CartRow.HEIGHT));
		lv.item_height = RecruitRow.HEIGHT;
		lv.setScrollBar(Game.createVScrollInfo());
		lv.bgrColor = 0xFF686c51;
		lv.addControlListener(lv_listener);

		Cart current = sq.getCart();
		for (int i = 0; i < Cart.carts.length; i++)
		{
			Cart c = Cart.carts[i];
			if (!c.accessible || c == current) continue;
			lv.addItem(new CartRow(i, c));
		}
		if (lv.size() > 0)
			lv.selectIndex(0);

		height = TOP_PAD + CartRow.HEIGHT * lv.size() + BOTTOM_PAD;

		int by = TOP_PAD + lv.size() * RecruitRow.HEIGHT + 2;
		addButton(0, "Купить", 86, by, 70);
		addButton(TAG_CLOSE, "Закрыть", 160, by, 70);
	}

	private ControlListener lv_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag) 
		{
			if (action == ACTION_ITEM_DBLCLICK)
				buyCurrent();
		};
	};

	protected void onButtonPressed(int tag)
	{
		if (tag == 0)
			buyCurrent();
	};

	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			buyCurrent();
	}

	private void buyCurrent()
	{
		if (lv.selectedItem() == null) return;

		Cart cart = (Cart) lv.selectedItem().tag;

		if (Game.gold < cart.price)
		{
			MessageBox.show("Недостаточно золота");
			return;
		}

		Game.gold -= cart.price;
		squad.setCart(cart);
		close();
	}

}
