package com.deadman.gameeditor.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public abstract class ClassInfo
{
	private ClassInfo _baseClass;
	private IType _type;
	private HashMap<String, PropertyInfo> _properties;
	public boolean isAbstract;
	protected UICompiler compiler;
	private ArrayList<IMethod> _constructors;

	public void init(UICompiler compiler, IType type)
	{
		this.compiler = compiler;
		_type = type;
		_properties = new HashMap<>();
		_constructors = new ArrayList<>();

		try
		{
			isAbstract = Flags.isAbstract(type.getFlags());

			IType base = UICompiler.getSuperclass(type);
			if (!base.getFullyQualifiedName().equals("java.lang.Object"))
				_baseClass = getStorage().register(base);

			for (IMethod m : _type.getMethods())
			{
				System.out.println(type.getElementName() + "  " + m.getElementName() + "  " + m.getReturnType() + "  " + String.join(", ", m.getParameterTypes()));
				if (m.getElementName().equals(type.getElementName()))
					_constructors.add(m);

				String prop = getPropertyName(m);
				if (prop != null)
				{
					if (prop.isEmpty()) prop = m.getElementName();

					//if (m.getReturnType().equals("V"))
					// TODO Установка как get или set
					_properties.put(prop, new PropertyInfo(m));
				}
			}

			for (IField f : _type.getFields())
			{
				String prop = getPropertyName(f);
				if (prop == null || prop.isEmpty()) prop = f.getElementName();
				_properties.put(prop, new PropertyInfo(f));
			}
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
	}

	public IType type()
	{
		return _type;
	}

	protected abstract ClassStorage<?> getStorage();

	private String getPropertyName(IAnnotatable annotatable) throws JavaModelException
	{
		IAnnotation ann = getAnnotation(annotatable, "Property");
		if (ann != null)
		{
			String value = getMember(ann, "value");
			if (value != null && !value.isEmpty())
				return value;
			return "";
		}
		return null;
	}

	private static String getMember(IAnnotation ann, String name) throws JavaModelException
	{
		for (IMemberValuePair p : ann.getMemberValuePairs())
			if (p.getMemberName().equals(name))
				return p.getValue().toString();
		return null;
	}

	private static IAnnotation getAnnotation(IAnnotatable annotatable, String name) throws JavaModelException
	{
		for (IAnnotation ann : annotatable.getAnnotations())
			if (ann.getElementName().equals(name)) return ann;
		return null;
	}

	public boolean hasProperty(String name)
	{
		if (_properties.containsKey(name)) return true;
		if (_baseClass == null) return false;
		return _baseClass.hasProperty(name);
	}

	public PropertyInfo getProperty(String name)
	{
		return _properties.get(name);
	}

	public boolean hasMethod(String name)
	{
		try
		{
			for (IMethod m : _type.getMethods())
			{
				if (m.getElementName().equals(name))
					return true;
			}
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
		if (_baseClass == null) return false;
		return _baseClass.hasMethod(name);
	}

	private String resolve(String value, String type) throws Exception
	{
		// Число
		/*if (type.equals("D"))
		{
			try
			{
				double d = Double.parseDouble(value);
				return Double.toString(d);
			}
			catch (Exception e)
			{
			}
		}
		else
		*/

		if (type.equals("I")) // Integer
		{
			int i = Integer.parseInt(value);
			return Integer.toString(i);
		}
		else if (type.equals("Z")) // Boolean
		{
			boolean b = Boolean.parseBoolean(value);
			return Boolean.toString(b);
		}
		else if (type.equals("J")) // Long
		{
			long l = Long.parseLong(value);
			return Long.toString(l);
		}
		else if (type.equals("QString;"))
		{
			if (value.startsWith("\"") && value.endsWith("\"")) return value;
			return "\"" + value + "\"";
		}
		else
			System.err.println("Unknown type " + type);

		// Строка
		// TODO

		// Статичное поле класса
		try
		{
			IField f = _type.getField(value);
			if (f.exists() && Flags.isStatic(f.getFlags()))
				return _type.getElementName() + "." + f.getElementName();
		}
		catch (JavaModelException e)
		{
		}

		throw new Exception();
	}

	public String[] resolveConstruct(String[] args) throws ParseException
	{
		if (args == null) return args;

		String[] res = new String[args.length];

		for (IMethod c : _constructors)
		{
			if (c.getParameterTypes().length != args.length) continue;

			try
			{
				for (int i = 0; i < args.length; i++)
					res[i] = resolve(args[i], c.getParameterTypes()[i]);

				return res;
			}
			catch (Exception e)
			{
			}
		}

		throw new ParseException("Not found constructor for arguments: " + String.join(", ", args));
	}

	public String resolvePropertySet(String name, String value) throws ParseException
	{
		PropertyInfo prop = getProperty(name);

		ArrayList<IMethod> setters = prop.getSetters();
		for (IMethod m : setters)
		{
			if (m.getParameterTypes().length != 1) continue;

			try
			{
				return resolve(value, m.getParameterTypes()[0]);
			}
			catch (Exception e)
			{
			}
		}

		if (_baseClass != null)
			return _baseClass.resolvePropertySet(name, value);

		throw new ParseException("Not found property " + name + " for value " + value);
	}

	public String[] resolveCall(String name, String[] args) throws ParseException
	{
		String[] res = new String[args.length];

		try
		{
			for (IMethod m : _type.getMethods())
			{
				if (!m.getElementName().equals(name)) continue;
				if (m.getParameterTypes().length != args.length) continue;

				try
				{
					for (int i = 0; i < args.length; i++)
						res[i] = resolve(args[i], m.getParameterTypes()[i]);

					return res;
				}
				catch (Exception e)
				{
				}
			}
		}
		catch (JavaModelException e)
		{
		}

		if (_baseClass != null)
			return _baseClass.resolveCall(name, args);

		throw new ParseException("Not found method " + name + "(" + String.join(", ", args + ")"));
	}
}
