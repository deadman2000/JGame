package com.deadman.gameeditor.editors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class GameEditorContributor extends MultiPageEditorActionBarContributor
{
	private IEditorPart owner;
	private IEditorPart editor;

	public GameEditorContributor()
	{
		super();
	}

	protected void setTextAction(IActionBars actionBars, String actionID)
	{
		actionBars.setGlobalActionHandler(actionID, editor instanceof ITextEditor ? ((ITextEditor) editor).getAction(actionID) : null);
	}

	@Override
	public void setActiveEditor(IEditorPart part)
	{
		super.setActiveEditor(part);
		owner = part;
	}

	public void setActivePage(IEditorPart part)
	{
		if (part == null)
			part = owner;

		if (part != null)
		{
			if (part instanceof ISourceDesigner)
				part = ((ISourceDesigner) part).getTextEditor();
			else if (part.getEditorSite() instanceof ISourceDesigner)
				part = ((ISourceDesigner) part.getEditorSite()).getTextEditor();
		}

		if (editor == part) return;

		editor = part;

		IActionBars actionBars = getActionBars();
		if (actionBars != null)
		{
			setTextAction(actionBars, ActionFactory.DELETE.getId());
			//setTextAction(actionBars, ActionFactory.UNDO.getId());
			//setTextAction(actionBars, ActionFactory.REDO.getId());
			setTextAction(actionBars, ActionFactory.CUT.getId());
			setTextAction(actionBars, ActionFactory.COPY.getId());
			setTextAction(actionBars, ActionFactory.PASTE.getId());
			setTextAction(actionBars, ActionFactory.SELECT_ALL.getId());
			setTextAction(actionBars, ActionFactory.FIND.getId());
			setTextAction(actionBars, IDEActionFactory.BOOKMARK.getId());
			actionBars.updateActionBars();
		}

		System.out.println("setActivePage " + part);

		ITextEditor textEditor = null;

		if (part instanceof ITextEditor)
		{
			textEditor = (ITextEditor) part;
		}

		actionBars.setGlobalActionHandler(ITextEditorActionConstants.UNDO, getAction(textEditor, ITextEditorActionConstants.UNDO));
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.REDO, getAction(textEditor, ITextEditorActionConstants.REDO));
	}

	protected final IAction getAction(ITextEditor editor, String actionId)
	{
		return (editor == null ? null : editor.getAction(actionId));
	}

	public static final String COORD_ID = "com.deadman.gameeditor.editors.COORD";

	@Override
	public void contributeToStatusLine(IStatusLineManager manager)
	{
		IContributionItem coords = manager.find(COORD_ID);
		if (coords == null)
			coords = new StatusLineContributionItem(COORD_ID);
		manager.add(coords);
	}
}
