package com.deadman.gameeditor.resources;

import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

public class ResourceChangeReporter implements IResourceChangeListener
{
	private HashSet<IProject> toRebuild = new HashSet<>();

	@Override
	public void resourceChanged(IResourceChangeEvent event)
	{
		switch (event.getType())
		{
			case IResourceChangeEvent.POST_CHANGE:
				IResourceDelta mainDelta = event.getDelta();
				if (mainDelta == null)
					return;

				IResourceDelta[] children = mainDelta.getAffectedChildren();
				for (int i = 0; i < children.length; i++)
				{
					IResourceDelta delta = children[i];
					try
					{
						delta.accept(visitor);
					}
					catch (CoreException e)
					{
						e.printStackTrace();
					}
				}
				break;
			default:
				System.out.println("resourceChanged " + event.getType());
				break;
		}

		for (IProject project : toRebuild)
		{
			GameResources.build(project);
		}
		toRebuild.clear();
	}

	static String kindName(int kind)
	{
		switch (kind)
		{
			case IResourceDelta.ADDED:
				return "ADDED";
			case IResourceDelta.REMOVED:
				return "REMOVED";
			case IResourceDelta.CHANGED:
				return "CHANGED";
			case IResourceDelta.ADDED_PHANTOM:
				return "ADDED_PHANTOM";
			case IResourceDelta.REMOVED_PHANTOM:
				return "REMOVED_PHANTOM";
			default:
				return "Kind " + kind;
		}
	}

	static String typeName(int type)
	{
		switch (type)
		{
			case IResource.FILE:
				return "FILE";
			case IResource.FOLDER:
				return "FOLDER";
			case IResource.PROJECT:
				return "PROJECT";
			case IResource.ROOT:
				return "ROOT";
			default:
				return "Type " + type;
		}
	}

	IResourceDeltaVisitor visitor = new IResourceDeltaVisitor()
	{
		public boolean visit(IResourceDelta delta)
		{
			//System.out.println(kindName(delta.getKind()) + " " + typeName(resource.getType()) + " " + resource.getName() + " Flags: " + delta.getFlags());

			try
			{
				IResource resource = delta.getResource();

				if (delta.getKind() == IResourceDelta.CHANGED)
				{
					if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
						return true;

					if (resource.getType() == IResource.FILE)
					{
						IFile file = (IFile) resource;

						if (resource.getName().equals("resources.xml"))
						{
							toRebuild.add(resource.getProject());
						}
						else if (resource.getFileExtension().equals("ui"))
						{
							GameResources.compileUI(file);
						}
						else if (resource.getFileExtension().equals("class") )
						{
							GameResources.invalidateModel(file.getProject());
						}
						else if (GameResources.contains(file))
						{
							System.out.println("Resource changed " + resource);
							toRebuild.add(resource.getProject());
						}
					}
				}
				else if (delta.getKind() == IResourceDelta.REMOVED)
				{
					if (resource.getType() == IResource.FOLDER && resource.getName() == "gen")
					{
						toRebuild.add(resource.getProject());
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return true;
		}
	};
}
