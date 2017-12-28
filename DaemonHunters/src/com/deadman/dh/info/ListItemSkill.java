package com.deadman.dh.info;

import com.deadman.dh.R;
import com.deadman.dh.model.Skill;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ListViewItem;
import com.deadman.jgame.ui.ProgressBar;

public class ListItemSkill extends ListViewItem
{
	private Drawable _icon, pbBgr;
	private ProgressBar pb;
	private Label la;

	public ListItemSkill(int icon, ProgressBar pb)
	{
		_icon = getDrawable(icon);
		pbBgr = getDrawable(R.ui.pb_skill_bgr);
		height = 9;

		addControl(this.pb = pb);
		addControl(la = new Label(GuildInfoEngine.fnt3x5, 86, 2));
	}

	public ListItemSkill(int icon)
	{
		this(icon, new ProgressBar(R.ui.pb_skill, 13, 3));
	}

	public ListItemSkill(int icon, Drawable bgr)
	{
		this(icon, new ProgressBar(bgr, 13, 3));
	}

	public void setValue(Skill skill)
	{
		pb.setValue(skill.value % 100);
		la.setText(skill.value / 100);
		visible = skill.value > 0;
	}

	@Override
	public void draw()
	{
		_icon.drawAt(scrX, scrY);
		pbBgr.drawAt(scrX + 11, scrY + 2);

		super.draw();
	}
}
