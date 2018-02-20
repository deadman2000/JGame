package com.deadman.gameeditor.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class UIParser
{
	private UICompiler _compiler;

	public UIParser(UICompiler compiler)
	{
		_compiler = compiler;
	}

	int lineNumber;
	int lineIndex;
	String line;
	ControlDescription currentControl;

	public void parse(IFile file)
	{
		lineNumber = 0;
		currentControl = null;

		try
		{
			file.deleteMarkers(IMarker.PROBLEM, true, IFile.DEPTH_INFINITE);
		}
		catch (CoreException e)
		{
		}

		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(file.getContents(true)));

			while ((line = in.readLine()) != null)
			{
				lineNumber++;
				lineIndex = 0;

				try
				{
					// Вырезаем коммент
					int c = line.indexOf('#');
					if (c != -1)
						line = line.substring(0, c);

					// Считаем глубину
					int spaces = getSpaces(); // Число пробелов
					if (spaces == -1) continue;

					// Определяем родительский элемент
					checkParent(spaces);

					// Считываем слово
					String word = getJavaIdentifier();

					ControlInfo control;
					LayoutInfo layout;

					if ((control = _compiler.controls.get(word)) != null)
					{
						currentControl = control.createDescription(currentControl, spaces, getCallArgs());
					}
					else if ((layout = _compiler.layouts.get(word)) != null)
					{
						currentControl.layout = layout.createDescription(spaces, getCallArgs());
					}
					else if (currentControl == null) // Дальше пойдут свойства и методы, поэтому проверяем наличие элемента в корне
					{
						throw new ParseException("No root for " + word);
					}
					else if (word.equals("L"))
					{
					}
					else if (word.equals("id"))
					{
						skipAssign();
						currentControl.id = getJavaIdentifier();
					}
					else if (currentControl.hasProperty(word))
					{
						currentControl.setProperty(word, getAssign());
					}
					else if (currentControl.hasMethod(word))
					{
						currentControl.addCall(word, getCallArgs());
					}
					else
						throw new ParseException("Wrong word '" + word + "'");

					//System.out.println(String.format("%3s: %2s: %s", lineNumber, spaces, word));
				}
				catch (ParseException pe)
				{
					IMarker marker = file.createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.MESSAGE, pe.getMessage());
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				}
			}
		}
		catch (Exception e)
		{
			System.err.println(String.format("Exception %s at line %s char %s: %s", e.getMessage(), lineNumber, lineIndex, line));
			e.printStackTrace();
		}
	}

	private Character currentSymbol()
	{
		return line.charAt(lineIndex);
	}

	private void checkParent(int spaces) throws ParseException
	{
		if (currentControl != null)
		{
			if (spaces == currentControl.deep) // Такая же глубина - нам нужен родительский
			{
				currentControl = currentControl.parent;
			}
			else if (spaces < currentControl.deep) // Глубина меньше - идем по родителям, пока не найдем нужную глубину
			{
				while (true)
				{
					currentControl = currentControl.parent;
					if (currentControl.parent == null) // Дошли до верха
					{
						if (currentControl.deep <= spaces) // Элемент на том же уровне что и корень - ошибка синтаксиса, может быть только один корневой элемент
							throw new ParseException("Wrong root");
						break;
					}

					if (spaces == currentControl.deep)
					{
						currentControl = currentControl.parent;
						break;
					}

					if (spaces > currentControl.deep) // Перепрыгнули нужную глубину - ошибка синтаксиса
						throw new ParseException("Wrong element deep");
				}
			}
		}
	}

	private int getSpaces()
	{
		int spaces = 0;
		for (; lineIndex < line.length(); lineIndex++)
		{
			switch (currentSymbol())
			{
				case ' ':
					spaces++;
					break;
				case '\t':
					spaces += 4;
					break;
				default:
					return spaces;
			}
		}
		return -1;
	}

	private String getJavaIdentifier() throws ParseException
	{
		skipSpaces();
		checkEndOfLine();
		
		if (!Character.isJavaIdentifierStart(currentSymbol()))
			throw new ParseException("Wrong identifier");

		int s = lineIndex;
		for (lineIndex++; lineIndex < line.length(); lineIndex++)
			if (!Character.isJavaIdentifierPart(currentSymbol()))
				return line.substring(s, lineIndex);

		return line.substring(s);
	}

	private String[] getCallArgs() throws ParseException
	{
		skipSpaces();
		if (isEndOfLine()) return null;

		if (currentSymbol() != '(')
			return null;

		lineIndex++; // Skipping symbol '('
		skipSpaces();
		if (currentSymbol() == ')') return null;

		ArrayList<String> args = new ArrayList<>();
		while (true)
		{
			if (isEndOfLine()) break;

			String val = getExpression();
			args.add(val);

			skipSpaces();
			checkEndOfLine();

			if (currentSymbol() == ',')
			{
				lineIndex++;
				continue;
			}

			if (currentSymbol() == ')')
				break;

			throw new ParseException("Unexpected symbol");
		}

		return args.toArray(new String[args.size()]);
	}

	private void skipAssign() throws ParseException
	{
		skipSpaces();
		checkEndOfLine();

		if (currentSymbol() != '=')
			throw new ParseException("Assignment not found");

		lineIndex++; // Skipping symbol '='
	}

	private String getAssign() throws ParseException
	{
		skipAssign();
		return getExpression();
	}

	private String getExpression() throws ParseException
	{
		skipSpaces();
		checkEndOfLine();

		int start = lineIndex;
		for (; lineIndex < line.length(); lineIndex++)
		{
			Character s = currentSymbol();
			// TODO Обработка строк
			if (s == ',' || s == ')') break;
		}

		return line.substring(start, lineIndex);
	}

	/*private String readWordOrNumber()
	{
		int start = lineIndex;
		if (currentSymbol() == '-')
			lineIndex++;
		for (; lineIndex < line.length(); lineIndex++)
			if (!Character.isLetterOrDigit(currentSymbol()))
				break;
		return line.substring(start, lineIndex);
	}*/

	private void checkEndOfLine() throws ParseException
	{
		if (isEndOfLine())
			throw new ParseException("Unexpected end of line");
	}

	private void skipSpaces()
	{
		for (; lineIndex < line.length(); lineIndex++)
			if (!Character.isWhitespace(currentSymbol()))
				return;
	}

	public boolean isEndOfLine()
	{
		return lineIndex >= line.length();
	}
}
