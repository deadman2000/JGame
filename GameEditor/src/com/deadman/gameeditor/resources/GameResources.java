package com.deadman.gameeditor.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import com.deadman.gameeditor.ui.UICompiler;

public class GameResources
{
	private static final QualifiedName resourceKey = new QualifiedName("com.deadman", "gameresources");

	private static GameResources getRes(IProject project)
	{
		try
		{
			return (GameResources) project.getSessionProperty(resourceKey);
		}
		catch (CoreException e)
		{
			return null;
		}
	}

	public static void build(IProject project)
	{
		GameResources res = getRes(project);
		if (res == null)
		{
			res = new GameResources(project);
			try
			{
				project.setSessionProperty(resourceKey, res);
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
		}

		res.schedule();
	}

	public static boolean contains(IFile file)
	{
		GameResources res = getRes(file.getProject());
		if (res == null) return false;
		return res.containsFile(file);
	}

	public final IProject project;
	private IFile _resourceFile;

	public IJavaProject javaProject;

	public IFolder res;
	public String pckg;

	private UICompiler uiCompiler;

	private ResourceGroup root;

	private GameResources(IProject project)
	{
		this.project = project;
		_resourceFile = project.getFile("/resources.xml");
		javaProject = JavaCore.create(project);

		uiCompiler = new UICompiler(this);
		uiCompiler.refresh();

		try
		{
			parseXML();
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}

	private boolean containsFile(IFile file)
	{
		IPath filePath = file.getProjectRelativePath();
		IPath resPath = res.getProjectRelativePath();
		if (!resPath.isPrefixOf(filePath)) return false;

		return root.contains(filePath.toString());
	}

	private void schedule()
	{
		WorkspaceJob job = new WorkspaceJob("Generating resource")
		{
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
			{
				monitor.beginTask("Building", 1);

				try
				{
					build();
				}
				catch (Exception e)
				{
					System.err.println("Build failed:");
					e.printStackTrace();
				}
				monitor.worked(1);
				return Status.OK_STATUS;
			}
		};
		job.setRule(project.getWorkspace().getRoot());
		job.schedule();
	}

	private void build() throws Exception
	{
		System.out.println(project.getName() + " Building...");

		if (!_resourceFile.exists())
			return;

		parseXML();

		if (!res.exists())
			return;

		newResId = 0;
		root = new ResourceGroup(this, "R");
		root.scan(res);

		IFolder gen = checkFolder(null, "gen");
		IFolder src = checkFolder(gen, "src");
		checkSourceEntry(src.getFullPath());
		IFolder pack = checkFolder(src, pckg.replace('.', '/'));
		saveJava(pack.getFile("R.java"));

		saveCSV(gen.getFile("resources.csv"));

		System.out.println("Build complete");
	}

	private void parseXML() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setValidating(false);
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.parse(_resourceFile.getContents());
		NamedNodeMap rootAttr = doc.getFirstChild().getAttributes();
		res = _resourceFile.getProject().getFolder(rootAttr.getNamedItem("path").getNodeValue());
		pckg = rootAttr.getNamedItem("package").getNodeValue();
	}

	// Обработка ресурсов

	private int newResId = 0;

	int genResId()
	{
		return newResId++;
	}

	private IFolder checkFolder(IFolder parent, String name) throws CoreException
	{
		if (name.indexOf('/') >= 0)
		{
			String[] parts = name.split("/");
			for (int i = 0; i < parts.length; i++)
				parent = checkFolder(parent, parts[i]);
			return parent;
		}

		IFolder folder;
		if (parent == null)
			folder = _resourceFile.getProject().getFolder(name);
		else
			folder = parent.getFolder(name);
		if (!folder.exists())
		{
			System.out.println("Creating folder " + folder);
			folder.create(true, true, null);
		}
		return folder;
	}

	private void checkSourceEntry(IPath path) throws CoreException
	{
		for (IClasspathEntry entry : javaProject.getRawClasspath())
		{
			if (entry.getPath().equals(path)) { return; }
		}

		System.out.println("Add gen src: " + path);
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject.getRawClasspath()));
		list.add(JavaCore.newSourceEntry(path));

		IClasspathEntry[] entries = list.toArray(new IClasspathEntry[list.size()]);
		javaProject.setRawClasspath(entries, null);
	}

	public void saveJava(IFile file) throws CoreException
	{
		if (file.exists())
			file.delete(true, null);

		StringBuilder sourceText = new StringBuilder("// Generated by GameEditor v").append(Platform.getBundle("com.deadman.gameeditor").getVersion()).append("\r\n\r\n");
		sourceText.append("package ").append(pckg).append(";\r\n");
		root.writeJava(sourceText);

		byte[] bytes = sourceText.toString().getBytes();
		InputStream source = new ByteArrayInputStream(bytes);
		file.create(source, IResource.NONE, null);
	}

	public void saveCSV(IFile file) throws CoreException
	{
		if (file.exists())
			file.delete(true, null);

		StringBuilder sourceText = new StringBuilder();
		root.writeCSV(sourceText);

		byte[] bytes = sourceText.toString().getBytes();
		InputStream source = new ByteArrayInputStream(bytes);
		file.create(source, IResource.NONE, null);
	}

	IJavaSearchScope scope;
	SearchEngine searchEngine;
	int totalEntries;
	int scanEntries;
	boolean buildStopped;

	/*public void build(IBuildProgressListener progressListener)
	{
		this.progressListener = progressListener;
		buildStopped = false;
	
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				scope = SearchEngine.createWorkspaceScope();
				searchEngine = new SearchEngine();
				totalEntries = 0;
	
				root.calcEntries();
				root.checkUsages();
			}
		}).start();
	}*/

	public void checkCompleted()
	{
		scope = null;
		searchEngine = null;
		//progressListener.onBuildCompleted();
		//progressListener = null;
	}

	public void onEntryScaned()
	{
		scanEntries++;
		//progressListener.onBuildProgressChanged(scanEntries, totalEntries);
	}

	public void stopBuild()
	{
		buildStopped = true;
	}

	// XCF Cache

	private HashMap<IFile, XCF> xcfCache = new HashMap<>();

	public XCF getXCF(IFile file)
	{
		boolean changed = FileChanges.isChanged(file);
		XCF xcf = xcfCache.get(file);
		if (xcf != null && changed) xcf = null;
		if (xcf == null)
		{
			xcf = XCF.loadFile(file.getRawLocation().toString());
			xcfCache.put(file, xcf);
		}
		return xcf;
	}

	// UI Compiling

	public void addUI(IFile file)
	{
		uiCompiler.compile(file);
	}

	public void scheduleCompileUI(IFile file)
	{
		WorkspaceJob job = new WorkspaceJob("Generating ui")
		{
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
			{
				monitor.beginTask("Parsing", 1);

				try
				{
					uiCompiler.compile(file);
				}
				catch (Exception e)
				{
					System.err.println("Parsing failed:");
					e.printStackTrace();
				}
				monitor.worked(1);
				return Status.OK_STATUS;
			}
		};
		job.setRule(project.getWorkspace().getRoot());
		job.schedule();
	}

	public static void compileUI(IFile file)
	{
		GameResources res = getRes(file.getProject());
		if (res != null)
		{
			res.scheduleCompileUI(file);
		}
	}

	public static void invalidateModel(IProject project)
	{
		GameResources res = getRes(project);
		if (res != null)
		{
			res.javaProject = JavaCore.create(project);
		}
	}
}
