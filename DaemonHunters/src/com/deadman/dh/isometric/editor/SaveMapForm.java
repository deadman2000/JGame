package com.deadman.dh.isometric.editor;

import java.awt.event.MouseEvent;
import java.io.File;

import com.deadman.dh.R;
import com.deadman.dh.isometric.IsoMapInfo;
import com.deadman.dh.isometric.MapFormat;
import com.deadman.jgame.ui.Column;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.Row;
import com.deadman.jgame.ui.RowLayout;
import com.deadman.jgame.ui.TextBox;

public class SaveMapForm extends IEWindow
{
	private IsoEditor _editor;
	private FileView fw;

	private Column infoPanel;
	private Label laWidth, laHeight, laZHeight;
	private TextBox tbFileName;

	public SaveMapForm(IsoEditor editor)
	{
		_editor = editor;
		width = 230;
		
		{
			Row row1 = new Row();
			row1.setHeight(140);
			row1.fillContent();
			row1.setSpacing(4);
			addControl(row1);

			fw = new FileView();
			fw.setWidth(160);
			fw.setFilterByExt(".map");
			fw.setPath(new File(editor.getMap().fileName).getParentFile());
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
			addControl(row1);
			row1.setHeight(10);
			row1.setSpacing(4);

			row1.addControl(new Label(IsoEditor.fnt_light_3x5, 0, 2, "FILE NAME:"));

			row1.addControl(tbFileName = new TextBox(IsoEditor.fnt_dark_3x5, 0, 0, 0));
			RowLayout.settings(tbFileName).fillWidth();
			tbFileName.background = getDrawable(R.editor.tb_bgr);
			tbFileName.filter = TextBox.FILTER_FILENAME;
		}

		{
			Row row1 = new Row();
			row1.setHeight(BTN_H);
			row1.setSpacing(4);
			row1.setRightToLeft();
			addControl(row1);

			EditorButton btSave = new EditorButton();
			btSave.setSize(BTN_W, BTN_H);
			btSave.setLabel(IsoEditor.fnt_light_3x5, 3, "SAVE");
			btSave.addControlListener(new ControlListener()
			{
				@Override
				public void onControlPressed(Control control, MouseEvent e)
				{
					String fileName = tbFileName.text.trim();
					if (fileName.isEmpty()) return;

					if (!fileName.contains("."))
						fileName = fileName + ".map";

					File f = new File(fw.currentDir(), fileName);
					_editor.getMap().saveMap(f.getAbsolutePath());
					close();
				}
			});
			row1.addControl(btSave);

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
				_editor.getMap().saveMap(file.getAbsolutePath());
				close();
			}
			else if (action == ACTION_ITEM_SELECTED)
			{
				File file = fw.currentFile();
				if (file != null && file.isFile())
				{
					setInfo(MapFormat.loadInfo(file.getAbsolutePath()));
					tbFileName.setText(file.getName());
				}
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
