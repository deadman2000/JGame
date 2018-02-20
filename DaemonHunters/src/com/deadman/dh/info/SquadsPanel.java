package com.deadman.dh.info;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.R;
import com.deadman.dh.dialogs.MenuAction;
import com.deadman.dh.dialogs.MessageBox;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.model.Cart;
import com.deadman.dh.model.Squad;
import com.deadman.dh.model.Unit;
import com.deadman.dh.model.items.ItemsGrid;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;

public class SquadsPanel extends Control
{
	private Guild guild;
	
	Squad _currentSquad;
	int _squadInd;

	private Label laSquad;
	private Button btAddSquad, btRemSquad, btPrevSquad, btNextSquad;
	private BigSlot btCart, btHorse;
	private ItemsGrid igCartItems;

	private UnitSlot[] cartUnitSlots;

	public SquadsPanel()
	{
		addControl(new Control(R.ui.squad_top_bgr, 40, 0));

		addControl(btAddSquad = new Button(R.ui.bt_add_squad, R.ui.bt_add_squad_pr));
		btAddSquad.setPosition(41, 0);
		btAddSquad.addControlListener(btSquad_listener);

		addControl(btPrevSquad = new Button(R.ui.bt_paper_prev, R.ui.bt_paper_prev_pr));
		btPrevSquad.setPosition(90, 4);
		btPrevSquad.addControlListener(btSquad_listener);

		laSquad = new Label(GlobalEngine.fnt4x7_brown_sh);
		laSquad.setBounds(103, 5, 103, 10);
		laSquad.autosize = false;
		laSquad.halign = Label.ALIGN_CENTER;
		addControl(laSquad);
		laSquad.addControlListener(btSquad_listener);

		addControl(btNextSquad = new Button(R.ui.bt_paper_next, R.ui.bt_paper_next_pr));
		btNextSquad.setPosition(206, 4);
		btNextSquad.addControlListener(btSquad_listener);

		addControl(btRemSquad = new Button(R.ui.bt_remove_squad, R.ui.bt_remove_squad_pr));
		btRemSquad.setPosition(232, 0);
		btRemSquad.addControlListener(btSquad_listener);

		addControl(btCart = new BigSlot(7, 18));
		btCart.addControlListener(btSquad_listener);

		addControl(btHorse = new BigSlot(159, 18));
		btHorse.addControlListener(btSquad_listener);

		cartUnitSlots = new UnitSlot[2 * 4];
		Control unitItem;
		for (int x = 0; x < 4; x++)
			for (int y = 0; y < 2; y++)
			{
				int i = y + x * 2;
				int sx = 119 - x * 37;
				int sy = 102 + y * 38;
				addControl(unitItem = (cartUnitSlots[i] = new UnitSlot(i, sx, sy)));
				unitItem.addControlListener(btSquadUnitItem_listener);
			}

		addControl(igCartItems = new ItemsGrid(170, 110));
	}
	
	public void setGuild(Guild g)
	{
		guild = g;
		showSquad(null);
	}

	public void showSquad(Squad sq)
	{
		if (sq == null && guild.squads.size() > 0)
			sq = guild.squads.get(0);

		_currentSquad = sq;
		if (sq == null)
		{
			laSquad.setText("");
			btRemSquad.visible = false;

			btCart.visible = false;
			btHorse.visible = false;

			for (int i = 0; i < cartUnitSlots.length; i++)
			{
				cartUnitSlots[i].visible = false;
			}

			igCartItems.visible = false;
			return;
		}

		btCart.visible = true;
		btHorse.visible = true;

		btRemSquad.visible = true;
		_squadInd = guild.squads.indexOf(sq);
		laSquad.setText(sq.name);

		Cart cart = sq.getCart();
		boolean inHome = sq.inHome();

		if (cart != null)
		{
			btCart.icon = cart.icon;
			for (int i = 0; i < cartUnitSlots.length; i++)
			{
				UnitSlot slot = cartUnitSlots[i];
				slot.enabled = inHome;
				if (i >= cart.unitsCapacity)
				{
					slot.visible = false;
				}
				else
				{
					slot.visible = true;
					slot.unit = sq.getUnit(i);
				}
			}
		}
		else
		{
			btCart.icon = null;
			for (UnitSlot slot : cartUnitSlots)
				slot.visible = false;
		}
		btCart.enabled = inHome;

		if (sq.horse != null)
			btHorse.icon = sq.horse.icon;
		else
			btHorse.icon = null;
		btHorse.enabled = inHome;

		igCartItems.visible = true;
		igCartItems.setPage(sq.items);

		if (inHome)
			igCartItems.enable();
		else
			igCartItems.disable();

	}

	public void updateSquad()
	{
		showSquad(_currentSquad);
	}

	private ControlListener btSquad_listener = new ControlListener()
	{
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			if (sender == btCart)
			{
				if (btCart.enabled) new BuyCart(_currentSquad).showModal();
			}
			else if (sender == btHorse)
			{
				if (btHorse.enabled) new BuyHorse(_currentSquad).showModal();
			}
			else if (sender == btAddSquad)
				new EnterSquadName(SquadsPanel.this, guild).showModal();
			else if (sender == btRemSquad)
				removeSquad();
			else if (sender == btPrevSquad)
			{
				if (guild.squads.size() < 2) return;
				_squadInd--;
				if (_squadInd < 0)
					_squadInd = guild.squads.size() - 1;
				showSquad(guild.squads.get(_squadInd));
			}
			else if (sender == btNextSquad)
			{
				if (guild.squads.size() < 2) return;
				_squadInd++;
				if (_squadInd >= guild.squads.size())
					_squadInd = 0;
				showSquad(guild.squads.get(_squadInd));
			}
			else if (sender == laSquad)
			{
				if (_currentSquad == null) return;
				new EnterSquadName(SquadsPanel.this, _currentSquad).showModal();
			}
		};
	};

	private ControlListener btSquadUnitItem_listener = new ControlListener()
	{
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			UnitSlot slot = (UnitSlot) sender;
			if (!slot.enabled) return;
			int pos = slot.index;
			if (e.getButton() == 3)
			{
				if (_currentSquad.getUnit(pos) != null)
					setUnitInSquad(null, pos);
				else if (guild.squadFreeUnits.size() > 0)
					setUnitInSquad(guild.squadFreeUnits.get(0), pos);
				return;
			}

			new UnitSelect(_currentSquad, pos).showModal();
		}
	};

	private void setUnitInSquad(Unit unit, int pos)
	{
		_currentSquad.setUnit(unit, pos);
		updateSquad();
	}

	protected void removeSquad()
	{
		MessageBox.showYesNo("Удалить отряд?", new MenuAction()
		{
			@Override
			public void onAction(Object sender, int action)
			{
				if (action == MessageBox.MBACTION_YES)
				{
					_currentSquad.remove();
					showSquad(null);
				}
			}
		});
	};

}
