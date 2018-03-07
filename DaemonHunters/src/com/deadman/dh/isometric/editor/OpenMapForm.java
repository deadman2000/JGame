package com.deadman.dh.isometric.editor;

import java.awt.event.MouseEvent;
import java.io.File;

import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoMapInfo;
import com.deadman.dh.isometric.MapFormat;
import com.deadman.jgame.ui.Column;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.Row;
import com.deadman.jgame.ui.RowLayout;

public class OpenMapForm extends IEWindow
{
	private IIsoEditor _editor;
	private FileView fw;

	private Column infoPanel;
	private Label laWidth, laHeight, laZHeight;

	public OpenMapForm(IIsoEditor editor)
	{
		_editor = editor;
		width = 230;

		IsoMap map = editor.getMap();

		{
			Row row1 = new Row();
			row1.setHeight(140);
			row1.fillContent();
			row1.setSpacing(4);
			row1.x = 4;
			addControl(row1);

			fw = new FileView();
			fw.setWidth(160);
			fw.setFilterByExt(".map");
			if (map != null)
				fw.setPath(new File(map.fileName).getParentFile());
			fw.addControlListener(file_listener);
			row1.addControl(fw);

			infoPanel = new Column();
			infoPanel.visible = false;
			RowLayout.settings(infoPanel).fillWidth();
			infoPanel.fillContent();
			row1.addControl(infoPanel);

			{
				{
					Row row = new Row();
					row.setHeight(12);
					infoPanel.addControl(row);

					Label la = new Label(IsoEditor.fnt_light_3x5, "WIDTH:");
					la.autosize = false;
					//RowLayout.settings(la).fillWidth();
					la.setWidth(36);
					row.addControl(la);

					row.addControl(laWidth = new Label(IsoEditor.fnt_light_3x5));
				}

				{
					Row row = new Row();
					row.setHeight(12);
					infoPanel.addControl(row);

					Label la = new Label(IsoEditor.fnt_light_3x5, "HEIGHT:");
					la.autosize = false;
					//RowLayout.settings(la).fillWidth();
					la.setWidth(36);
					row.addControl(la);

					row.addControl(laHeight = new Label(IsoEditor.fnt_light_3x5));
				}

				{
					Row row = new Row();
					row.setHeight(12);
					infoPanel.addControl(row);

					Label la = new Label(IsoEditor.fnt_light_3x5, "Z-HEIGHT:");
					la.autosize = false;
					//RowLayout.settings(la).fillWidth();
					la.setWidth(36);
					row.addControl(la);

					row.addControl(laZHeight = new Label(IsoEditor.fnt_light_3x5));
				}
			}
		}

		{
			Row row1 = new Row();
			row1.x = 4;
			row1.setHeight(BTN_H);
			row1.setSpacing(4);
			row1.setRightToLeft();
			addControl(row1);

			EditorButton btOpen = new EditorButton();
			btOpen.setSize(BTN_W, BTN_H);
			btOpen.setLabel(IsoEditor.fnt_light_3x5, 3, "SAVE");
			btOpen.addControlListener(new ControlListener()
			{
				@Override
				public void onControlPressed(Control control, MouseEvent e)
				{
					File f = fw.currentFile();
					if (f != null && f.isFile())
					{
						_editor.loadMap(f.getAbsolutePath());
						close();
					}
				}
			});
			row1.addControl(btOpen);

			EditorButton btCancel = new EditorButton();
			btCancel.setSize(BTN_W, BTN_H);
			btCancel.setLabel(IsoEditor.fnt_light_3x5, 3, "CANCEL");
			btCancel.addControlListener(new ControlListener()
			{
				@Override
				public void onControlPressed(Control control, MouseEvent e)
				{
					close();
				}
			});
			row1.addControl(btCancel);
		}
	}

	ControlListener file_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == FileView.ACTION_FILE_SELECTED)
			{
				File file = (File) tag;
				_editor.loadMap(file.getAbsolutePath());
				close();
			}
			else if (action == ACTION_ITEM_SELECTED)
			{
				File file = fw.currentFile();
				if (file != null && file.isFile())
					setInfo(MapFormat.loadInfo(file.getAbsolutePath()));
				else
					setInfo(null);
			}
		}
	};

	private void setInfo(IsoMapInfo info)
	{
		if (info != null)
		{
			infoPanel.show();
			laWidth.setText(info.width);
			laHeight.setText(info.height);
			laZHeight.setText(info.zheight);
		}
		else
		{
			infoPanel.hide();
		}
	}
}
