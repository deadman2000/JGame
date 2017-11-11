package com.deadman.gameeditor.editors;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.XMLContentDescriber;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ResourcesDescriber extends XMLContentDescriber
{
	@Override
	public int describe(InputStream contents, IContentDescription description) throws IOException
	{
		if (super.describe(contents, description) == INVALID)
			return INVALID;
		contents.reset();
		return checkCriteria(new InputSource(contents));
	}

	private int checkCriteria(InputSource contents) throws IOException
	{
		ResourceHandler handler = new ResourceHandler();
		try
		{
			if (!handler.parseContents(contents)) { return INDETERMINATE; }
		}
		catch (SAXException e)
		{
			return INDETERMINATE;
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException(e.getMessage());
		}

		if (handler.hasRootElement())
			return VALID;

		return INDETERMINATE;
	}
}
