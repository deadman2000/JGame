package com.deadman.gameeditor.ui;

import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import com.deadman.gameeditor.resources.GameResources;

public class UICompiler
{
	public final GameResources resources;

	private HashSet<String> _varPrefixes = new HashSet<>();

	public final ClassStorage<ControlInfo> controls = new ClassStorage<>(ControlInfo.class, this);
	public final ClassStorage<LayoutInfo> layouts = new ClassStorage<>(LayoutInfo.class, this);
	public final ClassStorage<LayoutSettingsInfo> layoutSettings = new ClassStorage<>(LayoutSettingsInfo.class, this);

	public UICompiler(GameResources res)
	{
		resources = res;
	}

	public IProject project()
	{
		return resources.project;
	}

	public IJavaProject javaProject()
	{
		return resources.javaProject;
	}

	public void refresh()
	{
		try
		{
			findControls();
			findLayouts();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public IType getType(String t)
	{
		try
		{
			return javaProject().findType(t);
		}
		catch (JavaModelException e)
		{
			return null;
		}
	}

	private void findControls() throws Exception
	{
		controls.clear();

		IType baseType = javaProject().findType("com.deadman.jgame.ui.Control");
		if (baseType == null)
			throw new Exception();

		controls.register(baseType);
		for (IType t : getSubtypes(baseType))
			controls.register(t);
	}

	private void findLayouts() throws Exception
	{
		layouts.clear();
		layoutSettings.clear();

		IType baseType = javaProject().findType("com.deadman.jgame.ui.Layout");
		if (baseType == null)
			throw new Exception();

		layouts.register(baseType);
		for (IType t : getSubtypes(baseType))
			layouts.register(t);
	}

	public static IType[] getSubtypes(IType type) throws JavaModelException
	{
		ITypeHierarchy hierarhy = type.newTypeHierarchy(new NullProgressMonitor());
		return hierarhy.getAllSubtypes(type);
	}

	public static IType getSuperclass(IType type) throws JavaModelException
	{
		ITypeHierarchy hierarhy = type.newSupertypeHierarchy(new NullProgressMonitor());
		return hierarhy.getSuperclass(type);
	}

	public void compile(IFile file)
	{
		controls.resetVars();
		layouts.resetVars();
		layoutSettings.resetVars();

		UIParser parser = new UIParser(this, file);
		parser.parse();
	}

	public String getVarPrefix(IType type)
	{
		String name = type.getElementName();

		StringBuilder prefixBuild = new StringBuilder();
		for (int i = 0; i < name.length(); i++)
		{
			char c = name.charAt(i);
			if (Character.isUpperCase(c))
				prefixBuild.append(Character.toLowerCase(c));
		}
		String prefix = prefixBuild.toString();

		if (prefix.length() == 0)
			prefix = name;

		String origPrefix = prefix;
		int n = 0;
		while (_varPrefixes.contains(prefix))
		{
			n++;
			prefix = origPrefix + n;
		}

		return prefix;
	}
}
