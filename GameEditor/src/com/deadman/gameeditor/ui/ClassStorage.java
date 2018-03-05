package com.deadman.gameeditor.ui;

import java.util.HashMap;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class ClassStorage<E extends ClassInfo>
{
	private Class<E> _clazz;
	private HashMap<String, E> _map = new HashMap<>();
	private UICompiler _compiler;

	public ClassStorage(Class<E> clazz, UICompiler compiler)
	{
		_clazz = clazz;
		_compiler = compiler;
	}

	public void clear()
	{
		_map.clear();
	}

	private E createInstance()
	{
		try
		{
			return _clazz.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			return null;
		}
	}

	public E register(IType t) throws JavaModelException
	{
		E ci = _map.get(t.getElementName());
		if (ci != null) return ci;

		ci = createInstance();
		ci.init(_compiler, t);
		_map.put(t.getElementName(), ci);
		return ci;
	}

	public E get(String name)
	{
		return _map.get(name);
	}

	public void resetVars()
	{
		for (ClassInfo c : _map.values())
			c.resetVars();
	}
}