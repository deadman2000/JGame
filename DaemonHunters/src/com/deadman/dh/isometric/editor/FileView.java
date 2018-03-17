package com.deadman.dh.isometric.editor;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ListViewItem;
import com.deadman.jgame.ui.VListView;

public class FileView extends VListView
{
	private Drawable ic_folder, ic_file;
	private File _currentDir;

	public FileView()
	{
		ic_folder = getDrawable(R.editor.fb_folder);
		ic_file = getDrawable(R.editor.fb_file);

		bgrColor = 0xFF000000;
		setScrollBar(Game.createVScrollInfo());

		setPath(new File("").getAbsoluteFile());
	}

	public static final int ACTION_FILE_SELECTED = 10000;

	public void setPath(File path)
	{
		if (path != null && !path.isDirectory())
		{
			onAction(ACTION_FILE_SELECTED, path);
			return;
		}

		/*System.out.println("Set path " + path.getAbsolutePath());
		if (path.getParentFile() == null)
			System.out.println("Parent: null");
		else
			System.out.println("Parent: " + path.getParentFile()
					.getAbsolutePath());*/

		clear();

		if (path == null)
		{
			_currentDir = null;
			File[] files = File.listRoots();
			for (File f : files)
			{
				if (!f.isDirectory()) continue;
				addItem(new FileItem(f, f.toString()));
			}
		}
		else
		{
			_currentDir = path.getAbsoluteFile();
			File parent = _currentDir.getParentFile();
			addItem(new FileItem(parent, ".."));

			File[] files = _currentDir.listFiles(filter);
			Arrays.sort(files, fileComparer);
			for (File f : files)
			{
				if (f.isHidden()) continue;

				addItem(new FileItem(f.getAbsoluteFile()));
			}
		}

		selectFirst();
	}

	private FileFilter filter;

	public void setFilter(FileFilter f)
	{
		filter = f;
		setPath(_currentDir);
	}
	
	public File currentDir()
	{
		return _currentDir;
	}

	public File currentFile()
	{
		FileItem it = (FileItem) selectedItem;
		if (it != null)
			return it.file;
		return null;
	}

	@Override
	protected void onKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if (selectedItem != null)
				setPath(currentFile());

			e.consume();
			return;
		}

		super.onKeyPressed(e);
	}

	static Comparator<File> fileComparer = new Comparator<File>()
	{
		@Override
		public int compare(File f1, File f2)
		{
			if (f1.isDirectory())
			{
				if (f2.isDirectory())
					return compareByName(f1, f2);
				else
					return -1;
			}
			if (f2.isDirectory())
				return 1;

			return compareByName(f1, f2);
		}

		private int compareByName(File f1, File f2)
		{
			return f1.getName()
					.compareTo(f2.getName());
		}
	};

	class FileItem extends ListViewItem
	{
		public final File file;

		public FileItem(File f)
		{
			this(f, f.getName());
		}

		public FileItem(File f, String name)
		{
			height = 8;
			file = f;

			Drawable icon;
			GameFont font;

			if (file == null || file.isDirectory())
			{
				icon = ic_folder;
				font = IsoEditor.fnt_light_3x5;
			}
			else
			{
				icon = ic_file;
				font = IsoEditor.fnt_medium_3x5;
			}

			addControl(new Control(icon, 1, 1));
			Label la = new Label(font, 10, 2);
			la.autosize = false;
			la.width = FileView.this.width - 19;
			la.height = 6;
			la.setText(name);
			addControl(la);
		}

		@Override
		public void onSelected()
		{
			bgrColor = 0xff6e5f36;
		}

		@Override
		public void onDeselected()
		{
			bgrColor = 0;
		}

		@Override
		protected void onClick(Point p, MouseEvent e)
		{
			if (e.getClickCount() % 2 == 0)
				setPath(file);

			e.consume();
		}
	}

	public void setFilterByExt(final String extension)
	{
		setFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.isDirectory() || f.getName()
						.endsWith(extension);
			}
		});
	}
}
