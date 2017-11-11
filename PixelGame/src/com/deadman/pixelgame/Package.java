package com.deadman.pixelgame;

import java.io.FileInputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Package
{
	public Package(String fileName, GameScreen screen) throws Exception
	{
		System.out.println("Load pacage '" + fileName + "'");
		_screen = screen;

		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(fileName));
		xPath = XPathFactory.newInstance().newXPath();
	}

	private GameScreen _screen;
	private XPath xPath;
	private Document doc;

	public Scene loadScene(String name) throws Exception
	{
		Node n = (Node) xPath.evaluate("/package/scene[@name='" + name + "']", doc.getDocumentElement(), XPathConstants.NODE);

		if (n == null) throw new Exception("Scene '" + name + "' not found");

		String data = (String) xPath.evaluate("background", n, XPathConstants.STRING); // background
		Background background = Background.loadStream(new DataStream(data));

		data = (String) xPath.evaluate("mask", n, XPathConstants.STRING); // mask
		Matrix mask = Matrix.loadStream(new DataStream(data));

		String palId = (String) xPath.evaluate("background/@palette", n, XPathConstants.STRING);
		background.pal = loadPaletteByID(palId);
		background.shift(background.pal.offset);

		Scene scene = new Scene();
		scene.background = background;
		scene.mask = mask;
		return scene;
	}

	public Background loadBackground(String name) throws Exception
	{
		Node n = (Node) xPath.evaluate("/package/pics[@name='" + name + "']", doc.getDocumentElement(), XPathConstants.NODE);
		Background background = Background.loadStream(new DataStream(n.getTextContent()));

		String palId = (String) xPath.evaluate("@palette", n, XPathConstants.STRING);
		background.pal = loadPaletteByID(palId);
		background.shift(background.pal.offset);
		
		return background;
	}

	public Sprite loadSprite(String name) throws Exception
	{
		Node n = (Node) xPath.evaluate("/package/sprite[@name='" + name + "']", doc.getDocumentElement(), XPathConstants.NODE);

		Sprite spr = new Sprite();

		NodeList frameNodes = (NodeList) xPath.evaluate("frame", n, XPathConstants.NODESET);
		int fcnt = frameNodes.getLength();
		spr.frames = new Picture[fcnt];
		String[] frIds = new String[fcnt];
		for (int i = 0; i < fcnt; i++)
		{
			Node fr = frameNodes.item(i);
			spr.frames[i] = Picture.loadRLE(new DataStream(fr.getTextContent()));
			frIds[i] = ((Element) fr).getAttribute("id");
		}

		String palId = (String) xPath.evaluate("@palette", n, XPathConstants.STRING);
		spr.pal = loadPaletteByID(palId);

		for (Picture fr : spr.frames)
			fr.pal = spr.pal;

		// Animations

		NodeList animNodes = (NodeList) xPath.evaluate("animation", n, XPathConstants.NODESET);
		spr.animations = new Animation[animNodes.getLength()];
		for (int i = 0; i < animNodes.getLength(); i++)
		{
			Node an = animNodes.item(i);

			NodeList afrNodes = (NodeList) xPath.evaluate("frame", an, XPathConstants.NODESET);
			int afrCount = afrNodes.getLength();

			DrawableObject[] frames = new DrawableObject[afrCount];
			int[] delays = new int[afrCount];
			
			int delaySum = 0;
			for (int j = 0; j < afrCount; j++)
			{
				Node af = afrNodes.item(j);
				String frId = ((Element) af).getAttribute("id");

				for (int k = 0; k < fcnt; k++)
					if (frIds[k].equalsIgnoreCase(frId))
					{
						frames[j] = spr.frames[k];
						break;
					}

				String delayStr = ((Element) af).getAttribute("delay");
				delaySum += Integer.parseInt(delayStr);
				delays[j] = delaySum;
			}
			
			spr.animations[i] = new Animation(frames, delays);
		}

		return spr;
	}

	public Palette loadPaletteByID(String id) throws Exception
	{
		return loadPalete("/package/palette[@id='" + id + "']");
	}

	public Palette loadPalette(String name) throws Exception
	{
		return loadPalete("/package/palette[@name='" + name + "']");
	}

	private Palette loadPalete(String xpath) throws XPathExpressionException
	{
		Node n = (Node) xPath.evaluate(xpath, doc.getDocumentElement(), XPathConstants.NODE);
		NodeList childs = n.getChildNodes();

		ArrayList<Integer> colors = new ArrayList<Integer>();
		for (int i = 0; i < childs.getLength(); i++)
		{
			Node ch = childs.item(i);
			if (ch.getNodeName().equals("color"))
			{
				colors.add(Integer.parseInt(ch.getTextContent(), 16) | 0xFF000000);
			}
		}

		int[] arr = new int[colors.size()];
		for (int i = 0; i < colors.size(); i++)
			arr[i] = colors.get(i);
		
		Palette pal = new Palette(arr);
		_screen.loadPalette(pal);

		return pal;
	}
}
