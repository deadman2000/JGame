package com.deadman.gameeditor.ui;

import java.util.ArrayList;

import org.eclipse.jdt.core.IMethod;

public class InstanceDescription
{
	public final ControlDescription parent;
	public final int deep;
	public final ClassInfo classInfo;
	public String varName;
	public final ArrayList<InstanceDescription> childs;

	private String _constuctCode;
	private ArrayList<String> _code;
	
	//private boolean _parentInConstruct = false;

	public InstanceDescription(ClassInfo classInfo, ControlDescription parent, int deep)
	{
		this.parent = parent;
		this.deep = deep;
		this.classInfo = classInfo;

		_code = new ArrayList<>();

		childs = new ArrayList<>();
		if (parent != null)
			parent.childs.add(this);
	}

	protected void appendCode(String code)
	{
		_code.add(code);
	}

	public void writeDeclaration(UIParser parser)
	{
		if (varName == null)
			varName = classInfo.generateVarName();

		writeCode(parser, "public " + classInfo.fullClassName() + " %1$s;");
		
		writeChildsDeclaration(parser);
	}
	
	public void writeChildsDeclaration(UIParser parser)
	{
		for (InstanceDescription child : childs)
			child.writeDeclaration(parser);
	}

	public void writeCode(UIParser parser)
	{
		writeInitialize(parser);
		writeParentAppend(parser);
		
		for (String line : _code)
			writeCode(parser, line);

		if (childs.size() > 0)
		{
			parser.appendLine();
			writeChildsCode(parser);
		}
	}
	
	protected void writeInitialize(UIParser parser)
	{
		if (_constuctCode != null)
			writeCode(parser, _constuctCode);
		else
			writeCode(parser, "%1$s = new " + classInfo.fullClassName() + "();");

	}

	protected void writeParentAppend(UIParser parser)
	{
		//if (!_parentInConstruct && parent != null)
		{
			if (parent.parent == null) // is root
				writeCode(parser, "addControl(%1$s);");
			else
				writeCode(parser, "%2$s.addControl(%1$s);");
		}
	}

	public void writeChildsCode(UIParser parser)
	{
		for (InstanceDescription child : childs)
			child.writeCode(parser);
	}

	protected void writeCode(UIParser parser, String line)
	{
		parser.appendLine(String.format(line, varName, parent != null ? parent.varName : null));
	}

	public void setConstruct(String[] constructorArgs) throws ParseException
	{
		_constuctCode = "%1$s = new " + resolveConstruct(parent, constructorArgs) + ";";
	}

	protected String resolveConstruct(InstanceDescription parent, String[] args) throws ParseException
	{
		if (args == null) return classInfo.fullClassName() + "()";

		String[] res = new String[args.length];

		for (IMethod c : classInfo.constructors)
		{
			String[] types = c.getParameterTypes();
			if (types.length == 0) continue;

			if (types[0].startsWith("Q") && types[0].endsWith(";") && types.length == 1 + args.length)
			{
				String t0 = types[0];
				t0 = t0.substring(1, t0.length() - 1);
				if (parent.isSubclass(t0))
				{
					// Первый параметр-родитель	
					try
					{
						for (int i = 0; i < args.length; i++)
							res[i] = classInfo.resolve(args[i], types[i + 1]);

						//_parentInConstruct = true;
						return classInfo.fullClassName() + "(%2$s, " + join(res) + ")";
					}
					catch (Exception e)
					{
					}
				}
			}

			// Обычные параметры
			if (types.length != args.length) continue;

			try
			{
				for (int i = 0; i < args.length; i++)
					res[i] = classInfo.resolve(args[i], types[i]);

				return classInfo.fullClassName() + "(" + join(res) + ")";
			}
			catch (Exception e)
			{
			}
		}

		throw new ParseException("Not found constructor for arguments: " + String.join(", ", args));
	}

	public boolean isSubclass(String className)
	{
		return classInfo.isSubclass(className);
	}

	protected String join(String[] args)
	{
		if (args == null)
			return "";
		return String.join(", ", args);
	}
}
