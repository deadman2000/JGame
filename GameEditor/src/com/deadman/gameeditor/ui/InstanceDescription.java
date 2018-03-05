package com.deadman.gameeditor.ui;

import java.util.ArrayList;

public class InstanceDescription
{
	public final ControlDescription parent;
	public final int deep;
	public final ClassInfo classInfo;
	public String varName = "<not_set>";
	public final ArrayList<InstanceDescription> childs;

	public InstanceDescription(ClassInfo classInfo, ControlDescription parent, int deep)
	{
		this.parent = parent;
		this.deep = deep;
		this.classInfo = classInfo;
		varName = classInfo.generateVarName();

		childs = new ArrayList<>();
		if (parent != null)
			parent.childs.add(this);
	}

	public void setConstruct(String[] constructorArgs) throws ParseException
	{
		String constrCode = classInfo.resolveConstruct(parent, constructorArgs);
		System.out.println(classInfo.className() + " " + varName + " = " + constrCode);
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
