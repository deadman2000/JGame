package com.deadman.dh.guild;

import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.VListView;

public class BuildingsPanel extends Control
{
	private GuildEngine engine;

	private Drawable img_scroll_bgr, img_scroll_bottom;
	private VListView lvHouses;

	private static final int top_pad = 12;

	public BuildingsPanel(GuildEngine eng)
	{
		engine = eng;

		img_scroll_bgr = getDrawable(R.ui.scroll_bgr);
		img_scroll_bottom = getDrawable(R.ui.scroll_bottom_2);

		lvHouses = new VListView(3, top_pad, 80, 0);
		lvHouses.item_height = BuildingItem.HEIGHT;
		addControl(lvHouses);

		width = img_scroll_bgr.width;
		setPosition(GameScreen.GAME_WIDTH - width - 2, 22, Control.ANCHOR_RIGHT_TOP);

		update();
	}

	public void update()
	{
		if (engine.guild == null)
		{
			lvHouses.visible = false;
			lvHouses.height = 0;
		}
		else
		{
			lvHouses.visible = true;
			lvHouses.clear();
			for (GuildBuilding gb : engine.guild.buildings)
			{
				if (gb.type.price == 0) continue;
				lvHouses.addItem(new BuildingItem(engine, gb));
			}
			lvHouses.height = lvHouses.items.size() * BuildingItem.HEIGHT;
		}

		height = top_pad + lvHouses.height + 2 + img_scroll_bottom.height - 2;
	}

	@Override
	public void draw()
	{
		img_scroll_bgr.drawAt(scrX, scrY, img_scroll_bgr.width, height - img_scroll_bottom.height + 2);
		img_scroll_bottom.drawAt(scrX, scrY + height - img_scroll_bottom.height);
		super.draw();
	}
}
