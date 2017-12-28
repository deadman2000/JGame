package com.deadman.dh.dialogs;

import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ListViewItem;

public class MenuRow extends ListViewItem
{
	Label laName;
	public static final int HEIGHT = 13;

	private final int SEL_COLOR = 0xff8da1b3;
	private final int ODD_COLOR = 0xff748ba2;

	private int index;

	public MenuRow(int ind, String text, Object t)
	{
		this(text, t);
		index = ind;

		if (index % 2 == 0)
			bgrColor = ODD_COLOR;
	}
	
	
	public MenuRow(String text, Object t)
	{
		addControl(laName = new Label(IGMPanel.fnt_igm, 2, 2, text));
		tag = t;
		height = 13;
	}

	public void setIndex(int i)
	{
		index = i;
		if (bgrColor != SEL_COLOR)
		{
			if (index % 2 == 0)
				bgrColor = ODD_COLOR;
			else
				bgrColor = 0;
		}
	}

	@Override
	public void onSelected()
	{
		bgrColor = SEL_COLOR;
		laName.setFont(IGMPanel.fnt_igm_selected);
	}

	@Override
	public void onDeselected()
	{
		if (index % 2 == 0)
			bgrColor = ODD_COLOR;
		else
			bgrColor = 0;
		laName.setFont(IGMPanel.fnt_igm);
	}
}
