package com.deadman.dh.isometric;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.deadman.dh.resources.GameResources;

public class SpritePal
{
	public final String id;
	public final int[] colors;
	public final int[][] customs;

	public SpritePal(String id, int[] colors, int[][] customs)
	{
		this.id = id;
		this.colors = colors;
		this.customs = customs;
	}

	public static SpritePal parse(Node node)
	{
		String id = GameResources.getString(node, "id");
		String source = GameResources.getString(node, "source");
		int[] colors = parseColors(source);

		ArrayList<int[]> customs = new ArrayList<>();

		NodeList childs = node.getChildNodes();
		for (int j = 0; j < childs.getLength(); j++)
		{
			Node n = childs.item(j);
			String nodeName = n.getNodeName();

			if (nodeName.equalsIgnoreCase("#text"))
				continue;

			if (nodeName.equals("custom"))
			{
				int[] custom = parseColors(n.getTextContent());
				customs.add(custom);
			}
		}

		int[][] custArr = customs.toArray(new int[customs.size()][]);

		return new SpritePal(id, colors, custArr);
	}

	private static int[] parseColors(String string)
	{
		String[] parts = string.split(",");
		int[] values = new int[parts.length];
		for (int i = 0; i < parts.length; i++)
		{
			String part = parts[i];
			int v = Integer.parseInt(part, 16);
			if (part.length() == 6)
				v = v | 0xFF000000;
			
			values[i] = v;
		}
		return values;
	}

}
