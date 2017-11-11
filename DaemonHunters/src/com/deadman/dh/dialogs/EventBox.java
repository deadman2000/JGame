package com.deadman.dh.dialogs;

import java.awt.event.MouseEvent;

import com.deadman.dh.GameEvent;
import com.deadman.dh.R;
import com.deadman.dh.battle.MissionEngine;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.Game;
import com.deadman.dh.model.Mission;
import com.deadman.dh.model.Squad;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;

public class EventBox extends Control
{
	static EventBox mb;

	public static void showEvents()
	{
		if (mb == null)
			mb = new EventBox();
		mb.showEvent(GameEvent.events.get(0));
		GameEvent.events.remove(0);
		mb.showModal();
	}

	private Control eventIcon;
	private Label laText;

	private Control ctlDefaultButtons;
	private Button btToLocation, btClose, btClosePause;

	private Control ctlMissionButtons;
	private Button btBeginMission, btToHome, btWait;
	private GameEvent currEvent;

	private EventBox()
	{
		super(R.ui.mbox_frame);
		
		GameFont fnt = GlobalEngine.fnt4x7_brown;

		eventIcon = new Control(42, 76, 36, 36);
		eventIcon.bgrMode = BGR_ONE;
		addControl(eventIcon);

		laText = new Label(fnt);
		laText.setBounds(83, 82, 134, 76);
		laText.word_wrap = true;
		addControl(laText);

		ctlDefaultButtons = new Control(0, 158, width, 12);
		addControl(ctlDefaultButtons);

		btToLocation = new Button(R.ui.bt_mbox_9p, R.ui.bt_mbox_pr_9p);
		btToLocation.setBounds(40, 0, 56, 12);
		btToLocation.setLabel(fnt, 2, "ПЕРЕЙТИ >>");
		btToLocation.addControlListener(bt_listener);
		ctlDefaultButtons.addControl(btToLocation);

		btClosePause = new Button(R.ui.bt_mbox_9p, R.ui.bt_mbox_pr_9p);
		btClosePause.setBounds(115, 0, 48, 12);
		btClosePause.setLabel(fnt, 2, "ПАУЗА");
		ctlDefaultButtons.addControl(btClosePause);
		btClosePause.addControlListener(bt_listener);

		btClose = new Button(R.ui.bt_mbox_9p, R.ui.bt_mbox_pr_9p);
		btClose.setBounds(165, 0, 48, 12);
		btClose.setLabel(fnt, 2, "ЗАКРЫТЬ");
		ctlDefaultButtons.addControl(btClose);
		btClose.addControlListener(bt_listener);

		ctlMissionButtons = new Control(0, 158, width, 12);
		addControl(ctlMissionButtons);

		btBeginMission = new Button(R.ui.bt_mbox_9p, R.ui.bt_mbox_pr_9p);
		btBeginMission.setPosition(40, 0);
		btBeginMission.height = 12;
		btBeginMission.setLabel(fnt, 2, "НАЧАТЬ ЗАДАНИЕ");
		btBeginMission.calcWidthByText(6);
		btBeginMission.addControlListener(bt_listener);
		ctlMissionButtons.addControl(btBeginMission);

		btToHome = new Button(R.ui.bt_mbox_9p, R.ui.bt_mbox_pr_9p);
		btToHome.setPosition(btBeginMission.right() + 2, 0);
		btToHome.height = 12;
		btToHome.setLabel(fnt, 2, "ВЕРНУТЬСЯ");
		btToHome.calcWidthByText(6);
		ctlMissionButtons.addControl(btToHome);
		btToHome.addControlListener(bt_listener);

		btWait = new Button(R.ui.bt_mbox_9p, R.ui.bt_mbox_pr_9p);
		btWait.setPosition(btToHome.right() + 2, 0);
		btWait.height = 12;
		btWait.setLabel(fnt, 2, "ЖДАТЬ");
		btWait.calcWidthByText(6);
		ctlMissionButtons.addControl(btWait);
		btWait.addControlListener(bt_listener);
	}

	ControlListener bt_listener = new ControlListener()
	{
		@Override
		public void onControlPressed(Control sender, MouseEvent e)
		{
			if (sender == btClose)
			{
				close();
			}
			else if (sender == btClosePause)
			{
				GameLoop.paused = true;
				close();
			}
			else if (sender == btToLocation)
			{
				GameLoop.paused = true;
				close();
				Game.global.setCenter(currEvent.location);
			}
			else if (sender == btBeginMission)
			{
				close();
				MissionEngine.beginMission((Mission) currEvent.location, (Squad) currEvent.target);
			}
			else if (sender == btWait)
			{
				close();
			}
			else if (sender == btToHome)
			{
				((Squad) currEvent.target).moveToHome();
				close();
			}
		};
	};

	private void showEvent(GameEvent event)
	{
		currEvent = event;
		eventIcon.background = event.icon;
		laText.setText(event.message);

		if (event.type == GameEvent.TYPE_SQUAD_MISSION_BEGIN)
		{
			ctlDefaultButtons.visible = false;
			ctlMissionButtons.visible = true;
		}
		else
		{
			ctlDefaultButtons.visible = true;
			ctlMissionButtons.visible = false;

			if (currEvent.location != null)
				btToLocation.visible = true;
			else
				btToLocation.visible = false;
		}
	}
}
