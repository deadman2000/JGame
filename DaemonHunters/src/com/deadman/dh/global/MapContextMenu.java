package com.deadman.dh.global;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.deadman.dh.R;
import com.deadman.dh.city.City;
import com.deadman.dh.dialogs.MenuAction;
import com.deadman.dh.model.MapObject;
import com.deadman.dh.model.Poi;
import com.deadman.dh.model.Squad;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;

public class MapContextMenu extends Control
{
	private Drawable picLens, picBgr, picRight;
	private Button btMoveSquad;
	private Label laName;
	private static GameFont fnt = getFont(R.fonts.font4x7, 0xff432f1b).shadow(0xffbfb185);

	public MapObject target;

	private GlobalMapView _map;

	public MapContextMenu(GlobalMapView map)
	{
		_map = map;

		width = 72;
		height = 30;

		picLens = getDrawable(R.ui.cc_lens);
		picBgr = getDrawable(R.ui.cc_bgr);
		picRight = getDrawable(R.ui.cc_right);
		addControl(laName = new Label(fnt, 24, 17));

		addControl(btMoveSquad = new Button(R.ui.cc_squad));
		btMoveSquad.setPosition(2, 0);
		btMoveSquad.addControlListener(cl_squad);

		visible = false;
	}

	ControlListener cl_squad = new ControlListener()
	{
		@Override
		public void onControlPressed(Control control, MouseEvent e)
		{
			if (target instanceof Poi)
			{
				Poi pt = (Poi) target;
				ArrayList<Squad> squads = pt.getAccessibleSquads();
				if (squads.size() > 1)
					new SelectSquad(squads, selectSquad_action).showModal();
				else if (squads.size() == 1)
					beginMoveSquad(squads.get(0));
			}
			else if (target instanceof Squad)
				beginMoveSquad((Squad) target);
		};
	};

	private MenuAction selectSquad_action = new MenuAction()
	{
		@Override
		public void onAction(Object sender, int action)
		{
			SelectSquad ss = (SelectSquad) sender;
			if (ss.squad != null)
				beginMoveSquad(ss.squad);
		}
	};

	private void beginMoveSquad(Squad s)
	{
		_map.sendSquad(s);
		hide();
	}

	@Override
	public void draw()
	{
		updatePos();
		picLens.drawAt(scrX, scrY + 13);
		picBgr.drawAt(scrX + picLens.width, scrY + 15, width - picLens.width - picRight.width, picBgr.height);
		picRight.drawAt(scrX + width - picRight.width, scrY + 15);
		super.draw();
	}

	public boolean attached;

	public void attach()
	{
		System.out.println("Context menu activated");
		attached = true;
		if (target instanceof City)
			btMoveSquad.visible = ((City) target).hasSquad();
		else if (target instanceof Squad)
			btMoveSquad.visible = true;
		else
			btMoveSquad.visible = false;
	}

	public void hide()
	{
		System.out.println("Context menu deactivated");
		target = null;
		attached = false;
		btMoveSquad.visible = false;
		visible = false;
	}

	private void updatePos()
	{
		setPosition(target.x + _map.viewX - 8, target.y + _map.viewY - 21);
	}

	public void show(MapObject obj)
	{
		target = obj;
		laName.setText(obj.getName());
		width = 24 + laName.width + 6;
		updatePos();
		btMoveSquad.visible = false;
		visible = true;
	}
}
