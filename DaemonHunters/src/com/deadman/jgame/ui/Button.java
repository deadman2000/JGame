package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.NinePart;
import com.deadman.jgame.resources.PicPartEntry;
import com.deadman.jgame.resources.ResourceEntry;

public class Button extends Control
{
	private Drawable _picUp, _picDown;

	public Drawable image;
	public int image_pad_x;
	public int image_pad_y;

	public Button()
	{
	}
	
	public Button(int bgrId)
	{
		ResourceEntry bgr = getResource(bgrId);

		if (bgr.getType() == ResourceEntry.PICPARTS)
		{
			Drawable[] parts = ((PicPartEntry)bgr).getArray();
			_picUp = parts[0];
			_picDown = parts[1];
		}
		else
		{
			_picUp = _picDown = bgr.getDrawable();
		}

		init();
	}

	public Button(int bgrUpId, int bgrDownId)
	{
		this(getDrawable(bgrUpId), getDrawable(bgrDownId));
	}

	public Button(Drawable picUp, Drawable picDown)
	{
		_picUp = picUp;
		_picDown = picDown;
		
		init();
	}
	
	void init()
	{
		background = _picUp;
		width = background.width;
		height = background.height;
		clickOnBgr = true;
		if (_picUp instanceof NinePart)
			bgrMode = BGR_FILL;
		else
			bgrMode = BGR_ONE;
	}

	@Override
	public void draw()
	{
		super.draw();
		if (image != null)
		{
			if (isPressed || checked)
				image.drawAt(scrX + image_pad_x + 1, scrY + image_pad_y + 1);
			else
				image.drawAt(scrX + image_pad_x, scrY + image_pad_y);
		}
	}

	@Override
	protected void onPressed(Point p, MouseEvent e)
	{
		super.onPressed(p, e);
		if (e.getButton() == 1)
		{
			background = _picDown;
			if (label != null)
				label.setPosition(1, labelY + 1);
			if (check_on_click)
				setChecked(true);
			e.consume();
		}
	}

	@Override
	protected void onReleased(Point p, MouseEvent e)
	{
		super.onReleased(p, e);

		if (check_on_click)
			return;

		if (e.getButton() == 1)
		{
			background = _picUp;
			if (label != null)
				label.setPosition(0, labelY);
		}
	}

	@Override
	protected void onClick(Point p, MouseEvent e)
	{
		super.onClick(p, e);
		e.consume();
	}

	public boolean check_on_click = false;

	public boolean checked = false;

	public void setChecked(boolean value)
	{
		if (checked == value) return;

		checked = value;
		if (checked)
		{
			background = _picDown;
			onAction(ACTION_CHECKED);
			if (label != null)
				label.setPosition(1, labelY + 1);
		}
		else
		{
			background = _picUp;
			onAction(ACTION_UNCHECKED);
			if (label != null)
				label.setPosition(0, labelY);
		}
	}

	private Label label;
	private int labelY;

	public void setLabel(GameFont font, int laY, String text)
	{
		label = new Label(font);
		label.setText(text);
		label.autosize = false;
		label.setBounds(0, laY, width, font.height, ANCHOR_LEFT | ANCHOR_RIGHT);
		label.halign = Label.ALIGN_CENTER;
		addControl(label);
		labelY = laY;
	}

	public void setText(String text)
	{
		label.setText(text);
	}

	public void calcWidthByText(int pad)
	{
		width = label.getTextWidth() + pad * 2;
	}

	public void setFont(GameFont font)
	{
		label.setFont(font);
	}

}
