package com.deadman.dh.battle;

public class ListAction extends Action
{
	private final Action[] _actions;
	private int _current;

	public ListAction(Action... actions)
	{
		_actions = actions;
	}

	@Override
	public Action tick()
	{
		while (true)
		{
			Action a = _actions[_current].tick();
			if (a != null)
				return this;
			
			_current++;
			if (_current == _actions.length)
				return null;
			
			if (!_actions[_current].isValid())
				return null;
		}
	}

	@Override
	public boolean isValid()
	{
		return _actions[0].isValid();
	}
}
