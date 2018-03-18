package com.deadman.dh.resources;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.deadman.dh.ItemTypes;
import com.deadman.dh.guild.GuildBuildingType;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.dh.model.MissionScenario;
import com.deadman.jgame.resources.XCF;

public class GameResources
{
	public static GameResources main;

	public static void init()
	{
		main = new GameResources();
		main.open("resources.xml");

		MissionScenario.initScenarios();
		GuildBuildingType.initBuildings();
		ItemTypes.armor.generate();
	}

	// Iso

	public HashMap<Integer, IsoSprite> floors = new HashMap<>();
	public HashMap<Integer, IsoSprite> walls = new HashMap<>();
	public HashMap<Integer, IsoSprite> objects = new HashMap<>();
	public HashMap<Integer, IsoSprite> wobjects = new HashMap<>();
	public HashMap<Integer, IsoSprite> helpers = new HashMap<>();
	public HashMap<Integer, IsoSprite> units = new HashMap<>();

	public Hashtable<String, IsoSprite> isoSprites = new Hashtable<>();

	public HashMap<Integer, IsoSprite> getIsoDB(byte objClass)
	{
		switch (objClass)
		{
			case IsoSprite.FLOOR:
				return floors;
			case IsoSprite.WALL:
				return walls;
			case IsoSprite.OBJECT:
				return objects;
			case IsoSprite.OBJ_ON_WALL:
				return wobjects;
			case IsoSprite.HELPERS:
				return helpers;
			case IsoSprite.UNIT:
				return units;
			default:
				System.err.println("Unknown iso-db " + objClass);
				return null;
		}
	}

	public IsoSprite getIso(String name)
	{
		return isoSprites.get(name);
	}

	private void parseIsoSprites(NodeList isosprites)
	{
		for (int i = 0; i < isosprites.getLength(); i++)
		{
			Node n = isosprites.item(i);
			NodeList childs = n.getChildNodes();
			for (int j = 0; j < childs.getLength(); j++)
			{
				Node node = childs.item(j);
				String nodeName = node.getNodeName();

				if (nodeName.equalsIgnoreCase("#text"))
					continue;

				IsoSprite spr = IsoSprite.parse(node);
				if (spr != null)
				{
					HashMap<Integer, IsoSprite> hash = getIsoDB(spr.type);
					if (hash != null) hash.put(spr.id, spr);
					isoSprites.put(spr.name, spr);
				}
			}
		}
	}

	// Parsing XML

	public void open(String fileName)
	{
		try
		{
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			f.setValidating(false);
			DocumentBuilder builder = f.newDocumentBuilder();
			Document doc = builder.parse(new File(fileName));
			Element root = (Element) doc.getFirstChild();

			parseIsoSprites(root.getElementsByTagName("isosprites"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static HashMap<String, XCF> _xcfs = new HashMap<>();

	public static XCF loadXCF(String fileName)
	{
		XCF xcf = _xcfs.get(fileName);
		if (xcf == null)
		{
			xcf = XCF.loadFile(fileName);
			_xcfs.put(fileName, xcf);
		}
		return xcf;
	}

	// XML Helpers

	public static int getInt(Node node, String attrName)
	{
		Node it = node.getAttributes()
				.getNamedItem(attrName);
		if (it != null)
			return Integer.parseInt(it.getNodeValue());
		return 0;
	}

	public static int getInt(Node node, String attrName, int def)
	{
		Node it = node.getAttributes()
				.getNamedItem(attrName);
		if (it != null)
			return Integer.parseInt(it.getNodeValue());
		return def;
	}

	public static String getString(Node node, String attrName)
	{
		Node it = node.getAttributes()
				.getNamedItem(attrName);
		if (it != null) return it.getNodeValue();
		return null;
	}

	public static int getHexInt(Node node, String attrName)
	{
		Node it = node.getAttributes()
				.getNamedItem(attrName);
		if (it != null)
			return Integer.parseInt(it.getNodeValue(), 16);
		return 0;
	}

	public static float getFloat(Node node, String attrName)
	{
		Node it = node.getAttributes()
				.getNamedItem(attrName);
		if (it != null)
			return Float.parseFloat(it.getNodeValue());
		return 0;
	}

	public static boolean getBool(Node node, String attrName)
	{
		Node it = node.getAttributes()
				.getNamedItem(attrName);
		if (it != null)
			return Boolean.parseBoolean(it.getNodeValue());
		return false;
	}
}
