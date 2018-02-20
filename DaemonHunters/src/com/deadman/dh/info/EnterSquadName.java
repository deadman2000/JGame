package com.deadman.dh.info;

import java.awt.event.KeyEvent;

import com.deadman.dh.dialogs.IGMPanel;
import com.deadman.dh.dialogs.MessageBox;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.model.Squad;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.TextBox;

public class EnterSquadName extends IGMPanel
{
	final TextBox tbName;
	final Guild guild;
	final SquadsPanel panel;
	final Button btAccept;

	Squad renameSquad;

	public EnterSquadName(SquadsPanel p, Guild guild)
	{
		super(200, 53);

		panel = p;
		this.guild = guild;

		addControl(new Label(IGMPanel.fnt_igm, 10, 13, "Название:"));
		addControl(tbName = new TextBox(IGMPanel.fnt_igm, 64, 10, 122));
		tbName.contentColor = 0xFF8DA1B3;
		tbName.isFocused = true;

		btAccept = addButton(0, "Создать", 87, 28, 50);
		addButton(TAG_CLOSE, "Отмена", 140, 28, 50);
	}

	public EnterSquadName(SquadsPanel p, Squad squad)
	{
		this(p, squad.guild);

		renameSquad = squad;
		tbName.setText(squad.name);
		btAccept.setText("OK");
	}

	@Override
	protected void onButtonPressed(int tag)
	{
		if (tag == 0)
			accept();
	}

	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			accept();
	}

	void accept()
	{
		if (getName().length() == 0)
		{
			MessageBox.show("Введите название отряда");
			return;
		}

		if (renameSquad == null)
			createSquad();
		else
			renameSquad();
		close();
	}

	private String getName()
	{
		return tbName.text.trim();
	}

	private void createSquad()
	{
		Squad squad = guild.createSquad(getName());
		panel.showSquad(squad);
	}

	private void renameSquad()
	{
		renameSquad.name = getName();
	}

}
