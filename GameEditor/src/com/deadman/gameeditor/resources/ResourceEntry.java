package com.deadman.gameeditor.resources;

public class ResourceEntry
{
	public static final int PICTURE = 0;
	public static final int PICPARTS = 1;
	public static final int FONT = 2;
	public static final int FILE = 3;
	public static final int ARRAY = -1;

	public final ResourceGroup group;
	public final int type;
	public final String name;
	public int id;
	public String path;
	public boolean used = false;

	public ResourceEntry(ResourceGroup group, int type, String name, String path)
	{
		this.group = group;
		this.type = type;
		this.name = name;
		this.path = path;
		id = group.resources.genResId();
	}

	public String fullName()
	{
		return group.fullName() + "." + name;
	}

	public void writeCSV(StringBuilder str)
	{
		str.append(id).append(';').append(type).append(';').append(path);
	}

	public void writeJava(StringBuilder str)
	{
		str.append("public static final int ").append(name).append("=").append(id).append(";\r\n");
	}

	public boolean contains(String filePath)
	{
		return filePath.equals(path);
	}
}