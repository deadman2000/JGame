package com.deadman.dh.dialogs;

import java.awt.event.MouseEvent;

import com.deadman.dh.R;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;

public class IGMPanel extends Control
{
	private Label laTitle;
	private static final int la_pad = 7;

	public static final GameFont fnt_igm = getFont(R.fonts.font4x7, 0xffffffff).shadow(0xff4f6477);
	public static final GameFont fnt_igm_selected = getFont(R.fonts.font4x7, 0xfff3e69f).shadow(0xff4f6477);

	public IGMPanel(int w, int h)
	{
		background = getDrawable(R.ui.igm_panel_9p);

		width = w;
		height = h;
	}

	public void setTitle(String text)
	{
		if (laTitle == null)
		{
			laTitle = new Label(fnt_igm, la_pad, 10);
			laTitle.width = width - la_pad * 2 - 3;
			laTitle.height = fnt_igm.height;
			laTitle.anchor = ANCHOR_LEFT_TOP | ANCHOR_RIGHT;
			laTitle.autosize = false;
			laTitle.word_wrap = true;
			laTitle.halign = Label.ALIGN_CENTER;
			addControl(laTitle);
		}

		laTitle.setText(text);
	}

	protected Button addButton(int tag, String title, int x, int y, int w)
	{
		Button btn = new Button(R.ui.bt_igm_9p, R.ui.bt_igm_pr_9p);
		addControl(btn);
		btn.setBounds(x, y, w, 15);
		btn.setLabel(fnt_igm, 3, title);
		btn.tag = tag;
		btn.addControlListener(btn_listener);
		return btn;
	}

	private ControlListener btn_listener = new ControlListener()
	{
		@Override
		public void onControlPressed(Control control, MouseEvent e)
		{
			int id = (int) ((Button) control).tag;
			switch (id)
			{
				case TAG_CLOSE:
					close();
					break;
				default:
					onButtonPressed(id);
					break;
			}

			onMouseLeave(control);
		};

		public void onMouseEnter(Object sender)
		{
			Button bt = (Button) sender;
			bt.setFont(fnt_igm_selected);
		};

		public void onMouseLeave(Object sender)
		{
			Button bt = (Button) sender;
			bt.setFont(fnt_igm);
		};
	};

	protected void onButtonPressed(int tag)
	{
	}

	public static final int TAG_CLOSE = 0xffffffff;
}
