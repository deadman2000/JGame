package com.deadman.gameeditor.resources;

class ResourceEntry
{
	private final GameResources resources;
	public final int type;
	public final String name;
	public int id;
	public String path;
	public boolean used = false;

	public ResourceEntry(GameResources res, int type, String name, String path)
	{
		resources = res;
		this.type = type;
		this.name = name;
		this.path = path;
		id = resources.genResId();
	}

	public void writeCSV(StringBuilder str)
	{
		str.append(id).append(';').append(type).append(';').append(path);
	}

	public void writeJava(StringBuilder str)
	{
		str.append("public static final int ").append(name).append("=").append(id).append(";\r\n");
	}

	public static final int PICTURE = 0;
	public static final int PICPARTS = 1;
	public static final int FONT = 2;
	public static final int FILE = 3;
	public static final int ARRAY = -1;
}