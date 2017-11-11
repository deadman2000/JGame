package com.deadman.gameeditor.resources;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class Packer
{
	public static Image packToImage(Collection<Layer> layers)
	{
		// TODO Поиск оптимального метода сортировки

		ArrayList<Layer> sorted = new ArrayList<>(layers);
		Layer.sortByHeight(sorted);

		Packer packer = new Packer();
		packer.fit(sorted);

		if (packer.root.w <= 0 || packer.root.h <= 0)
		{
			System.err.println("Wrong size");
			return null;
		}

		Image img = new Image(Display.getCurrent(), packer.root.w, packer.root.h);
		GC gc = new GC(img);

		for (int i = 0; i < sorted.size(); i++)
		{
			Layer layer = sorted.get(i);
			if (layer.image == null) continue;

			if (layer.globalX == -1)
			{
				System.err.println("No map coords " + sorted.get(i));
				return null;
			}

			gc.drawImage(layer.image, layer.globalX, layer.globalY);
		}

		return img;
	}

	public static void pack(Collection<Layer> layers, String path)
	{
		Image img = packToImage(layers);
		if (img == null)
		{
			System.err.println("Cannot pack " + path);
			return;
		}
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] { img.getImageData() };
		imageLoader.save(path, SWT.IMAGE_PNG);
	}

	private Node root;

	private void fit(ArrayList<Layer> layers)
	{
		int len = layers.size();
		int w = len > 0 ? layers.get(0).width : 0;
		int h = len > 0 ? layers.get(0).height : 0;

		root = new Node(0, 0, w, h);
		for (int n = 0; n < len; n++)
		{
			Layer layer = layers.get(n);
			Node node = findNode(root, layer.width, layer.height);
			if (node != null)
				node = splitNode(node, layer.width, layer.height);
			else
				node = growNode(layer.width, layer.height);

			if (node != null)
			{
				layer.globalX = node.x;
				layer.globalY = node.y;
			}
		}
	}

	private Node findNode(Node root, int w, int h)
	{
		if (root.used)
		{
			Node node = findNode(root.right, w, h);
			if (node != null)
				return node;
			return findNode(root.down, w, h);
		}
		else if (w <= root.w && h <= root.h)
			return root;
		else
			return null;
	}

	private Node splitNode(Node node, int w, int h)
	{
		node.used = true;
		node.down = new Node(node.x, node.y + h, node.w, node.h - h);
		node.right = new Node(node.x + w, node.y, node.w - w, h);
		return node;
	}

	private Node growNode(int w, int h)
	{
		boolean canGrowDown = (w <= this.root.w);
		boolean canGrowRight = (h <= this.root.h);

		boolean shouldGrowRight = canGrowRight && (this.root.h >= (this.root.w + w));
		boolean shouldGrowDown = canGrowDown && (this.root.w >= (this.root.h + h));

		if (shouldGrowRight)
			return growRight(w, h);
		else if (shouldGrowDown)
			return growDown(w, h);
		else if (canGrowRight)
			return growRight(w, h);
		else if (canGrowDown)
			return growDown(w, h);
		else
			return null; // need to ensure sensible root starting size to avoid
							// this happening
	}

	private Node growRight(int w, int h)
	{
		Node newRoot = new Node(0, 0, root.w + w, root.h);
		newRoot.used = true;
		newRoot.down = root;
		newRoot.right = new Node(root.w, 0, w, root.h);
		root = newRoot;

		Node node = findNode(root, w, h);
		if (node != null)
			return splitNode(node, w, h);
		else
			return null;
	}

	private Node growDown(int w, int h)
	{
		Node newRoot = new Node(0, 0, root.w, root.h + h);
		newRoot.used = true;
		newRoot.down = new Node(0, root.h, root.w, h);
		newRoot.right = root;
		root = newRoot;

		Node node = findNode(root, w, h);
		if (node != null)
			return splitNode(node, w, h);
		else
			return null;
	}

	private static class Node
	{
		public int x, y, w, h;
		public boolean used;
		public Node right, down;

		public Node(int x, int y, int w, int h)
		{
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}
}
