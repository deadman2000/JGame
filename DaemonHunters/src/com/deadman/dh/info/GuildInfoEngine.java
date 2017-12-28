package com.deadman.dh.info;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.dialogs.MenuAction;
import com.deadman.dh.dialogs.MessageBox;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.model.Cart;
import com.deadman.dh.model.Squad;
import com.deadman.dh.model.Unit;
import com.deadman.dh.model.items.AmmunitionGrid;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.dh.model.items.ItemsGrid;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ProgressBar;
import com.deadman.jgame.ui.RelativeLayout;
import com.deadman.jgame.ui.VListView;

public class GuildInfoEngine extends GameEngine
{
	public static final GameFont fnt4x6 = getFont(R.fonts.font4x6, 0xFF3a2717).shadow(0xFF9f9a8a);
	public static final GameFont fnt3x5 = getFont(R.fonts.font3x5, 0xFF3a2717).shadow(0xFF9f9a8a);

	public static final GameFont fnt4x7_l = getFont(R.fonts.font4x7, 0xFF3a2717).shadow(0xFF808464);
	public static final GameFont fnt4x7_work = getFont(R.fonts.font4x7, 0xFF3a2717).shadow(0xFFada793);

	private Guild guild;

	private Label laGuild;
	private Button btPrevG, btNextG;
	private Button chbUnits, chbSquads;
	private int guildInd;

	private ItemsGrid igStorage;

	public GuildInfoEngine()
	{
		this(null);
	}

	public GuildInfoEngine(Guild g)
	{
		cursor = Game.ItemCursor;

		Control ctlPaper = new Control(R.ui.paper);
		addControl(ctlPaper);
		RelativeLayout.settings(ctlPaper)
				.fill();

		Control ctlBorder = new Control(R.ui.pl_info_9p);
		addControl(ctlBorder);
		RelativeLayout.settings(ctlBorder)
				.fill();

		addControl(laGuild = new Label(fnt4x7_l));
		laGuild.setBounds(38, 7, 128, 17);
		laGuild.autosize = false;
		laGuild.halign = Label.ALIGN_CENTER;
		laGuild.valign = Label.ALIGN_CENTER;
		laGuild.bgrColor = 0xff9ba07c;

		addControl(btPrevG = new Button(R.ui.bt_prev_g, R.ui.bt_prev_g_pr));
		btPrevG.setPosition(12, 7);
		btPrevG.addControlListener(prev_next_listener);

		addControl(btNextG = new Button(R.ui.bt_next_g, R.ui.bt_next_g_pr));
		btNextG.setPosition(174, 7);
		btNextG.addControlListener(prev_next_listener);

		addControl(new Control(getDrawable(R.ui.bt_border_info_9p), 215, 5, 41, 21));
		addControl(chbUnits = new Button(R.ui.bt_info_units, R.ui.bt_info_units_pr));
		chbUnits.setPosition(217, 7);
		chbUnits.addControlListener(chb_tabs_listener);
		chbUnits.check_on_click = true;

		addControl(new Control(getDrawable(R.ui.bt_border_info_9p), 260, 5, 41, 21));
		addControl(chbSquads = new Button(R.ui.bt_info_squad, R.ui.bt_info_squad_pr));
		chbSquads.setPosition(262, 7);
		chbSquads.addControlListener(chb_tabs_listener);
		chbSquads.check_on_click = true;

		// Рамка для кнопки
		Control ctlBorderInfo = new Control(getDrawable(R.ui.bt_border_info_9p));
		RelativeLayout.settings(ctlBorderInfo).alignRight(5).alignTop(5);
		ctlBorderInfo.setSize(26, 21);
		addControl(ctlBorderInfo);
		
		Button btClose = new Button(R.ui.bt_info_close, R.ui.bt_info_close_pr);
		RelativeLayout.settings(btClose).alignRight(7).alignTop(7);
		addControl(btClose);
		btClose.addControlListener(btClose_listener);

		createUnitsPanel();
		createSquadsPanel();

		// Слоты с предметами гильдии
		addControl(igStorage = new ItemsGrid(getDrawable(R.ui.storage_slot)));
		RelativeLayout.settings(igStorage).alignLeft(10).alignBottom(6);

		if (g == null) g = Game.guilds.get(0);
		setGuild(g);

		chbUnits.setChecked(true);
	}

	private ControlListener prev_next_listener = new ControlListener()
	{
		@Override
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			if (sender == btPrevG)
			{
				guildInd--;
				if (guildInd < 0) guildInd = Game.guilds.size() - 1;
				setGuild(Game.guilds.get(guildInd));
			}
			else
			{
				guildInd++;
				if (guildInd >= Game.guilds.size()) guildInd = 0;
				setGuild(Game.guilds.get(guildInd));
			}
		};
	};

	private ControlListener chb_tabs_listener = new ControlListener()
	{
		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_CHECKED)
			{
				if (sender == chbUnits)
				{
					chbSquads.setChecked(false);
					plSquads.visible = false;
					plUnits.visible = true;
					updateUnit();
				}
				else if (sender == chbSquads)
				{
					chbUnits.setChecked(false);
					plUnits.visible = false;
					plSquads.visible = true;
					updateSquad();
				}
			}
		};
	};

	private ControlListener btClose_listener = new ControlListener()
	{
		@Override
		public void onControlPressed(Control control, MouseEvent e)
		{
			close();
		};
	};

	@Override
	public void close()
	{
		if (ItemSlot.pickedItem() != null) // Не даем закрыть, если в руках предмет
			return;
		super.close();
	};

	@Override
	protected void onChildClosed(GameEngine child)
	{
		if (chbUnits.checked)
			updateUnit();
		else if (chbSquads.checked)
			updateSquad();
	}

	void setGuild(Guild g)
	{
		if (guild == g) return;

		if (ItemSlot.pickedItem() != null) // Не даем закрыть, если в руках предмет
			return;

		guild = g;
		guildInd = Game.guilds.indexOf(g);
		laGuild.setText(g.city.name);

		igStorage.setPage(guild.storage);

		updateUnitsCount();
		showUnit(null);
		showSquad(null);
	}

	public void updateUnitsCount()
	{
		laUnitsCount.setText(guild.units.size() + "/" + guild.maxUnitCapcity());
	}

	// Region UNITS

	private Control plUnits;
	private int _unitIndex = 0;

	Button btPrevUnit, btNextUnit;
	private Label laUnitsCount;
	private Control pbUnitPortrait;
	private Label laUnitName;
	private Control icUnitGender;
	private Drawable picMale, picFemale;
	private Drawable pbBlue;
	private ProgressBar pbExp, pbHP, pbMP;
	private Label laLvl, laWork, laExp, laHP, laMP, laST, laDX, laIN;
	private VListView lvSkills;
	private ListItemSkill lviSword, lviShield, lviThrow, lviCraft, lviScience, lviMagic;

	private ItemsGrid igBackpack;
	private AmmunitionGrid agAmmunition;

	private ControlListener units_navigate_listener = new ControlListener()
	{
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			if (sender == btPrevUnit)
			{
				_unitIndex--;
				if (_unitIndex < 0)
					_unitIndex = guild.units.size() - 1;
			}
			else if (sender == btNextUnit)
			{
				_unitIndex++;
				if (_unitIndex >= guild.units.size())
					_unitIndex = 0;
			}
			showUnit(guild.units.get(_unitIndex));
		};
	};

	private void createUnitsPanel()
	{
		plUnits = new Control();
		RelativeLayout.settings(plUnits).fill(0, 32, 0, 32);
		addControl(plUnits);

		plUnits.addControl(new Control(R.ui.unit_stat_bgr, 4, 0));

		Button btUnitWork = new Button(R.ui.bt_unit_work);
		btUnitWork.setPosition(203, 16);
		btUnitWork.addControlListener(btUnitWork_listener);
		btUnitWork.clickOnBgr = true;
		plUnits.addControl(btUnitWork);

		plUnits.addControl(laWork = new Label(fnt4x7_work, 203, 21));
		laWork.width = 115;
		laWork.height = 9;
		laWork.halign = Label.ALIGN_CENTER;
		laWork.autosize = false;

		Button btRecruit = new Button(R.ui.bt_gem_add);
		btRecruit.setPosition(9, 3);
		btRecruit.addControlListener(btRecruit_listener);
		plUnits.addControl(btRecruit);

		Button btDismiss = new Button(R.ui.bt_gem_remove);
		btDismiss.addControlListener(btDismiss_listener);
		btDismiss.setPosition(9, 28);
		plUnits.addControl(btDismiss);

		plUnits.addControl(btPrevUnit = new Button(R.ui.bt_paper_prev, R.ui.bt_paper_prev_pr));
		btPrevUnit.setPosition(35, 19);
		btPrevUnit.addControlListener(units_navigate_listener);

		plUnits.addControl(pbUnitPortrait = new Control(53, 9, Unit.PORTRAIT_WIDTH, Unit.PORTRAIT_HEIGHT));

		picMale = getDrawable(R.ui.ic_unit_male);
		picFemale = getDrawable(R.ui.ic_unit_female);
		plUnits.addControl(icUnitGender = new Control(88, 12, 13, 13));
		icUnitGender.bgrMode = Control.BGR_ONE;

		plUnits.addControl(laUnitName = new Label(GlobalEngine.fnt4x7_brown_sh, 101, 15));

		plUnits.addControl(new Control(R.ui.lvl_txt, 90, 30));
		plUnits.addControl(laLvl = new Label(GlobalEngine.fnt4x7_brown_sh, 119, 32));

		plUnits.addControl(btNextUnit = new Button(R.ui.bt_paper_next, R.ui.bt_paper_next_pr));
		btNextUnit.setPosition(191, 19);
		btNextUnit.addControlListener(units_navigate_listener);

		plUnits.addControl(laUnitsCount = new Label(GlobalEngine.fnt4x7_brown_sh, 160, 1));
		laUnitsCount.autosize = false;
		laUnitsCount.height = 9;
		laUnitsCount.width = 63;

		plUnits.addControl(new Control(R.ui.unit_info_bgr, 10, 55));

		plUnits.addControl(pbExp = new ProgressBar(R.ui.pb_exp, 39, 56));
		plUnits.addControl(laExp = new Label(fnt4x6, 143, 55));

		plUnits.addControl(pbHP = new ProgressBar(R.ui.pb_hp, 69, 72));
		plUnits.addControl(laHP = new Label(fnt3x5, 141, 72));

		plUnits.addControl(pbMP = new ProgressBar(R.ui.pb_mp, 69, 84));
		plUnits.addControl(laMP = new Label(fnt3x5, 141, 84));

		plUnits.addControl(laST = new Label(fnt3x5, 25, 71));
		plUnits.addControl(laDX = new Label(fnt3x5, 25, 82));
		plUnits.addControl(laIN = new Label(fnt3x5, 25, 93));

		lvSkills = new VListView();
		lvSkills.setSpacing(2);
		lvSkills.setWidth(100);
		RelativeLayout.settings(lvSkills).alignLeft(10).alignTop(107).alignBottom(73);
		plUnits.addControl(lvSkills);

		pbBlue = getDrawable(R.ui.blue_pb);
		lvSkills.addItem(lviSword = new ListItemSkill(R.ui.ic_sword));
		lvSkills.addItem(lviShield = new ListItemSkill(R.ui.ic_shield));
		lvSkills.addItem(lviThrow = new ListItemSkill(R.ui.ic_bow));
		lvSkills.addItem(lviCraft = new ListItemSkill(R.ui.ic_anvil));
		lvSkills.addItem(lviScience = new ListItemSkill(R.ui.ic_book));
		lvSkills.addItem(lviMagic = new ListItemSkill(R.ui.ic_magic, pbBlue));

		plUnits.addControl(agAmmunition = new AmmunitionGrid(220, 50));

		plUnits.addControl(new Control(R.ui.backpack_bgr, 218, 148));
		plUnits.addControl(igBackpack = new ItemsGrid(220, 150));
	}

	private ControlListener btRecruit_listener = new ControlListener()
	{
		@Override
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			new RecruitsTable(GuildInfoEngine.this, guild).showModal();
		};
	};

	private ControlListener btDismiss_listener = new ControlListener()
	{
		@Override
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			removeCurrentUnit();
		}
	};

	private ControlListener btUnitWork_listener = new ControlListener()
	{
		@Override
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			new BuildingSelect(_currUnit).showModal();
		};
	};

	private Unit _currUnit;

	protected void showUnit(Unit unit)
	{
		if (unit == null && guild.units.size() > 0)
			unit = guild.units.get(0);

		_currUnit = unit;
		if (unit == null)
			return;

		_unitIndex = guild.units.indexOf(unit);

		pbUnitPortrait.background = unit.portrait;

		if (unit.male)
			icUnitGender.background = picMale;
		else
			icUnitGender.background = picFemale;
		laUnitName.setText(unit.name);
		laLvl.setText(unit.lvl);

		if (unit.building == null)
			laWork.setText("СВОБОДЕН");
		else
			laWork.setText(unit.building.type.name);

		pbExp.setValue((float) unit.experience / unit.getExpNext());
		laExp.setText(unit.experience + " /" + unit.getExpNext());

		pbHP.setValue((float) unit.hpCount / unit.hpMax);
		laHP.setText(unit.hpCount + "/" + unit.hpMax);

		pbMP.setValue((float) unit.mpCount / unit.mpMax);
		laMP.setText(unit.mpCount + "/" + unit.mpMax);

		laST.setText(unit.str);
		laDX.setText(unit.dex);
		laIN.setText(unit.intl);

		lviSword.setValue(unit.skSword);
		lviShield.setValue(unit.skShield);
		lviThrow.setValue(unit.skThrow);
		lviCraft.setValue(unit.skCraft);
		lviScience.setValue(unit.skScience);
		lviMagic.setValue(unit.skMagic);
		lvSkills.update();

		igBackpack.setPage(unit.backpack);
		agAmmunition.setPage(unit.ammunition);
	}

	void updateUnit()
	{
		showUnit(_currUnit);
	}

	private void removeCurrentUnit()
	{
		if (_currUnit == null)
			return;

		MessageBox.showYesNo("Уволить члена гильдии?", new MenuAction()
		{
			@Override
			public void onAction(Object sender, int action)
			{
				if (action == MessageBox.MBACTION_YES)
				{
					_currUnit.removeFromGuild();
					showUnit(null);
					updateUnitsCount();
				}
			}
		});
	}

	// End region UNITS

	// Region SQUADS

	private Control plSquads;
	private Label laSquad;
	private Button btAddSquad, btRemSquad, btPrevSquad, btNextSquad;
	private BigSlot btCart, btHorse;
	private ItemsGrid igCartItems;

	private UnitSlot[] cartUnitSlots;

	private void createSquadsPanel()
	{
		plSquads = new Control(0, 32, GameScreen.GAME_WIDTH, GameScreen.GAME_HEIGHT);
		RelativeLayout.settings(plSquads).fill(0, 32, 0, 32);
		addControl(plSquads);

		plSquads.addControl(new Control(R.ui.squad_top_bgr, 40, 0));

		plSquads.addControl(btAddSquad = new Button(R.ui.bt_add_squad, R.ui.bt_add_squad_pr));
		btAddSquad.setPosition(41, 0);
		btAddSquad.addControlListener(btSquad_listener);

		plSquads.addControl(btPrevSquad = new Button(R.ui.bt_paper_prev, R.ui.bt_paper_prev_pr));
		btPrevSquad.setPosition(90, 4);
		btPrevSquad.addControlListener(btSquad_listener);

		laSquad = new Label(GlobalEngine.fnt4x7_brown_sh);
		laSquad.setBounds(103, 5, 103, 10);
		laSquad.autosize = false;
		laSquad.halign = Label.ALIGN_CENTER;
		plSquads.addControl(laSquad);
		laSquad.addControlListener(btSquad_listener);

		plSquads.addControl(btNextSquad = new Button(R.ui.bt_paper_next, R.ui.bt_paper_next_pr));
		btNextSquad.setPosition(206, 4);
		btNextSquad.addControlListener(btSquad_listener);

		plSquads.addControl(btRemSquad = new Button(R.ui.bt_remove_squad, R.ui.bt_remove_squad_pr));
		btRemSquad.setPosition(232, 0);
		btRemSquad.addControlListener(btSquad_listener);

		plSquads.addControl(btCart = new BigSlot(7, 18));
		btCart.addControlListener(btSquad_listener);

		plSquads.addControl(btHorse = new BigSlot(159, 18));
		btHorse.addControlListener(btSquad_listener);

		cartUnitSlots = new UnitSlot[2 * 4];
		Control unitItem;
		for (int x = 0; x < 4; x++)
			for (int y = 0; y < 2; y++)
			{
				int i = y + x * 2;
				int sx = 119 - x * 37;
				int sy = 102 + y * 38;
				plSquads.addControl(unitItem = (cartUnitSlots[i] = new UnitSlot(i, sx, sy)));
				unitItem.addControlListener(btSquadUnitItem_listener);
			}

		plSquads.addControl(igCartItems = new ItemsGrid(170, 110));
	}

	Squad _currentSquad;
	int _squadInd;

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
				new EnterSquadName(GuildInfoEngine.this, guild).showModal();
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
				new EnterSquadName(GuildInfoEngine.this, _currentSquad).showModal();
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

	// End region SQUADS
}
