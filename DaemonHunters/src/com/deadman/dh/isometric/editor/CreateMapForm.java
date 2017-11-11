package com.deadman.dh.isometric.editor;

import java.awt.event.MouseEvent;
import java.io.File;

import com.deadman.dh.R;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
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
		height = 180;

		fw = new FileView(4, 4, 160, 140);
		fw.setFilterByExt(".map");
		fw.setPath(new File(editor.getMap().fileName).getParentFile());
		fw.addControlListener(file_listener);
		addControl(fw);

		addControl(new Label(IsoEditor.fnt_light_3x5, 4, 151, "FILE NAME: "));
		addControl(tbFileName = new TextBox(IsoEditor.fnt_dark_3x5, 45, 148, 177));
		tbFileName.background = getDrawable(R.editor.tb_bgr);
		tbFileName.filter = TextBox.FILTER_FILENAME;

		addControl(new Label(IsoEditor.fnt_light_3x5, 167, 8, "WIDTH:"));
		addControl(new Label(IsoEditor.fnt_light_3x5, 167, 18, "HEIGHT:"));
		addControl(new Label(IsoEditor.fnt_light_3x5, 167, 28, "Z-HEIGHT:"));
		addControl(tbWidth = new TextBox(IsoEditor.fnt_dark_3x5, 204, 6, 20));
		tbWidth.background = tbFileName.background;
		tbWidth.filter = TextBox.FILTER_INT;
		addControl(tbHeight = new TextBox(IsoEditor.fnt_dark_3x5, 204, 16, 20));
		tbHeight.background = tbFileName.background;
		tbHeight.filter = TextBox.FILTER_INT;
		addControl(tbZHeight = new TextBox(IsoEditor.fnt_dark_3x5, 204, 26, 20));
		tbZHeight.background = tbFileName.background;
		tbZHeight.filter = TextBox.FILTER_INT;

		Button btCreate = new Button(R.editor.ie_button_9p, R.editor.ie_button_pr_9p);
		btCreate.setBounds(width - 8 - BTN_W * 2 - 4, height - 8 - BTN_H, BTN_W, BTN_H, ANCHOR_BOTTOM | ANCHOR_RIGHT);
		btCreate.setLabel(IsoEditor.fnt_light_3x5, 3, "CREATE");
		btCreate.addControlListener(new ControlListener()
		{
			@Override
			public void onControlPressed(Control control, MouseEvent e)
			{
				createMap();
			}
		});
		addControl(btCreate);

		Button btCancel = new Button(R.editor.ie_button_9p, R.editor.ie_button_pr_9p);
		btCancel.setBounds(width - 8 - BTN_W, height - 8 - BTN_H, BTN_W, BTN_H, ANCHOR_BOTTOM | ANCHOR_RIGHT);
		btCancel.setLabel(IsoEditor.fnt_light_3x5, 3, "CANCEL");
		btCancel.addControlListener(new ControlListener()
		{
			@Override
			public void onControlPressed(Control control, MouseEvent e)
			{
				close();
			}
		});
		addControl(btCancel);
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
				File f = (File)tag;
				tbFileName.setText(f.getName());
			}
		};
	};
}
