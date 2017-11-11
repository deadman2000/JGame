package com.deadman.gameeditor.resources;

public class LayerEntry extends ResourceEntry {
	public Layer layer;

	public LayerEntry(GameResources res, String name, String path, Layer layer) {
		super(res, PICTURE, name, path + "?" + layer.name);
		this.layer = layer;
		layer.res = this;
	}
}
