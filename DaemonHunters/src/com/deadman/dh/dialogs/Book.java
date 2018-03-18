package com.deadman.dh.dialogs;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.global.GlobalEngine;
import com.deadman.dh.guild.GuildBuildingType;
import com.deadman.dh.guild.GuildEngine;
import com.deadman.dh.isometric.IsoViewer;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ListViewItem;
import com.deadman.jgame.ui.TextArea;
import com.deadman.jgame.ui.VListView;

public class Book extends Control
{
	private Drawable picBook;

	private GameEngine _engine;
	private Control currentPage;
	private Control buildingsPage;
	private IsoViewer iso;
	private VListView lvBuildings;
	private TextArea taDescr;
	private Button btnBuild;

	static final int PAD_LEFT = 20, PAD_RIGHT = 20;
	static final int PAD_TOP = 25, PAD_BOTTOM = 25;

	private Book()
	{
		picBook = getDrawable(R.ui.book);
		width = PAD_LEFT + picBook.width + PAD_RIGHT;
		height = PAD_TOP + picBook.height + PAD_BOTTOM;

		addControl(new Control(picBook, PAD_LEFT, PAD_TOP));

		Control bmClose = new Control(R.ui.bm_close, PAD_LEFT + 287, PAD_TOP + 22);
		bmClose.clickOnBgr = true;
		addControl(bmClose);
		bmClose.addControlListener(new ControlListener()
		{
			@Override
			public void onControlPressed(Control control, MouseEvent e)
			{
				close();
			}
		});

		createBuildingsPage();
		
		submitChilds();
	}

	@Override
	public void draw()
	{
		//picBook.drawAt(scrX + PAD_LEFT, scrY + PAD_TOP);
		super.draw();
	}

	void showPage(Control page)
	{
		if (currentPage != null) currentPage.visible = false;
		page.visible = true;
		currentPage = page;
	}

	public void show()
	{
		_engine = GameEngine.current;
		showPage(buildingsPage);
		btnBuild.visible = _engine instanceof GuildEngine;
		showModal();
	}

	// Buildings

	private void createBuildingsPage()
	{
		buildingsPage = new Control();
		buildingsPage.visible = false;
		addControl(buildingsPage);
		buildingsPage.setBounds(PAD_LEFT + 14, PAD_TOP + 10, 261, 182);

		lvBuildings = new VListView();
		lvBuildings.name = "Buildings";
		lvBuildings.setBounds(0, 0, 128, 184);
		lvBuildings.setScrollBar(Game.getScrollThemePaper());

		buildingsPage.addControl(lvBuildings);

		for (GuildBuildingType t : GuildBuildingType.all)
		{
			if (t.price > 0)
				lvBuildings.addItem(new BookItemBulding(t));
		}

		lvBuildings.addControlListener(lvBuildings_listener);

		iso = new IsoViewer();
		iso.setBounds(141, 2, 120, 60);
		iso.allLevels = true;
		buildingsPage.addControl(iso);
		iso.bgrColor = 0xFF000000;

		buildingsPage.addControl(new Control(R.ui.book_map_rect, 140, 1));

		buildingsPage.addControl(taDescr = new TextArea(GlobalEngine.fnt4x7_brown));
		taDescr.setVScrollBar(Game.getScrollThemePaper());
		taDescr.setBounds(140, 66, 122, 100);

		buildingsPage.addControl(new Control(R.ui.book_descr_bottom, 141, 170));

		buildingsPage.addControl(btnBuild = new Button());
		btnBuild.setBounds(175, 170, 52, 11);
		btnBuild.setLabel(GlobalEngine.fnt4x7_brown, 3, "ПОСТРОИТЬ");
		btnBuild.visible = false;
		btnBuild.addControlListener(new ControlListener()
		{
			@Override
			public void onClick(Object sender, Point p, MouseEvent e)
			{
				build();
			}
		});

		lvBuildings.selectFirst();
	}

	GuildBuildingType _selectedBuildingType;

	private ControlListener lvBuildings_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_ITEM_SELECTED)
			{
				ListViewItem lvi = (ListViewItem) tag;
				showBuilding((GuildBuildingType) lvi.tag);
			}
			else if (action == Control.ACTION_ITEM_DBLCLICK)
			{
				build();
			}
		};
	};

	protected void showBuilding(GuildBuildingType type)
	{
		_selectedBuildingType = type;
		iso.setMap(type.getMapPreview());
		iso.centerView();
		taDescr.setText("At the mouth of the river, the little group of half a dozen intermingled families gathered salt from the great salt beds beside the sea. They groomed and sifted the salt and loaded it into handcarts. When the carts were full, most of the group would stay behind, taking shelter amid rocks and simple lean-tos, while a band of fifteen or so of the heartier members set out on the path that ran alongside the river.\r\n\r\nWith their precious cargo of salt, the travelers crossed the coastal lowlands and traveled toward the mountains. But Lara’s people never reached the mountaintops; they traveled only as far as the foothills. Many people lived in the forests and grassy meadows of the foothills, gathered in small villages. In return for salt, these people would give Lara’s people dried meat, animal skins, cloth spun from wool, clay pots, needles and scraping tools carved from bone, and little toys made of wood.");
	}

	protected void build()
	{
		if (_engine instanceof GuildEngine)
		{
			GuildEngine eng = (GuildEngine) _engine;
			GuildBuildingType building = (GuildBuildingType) lvBuildings.selectedItem().tag;
			eng.beginBuild(building);
			close();
		}
	}

	// END Buildings

	private static Book _book;

	public static Book inst()
	{
		if (_book == null)
			_book = new Book();
		return _book;
	}

	class BookItemBulding extends ListViewItem
	{
		Drawable picSmall;
		Drawable picSelected;

		private final int TEXT_MARGIN = 27;

		public BookItemBulding(GuildBuildingType type)
		{
			picSmall = type.getPicture();
			tag = type;
			height = 34;

			addControl(new Label(GlobalEngine.fnt4x7_brown, TEXT_MARGIN, 2 + 1, type.name));
			addControl(new Label(GlobalEngine.fnt3x5_brown, TEXT_MARGIN, 14 + 1, "ЦЕНА: " + type.price));
			addControl(new Label(GlobalEngine.fnt3x5_brown, TEXT_MARGIN, 24 + 1, "ПРОИЗВОДСТВО: " + (type.buildTime / GlobalEngine.getTimeFromDays(1)) + " ДН."));
		}

		@Override
		public void draw()
		{
			if (picSelected != null) picSelected.drawAt(scrX, scrY);
			picSmall.drawAt(scrX + 2, scrY + 2);
			super.draw();
		}

		@Override
		public void onSelected()
		{
			picSelected = getDrawable(R.ui.building_book_sel_bgr);
		}

		@Override
		public void onDeselected()
		{
			picSelected = null;
		}

	}
}
