package com.deadman.dh.info;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.dh.model.items.ItemsGrid;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.CheckGroup;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.RelativeLayout;

public class GuildInfoEngine extends GameEngine
{
	public static final GameFont fnt4x6 = getFont(R.fonts.font4x6, 0xFF3a2717).shadow(0xFF9f9a8a);
	public static final GameFont fnt3x5 = getFont(R.fonts.font3x5, 0xFF3a2717).shadow(0xFF9f9a8a);

	public static final GameFont fnt4x7_l = getFont(R.fonts.font4x7, 0xFF3a2717).shadow(0xFF808464);
	public static final GameFont fnt4x7_work = getFont(R.fonts.font4x7, 0xFF3a2717).shadow(0xFFada793);

	private Guild guild;
	private int guildInd;

	private Label laGuild;
	private Button btPrevG, btNextG;
	private Button chbUnits, chbSquads;
	private UnitsPanel plUnits;
	private SquadsPanel plSquads;

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
		RelativeLayout.settings(ctlPaper).fill();

		Control ctlBorder = new Control(R.ui.pl_info_9p);
		addControl(ctlBorder);
		RelativeLayout.settings(ctlBorder).fill();

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

		addControl(new Control(getDrawable(R.ui.bt_border_info_9p), 260, 5, 41, 21));
		addControl(chbSquads = new Button(R.ui.bt_info_squad, R.ui.bt_info_squad_pr));
		chbSquads.setPosition(262, 7);
		chbSquads.addControlListener(chb_tabs_listener);
		new CheckGroup(chbUnits, chbSquads);

		// Рамка для кнопки
		Control ctlBorderInfo = new Control(getDrawable(R.ui.bt_border_info_9p));
		RelativeLayout.settings(ctlBorderInfo).alignRight(5).alignTop(5);
		ctlBorderInfo.setSize(26, 21);
		addControl(ctlBorderInfo);

		Button btClose = new Button(R.ui.bt_info_close, R.ui.bt_info_close_pr);
		RelativeLayout.settings(btClose).alignRight(7).alignTop(7);
		addControl(btClose);
		btClose.addControlListener(btClose_listener);

		plUnits = new UnitsPanel();
		RelativeLayout.settings(plUnits).fill(0, 32, 0, 32);
		addControl(plUnits);

		plSquads = new SquadsPanel();
		RelativeLayout.settings(plSquads).fill(0, 32, 0, 32);
		addControl(plSquads);

		// Слоты с предметами гильдии
		addControl(igStorage = new ItemsGrid());
		igStorage.setBgrs(R.slots.item_slot);
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
					plSquads.visible = false;
					plUnits.visible = true;
					plUnits.updateUnit();
				}
				else if (sender == chbSquads)
				{
					plUnits.visible = false;
					plSquads.visible = true;
					plSquads.updateSquad();
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
			plUnits.updateUnit();
		else if (chbSquads.checked)
			plSquads.updateSquad();
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

		plUnits.setGuild(g);
		plSquads.setGuild(g);
	}
}
