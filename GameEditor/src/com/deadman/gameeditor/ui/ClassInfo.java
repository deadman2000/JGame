package com.deadman.gameeditor.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.deadman.gameeditor.resources.ResourceEntry;

public abstract class ClassInfo
{
	private ClassInfo _baseClass;
	private IType _type;
	private HashMap<String, PropertyInfo> _properties;
	public boolean isAbstract;
	protected UICompiler compiler;
	public ArrayList<IMethod> constructors;

	String _varPrefix;
	int _varCount = 0;

	public void init(UICompiler compiler, IType type)
	{
		this.compiler = compiler;
		_type = type;
		_properties = new HashMap<>();
		constructors = new ArrayList<>();

		_varPrefix = compiler.getVarPrefix(type);

		try
		{
			isAbstract = Flags.isAbstract(type.getFlags());

			IType base = UICompiler.getSuperclass(type);
			if (!base.getFullyQualifiedName().equals("java.lang.Object"))
				_baseClass = getStorage().register(base);

			for (IMethod m : _type.getMethods())
			{
				//System.out.println(type.getElementName() + "  " + m.getElementName() + "  " + m.getReturnType() + "  " + String.join(", ", m.getParameterTypes()));

				if (m.getElementName().equals(type.getElementName()))
					constructors.add(m);

				String prop = getPropertyName(m);
				if (prop != null)
				{
					if (prop.isEmpty()) prop = m.getElementName();

					// TODO Проверка на возвращаемый тип и аргументы
					//if (m.getReturnType().equals("V"))

					PropertyInfo pi = createProperty(prop);
					pi.addMethod(m);
				}
			}

			for (IField f : _type.getFields())
			{
				String prop = getPropertyName(f);
				if (prop == null || prop.isEmpty()) prop = f.getElementName();

				PropertyInfo pi = createProperty(prop);
				pi.addField(f);
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

	public String className()
	{
		return _type.getElementName();
	}

	public String fullClassName()
	{
		return _type.getFullyQualifiedName();
	}

	protected abstract ClassStorage<?> getStorage();

	private String getPropertyName(IAnnotatable annotatable) throws JavaModelException
	{
		return getAnnotationValue(annotatable, "Property", "");
	}

	protected String getAnnotationValue(IAnnotatable annotatable, String name, String def) throws JavaModelException
	{
		IAnnotation ann = getAnnotation(annotatable, name);
		if (ann != null)
		{
			String value = getMember(ann, "value");
			if (value != null && !value.isEmpty())
				return value;
			return def;
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

	private PropertyInfo createProperty(String name)
	{
		PropertyInfo pi = _properties.get(name);
		if (pi == null)
			_properties.put(name, pi = new PropertyInfo(this, name));
		return pi;
	}

	public boolean hasProperty(String name)
	{
		if (_properties.containsKey(name)) return true;
		if (_baseClass == null) return false;
		return _baseClass.hasProperty(name);
	}

	public PropertyInfo getProperty(String name)
	{
		PropertyInfo info = _properties.get(name);
		if (info != null)
			return info;
		return _baseClass.getProperty(name);
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

	static final Pattern fontPattern = Pattern.compile("^(?<name>\\w+?)(?<fntcol>(_[a-fA-F0-9]{6,8})+?)?(?<ol>_ol_[a-fA-F0-9]{6,8})?(?<sh>_sh_[a-fA-F0-9]{6,8})?$");

	public String resolve(String value, String type) throws Exception
	{
		// Пытаемся перевести в шрифт
		if (type.equals("QGameFont;"))
		{
			Matcher m = fontPattern.matcher(value);
			if (m.find())
			{
				String name = m.group("name");
				ResourceEntry e = compiler.resources.getFont(name);
				if (e != null)
				{
					StringBuilder call = new StringBuilder("getFont(").append(e.fullName());
					String fntcol = m.group("fntcol");
					if (fntcol != null)
					{
						String[] colors = fntcol.split("_");
						for (int i = 1; i < colors.length; i++)
						{
							call.append(", 0x");
							String c = colors[i];
							if (c.length() == 6) call.append("ff");
							call.append(c);
						}
					}

					return call.append(")").toString();
				}
				System.out.println("Font not found " + name);
			}
		}

		// Ищем статичное поле класса
		try
		{
			IField f = _type.getField(value);
			if (f.exists() && Flags.isStatic(f.getFlags()))
				return fullClassName() + "." + f.getElementName();
		}
		catch (JavaModelException e)
		{
		}

		// Пытаемся преобразовать в базовый тип
		try
		{
			String v = stringToType(value, type);
			if (v != null)
				return v;
		}
		catch (Exception e)
		{
		}

		if (value.startsWith("R."))
		{
			int i = value.lastIndexOf('.');
			String typeName = value.substring(0, i);
			IType t = compiler.getType(compiler.resources.pckg + "." + typeName);
			if (t != null)
			{
				String propName = value.substring(i + 1);
				IField f = t.getField(propName);
				if (f != null)
				{
					value = t.getFullyQualifiedName().replace('$', '.') + "." + f.getElementName();
					//System.out.println("Field found " + value);

					if (type.equals("QDrawable;"))
						return "getDrawable(" + value + ")";
					else if (type.equals("I"))
						return value;

					System.err.println("Type not found " + type);
				}
			}
		}

		throw new Exception();
	}

	public String stringToType(String value, String type) throws Exception
	{
		// Число
		if (type.equals("D"))
		{
			double d = Double.parseDouble(value);
			return Double.toString(d);
		}
		else if (type.equals("I")) // Integer
		{
			if (value.startsWith("0x"))
			{
				int i = Integer.parseInt(value.substring(2), 16);
				return Integer.toString(i);
			}
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
			return "\"" + value.replace("\"", "\\\"") + "\"";
		}
		else
			return null;
	}

	// Возвращает строку кода для установки значения свойства
	public String resolvePropertySet(String name, String value) throws ParseException
	{
		PropertyInfo prop = getProperty(name);
		if (prop == null)
			throw new ParseException("Not found property " + name + " for value " + value);

		return prop.resolveSet(value);
	}

	protected String join(String[] args)
	{
		if (args == null)
			return "";
		return String.join(", ", args);
	}

	public boolean isSubclass(String className)
	{
		return _type.getElementName().equals(className) || (_baseClass != null && _baseClass.isSubclass(className));
	}

	public String resolveCall(String name, String[] args) throws ParseException
	{
		try
		{
			if (args == null || args.length == 0) // Ищем метод без аргументов
			{
				for (IMethod m : _type.getMethods())
				{
					if (!m.getElementName().equals(name)) continue;
					if (m.getParameterTypes().length == 0)
						return m.getElementName() + "()";
				}

				if (_baseClass != null)
					return _baseClass.resolveCall(name, args);
				throw new ParseException("Method not found " + name + "()");
			}

			String[] res = new String[args.length];

			for (IMethod m : _type.getMethods())
			{
				if (!m.getElementName().equals(name)) continue;
				if (m.getParameterTypes().length != args.length) continue;

				try
				{
					for (int i = 0; i < args.length; i++)
						res[i] = resolve(args[i], m.getParameterTypes()[i]);

					return m.getElementName() + "(" + join(res) + ")";
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

		throw new ParseException("Method not found " + name + "(" + String.join(", ", args + ")"));
	}

	public String generateVarName()
	{
		return _varPrefix + ++_varCount;
	}

	public void resetVars()
	{
		_varCount = 0;
	}
}
