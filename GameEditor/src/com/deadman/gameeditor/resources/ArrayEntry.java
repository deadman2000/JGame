package com.deadman.gameeditor.resources;

class ArrayEntry extends ResourceEntry
{
	// private ArrayList<Layer> _entries = new ArrayList<>();
	private ResourceEntry[] _entries;

	public ArrayEntry(ResourceGroup group, String name, ResourceEntry[] arr)
	{
		super(group, ARRAY, name, "");
		_entries = arr;
	}

	@Override
	public void writeJava(StringBuilder str)
	{
		str.append("public static final int[] ").append(name).append("={");

		boolean first = true;
		for (int i = 0; i < _entries.length; i++)
		{
			ResourceEntry e = _entries[i];

			if (!first)
				str.append(',');
			if (e != null)
				str.append(e.id);
			else
				str.append("-1");
			first = false;
		}

		str.append("};\r\n");
	}
}
