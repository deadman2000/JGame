package com.deadman.gameeditor.ui;

import java.util.ArrayList;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;

public class PropertyInfo
{
	private ClassInfo _classInfo;
	private String _name;
	private ArrayList<IField> _fields;
	private ArrayList<IMethod> _methods;

	public PropertyInfo(ClassInfo ci, String name)
	{
		_classInfo = ci;
		_name = name;
	}

	public void addField(IField f)
	{
		if (_fields == null) _fields = new ArrayList<>();
		_fields.add(f);
	}

	public void addMethod(IMethod m)
	{
		if (_methods == null) _methods = new ArrayList<>();
		_methods.add(m);
	}

	public String resolveSet(String value) throws ParseException
	{
		String v;

		if (_fields != null)
		{
			for (IField f : _fields)
			{
				try
				{
					//v = _classInfo.compiler.stringToType(value, f.getTypeSignature());
					v = _classInfo.resolve(value, f.getTypeSignature());
					if (v != null)
						return f.getElementName() + " = " + v;
				}
				catch (Exception e)
				{
				}
			}
		}

		if (_methods != null)
		{
			for (IMethod m : _methods)
			{
				if (m.getParameterTypes().length != 1) continue;

				try
				{
					//v = _classInfo.compiler.stringToType(value, m.getParameterTypes()[0]);
					v = _classInfo.resolve(value, m.getParameterTypes()[0]);
					if (v != null)
						return m.getElementName() + "(" + v + ")";
				}
				catch (Exception e)
				{
				}
			}
		}
		throw new ParseException("Property " + _name + " not resolved for value " + value);
	}
}
