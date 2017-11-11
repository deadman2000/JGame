package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;

public class NumericUpDown extends Control
{
	public double value;
	public double increment = 1.0;
	public double min = 0;
	public double max = 100;
	private Button btUp, btDown;
	private TextBox tb;

	private int decimals = 2;

	public NumericUpDown(int id, GameFont font, int x, int y, int width)
	{
		Drawable[] buttons = getParts(id);
		
		btUp = new Button(buttons[0], buttons[1]);
		btUp.setPosition(width - btUp.width, 0);
		btUp.addControlListener(upEvents);

		btDown = new Button(buttons[2], buttons[3]);
		btDown.setPosition(width - btUp.width, btUp.height);
		btDown.addControlListener(downEvents);

		tb = new TextBox(font, 0, 0, width - btUp.width);
		tb.textAlignLeft = false;
		tb.filter = TextBox.FILTER_FLOAT;

		/*this.width = width;
		this.height = tb.height;
		setPosition(x, y);*/
		setBounds(x, y, width, tb.height);

		addControl(btUp);
		addControl(btDown);
		addControl(tb);

		setValue(0);
	}

	private long pressTime;
	private int pressCount;
	private int incrDelay;

	private ControlListener upEvents = new ControlListener()
	{
		@Override
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			increment();
			pressTime = GameLoop.frames;
			pressCount = 0;
			incrDelay = 10;
		}
	};

	private ControlListener downEvents = new ControlListener()
	{
		@Override
		public void onPressed(Object sender, Point p, MouseEvent e)
		{
			decrement();
			pressTime = GameLoop.frames;
			pressCount = 0;
			incrDelay = 10;
		}
	};

	@Override
	public void draw()
	{
		super.draw();

		if (btUp.isPressed || btDown.isPressed)
		{
			if ((GameLoop.frames - pressTime) % incrDelay == incrDelay - 1)
			{
				if (btUp.isPressed)
					increment();
				else
					decrement();
				pressCount++;
				if (pressCount > 20)
					incrDelay = 2;
				else if (pressCount > 3)
					incrDelay = 5;
			}
		}
	}

	public void increment()
	{
		setValue(value + increment);
	}

	public void decrement()
	{
		setValue(value - increment);
	}

	public void setValue(double v)
	{
		if (v < min) v = min;
		if (v > max) v = max;
		value = v;
		tb.setText(getValueString());
	}

	private String getValueString()
	{
		return String.format("%,." + decimals + "f", value).replace(',', '.');
	}

	public void setDecimals(int count)
	{
		decimals = count;
		if (count == 0)
			tb.filter = TextBox.FILTER_INT;
		else
			tb.filter = TextBox.FILTER_FLOAT;
	}

	@Override
	protected void onFocusLoss()
	{
		super.onFocusLoss();

		double v;
		try
		{
			v = Double.parseDouble(tb.text.replace(',', '.'));
			double p = Math.pow(10, decimals);
			v = (double) ((int) (v * p)) / p;
		}
		catch (Exception ex)
		{
			v = value;
		}

		setValue(v);
	}
}
