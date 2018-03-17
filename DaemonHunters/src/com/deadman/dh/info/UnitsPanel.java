package com.deadman.dh.info;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.R;
import com.deadman.dh.dialogs.MenuAction;
import com.deadman.dh.dialogs.MessageBox;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.model.Unit;
import com.deadman.dh.model.items.AmmunitionGrid;
import com.deadman.dh.model.items.ItemsGrid;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ProgressBar;
import com.deadman.jgame.ui.RelativeLayout;
import com.deadman.jgame.ui.VListView;

public class UnitsPanel extends Control
{
	private Guild guild;
	
	private int _unitIndex = 0;
	private Unit _currUnit;

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

	public UnitsPanel()
	{
		addControl(new Control(R.ui.unit_stat_bgr, 4, 0));

		Button btUnitWork = new Button(R.ui.bt_unit_work);
		btUnitWork.setPosition(203, 16);
		btUnitWork.addControlListener(btUnitWork_listener);
		btUnitWork.clickOnBgr = true;
		addControl(btUnitWork);

		addControl(laWork = new Label(GuildInfoEngine.fnt4x7_work, 203, 21));
		laWork.width = 115;
		laWork.height = 9;
		laWork.halign = Label.ALIGN_CENTER;
		laWork.autosize = false;

		Button btRecruit = new Button(R.ui.bt_gem_add);
		btRecruit.setPosition(9, 3);
		btRecruit.addControlListener(btRecruit_listener);
		addControl(btRecruit);

		Button btDismiss = new Button(R.ui.bt_gem_remove);
		btDismiss.addControlListener(btDismiss_listener);
		btDismiss.setPosition(9, 28);
		addControl(btDismiss);

		addControl(btPrevUnit = new Button(R.ui.bt_paper_prev, R.ui.bt_paper_prev_pr));
		btPrevUnit.setPosition(35, 19);
		btPrevUnit.addControlListener(units_navigate_listener);

		addControl(pbUnitPortrait = new Control(53, 9, Unit.PORTRAIT_WIDTH, Unit.PORTRAIT_HEIGHT));

		picMale = getDrawable(R.ui.ic_unit_male);
		picFemale = getDrawable(R.ui.ic_unit_female);
		addControl(icUnitGender = new Control(88, 12, 13, 13));
		icUnitGender.bgrMode = Control.BGR_ONE;

		addControl(laUnitName = new Label(GlobalEngine.fnt4x7_brown_sh, 101, 15));

		addControl(new Control(R.ui.lvl_txt, 90, 30));
		addControl(laLvl = new Label(GlobalEngine.fnt4x7_brown_sh, 119, 32));

		addControl(btNextUnit = new Button(R.ui.bt_paper_next, R.ui.bt_paper_next_pr));
		btNextUnit.setPosition(191, 19);
		btNextUnit.addControlListener(units_navigate_listener);

		addControl(laUnitsCount = new Label(GlobalEngine.fnt4x7_brown_sh, 160, 1));
		laUnitsCount.autosize = false;
		laUnitsCount.height = 9;
		laUnitsCount.width = 63;

		addControl(new Control(R.ui.unit_info_bgr, 10, 55));

		addControl(pbExp = new ProgressBar(R.ui.pb_exp, 39, 56));
		addControl(laExp = new Label(GuildInfoEngine.fnt4x6, 143, 55));

		addControl(pbHP = new ProgressBar(R.ui.pb_hp, 69, 72));
		addControl(laHP = new Label(GuildInfoEngine.fnt3x5, 141, 72));

		addControl(pbMP = new ProgressBar(R.ui.pb_mp, 69, 84));
		addControl(laMP = new Label(GuildInfoEngine.fnt3x5, 141, 84));

		addControl(laST = new Label(GuildInfoEngine.fnt3x5, 25, 71));
		addControl(laDX = new Label(GuildInfoEngine.fnt3x5, 25, 82));
		addControl(laIN = new Label(GuildInfoEngine.fnt3x5, 25, 93));

		lvSkills = new VListView();
		lvSkills.setSpacing(2);
		lvSkills.setWidth(100);
		RelativeLayout.settings(lvSkills).alignLeft(10).alignTop(107).alignBottom(73);
		addControl(lvSkills);

		pbBlue = getDrawable(R.ui.blue_pb);
		lvSkills.addItem(lviSword = new ListItemSkill(R.ui.ic_sword));
		lvSkills.addItem(lviShield = new ListItemSkill(R.ui.ic_shield));
		lvSkills.addItem(lviThrow = new ListItemSkill(R.ui.ic_bow));
		lvSkills.addItem(lviCraft = new ListItemSkill(R.ui.ic_anvil));
		lvSkills.addItem(lviScience = new ListItemSkill(R.ui.ic_book));
		lvSkills.addItem(lviMagic = new ListItemSkill(R.ui.ic_magic, pbBlue));

		addControl(agAmmunition = new AmmunitionGrid(220, 50));

		addControl(new Control(R.ui.backpack_bgr, 218, 148));
		addControl(igBackpack = new ItemsGrid(220, 150));
	}

	private ControlListener btUnitWork_listener = new ControlListener()
	{
		@Override
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			new BuildingSelect(_currUnit).showModal();
		};
	};

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

	private ControlListener btRecruit_listener = new ControlListener()
	{
		@Override
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			new RecruitsTable(UnitsPanel.this, guild).showModal();
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

	public void updateUnitsCount()
	{
		laUnitsCount.setText(guild.units.size() + "/" + guild.maxUnitCapcity());
	}

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
		lvSkills.update(false);

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

	public void setGuild(Guild g)
	{
		guild = g;
		updateUnitsCount();
		showUnit(null);
	}

}
