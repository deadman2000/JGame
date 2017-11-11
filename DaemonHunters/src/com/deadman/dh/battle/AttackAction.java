package com.deadman.dh.battle;

import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.items.Weapon;
import com.deadman.jgame.drawing.AnimationPlayer;

public class AttackAction extends Action
{
	private MapCell cell;
	private GameCharacter enemy;
	private Weapon wp;
	private int duration;
	private int ticks;

	public AttackAction(MissionEngine eng, GameCharacter c, GameCharacter enemy)
	{
		super(eng, c);
		wp = ch.getWeapon();
		this.enemy = enemy;
	}

	public AttackAction(MissionEngine eng, GameCharacter c, MapCell cell)
	{
		super(eng, c);
		wp = ch.getWeapon();
		this.cell = cell;
	}

	@Override
	public String toString()
	{
		if (cell != null)
			return "Attack " + ch + " to " + cell + (cell.ch != null ? " " + cell.ch : "");
		else
			return "Attack " + ch + " to " + enemy;
	}

	@Override
	public Action tick()
	{
		if (ticks == 0)
		{
			if (cell != null)
				ch.rotateTo(cell);
			else
				ch.rotateTo(enemy.cell);
			AnimationPlayer aniPlay = ch.setAttack();
			duration = aniPlay.animation.duration;
		}

		ticks++;
		if (ticks > duration)
		{
			ch.useAP(wp.wtype().attackTime);
			if (cell != null)
				wp.hit(engine, ch, cell);
			else
				wp.hit(engine, ch, enemy.cell);
			ch.setState(GameCharacter.STATE_STAND); // Если убрать, юнит застынет на последнем кадре анимации. Можно использовать для бросков и выстрелов
			return null;
		}
		return this;
	}

	@Override
	public boolean isValid()
	{
		if (!ch.hasApAttack()) return false;
		if (cell != null)
			return ch.canAttack(cell);
		return ch.canAttack(enemy);
	}
}
