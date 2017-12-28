package com.deadman.dh.global;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.R;
import com.deadman.dh.dialogs.MenuAction;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;

public class MsgScroll extends Control
{
	private Drawable img_left;
	private Drawable img_right;
	private Drawable img_bgr;
	private Label laText;

	public boolean hideOnClick = true;

	static final GameFont fnt = getFont(R.fonts.font4x7, 0xFF50493a);
	private Button button1;
	private MenuAction button1_action;

	static final int PADDING_H = 15;

	public MsgScroll()
	{
		img_left = getDrawable(R.ui.msg_left);
		img_right = getDrawable(R.ui.msg_right);
		img_bgr = getDrawable(R.ui.msg_bgr);

		laText = new Label(GlobalEngine.fnt4x7_brown);
		laText.setPosition(PADDING_H, 11);
		addControl(laText);

		addControl(button1 = new Button(R.ui.bt_mbox_9p, R.ui.bt_mbox_pr_9p));
		button1.setBounds(0, 0, 10, 12);
		button1.setLabel(fnt, 2, "ОТМЕНА");
		button1.calcWidthByText(3);
		button1.visible = false;
		button1.addControlListener(button1_listener);

		height = img_left.height;
	}

	private ControlListener button1_listener = new ControlListener()
	{
		@Override
		public void onControlPressed(Control control, MouseEvent e)
		{
			if (button1_action != null)
				button1_action.onAction(this, 1);
		};
	};

	@Override
	public void draw()
	{
		img_bgr.drawAt(scrX + 9, scrY, width - 9 - 9, height);
		img_left.drawAt(scrX, scrY);
		img_right.drawAt(scrX + width - img_right.width, scrY);

		super.draw();
	}

	public void setText(String text)
	{
		laText.setText(text);
		setSize(laText.width + PADDING_H * 2, height);
	}

	public void setText(String text, String buttonText, MenuAction buttonAction)
	{
		button1.setText(buttonText);
		button1.calcWidthByText(3);
		button1_action = buttonAction;
		button1.visible = true;

		laText.setText(text);
		setSize(laText.width + 4 + button1.width + PADDING_H * 2, height);
		button1.setPosition(width - button1.width - 10, 9);
	}

	@Override
	protected void onPressed(Point p, MouseEvent e)
	{
		if (hideOnClick)
			visible = false;
	}
}
