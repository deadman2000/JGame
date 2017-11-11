package com.deadman.dh.battle;

import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;

public class RotateAction extends Action
{
	private MapCell target;
	private byte rotation;
	private int time = 10;
	boolean immediately;
	boolean finished = false;

	public RotateAction(GameCharacter ch, MapCell target, boolean immediately)
	{
		super(ch);
		this.target = target;
		this.immediately = immediately;
	}

	public RotateAction(GameCharacter ch, byte rotation, boolean immediately)
	{
		super(ch);
		this.rotation = rotation;
		this.immediately = immediately;
	}

	@Override
	public Action tick()
	{
		if (!finished)
		{
			ch.useAP(GameCharacter.ROTATE_COST);
			if (target != null)
				ch.rotateTo(target);
			else
				ch.setRotation(rotation);
			finished = true;
		}

		if (immediately) return null;

		time--;
		if (time <= 0)
			return null;
		return this;
	}

	@Override
	public boolean isValid()
	{
		return ch.apCount >= GameCharacter.ROTATE_COST && !isRotated();
	}

	private boolean isRotated()
	{
		if (target == null)
			return ch.getRotation() == rotation;
		return ch.isRotated(target);
	}
}
