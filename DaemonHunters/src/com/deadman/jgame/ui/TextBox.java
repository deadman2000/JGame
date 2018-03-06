package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;

import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.drawing.Picture;

public class TextBox extends Control
{
	public GameFont _font;
	public String text = "";
	public Picture[] _chars;
	public int maxLength = Integer.MAX_VALUE;
	public boolean textAlignLeft = true;
	public int filter = FILTER_NONE;
	private long cur_tick_begin;

	public TextBox()
	{
	}
	
	public TextBox(GameFont font)
	{
		setFont(font);
	}

	public TextBox(GameFont font, int x, int y, int width)
	{
		_font = font;
		setBounds(x, y, width, font.height + 5);
		setText("");
	}
	
	@Property("font")
	public void setFont(GameFont font)
	{
		_font = font;
		setHeight(font.height + 5);
		setText(text);
	}
	
	@Override
	protected void onResize()
	{
		super.onResize();
		maxLength = (width - 4) / (_font.width + 1);
	}

	public int contentColor = 0;
	public int cursorColor = 0xFF000000;

	@Override
	public void draw()
	{
		super.draw();
		/*if (bgrColor != 0)
			GameScreen.screen.drawRect(scrX, scrY, width, height, bgrColor);*/
		if (contentColor != 0)
			GameScreen.screen.drawRect(scrX + 1, scrY + 1, width - 2, height - 2, contentColor);

		int cx = scrX + 2;
		int dx;
		if (textAlignLeft)
		{
			dx = 2;
			for (int i = 0; i < _chars.length; i++)
			{
				if (i == _cursor_pos)
					cx = scrX + dx - 1;

				Picture ch = _chars[i];
				if (ch != null)
				{
					ch.drawAt(scrX + dx, scrY + 3);
					dx += ch.width + 1;
				}
			}
			if (_cursor_pos == text.length())
				cx = scrX + dx - 1;
		}
		else
		{
			dx = width - 2;
			if (_cursor_pos == text.length())
				cx = scrX + dx;
			for (int i = _chars.length - 1; i >= 0; i--)
			{
				Picture ch = _chars[i];
				if (ch != null)
				{
					dx -= ch.width;
					ch.drawAt(scrX + dx, scrY + 3);
					dx--;
				}

				if (i == _cursor_pos)
					cx = scrX + dx;

			}
		}

		if (isFocused && (GameLoop.frames - cur_tick_begin) % 40 < 20)
			GameScreen.screen.drawRect(cx, scrY + 2, 1, height - 4, cursorColor);
	}

	public void setText(String str)
	{
		//System.out.println("Text: " + str);
		text = str;
		_chars = getChars(text);
		setCursorPos(text.length());
		onAction(ACTION_VALUE_CHANGED);
	}

	public void clear()
	{
		text = "";
		_chars = new Picture[0];
		setCursorPos(0);
		onAction(ACTION_VALUE_CHANGED);
	}

	private Picture[] getChars(String string)
	{
		Picture[] pics = new Picture[string.length()];
		for (int i = 0; i < string.length(); i++)
		{
			pics[i] = _font.getCharPic(string.charAt(i));
		}
		return pics;
	}

	int _cursor_pos;

	public void setCursorPos(int pos)
	{
		if (pos >= 0 && pos <= _chars.length)
			_cursor_pos = pos;
		cur_tick_begin = GameLoop.frames;
	}

	@Override
	public void pressKey(KeyEvent e)
	{
		if (!isFocused) return;

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_BACK_SPACE:
				if (_cursor_pos > 0)
				{
					if (_cursor_pos == text.length())
						setText(text.substring(0, text.length() - 1));
					else
					{
						setText(text.substring(0, _cursor_pos - 1) + text.substring(_cursor_pos));
						setCursorPos(_cursor_pos - 1);
					}
				}
				e.consume();
				return;

			case KeyEvent.VK_DELETE:
				if (_cursor_pos < _chars.length)
				{
					if (_cursor_pos == 0)
						setText(text.substring(1));
					else
					{
						setText(text.substring(0, _cursor_pos) + text.substring(_cursor_pos + 1));
						//setCursorPos(_cursor_pos - 1);
					}
				}
				e.consume();
				return;

			case KeyEvent.VK_LEFT:
				setCursorPos(_cursor_pos - 1);
				e.consume();
				return;
			case KeyEvent.VK_RIGHT:
				setCursorPos(_cursor_pos + 1);
				e.consume();
				return;
			case KeyEvent.VK_HOME:
				setCursorPos(0);
				e.consume();
				return;
			case KeyEvent.VK_END:
				setCursorPos(text.length());
				e.consume();
				return;
		}

		char ch = e.getKeyChar();
		if (isPass(ch))
		{
			if (text.length() < maxLength && _font.hasCharPic(ch))
			{
				String t;
				if (_cursor_pos == text.length())
					t = text + ch;
				else
					t = text.substring(0, _cursor_pos) + ch + text.substring(_cursor_pos);

				setText(t);
				setCursorPos(_cursor_pos + 1);
			}

			e.consume();
		}
	}

	private boolean isPass(char ch)
	{
		if (!isPrintableChar(ch)) return false;
		switch (filter)
		{
			case FILTER_INT:
				return isInt(ch);
			case FILTER_FLOAT:
				return isFloat(ch);
			case FILTER_FILENAME:
				return isFileName(ch);
			default:
			case FILTER_NONE:
				return true;
		}
	}

	private static boolean isPrintableChar(char c)
	{
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return (!Character.isISOControl(c)) && c != KeyEvent.CHAR_UNDEFINED && block != null && block != Character.UnicodeBlock.SPECIALS;
	}

	private static boolean isInt(char c)
	{
		return c >= '0' && c <= '9';
	}

	private static boolean isFloat(char c)
	{
		return (c >= '0' && c <= '9') || (c == '.' || c == ',');
	}

	private static final HashSet<Character> ILLEGAL_FILE_CHARACTERS = new HashSet<>(Arrays.asList(new Character[]{ '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' }));
	
	private static boolean isFileName(char ch)
	{
		return !ILLEGAL_FILE_CHARACTERS.contains(ch);
	}
	
	
	@Override
	public void onPressed(Point p, MouseEvent e)
	{
		super.onPressed(p, e);

		if (textAlignLeft)
		{
			int dx = 2;
			int pos = _chars.length;
			for (int i = 0; i < _chars.length; i++)
			{
				if (dx + _chars[i].width / 2 + 1 > p.x)
				{
					pos = i;
					break;
				}

				dx += _chars[i].width + 1;
			}
			setCursorPos(pos);
		}
		else
		{
			int dx = width - 2;
			int pos = 0;
			for (int i = _chars.length - 1; i >= 0; i--)
			{
				if (dx - (_chars[i].width + 1) / 2 < p.x)
				{
					pos = i + 1;
					break;
				}
				dx -= _chars[i].width + 1;
			}
			setCursorPos(pos);
		}
	}

	public static final int FILTER_NONE = 0;
	public static final int FILTER_INT = 1;
	public static final int FILTER_FLOAT = 2;
	public static final int FILTER_FILENAME = 3;
}
