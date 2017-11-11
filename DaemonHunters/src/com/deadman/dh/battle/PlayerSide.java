package com.deadman.dh.battle;

import com.deadman.dh.model.Squad;
import com.deadman.dh.model.Unit;

public class PlayerSide extends BattleSide
{
	public Squad squad; // Отряд (если сторона игрока)

	public PlayerSide(MissionEngine e)
	{
		super(e);
	}
	
	public PlayerSide(MissionEngine e, Squad squad)
	{
		super(e);

		this.squad = squad;

		for (int i = 0; i < squad.units.length; i++)
		{
			Unit unit = squad.units[i];
			if (unit != null)
			{
				units.add(unit);
				unit.side = this;
			}
		}
	}

	@Override
	public String toString()
	{
		return "Player";
	}

}
