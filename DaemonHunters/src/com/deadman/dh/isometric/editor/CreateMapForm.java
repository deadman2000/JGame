package com.deadman.dh.isometric.editor;

import java.awt.event.MouseEvent;
import java.io.File;

import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.ui.Column;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.Row;
import com.deadman.jgame.ui.RowLayout;
import com.deadman.jgame.ui.TextBox;

public class CreateMapForm extends IEWindow
{
	private IsoEditor editor;
	private FileView fw;
	private TextBox tbFileName, tbWidth, tbHeight, tbZHeight;

	public CreateMapForm(IsoEditor editor)
	{
		this.editor = editor;
		width = 230;
		
		Drawable tbBgr = getDrawable(R.editor.tb_bgr);

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

			Column col1 = new Column();
			RowLayout.settings(col1).fillWidth();
			col1.fillContent();
			row1.addControl(col1);
			{
				{
					Row row = new Row();
					row.setHeight(12);
					col1.addControl(row);

					Label la = new Label(IsoEditor.fnt_light_3x5, 0, 2, "WIDTH:");
					la.autosize = false;
					RowLayout.settings(la).fillWidth();
					//la.setWidth(36);
					row.addControl(la);

					row.addControl(tbWidth = new TextBox(IsoEditor.fnt_dark_3x5, 0, 0, 20));
					tbWidth.background = tbBgr;
					tbWidth.filter = TextBox.FILTER_INT;
				}

				{
					Row row = new Row();
					row.setHeight(12);
					col1.addControl(row);

					Label la = new Label(IsoEditor.fnt_light_3x5, 0, 2, "HEIGHT:");
					la.autosize = false;
					RowLayout.settings(la).fillWidth();
					//la.setWidth(36);
					row.addControl(la);

					row.addControl(tbHeight = new TextBox(IsoEditor.fnt_dark_3x5, 0, 0, 20));
					tbHeight.background = tbBgr;
					tbHeight.filter = TextBox.FILTER_INT;
				}

				{
					Row row = new Row();
					row.setHeight(12);
					col1.addControl(row);

					Label la = new Label(IsoEditor.fnt_light_3x5, 0, 2, "Z-HEIGHT:");
					la.autosize = false;
					RowLayout.settings(la).fillWidth();
					//la.setWidth(36);
					row.addControl(la);

					row.addControl(tbZHeight = new TextBox(IsoEditor.fnt_dark_3x5, 0, 0, 20));
					tbZHeight.background = tbBgr;
					tbZHeight.filter = TextBox.FILTER_INT;
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
			tbFileName.background = tbBgr;
			tbFileName.filter = TextBox.FILTER_FILENAME;
		}

		{
			Row row1 = new Row();
			addControl(row1);
			row1.setHeight(BTN_H);
			row1.setSpacing(4);
			row1.setRightToLeft();

			EditorButton btCreate = new EditorButton();
			btCreate.setSize(BTN_W, BTN_H);

			btCreate.setLabel(IsoEditor.fnt_light_3x5, 3, "CREATE");
			btCreate.addControlListener(new ControlListener()
			{
				@Override
				public void onControlPressed(Control control, MouseEvent e)
				{
					createMap();
				}
			});
			row1.addControl(btCreate);

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

	private void createMap()
	{
		try
		{
			int w = Integer.parseInt(tbWidth.text);
			int h = Integer.parseInt(tbHeight.text);
			int l = Integer.parseInt(tbZHeight.text);

			String fileName = tbFileName.text.trim();
			if (fileName.isEmpty())
			{
				System.err.println("File name not set");
				return;
			}

			if (!fileName.endsWith(".map"))
				fileName += ".map";

			File f = new File(fw.currentDir(), fileName);

			editor.createMap(f.getAbsolutePath(), w, h, l, 0);
			close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	ControlListener file_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == FileView.ACTION_FILE_SELECTED)
			{
				File f = (File) tag;
				tbFileName.setText(f.getName());
			}
		};
	};
}
