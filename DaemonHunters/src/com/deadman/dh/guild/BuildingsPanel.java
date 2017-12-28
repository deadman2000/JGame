package com.deadman.dh.guild;

import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.VListView;

public class BuildingsPanel extends Control
{
	private GuildEngine engine;

	private Drawable img_scroll_bgr, img_scroll_bottom;
	private HousesList lvHouses;

	private static final int top_pad = 12;

	class HousesList extends VListView
	{
		public HousesList()
		{
			heightByContent = true;
		}
		
		@Override
		protected void onResize()
		{
			super.onResize();
			BuildingsPanel.this.height = top_pad + height + img_scroll_bottom.height;
		}
	}

	public BuildingsPanel(GuildEngine eng)
	{
		engine = eng;

		img_scroll_bgr = getDrawable(R.ui.scroll_bgr);
		img_scroll_bottom = getDrawable(R.ui.scroll_bottom_2);

		lvHouses = new HousesList();
		lvHouses.setBounds(3, top_pad, 80, 0);
		addControl(lvHouses);

		width = img_scroll_bgr.width;

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
		}
	}

	@Override
	public void draw()
	{
		img_scroll_bgr.drawAt(scrX, scrY, img_scroll_bgr.width, height - img_scroll_bottom.height + 2);
		img_scroll_bottom.drawAt(scrX, scrY + height - img_scroll_bottom.height);
		super.draw();
	}
}
