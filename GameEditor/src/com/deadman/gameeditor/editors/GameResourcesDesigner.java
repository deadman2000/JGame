package com.deadman.gameeditor.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.FileEditorInput;

import com.deadman.gameeditor.resources.GameResources;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Button;

// http://www.ibm.com/developerworks/ru/library/os-eclipse-emf/

public class GameResourcesDesigner extends Composite implements IChangeListener, IBuildProgressListener
{

	private GameResources resources;

	public GameResourcesDesigner(Composite parent)
	{
		super(parent, SWT.NONE);

		this.setLayout(new GridLayout(3, false));

		btnScan = new Button(this, SWT.NONE);
		btnScan.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnScan.setText("SCAN");
		btnScan.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				reload();
			}
		});
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		btnBuild = new Button(this, SWT.NONE);
		btnBuild.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnBuild.setText("BUILD");
		btnBuild.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				btnBuild.setEnabled(false);
				btnStopBuild.setEnabled(true);
				buildResource();
			}
		});

		progressBar = new ProgressBar(this, SWT.NONE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnStopBuild = new Button(this, SWT.NONE);
		btnStopBuild.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnStopBuild.setEnabled(false);
		btnStopBuild.setText("STOP");
		btnStopBuild.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				resources.stopBuild();
			}
		});
	}

	protected void reload()
	{
		try
		{
			if (resources == null)
				resources = GameResources.load(_input, _document);
			else
				resources.reload();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	protected void buildResource()
	{
		try
		{
			if (resources == null) reload();
			resources.build(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	private FileEditorInput _input;
	private IDocument _document;

	public void setDocument(FileEditorInput input, IDocument document)
	{
		_input = input;
		_document = document;

		/*try
		{
			resources = GameResources.load(input, document);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}*/
	}

	@Override
	public void onChanged()
	{
		notifyChange();
	}

	private IChangeListener changeListener;
	private ProgressBar progressBar;
	private Button btnBuild;
	private Button btnStopBuild;
	private Button btnScan;

	public void setChangeListener(IChangeListener listener)
	{
		changeListener = listener;
	}

	void notifyChange()
	{
		if (changeListener != null)
			changeListener.onChanged();
	}

	@Override
	public void onBuildProgressChanged(int progress, int total)
	{
		getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				progressBar.setMaximum(total);
				progressBar.setSelection(progress);
			}
		});
	}

	@Override
	public void onBuildCompleted()
	{
		getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				btnBuild.setEnabled(true);
				btnStopBuild.setEnabled(false);
				progressBar.setSelection(0);
			}
		});
	}
}
