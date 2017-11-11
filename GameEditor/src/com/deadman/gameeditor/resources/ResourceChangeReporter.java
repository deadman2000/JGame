package com.deadman.gameeditor.resources;

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
	@Override
	public void resourceChanged(IResourceChangeEvent event)
	{
		//System.out.println("Resource changed: " + event);

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
	}

	IResourceDeltaVisitor visitor = new IResourceDeltaVisitor()
	{
		public boolean visit(IResourceDelta delta)
		{
			//only interested in changed resources (not added or removed)
			if (delta.getKind() != IResourceDelta.CHANGED)
				return true;
			//only interested in content changes
			if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
				return true;

			IResource resource = delta.getResource();
			if (resource.getType() == IResource.FILE) //  && "xcf".equalsIgnoreCase(resource.getFileExtension())
			{
				//System.out.println("Changed " + resource);

				IProject proj = resource.getProject();

				IFile f = proj.getFile("/resources.xml");
				if (f.exists())
				{
					//System.out.println("File:" + f);

					//IJavaProject jproj = JavaCore.create(proj);
					//System.out.println("JavaProject:" + jproj);
				}
			}
			return true;
		}
	};
}
