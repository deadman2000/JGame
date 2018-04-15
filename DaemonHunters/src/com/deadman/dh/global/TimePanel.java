package com.deadman.dh.global;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.RelativeLayout;

public class TimePanel extends Control
{
	int displayedSeconds = -1;
	Label laDate, laTime;
	Label laSeconds;
	Drawable picBubbles;
	
	public TimePanel()
	{
		RelativeLayout.settings(this).alignRight(5).alignBottom(5);
		clickOnBgr = true;
		
		setLayout(new RelativeLayout());
		setSize(102, 82);
		
		Control bgr = new Control(getDrawable(R.ui.time_bgr).shadow(0x30000000, -2, 2));
		RelativeLayout.settings(bgr).alignRight().alignBottom(-2);
		addControl(bgr);

		GameFont fnt = getFont(R.fonts.font4x7, 0xff312013).shadow(0xff646443);
		addControl(laDate = new Label(fnt, 9, 67));
		laDate.char_interval = 0;

		addControl(laTime = new Label(fnt, 55, 67));
		laTime.char_interval = 0;
		
		addControl(laSeconds = new Label(getFont(R.fonts.font3x5, 0xff312013).shadow(0xff646443), 77, 69));
		laSeconds.char_interval = 0;

		picBubbles = getDrawable(R.ui.bubbles);
	}

	public void update()
	{
		if (GlobalEngine.time != displayedSeconds)
		{
			displayedSeconds = GlobalEngine.time;
			int seconds =  displayedSeconds % 60;
			int minutes = (displayedSeconds / 60) % 60;
			int hours = (displayedSeconds / GlobalEngine.SECONDS_IN_HOUR) % 24;
			int days = (displayedSeconds / GlobalEngine.SECONDS_IN_DAY) % 30 + 1;
			int month = (displayedSeconds / GlobalEngine.SECONDS_IN_MONTH) % 12 + 1;
			int year = 1400 + displayedSeconds / GlobalEngine.SECONDS_IN_YEAR;

			laDate.setText("%04d.%02d.%02d", year, month, days);
			laTime.setText("%02d:%02d", hours, minutes);
			laSeconds.setText(":%02d", seconds);
		}
	}

	@Override
	public void draw()
	{
		super.draw();
		int tshift = (int) ((Game.global.ticks * GlobalEngine.timeSpeedLvl) / 4) % picBubbles.height;

		picBubbles.drawAt(scrX + 89, scrY + 3, 0, tshift, picBubbles.width, picBubbles.height);
	}
}
