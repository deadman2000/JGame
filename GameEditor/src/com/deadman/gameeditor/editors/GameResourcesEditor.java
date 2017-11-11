package com.deadman.gameeditor.editors;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class GameResourcesEditor extends MultiPageEditorPart implements IResourceChangeListener, ISourceDesigner, IChangeListener
{
	public GameResourcesEditor()
	{
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_CLOSE);
	}

	public void dispose()
	{
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	private GameResourcesDesigner resourceDesigner;

	private void createPageEditor()
	{
		resourceDesigner = new GameResourcesDesigner(getContainer());
		resourceDesigner.setChangeListener(this);
		int index = addPage(resourceDesigner);
		setPageText(index, "Editor");
	}

	private TextEditor textEditor;

	private void createPageText()
	{
		textEditor = new TextEditor();
		try
		{
			int index = addPage(textEditor, getEditorInput());
			setPageText(index, "Source");
		}
		catch (PartInitException e)
		{
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	@Override
	protected void createPages()
	{
		createPageEditor();
		createPageText();
		updateEditor();
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
				for (int i = 0; i < pages.length; i++)
				{
					if (((FileEditorInput) textEditor.getEditorInput()).getFile().getProject().equals(event.getResource()))
					{
						IEditorPart editorPart = pages[i].findEditor(textEditor.getEditorInput());
						pages[i].closeEditor(editorPart, true);
					}
				}
			}
		});
	}

	@Override
	public void doSave(IProgressMonitor monitor)
	{
		textEditor.doSave(monitor);
	}

	@Override
	public void doSaveAs()
	{
		textEditor.doSaveAs();
		updateEditor();
	}

	@Override
	public boolean isSaveAsAllowed()
	{
		return true;
	}

	@Override
	public ITextEditor getTextEditor()
	{
		return textEditor;
	}

	@Override
	protected void setInput(IEditorInput input)
	{
		super.setInput(input);
		setPartName(input.getName());
		updateEditor();
	}

	void updateEditor()
	{
		if (resourceDesigner != null)
			resourceDesigner.setDocument((FileEditorInput) getEditorInput(), getDocument());
	}

	private IDocument getDocument()
	{
		IDocument document = null;
		if (textEditor != null)
		{
			final IDocumentProvider provider = textEditor.getDocumentProvider();
			if (provider != null)
				document = provider.getDocument(textEditor.getEditorInput());
		}
		return document;
	}

	@Override
	public void onChanged()
	{
		//firePropertyChange(PROP_DIRTY);
	}

}
