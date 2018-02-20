package com.deadman.gameeditor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import com.deadman.gameeditor.resources.GameResources;

public class StartupClass implements IStartup
{
	@Override
	public void earlyStartup()
	{
		System.out.println("Build all projects");
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
		{
			GameResources.build(project);
		}
	}
}
