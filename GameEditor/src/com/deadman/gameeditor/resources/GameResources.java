package com.deadman.gameeditor.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.deadman.gameeditor.editors.IBuildProgressListener;

public class GameResources
{
	public final String VERSION = "0.1";

	public static GameResources load(FileEditorInput input, IDocument document) throws Exception
	{
		GameResources res = new GameResources(input, document);
		res.reload();
		return res;
	}

	private IProject project;
	public IJavaProject javaProject;

	private IDocument doc;

	public String directory;
	public String pckg;

	public GameResources(FileEditorInput input, IDocument document)
	{
		project = input.getFile().getProject();
		javaProject = JavaCore.create(project);
		doc = document;
	}

	public void reload() throws Exception
	{
		parseXML(doc.get());

		// Clearing resources
		_loadedXCF.clear();
		_loadedPPR.clear();

		generateSrc();
	}

	private void parseXML(String xml) throws Exception
	{
		if (xml.isEmpty())
			return;

		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setValidating(false);
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		Node root = doc.getFirstChild();

		NamedNodeMap rootAttr = root.getAttributes();
		directory = rootAttr.getNamedItem("path").getNodeValue();
		pckg = rootAttr.getNamedItem("package").getNodeValue();
	}

	private static HashMap<String, XCF> _loadedXCF = new HashMap<>();

	public XCF loadXCF(String fileName)
	{
		XCF file = _loadedXCF.get(fileName);
		if (file == null)
		{
			file = XCF.loadFile(directory + "/" + fileName);
			_loadedXCF.put(fileName, file);
		}
		return file;
	}

	private static HashMap<String, PicturePartCollection> _loadedPPR = new HashMap<>();

	public PicturePartCollection loadPPR(String fileName)
	{
		PicturePartCollection file = _loadedPPR.get(fileName);
		if (file == null)
		{
			file = PicturePartCollection.loadFile(directory + "/" + fileName);
			_loadedPPR.put(fileName, file);
		}
		return file;
	}

	// Обработка ресурсов

	ResourceGroup root;

	private int newResId = 0;

	int genResId()
	{
		return newResId++;
	}

	private void generateSrc() throws CoreException, IOException
	{
		newResId = 0;

		IFolder res = project.getFolder(directory);
		if (!res.exists())
			return;

		root = new ResourceGroup(this, "R");
		root.scan(res);

		IFolder gen = checkFolder(null, "gen");
		IFolder src = checkFolder(gen, "src");
		checkSourceEntry(src.getFullPath());
		IFolder pack = checkFolder(src, pckg.replace('.', '/'));
		saveJava(pack.getFile("R.java"));

		saveCSV(res.getFile("resources.csv"));
	}

	private IFolder checkFolder(IFolder parent, String name) throws CoreException
	{
		if (name.indexOf('/') >= 0)
		{
			String[] parts = name.split("/");
			for (int i = 0; i < parts.length; i++)
			{
				parent = checkFolder(parent, parts[i]);
			}
			return parent;
		}

		IFolder folder;
		if (parent == null)
			folder = project.getFolder(name);
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

		StringBuilder sourceText = new StringBuilder("// Generated by GameEditor V").append(VERSION).append("\r\n\r\n");
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

	IBuildProgressListener progressListener;
	IJavaSearchScope scope;
	SearchEngine searchEngine;
	int totalEntries;
	int scanEntries;
	boolean buildStopped;

	public void build(IBuildProgressListener progressListener)
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
	}

	public void checkCompleted()
	{
		scope = null;
		searchEngine = null;
		progressListener.onBuildCompleted();
		progressListener = null;
	}

	public void onEntryScaned()
	{
		scanEntries++;
		progressListener.onBuildProgressChanged(scanEntries, totalEntries);
	}

	public void stopBuild()
	{
		buildStopped = true;
	}
}