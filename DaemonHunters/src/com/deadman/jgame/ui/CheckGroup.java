package com.deadman.jgame.ui;

public class CheckGroup
{
	private Button _checked;

	public CheckGroup(Button... buttons)
	{
		for (Button b : buttons)
		{
			if (b.checked) _checked = b;
			b.checkOnClick = true;
			b.addControlListener(button_listener);
		}
	}

	private ControlListener button_listener = new ControlListener()
	{
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_CHECKED)
			{
				if (_checked != null) _checked.setChecked(false);
				_checked = (Button) sender;
			}
		};
	};
}
