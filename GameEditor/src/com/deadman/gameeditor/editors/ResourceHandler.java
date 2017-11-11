package com.deadman.gameeditor.editors;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ResourceHandler extends DefaultHandler
{
	private SAXParserFactory fFactory;

	private String fTopElementFound = null;

	//private int fLevel = -1;
	
	private final String ROOT = "game-resources";

	protected boolean parseContents(InputSource contents) throws IOException, ParserConfigurationException, SAXException
	{
		try
		{
			fFactory = getFactory();
			if (fFactory == null) { return false; }
			final SAXParser parser = createParser(fFactory);
			contents.setSystemId("/");
			parser.parse(contents, this);
		}
		catch (StopParsingException e)
		{
		}
		return true;
	}

	private SAXParserFactory getFactory()
	{
		synchronized (this)
		{
			if (fFactory != null) { return fFactory; }
			fFactory = SAXParserFactory.newInstance();
			fFactory.setNamespaceAware(true);
		}
		return fFactory;
	}

	private final SAXParser createParser(SAXParserFactory parserFactory) throws ParserConfigurationException, SAXException, SAXNotRecognizedException, SAXNotSupportedException
	{
		final SAXParser parser = parserFactory.newSAXParser();
		final XMLReader reader = parser.getXMLReader();
		try
		{
			reader.setFeature("http://xml.org/sax/features/validation", false);
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		}
		catch (SAXNotRecognizedException e)
		{
		}
		catch (SAXNotSupportedException e)
		{
		}
		return parser;
	}

	@Override
	public void startElement(final String uri, final String elementName, final String qualifiedName, final Attributes attributes) throws SAXException
	{
		//fLevel++;
		if (fTopElementFound == null)
		{
			fTopElementFound = elementName;
			if (!hasRootElement())
				throw new StopParsingException();
		}
	}

    /*public void endElement(String uri, String localName, String qName) throws SAXException {
    	super.endElement(uri, localName, qName);
    	fLevel--;
    }*/
    
	public boolean hasRootElement()
	{
		return ROOT.equals(fTopElementFound);
	}

	private class StopParsingException extends SAXException
	{
		private static final long serialVersionUID = 1L;

		public StopParsingException()
		{
			super((String) null);
		}
	}
}
