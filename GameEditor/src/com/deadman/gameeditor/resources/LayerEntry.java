package com.deadman.gameeditor.resources;

public class LayerEntry extends ResourceEntry {
	public Layer layer;

	public LayerEntry(ResourceGroup group, String name, String path, Layer layer) {
		super(group, PICTURE, name, path + "?" + layer.name);
		this.layer = layer;
		layer.res = this;
	}
	
	@Override
	public boolean contains(String filePath)
	{
		return false;
	}
}
