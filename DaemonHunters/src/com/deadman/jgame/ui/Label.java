package com.deadman.jgame.ui;

import java.util.ArrayList;

import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.Picture;

public class Label extends Control
{
	GameFont _font;

	String _text;
	String[] _lines; // Строки текста
	int[] _shifts; // Количество лишнего пространства в строках

	public int line_interval = 1;
	public int char_interval = 1;

	public boolean word_wrap = false;
	public int halign = ALIGN_LEFT;
	public int valign = ALIGN_TOP;
	public boolean autosize = true;

	public static final byte ALIGN_LEFT = 0;
	public static final byte ALIGN_TOP = 0;
	public static final byte ALIGN_CENTER = 1;
	public static final byte ALIGN_RIGHT = 2;
	public static final byte ALIGN_BOTTOM = 2;
	public static final byte ALIGN_FILL = 3;

	public Label(GameFont font)
	{
		_font = font;
		height = font.height;
	}

	public Label(String text, GameFont font, int x, int y)
	{
		this(font);
		setPosition(x, y);
		setText(text);
	}

	public Label(GameFont font, int x, int y)
	{
		this(font);
		setPosition(x, y);
	}

	public Label(GameFont font, int x, int y, String text)
	{
		this(font);
		setPosition(x, y);
		setText(text);
	}

	@Override
	protected void onResize()
	{
		updateText();
	}

	public void clear()
	{
		_text = null;
		_lines = null;
		_shifts = null;
		return;
	}

	public void setText(String format, Object... args)
	{
		setText(String.format(format, args));
	}

	public void setText(String text)
	{
		if (_text != null && _text.equals(text))
			return;
		_text = text;
		updateText();
	}

	public void setText(Object value)
	{
		if (value == null)
			setText("");
		else
			setText(value.toString());
	}

	public String getText()
	{
		return _text;
	}

	public int textWidth, textHeight;

	private void updateText()
	{
		//System.out.println("--------------");
		//System.out.println("");
		//System.out.println("Set text");
		if (_text == null)
		{
			_lines = null;
			return;
		}

		textWidth = 0;
		String[] lines = _text.split("\\r?\\n");

		ArrayList<String> linesList = new ArrayList<>();

		int newW, space;

		for (int l = 0; l < lines.length; l++)
		{
			//System.out.println("Line: " + lines[l]);
			int from = 0, to = 0, w = 0;
			String line = lines[l];
			if (line == null) continue;

			if (line.length() == 0)
			{
				linesList.add("");
				continue;
			}

			while (to < line.length())
			{
				char curr = line.charAt(to);
				newW = w + _font.getCharPic(curr).width;
				if (w > 0) newW += char_interval;

				//System.out.println("Char: " + curr +  " w:" + newW + " / " + width);

				if (word_wrap && newW > width && from != to) // Переход на новую строку
				{
					space = line.lastIndexOf(" ", to);
					//System.out.println("Space: " + space + " to " + to);
					if (space > from && space < to)
						to = space;

					String subLine = line.substring(from, to);
					//System.out.println("SubLine:" + subLine + " (" + subLine.length() + ")");
					linesList.add(subLine);

					while (line.charAt(to) == ' ')
						to++;
					//System.out.println("To: " + line.charAt(to));

					from = to;
					w = 0;
				}
				else
				{
					w = newW;
					to++;
				}
			}

			//System.out.println("End str: " + line.substring(from));
			linesList.add(line.substring(from));
		}

		textHeight = linesList.size() * _font.height + (linesList.size() - 1) * line_interval;

		_lines = linesList.toArray(new String[linesList.size()]);
		int[] widths = new int[_lines.length];
		for (int i = 0; i < _lines.length; i++)
		{
			int cw = getLineWidth(_lines[i]);
			if (cw > textWidth) textWidth = cw;
			widths[i] = cw;
		}

		if (!word_wrap && autosize)
			width = textWidth;

		_shifts = new int[_lines.length];
		for (int i = 0; i < _lines.length; i++)
		{
			_shifts[i] = width - widths[i];
		}

		if (autosize)
			height = textHeight;
	}

	private int getLineWidth(String line)
	{
		if (line.isEmpty()) return 0;

		int w = 0;
		for (int i = 0; i < line.length(); i++)
			w += _font.getCharPic(line.charAt(i)).width + char_interval;

		w -= char_interval;
		if (w < 0) return 0;
		return w;
	}

	public void setFont(GameFont font)
	{
		_font = font;
	}

	@Override
	public void draw()
	{
		super.draw();

		if (_text == null) return;

		int dy;
		if (valign == ALIGN_CENTER)
			dy = (height - textHeight) / 2;
		else if (valign == ALIGN_BOTTOM)
			dy = textHeight;
		else
			dy = 0;

		for (int l = 0; l < _lines.length; l++)
		{
			String line = _lines[l];

			int dx; // Смещение слева
			if (halign == ALIGN_CENTER)
				dx = _shifts[l] / 2;
			else if (halign == ALIGN_RIGHT)
				dx = _shifts[l];
			else
				dx = 0;

			for (int i = 0; i < line.length(); i++)
			{
				char ch = line.charAt(i);
				Picture letter = _font.getCharPic(ch);

				int nx = dx + letter.width + char_interval;
				if (!autosize && nx > width)
					break;

				letter.drawAt(scrX + dx, scrY + dy);
				dx = nx;

			}
			scrY += _font.height + line_interval;
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + " \"" + _text + "\"";
	}

	public int getTextWidth()
	{
		return textWidth;
	}
}
