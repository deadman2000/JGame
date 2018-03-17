package com.deadman.gameeditor.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

class ResourceGroup
{
	public final GameResources resources;
	public final String name;
	public final String className;
	public final ResourceGroup parent;
	public String path;
	public HashMap<String, ResourceGroup> groups = new HashMap<>();
	public HashMap<String, ResourceEntry> fields = new HashMap<>();
	public boolean used = false;

	public ArrayList<ResourceEntry> entries = new ArrayList<>();

	public ResourceGroup(GameResources res, String n)
	{
		this.resources = res;
		name = n;
		className = res.pckg + "." + n;
		parent = null;
	}

	public ResourceGroup(ResourceGroup g, String n)
	{
		this.resources = g.resources;
		name = n;
		parent = g;
		className = g.className + "." + n;
	}

	public void setPath(IResource res)
	{
		path = res.getProjectRelativePath().toString();
	}

	public void writeJava(StringBuilder str)
	{
		if (groups.size() == 0 && entries.size() == 0)
			return;

		str.append("public ");
		if (parent != null)
			str.append("static ");
		str.append("final class ").append(name).append(" {\r\n");

		ArrayList<ResourceGroup> groupList = new ArrayList<>(groups.values());
		Collections.sort(groupList, new Comparator<ResourceGroup>()
		{
			@Override
			public int compare(ResourceGroup o1, ResourceGroup o2)
			{
				return o1.name.compareToIgnoreCase(o2.name);
			}
		});

		for (ResourceGroup g : groupList)
		{
			g.writeJava(str);
		}

		Collections.sort(entries, new Comparator<ResourceEntry>()
		{
			@Override
			public int compare(ResourceEntry o1, ResourceEntry o2)
			{
				return o1.name.compareToIgnoreCase(o2.name);
			}
		});
		for (ResourceEntry e : entries)
		{
			e.writeJava(str);
		}

		str.append("}\r\n");
	}

	void writeCSV(StringBuilder str)
	{
		for (ResourceGroup g : groups.values())
		{
			g.writeCSV(str);
		}

		for (ResourceEntry e : entries)
		{
			if (e.type != ResourceEntry.ARRAY)
			{
				e.writeCSV(str);
				str.append("\r\n");
			}
		}
	}

	private ResourceEntry addEntry(ResourceEntry ent)
	{
		entries.add(ent);
		fields.put(ent.name, ent);
		return ent;
	}

	public void scan(IFolder folder) throws CoreException
	{
		IResource[] members = folder.members();
		for (IResource res : members)
		{
			switch (res.getType())
			{
				case IResource.FOLDER:
					addFolder((IFolder) res);
					break;
				case IResource.FILE:
					addFile((IFile) res);
					break;
			}
		}
	}

	private void addFolder(IFolder res) throws CoreException
	{
		if (res.getName().equals("fonts"))
		{
			addFontsFolder(res);
			return;
		}

		if (res.getName().endsWith("[]"))
		{
			addFolderArray(res);
			return;
		}

		String entryName = toFieldName(res.getName(), false);
		if (entryName == null)
			return;

		ResourceGroup group = createSubGroup(entryName);
		group.scan(res);
	}

	private void addFolderArray(IFolder res) throws CoreException
	{
		String entryName = res.getName();
		entryName = entryName.substring(0, entryName.length() - 2);
		entryName = toFieldName(entryName, true);
		if (entryName == null)
			return;

		ResourceGroup group = createSubGroup(entryName + "_arr");
		group.scan(res);

		HashMap<Integer, ResourceEntry> childs = new HashMap<>();

		for (ResourceEntry e : group.entries)
		{
			String indStr = e.name.substring(1);
			int s = indStr.indexOf('_');
			if (s > 0)
				indStr = indStr.substring(0, s);

			try
			{
				Integer ind = Integer.parseInt(indStr);
				childs.put(ind, e);
			}
			catch (Exception ex)
			{
			}
		}

		if (childs.size() > 0)
		{
			addEntry(new ArrayEntry(this, entryName, toArray(childs)));
		}
	}

	private ResourceEntry addFile(IFile res)
	{
		switch (res.getFileExtension())
		{
			case "xcf":
				addGimp(res);
				return null;
			case "png":
				return createEntry(ResourceEntry.PICTURE, getOnlyName(res), res);
			case "ppr":
				return addPpr(res);
			case "ui":
				resources.addUI(res);
				return null;
			default:
				return addOther(res);
		}
	}

	private void addGimp(IFile res)
	{
		String name = toFieldName(getOnlyName(res), false);
		if (name != null)
		{
			XCF xcf = resources.getXCF(res);
			if (xcf != null)
			{
				ResourceGroup group = createSubGroup(name);
				group.setPath(res);
				group.scan(xcf);
			}
		}
	}

	private ResourceEntry addPpr(IFile file)
	{
		String entryName = file.getName();
		entryName = toFieldName(entryName, true);
		if (entryName == null)
			return null;

		return addEntry(PicPartEntry.fromPpr(this, entryName, file));
	}

	private ResourceEntry addOther(IFile res)
	{
		return createEntry(ResourceEntry.FILE, res.getName(), res);
	}

	private XCF _xcf;

	public void scan(XCF xcf)
	{
		_xcf = xcf;
		for (Layer l : xcf.getLayers())
		{
			if (l.layers == null)
				addLayer(l);
			else
			{
				if (l.name.endsWith("[]"))
				{
					addLayerArray(l);
				}
				else
				{
					AnimationImages a = l.getAnimation();
					if (a != null)
					{
						addLayer(l); // TODO отдельно для анимации свой тип
					}
				}
			}
		}
	}

	private void addLayerArray(Layer layer)
	{
		String arrayName = layer.name.substring(0, layer.name.length() - 2);
		String entryName = toFieldName(arrayName, true);

		HashMap<Integer, ResourceEntry> arr = new HashMap<>();
		for (Layer sl : layer.layers)
		{
			if (sl.name.startsWith(arrayName) && sl.name.length() > arrayName.length() + 1)
			{
				String ind = sl.name.substring(arrayName.length() + 1, sl.name.length());
				try
				{
					int i = Integer.parseInt(ind);
					ResourceEntry layerEntry = addLayer(sl);
					if (layerEntry == null)
						System.out.println("No entry for " + sl.name);
					arr.put(i, layerEntry);
				}
				catch (Exception e)
				{
				}
			}
		}

		if (arr.size() > 0)
		{
			addEntry(new ArrayEntry(this, entryName, toArray(arr)));
		}
	}

	private static ResourceEntry[] toArray(HashMap<Integer, ResourceEntry> map)
	{
		ArrayList<ResourceEntry> list = new ArrayList<>();
		for (HashMap.Entry<Integer, ResourceEntry> entry : map.entrySet())
		{
			int i = entry.getKey();
			while (list.size() <= i)
				list.add(null);
			list.set(i, entry.getValue());
		}
		return list.toArray(new ResourceEntry[list.size()]);
	}

	private ResourceEntry createEntry(int type, String entryName, IResource path)
	{
		return createEntry(type, entryName, path.getProjectRelativePath().toString());
	}

	private ResourceEntry createEntry(int type, String entryName, String path)
	{
		entryName = toFieldName(entryName, true);
		if (entryName == null)
			return null;

		return addEntry(new ResourceEntry(this, type, entryName, path));
	}

	private HashMap<Layer, ResourceEntry> _layers;

	private ResourceEntry addLayer(Layer layer)
	{
		if (layer.name.startsWith("_"))
			return null;

		if (_layers != null)
		{
			ResourceEntry e = _layers.get(layer);
			if (e != null) return e;
		}

		String entryName = toFieldName(layer.name, true);
		if (entryName == null)
		{
			System.out.println("No field name " + layer.name);
			return null;
		}

		ResourceEntry entry;
		if (layer.name.endsWith("-9p"))
		{
			String sourceName = layer.name.substring(0, layer.name.length() - 3);
			Layer source = _xcf.getLayer(sourceName);
			if (source != null)
			{
				addLayer(source);

				entry = addEntry(PicPartEntry.fromLayers(this, entryName, path + "?" + source.name, layer, source));
			}
			else
			{
				System.out.println("Not implemented");
				entry = null;
				//_xcf.getLayer(sourceName);
			}
		}
		else
			entry = addEntry(new LayerEntry(this, entryName, path, layer));

		if (_layers == null)
			_layers = new HashMap<>();
		_layers.put(layer, entry);
		return entry;
	}

	// Font

	private void addFontsFolder(IFolder res) throws CoreException
	{
		ResourceGroup group = createSubGroup(res.getName());
		group.scanFonts(res);
	}

	private void scanFonts(IFolder folder) throws CoreException
	{
		IResource[] members = folder.members();
		for (IResource res : members)
		{
			switch (res.getType())
			{
				case IResource.FOLDER:
					addFont((IFolder) res);
					break;
				case IResource.FILE:
					addFont((IFile) res);
					break;
			}
		}
	}

	private void addFont(IFolder res)
	{
		createEntry(ResourceEntry.FONT, res.getName(), res);
	}

	private void addFont(IFile res)
	{
		switch (res.getFileExtension())
		{
			case "xcf":
				createEntry(ResourceEntry.FONT, getOnlyName(res), res);
				break;
			default:
				addFile(res);
				break;
		}
	}

	public ResourceGroup createSubGroup(String n)
	{
		ResourceGroup child = groups.get(n);
		if (child == null)
		{
			if (fields.get(name) instanceof ResourceEntry)
			{
				System.out.println("Duplicate resource class name: " + n);
				return null;
			}

			child = new ResourceGroup(this, n);
			groups.put(n, child);
		}
		return child;
	}

	private String toFieldName(String n, boolean isField)
	{
		if (isJavaKeyword(n))
			n = '_' + n;

		if (!Character.isJavaIdentifierStart(n.charAt(0)))
			n = '_' + n;

		for (int i = 1; i < n.length(); i++)
		{
			if (!Character.isJavaIdentifierPart(n.charAt(i)))
				n = setChar(n, i, '_');
		}

		if (n.equals("_"))
			n = '_' + n;

		if (fields.containsKey(n))
		{
			System.out.println("Duplicate field names: " + n);
			return null; // TODO Добавление числа в имя
		}
		if (isField && groups.containsKey(n))
		{
			System.out.println("Duplicate group names: " + n);
			return null;
		}

		return n;
	}

	static String setChar(String str, int pos, char s)
	{
		StringBuilder sb = new StringBuilder(str);
		sb.setCharAt(pos, s);
		return sb.toString();
	}

	static String getOnlyName(IResource file)
	{
		String ext = file.getFileExtension();
		if (ext.length() == 0)
			return file.getName();
		return file.getName().substring(0, file.getName().length() - ext.length() - 1);
	}

	static final String keywords[] = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends", "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while" };

	static boolean isJavaKeyword(String keyword)
	{
		return (Arrays.binarySearch(keywords, keyword) >= 0);
	}

	public void calcEntries()
	{
		resources.totalEntries += fields.size();
		for (ResourceGroup g : groups.values())
			g.calcEntries();
	}

	private IType _javaClass;
	private int _searchIndex;
	private ResourceEntry[] _allEntries;
	private ResourceGroup[] _allGroups;

	public void checkUsages()
	{
		//System.out.println("Scan group " + className);
		used = false;
		try
		{
			_javaClass = resources.javaProject.findType(className);
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
			return;
		}
		if (!_javaClass.exists()) return;

		_allEntries = fields.values().toArray(new ResourceEntry[] {});
		_searchIndex = 0;
		findNextEntry();
	}

	private void findNext()
	{
		if (resources.buildStopped)
		{
			_allEntries = null;
			_allGroups = null;
			_javaClass = null;

			if (parent != null)
				parent.findNext();
			else
				resources.checkCompleted();
			return;
		}

		_searchIndex++;
		if (_allEntries != null)
			findNextEntry();
		else
			findNextGroup();
	}

	private void findNextEntry()
	{
		if (_searchIndex >= _allEntries.length)
		{
			_searchIndex = 0;
			_allEntries = null;
			_allGroups = groups.values().toArray(new ResourceGroup[] {});
			findNextGroup();
			return;
		}

		ResourceEntry ent = _allEntries[_searchIndex];
		ent.used = false;
		IField f = _javaClass.getField(ent.name);
		if (!f.exists())
		{
			System.out.println(className + "." + ent.name + " not found");
			return;
		}

		//System.out.println("Search of "  + ent.name);
		SearchPattern pattern = SearchPattern.createPattern(f, IJavaSearchConstants.REFERENCES);
		try
		{
			resources.searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, resources.scope, requestor, null);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
			findNext();
		}
	}

	private void findNextGroup()
	{
		if (_searchIndex >= _allGroups.length) // Обошли все что можно было
		{
			_allGroups = null;
			_javaClass = null;
			if (parent != null)
				parent.findNext();
			else
				resources.checkCompleted();
			return;
		}

		ResourceGroup gr = _allGroups[_searchIndex];
		gr.checkUsages();
	}

	private SearchRequestor requestor = new SearchRequestor()
	{
		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException
		{
			_allEntries[_searchIndex].used = true;
			used = true;
		}

		@Override
		public void endReporting()
		{
			resources.onEntryScaned();
			findNext();
		}
	};

	public boolean contains(String filePath)
	{
		if (filePath.equals(path))
			return true;

		for (ResourceGroup g : groups.values())
			if (g.contains(filePath)) return true;

		for (ResourceEntry e : entries)
			if (e.contains(filePath)) return true;

		return false;
	}

	public ResourceEntry getFont(String fontName)
	{
		for (ResourceEntry e : fields.values())
			if (e.type == ResourceEntry.FONT && e.name.equals(fontName))
				return e;

		for (ResourceGroup g : groups.values())
		{
			ResourceEntry e = g.getFont(fontName);
			if (e != null) return e;
		}

		return null;
	}

	public String fullName()
	{
		if (parent != null)
			return parent.fullName() + "." + name;
		return resources.resourceName();
	}
}