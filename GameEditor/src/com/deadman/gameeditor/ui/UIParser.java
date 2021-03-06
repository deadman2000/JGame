package com.deadman.gameeditor.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class UIParser
{
	private final UICompiler _compiler;
	private final IFile _file;

	// File variables
	private StringBuilder _code;
	private ControlDescription _rootControl;

	// Current line variables
	private int lineNumber;
	private int lineIndex;
	private String line;
	private InstanceDescription currentControl;

	public UIParser(UICompiler compiler, IFile file)
	{
		_compiler = compiler;
		_file = file;
	}

	private static Pattern commentPattern = Pattern.compile("(\\\"(\\\\\\\"|.)*?\\\"|\\'(\\\\\\'|.)*?\\')|(?<comment>#[^\\r\\n]*$)"); // (\"(\\\"|.)*?\"|\'(\\\'|.)*?\')|(?<comment>#[^\r\n]*$)

	public void parse()
	{
		lineNumber = 0;
		currentControl = null;

		try
		{
			_file.deleteMarkers(IMarker.PROBLEM, true, IFile.DEPTH_INFINITE);
		}
		catch (CoreException e)
		{
		}

		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(_file.getContents(true)));

			while ((line = in.readLine()) != null)
			{
				lineNumber++;
				lineIndex = 0;

				try
				{
					// Вырезаем коммент
					Matcher m = commentPattern.matcher(line);
					while (m.find())
					{
						int c = m.start("comment");
						if (c != -1)
						{
							line = line.substring(0, c);
							break;
						}
					}

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
						if (currentControl == null || currentControl instanceof ControlDescription)
						{
							currentControl = control.createDescription((ControlDescription) currentControl, spaces);
							currentControl.setConstruct(getCallArgs());

							if (_rootControl == null) _rootControl = (ControlDescription) currentControl;
						}
						else
							throw new ParseException("Parent is not Control");
					}
					else if ((layout = _compiler.layouts.get(word)) != null)
					{
						if (currentControl instanceof ControlDescription)
						{
							LayoutDescription l = layout.createDescription((ControlDescription) currentControl, spaces);
							l.setConstruct(getCallArgs());
							((ControlDescription) currentControl).layout = l;
							currentControl = l;
						}
						else
							throw new ParseException("Parent is not Control");
					}
					else if (currentControl == null) // Дальше пойдут свойства и методы, поэтому проверяем наличие элемента в корне
					{
						throw new ParseException("No root for " + word);
					}
					else if (word.equals("L"))
					{
						if (currentControl.parent == null)
							throw new ParseException("No parent");
						if (currentControl.parent.layoutInfo() == null)
							throw new ParseException("No parent layout");

						currentControl.appendCode(currentControl.parent	.layoutInfo()
																		.fullClassName() + ".settings(%1$s)" + line.substring(lineIndex) + ";");
					}
					else if (word.equals("id"))
					{
						skipAssign();
						currentControl.varName = getJavaIdentifier();
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
					IMarker marker = _file.createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.MESSAGE, pe.getMessage());
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				}
			}

			buildSource();
		}
		catch (Exception e)
		{
			System.err.println(String.format("Exception %s at line %s char %s: %s", e.getMessage(), lineNumber, lineIndex, line));
			e.printStackTrace();
		}
	}

	int _indent;

	private void buildSource()
	{
		try
		{
			String className = _file.getName()
									.replace('.', '_');

			_code = new StringBuilder();
			_indent = 0;

			appendLine("package " + _compiler.resources.pckg + ";");
			appendLine();

			appendLine("public class " + className + " extends " + _rootControl.classInfo.fullClassName());
			appendLine("{");
			{
				_indent++;

				// Объявления переменных
				_rootControl.writeChildsDeclaration(this);
				appendLine();

				// Конструктор
				appendLine("public " + className + "()");
				appendLine("{");
				{
					_indent++;
					_rootControl.writeCode(this);
					_indent--;
				}
				appendLine("}");

				_indent--;
			}
			_code.append("}");

			_compiler.resources.saveGenerated(className + ".java", _code.toString());
		}
		catch (Exception e)
		{
			System.err.println("Build failed:");
			e.printStackTrace();
		}
		_code = null;
	}

	public StringBuilder appendIndent()
	{
		for (int i = 0; i < _indent; i++)
			_code.append('\t');
		return _code;
	}

	public void appendLine()
	{
		_code.append("\r\n");
	}

	public void appendLine(String text)
	{
		appendIndent();
		_code	.append(text)
				.append("\r\n");
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

	private static Pattern patternAssign = Pattern.compile("^\\s*=\\s*"); // ^\s*=\s*

	private void skipAssign() throws ParseException
	{
		Matcher m = patternAssign.matcher(line.substring(lineIndex));
		if (!m.find())
			throw new ParseException("Assignment not found");
		lineIndex += m.end();
	}

	private String getAssign() throws ParseException
	{
		skipAssign();
		return getExpression();
	}

	// Считывает выражение до конца строки или запятой. аргумент функции или присваивания
	private String getExpression() throws ParseException
	{
		skipSpaces();
		checkEndOfLine();

		int start = lineIndex;
		for (; lineIndex < line.length(); lineIndex++)
		{
			Character s = currentSymbol();
			if (s == '"')
				skipString();
			else if (s == ',' || s == ')') break;
		}

		return line.substring(start, lineIndex);
	}

	private void skipString()
	{
		Character boundSymbol = currentSymbol();
		lineIndex++;
		for (; lineIndex < line.length(); lineIndex++)
		{
			Character s = currentSymbol();
			if (s == '\\' && lineIndex < line.length() - 1)
				lineIndex++;
			else if (s == boundSymbol)
				return;
		}
		lineIndex--;
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
