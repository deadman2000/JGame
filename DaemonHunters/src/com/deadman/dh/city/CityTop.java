package com.deadman.dh.city;

import com.deadman.dh.R;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Label;

public class CityTop extends Control
{
	public CityTop(City c)
	{
		super(R.ui.city_top);
		clickOnBgr = true;

		Label laName = new Label(getFont(R.fonts.font4x7, 0xff3a2717).shadow(0xff82825b), 4, 2);
		laName.setBounds(0, 2, width-10, height-4);
		laName.halign = Label.ALIGN_CENTER;
		laName.autosize = false;
		addControl(laName);
		
		laName.setText(c.name);
	}
}
