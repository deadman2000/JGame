package com.deadman.dh.isometric.editor;

import com.deadman.dh.R;
import com.deadman.dh.isometric.IsoSimpleObject;
import com.deadman.dh.model.GameCharacter;
import com.deadman.jgame.ui.ColumnLayout;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.Row;
import com.deadman.jgame.ui.RowLayout;

public class PropertyRow extends Row
{
	private Label laCaption, laValue;
	private Control ctlValue;
	public Object value;

	public PropertyRow(String label)
	{
		height = 7;
		ColumnLayout.settings(this).fillWidth();

		addControl(laCaption = new Label(getFont(R.fonts.font3x5, 0xffd7ab34), label));
		laCaption.autosize = false;
		laCaption.setSize(30, 5);
		laCaption.y = 1;

		ctlValue = new Control();
		ctlValue.bgrColor = 0xff000000;
		RowLayout.settings(ctlValue).fillWidth().fillHeight();
		addControl(ctlValue);

		laValue = new Label(getFont(R.fonts.font3x5, 0xffc0c0c0));
		laValue.setPosition(2, 1);
		ctlValue.addControl(laValue);
	}

	public void setValue(IsoSimpleObject obj)
	{
		value = obj;
		if (obj == null)
			laValue.setText("-");
		else
			laValue.setText(obj.sprite.id + ": " + obj.sprite.name);
	}
	
	public void setValue(GameCharacter ch)
	{
		value = ch;
		if (ch == null)
			laValue.setText("-");
		else
			laValue.setText(ch.toString());
	}

	public void select()
	{
		laValue.setFont(getFont(R.fonts.font3x5, 0xffffffff));
	}

	public void unselect()
	{
		laValue.setFont(getFont(R.fonts.font3x5, 0xffc0c0c0));
	}
}
