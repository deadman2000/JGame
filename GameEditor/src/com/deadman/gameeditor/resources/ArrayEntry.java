package com.deadman.gameeditor.resources;

import java.util.HashMap;

class ArrayEntry extends ResourceEntry {
	// private ArrayList<Layer> _entries = new ArrayList<>();
	private HashMap<Integer, ResourceEntry> _entries;

	public ArrayEntry(ResourceGroup group, String name, HashMap<Integer, ResourceEntry> arr) {
		super(group, ARRAY, name, "");
		_entries = arr;
	}

	@Override
	public void writeJava(StringBuilder str) {
		str.append("public static final int[] ").append(name).append("={");

		int max = 0;
		for (Integer i : _entries.keySet()) {
			if (max < i)
				max = i;
		}

		boolean first = true;
		for (int i = 0; i <= max; i++) {
			ResourceEntry e = _entries.get(i);

			if (!first)
				str.append(',');
			if (e != null)
				str.append(e.id);
			else
				str.append('0');
			first = false;
		}

		str.append("};\r\n");
	}
}
