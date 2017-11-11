package com.deadman.dh.isometric.editor;

import java.awt.event.MouseEvent;
import java.io.File;

import com.deadman.dh.R;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.TextBox;

public class SaveMapForm extends IEWindow
{
	private IsoEditor _editor;
	private FileView fw;
	
	private Control infoPanel;
	private Label laWidth, laHeight, laZHeight;
	private TextBox tbFileName;

	public SaveMapForm(IsoEditor editor)
	{
		this._editor = editor;

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

		addControl(infoPanel = new Control(167, 6, 82, 36));
		infoPanel.visible = false;
		infoPanel.addControl(new Label(IsoEditor.fnt_light_3x5, 0, 0, "WIDTH:"));
		infoPanel.addControl(new Label(IsoEditor.fnt_light_3x5, 0, 10, "HEIGHT:"));
		infoPanel.addControl(new Label(IsoEditor.fnt_light_3x5, 0, 20, "Z-HEIGHT:"));
		infoPanel.addControl(laWidth = new Label(IsoEditor.fnt_light_3x5, 37, 0));
		infoPanel.addControl(laHeight = new Label(IsoEditor.fnt_light_3x5, 37, 10));
		infoPanel.addControl(laZHeight = new Label(IsoEditor.fnt_light_3x5, 37, 20));
		
		Button btSave = new Button(R.editor.ie_button_9p, R.editor.ie_button_pr_9p);
		btSave.setBounds(width - 8 - BTN_W * 2 - 4, height - 8 - BTN_H, BTN_W, BTN_H, ANCHOR_BOTTOM | ANCHOR_RIGHT);
		btSave.setLabel(IsoEditor.fnt_light_3x5, 3, "SAVE");
		btSave.addControlListener(new ControlListener()
		{
			@Override
			public void onControlPressed(Control control, MouseEvent e)
			{
				String fileName = tbFileName.text.trim();
				if (fileName.isEmpty()) return;
				
				File f = new File(fw.currentDir(), fileName);
				_editor.getMap().saveMap(f.getAbsolutePath());
				close();
			}
		});
		addControl(btSave);

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
					setInfo(IsoMapInfo.load(file.getAbsolutePath()));
				else
					setInfo(null);
			}
		}
	};

	private void setInfo(IsoMapInfo info)
	{
		if (info != null)
		{
			infoPanel.visible = true;
			laWidth.setText(info.width);
			laHeight.setText(info.height);
			laZHeight.setText(info.zheight);
		}
		else
		{
			infoPanel.visible = false;
		}
	}
}
