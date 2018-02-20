package com.deadman.gameeditor.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class UICompiler
{
	private IJavaProject javaProject;

	public final ClassStorage<ControlInfo> controls = new ClassStorage<>(ControlInfo.class, this);
	public final ClassStorage<LayoutInfo> layouts = new ClassStorage<>(LayoutInfo.class, this);
	public final ClassStorage<LayoutSettingsInfo> layoutSettings = new ClassStorage<>(LayoutSettingsInfo.class, this);

	public UICompiler(IProject project)
	{
		javaProject = JavaCore.create(project);
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

	private void findControls() throws Exception
	{
		controls.clear();
		
		IType baseType = javaProject.findType("com.deadman.jgame.ui.Control");
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
		
		IType baseType = javaProject.findType("com.deadman.jgame.ui.Layout");
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
		UIParser parser = new UIParser(this);
		parser.parse(file);
	}
}
