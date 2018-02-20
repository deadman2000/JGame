package com.deadman.jgame.systemui;

import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.Property;
import com.deadman.jgame.ui.Row;

public class StatusBar extends Row
{
	private Control left;
	private Label laStatus;
	
	public StatusBar()
	{
		addControl(left = new Control());
		
		addControl(laStatus = new Label());
		laStatus.y = 1;
	}

	@Property("leftBackground")
	public void setLeftBackground(Drawable bgr)
	{
		left.background = bgr;
		left.width = bgr.width;
		left.height = bgr.height;
	}
	
	@Property("font")
	public void setFont(GameFont font)
	{
		laStatus.setFont(font); // TODO Проверить, изменится ли размер Label
		setHeight(font.height + 2);
	}
	
	public void setText(String text)
	{
		laStatus.setText(text);
	}
	
	public static class Theme
	{
		public Theme(int bgrColor, int leftImageRes, GameFont font)
		{
			backgroundColor = bgrColor;
			labelFont = font;
			leftImage = Drawable.get(leftImageRes);
		}

		public int backgroundColor;
		
		public GameFont labelFont;
		
		public Drawable leftImage;
	}
}
