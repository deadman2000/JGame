package com.deadman.gameeditor.ui;

import java.util.ArrayList;

public class InstanceDescription
{
	public final ControlDescription parent;
	public final int deep;
	public final ClassInfo classInfo;
	public String varName;
	public final ArrayList<InstanceDescription> childs;

	private String _constuctCode;
	private ArrayList<String> _code;

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

	protected void addLine(String code)
	{
		_code.add(code);
	}

	public void writeCode(UIParser parser, int indent)
	{
		if (varName == null)
			varName = classInfo.generateVarName();

		if (_constuctCode != null)
			appendCode(parser, indent, _constuctCode);
		else
			appendCode(parser, indent, "%1$s = " + classInfo.className() + "();");

		for (String line : _code)
			appendCode(parser, indent, line);

		if (childs.size() > 0)
		{
			parser.appendLine(0, "");
			for (InstanceDescription child : childs)
				child.writeCode(parser, indent);
		}
	}

	private void appendCode(UIParser parser, int indent, String line)
	{
		parser.appendLine(indent, String.format(line, varName, parent != null ? parent.varName : null));
	}

	public void setConstruct(String[] constructorArgs) throws ParseException
	{
		_constuctCode = "%1$s = " + classInfo.resolveConstruct(parent, constructorArgs) + ";";
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
