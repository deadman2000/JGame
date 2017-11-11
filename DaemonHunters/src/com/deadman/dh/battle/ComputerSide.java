package com.deadman.dh.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.deadman.dh.isometric.MoveArea;
import com.deadman.dh.isometric.IsoWay;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.isometric.RouteNode;
import com.deadman.dh.model.GameCharacter;

public class ComputerSide extends BattleSide
{
	private ArrayList<GameCharacter> enemies;
	private int _state = ST_IDLE;

	private static final int ST_IDLE = 0;
	private static final int ST_SEARCH = 1;
	private static final int ST_ALERT = 2;

	protected HashMap<GameCharacter, MapCell> lastVisible = new HashMap<>();

	public ComputerSide(MissionEngine e)
	{
		super(e);
	}

	@Override
	public String toString()
	{
		return "AI";
	}

	@Override
	public boolean isPlayer()
	{
		return false;
	}

	@Override
	protected void onEnemyMoving(GameCharacter ch)
	{
		System.err.println("AI> I see moved enemy " + ch);
		lastVisible.put(ch, ch.cell);
		setState(ST_ALERT);
	}
	
	@Override
	protected void onFoundEnemy(GameCharacter ch)
	{
		System.err.println("AI> I found enemy " + ch);
		setState(ST_ALERT);
	}

	private void setState(int s)
	{
		if (_state == s) return;
		if (_state == ST_SEARCH)
			clearSearch();
		_state = s;
	}

	public Action generateAction()
	{
		System.out.println();
		System.out.println("AI> Generate");

		switch (_state)
		{
			case ST_IDLE:
				return null;
			case ST_ALERT:
				return alert();
			case ST_SEARCH:
				return search();
			default:
				return null;
		}
	}

	private Action alert()
	{
		enemies = getVisibleEnemies();
		System.out.println("AI> Visible enemies: " + enemies.size());
		for (GameCharacter c : enemies)
			System.out.println("AI>    " + c);

		if (enemies.isEmpty())
		{
			setState(ST_SEARCH);
			return search();
		}

		return attackVisible();
	}

	/**
	 * Двигаться к позиции последнего видимого юнита
	 */
	private Action search()
	{
		System.out.println("AI> Searching");
		for (GameCharacter ch : units)
		{
			if (!ch.isActive() || ch.completed) continue;

			if (ch.searching == null)
				ch.searching = createSearch(ch);

			Action a = ch.searching.getAction();
			if (a == null)
			{
				if (ch.searching.invalid)
				{
					ch.completed = true;
					ch.searching = null;
				}
				continue;
			}

			if (a.isValid())
				return a;

			ch.completed = true; // Действие пока не выполнимо. Переходим к след. юниту
		}

		return null;
	}

	private void clearSearch()
	{
		for (GameCharacter ch : units)
			ch.searching = null;
	}

	private EnemySearch createSearch(GameCharacter ch)
	{
		MapCell nearTarget = null;
		int minCost = 0;

		for (MapCell cell : lastVisible.values())
		{
			IsoWay way = ch.trace(cell);
			if (way == null)
				continue;

			if (nearTarget == null || minCost < way.cost)
			{
				nearTarget = cell;
				minCost = way.cost;
			}
		}

		return new EnemySearch(eng, ch, nearTarget);
	}

	/**
	 * Атаковать видимого с максимальной эффективностью 
	 */
	private Action attackVisible()
	{
		ArrayList<Attacking> attacks = new ArrayList<>();

		// Перебираем все возможности юнит/враг
		for (int i = 0; i < units.size(); i++)
		{
			GameCharacter c = units.get(i);
			if (!c.isActive() || c.completed)
			{
				System.out.println("AI> Skip " + c + " A:" + c.isActive() + " C:" + c.completed);
				continue;
			}

			for (GameCharacter e : enemies)
			{
				Attacking a = calcDamageRate(c, e);
				if (a != null)
					attacks.add(a);
			}
		}

		if (attacks.size() == 0)
		{
			System.out.println("AI> No attacks");
			return null;
		}

		System.out.println("AI> Found " + attacks.size() + " attacks");

		Collections.sort(attacks, attackCompare);

		for (int i = 0; i < attacks.size(); i++)
		{
			Attacking attack = attacks.get(i);
			GameCharacter c = attack.unit;
			if (c.completed) continue;

			if (attack.needMove)
			{
				System.out.println("AI> Tracing to attack: " + c + " to " + attack.enemy + " rate: " + attack.rate);
				IsoWay way = c.trace(attack.enemy.cell);
				if (way == null)
				{
					c.completed = true;
					System.out.println("AI> No way: " + c + " to " + attack.enemy);
					continue;
				}

				System.out.println("AI> Move to attack: " + c + " to " + attack.enemy + " rate: " + attack.rate);
				System.out.println("AI> Way from " + way.cells[0] + " to " + way.cells[way.cells.length - 1] + " cost: " + way.cost + " AP:" + c.apCount);
				return new ListAction(new MoveAction(eng, c, way), new AttackAction(eng, c, attack.enemy));
			}

			if (c.canAttack(attack.enemy))
			{

				System.out.println("AI> Attack: " + c + " to " + attack.enemy + " rate: " + attack.rate);
				return new AttackAction(eng, c, attack.enemy);
			}

			System.out.println("AI> Cannot attack: " + c + " to " + attack.enemy);
			c.completed = true;
		}

		return null;
	}

	private Attacking calcDamageRate(GameCharacter c, GameCharacter e)
	{
		if (c.canAttack(e))
		{
			if (!c.hasApAttack()) return null; // Может атаковать, но не сейчас

			System.out.println("AI> Found near attack " + c + " to " + e);
			return new Attacking(c, e, 0, false); // TODO Подумать над весами
		}

		MoveArea moveArea = c.getMoveArea();

		RouteNode rn = moveArea.getCalc(e.cell);
		if (rn == null) // Враг вне досягаемости
		{
			//System.out.println("AI> No way from " + c + " to " + e);
			return null;
		}

		if (rn.parent == null)
		{
			rn = moveArea.findNear(e.cell);
			if (rn == null)
				return null; // Нет пути даже из соседней
		}
		else
			rn = rn.parent; // Мы не идем не до этой ячейки, а до соседней, т.к. на этой стоит враг

		if (rn.cell == c.cell) // Идти не надо - мы рядом
		{
			System.err.println("AI> Found near attack??? " + c + " to " + e);
			return new Attacking(c, e, 0, false); // Возвращаем возможность атаковать
		}

		System.out.println("AI> Found move attack " + c + " to " + e + " cost: " + rn.cost);
		return new Attacking(c, e, e.hpCount + rn.cost * 2, true); // TODO Подумать над весами
	}

	class Attacking // Возможность атаковать юнита
	{
		public GameCharacter unit; // Атакующий
		public GameCharacter enemy;
		public int rate; // Рейтинг. Чем меньше тем лучше
		public boolean needMove;

		public Attacking(GameCharacter u, GameCharacter e, int r, boolean m)
		{
			unit = u;
			enemy = e;
			rate = r;
			needMove = m;
		}

		public boolean isValid()
		{
			return unit.hasApAttack() && unit.canAttack(enemy);
		}
	}

	private static final Comparator<Attacking> attackCompare = new Comparator<Attacking>()
	{
		@Override
		public int compare(Attacking a1, Attacking a2)
		{
			return a1.rate - a2.rate;
		}
	};
}
