package com.deadman.jgame.resources;

import java.util.ArrayList;

import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.Picture;

public class FontEntry extends ResourceEntry
{
	private GameFont _font;

	public FontEntry(String[] csv)
	{
		super(csv);
	}

	@Override
	public int getType()
	{
		return FONT;
	}

	public GameFont getFont()
	{
		if (_font == null)
			_font = getFont(null);
		return _font;
	}
	
	private ArrayList<GameFont> _palFonts = new ArrayList<>();

	public GameFont getFont(int[] pal)
	{
		for (GameFont f : _palFonts)
			if (f.isPalette(pal))
				return f;
		
		try
		{
			XCF xcf;
			if (pal != null)
				xcf = XCF.loadIndexed(path, pal);
			else
				xcf = XCF.loadFile(path);
			ArrayList<Picture> letters = new ArrayList<>();
			for (Layer l : xcf.getLayers())
			{
				int letter = (int) l.name.charAt(0);
				int insertNulls = letter - letters.size() + 1;
				for (int i = 0; i < insertNulls; i++)
				{
					letters.add(null);
				}

				letters.set(letter, l);
			}
			GameFont gf = new GameFont(path, letters.toArray(new Picture[letters.size()]));
			gf.palette = pal;
			_palFonts.add(gf);
			return gf;
		}
		catch (Exception ex)
		{
			System.err.println(ex.toString());
		}
		return null;
	}
}
