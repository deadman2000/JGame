package com.deadman.dh.isometric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import com.deadman.dh.model.GameCharacter;

public class MoveArea
{
	private static boolean DEBUG = false;

	private RouteNode[] route_nodes;
	private final ArrayList<RouteNode> moveArea = new ArrayList<>();

	private LinkedList<RouteNode> route_neighbors = new LinkedList<>();

	private final int height, width, zheight;
	public final IsoMap map;
	private final MapCell startCell;
	private final GameCharacter ch;

	public MoveArea(IsoMap map, GameCharacter ch)
	{
		this.map = map;
		this.ch = ch;
		startCell = ch.cell;

		height = map.height;
		width = map.width;
		zheight = map.zheight;
	}

	public MoveArea(IsoMap map, MapCell cell)
	{
		this.map = map;
		this.ch = null;
		startCell = cell;

		height = map.height;
		width = map.width;
		zheight = map.zheight;
	}

	public ArrayList<RouteNode> nodes()
	{
		return moveArea;
	}

	public void build()
	{
		if (DEBUG) System.out.println("Build");
		// Создаем матрицу нодов для трассировки
		int arrSize = height * width * zheight;
		if (route_nodes == null || route_nodes.length < arrSize)
			route_nodes = new RouteNode[arrSize];
		else
			Arrays.fill(route_nodes, null);
		//route_nodes = new RouteNode[height * width * zheight];

		RouteNode cc = new RouteNode(startCell);
		cc.currentTurn = true;
		setCalc(startCell.x, startCell.y, startCell.z, cc);
		route_neighbors.add(cc);

		while (!route_neighbors.isEmpty())
		{
			curr = route_neighbors.removeFirst();
			setCalcNeighbors();
		}

		route_neighbors = null;
		curr = null;
	}

	public RouteNode getCalc(MapCell c)
	{
		return getCalc(c.x, c.y, c.z);
	}

	private RouteNode getCalc(int x, int y, int z)
	{
		if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= zheight) return null;
		return route_nodes[x + y * width + z * width * height];
	}

	private RouteNode getNode(int x, int y, int z)
	{
		if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= zheight) return null;
		int addr = x + y * width + z * width * height;
		RouteNode c = route_nodes[addr];
		if (c == null)
		{
			c = new RouteNode(map.cells[z][x][y]);
			route_nodes[addr] = c;
		}
		return c;
	}

	private void setCalc(int x, int y, int z, RouteNode cell)
	{
		route_nodes[x + y * width + z * width * height] = cell;
	}

	private RouteNode curr;

	/*
	 *           /\
	 *          /0 \
	 *     Y   /\  /\  X
	 *    /   /7 \/1 \  \
	 *  |_   /\  /\  /\  _|
	 *      /6 \/fr\/2 \ 
	 *      \  /\om/\  /
	 *       \/5 \/3 \/
	 *        \  /\  /
	 *         \/4 \/
	 *          \  /
	 *           \/
	 */
	private void setCalcNeighbors()
	{
		int x = curr.cell.x;
		int y = curr.cell.y;
		for (int z = curr.cell.z - 1; z <= curr.cell.z + 1; z++)
		{
			if (z < 0) continue;
			if (z >= map.zheight) break;
			addCalcNode(x - 1, y - 1, z); // <0>
			addCalcNode(x, y - 1, z); //     <1>
			addCalcNode(x + 1, y - 1, z); // <2>
			addCalcNode(x + 1, y, z); //     <3>
			addCalcNode(x + 1, y + 1, z); // <4>
			addCalcNode(x, y + 1, z); //     <5>
			addCalcNode(x - 1, y + 1, z); // <6>
			addCalcNode(x - 1, y, z); //     <7>
		}
	}

	private void addCalcNode(int x, int y, int z)
	{
		RouteNode n = getNode(x, y, z);
		if (curr.parent == n) return;

		if (n == null || n.cell == startCell)
			return;

		//if (n.cell.isOccupied() && ) return; // Исключаем занятые ячейки

		int cost = n.cell.moveCost(curr.cell);
		if (cost == 0) // Пройти нельзя
			return;

		if (n.parent != null && !isOccupied(n.parent.cell) && isOccupied(curr.cell)) // Уже есть незанятый родитель
			return;

		// Признак того что этот ход лучше: нет родителя, меньше стоит, родитель занят, а мы нет
		if (n.parent == null || n.cost > curr.cost + cost || (curr.currentTurn && n.parent.cell != startCell && !isOccupied(curr.cell) && isOccupied(n.parent.cell)))
		{
			if (n.cost == 0) // Если новый, добавляем в очередь
				route_neighbors.add(n);
			
			curr.childs.add(n);
			n.setParrent(curr, cost);

			if (!isOccupied(n.cell))
			{
				if ((ch == null || n.cost < ch.apCount) && curr.currentTurn && !n.currentTurn)
				{
					n.currentTurn = true;
					moveArea.add(n);
				}
			}
		}
	}

	public boolean isOccupied(MapCell cell)
	{
		return (cell.obj != null && !cell.obj.isMovable()) || (cell.ch != null && ch.isVisible(cell));
	}

	public IsoWay traceNear(MapCell to)
	{
		RouteNode n = findNear(to);
		if (n == null)
		{
			System.out.println("No near cell " + n);
			return null;
		}
		return trace(n.cell);
	}

	static boolean debug = false;

	public IsoWay trace(MapCell to)
	{
		if (startCell == to) return null;

		// TODO Опускать назначение только для пользователя!
		while (to.obj == null && to.floor == null) // Дырка в полу - строим путь по нижнему уровню
		{
			int z = to.z - 1;
			if (z >= 0)
				to = map.getCell(to.x, to.y, z);
			else
				break;
		}

		if (debug) System.out.println("Tracing " + ch + " from " + ch.cell + " to " + to);

		RouteNode n = getCalc(to);
		if (n == null)
		{
			if (debug) System.out.println("No trace cell");
			return null;
		}

		if (debug) System.out.println("Node: " + n);

		while (n != null && !n.currentTurn)
		{
			if (debug) System.out.println("  Shift " + n + " to " + n.parent);
			n = n.parent;
		}

		if (n == null || n.num == 0)
		{
			if (debug) System.out.println("No trace cell 2 " + n);
			return null;
		}

		int totalCost = n.cost;
		boolean completed = n.cell == to;

		MapCell[] cells = new MapCell[n.num + 1];
		int[] costs = new int[n.num + 1];

		for (int i = 0; i < cells.length; i++)
		{
			int ind = cells.length - 1 - i;
			cells[ind] = n.cell;
			costs[ind] = n.cost;
			n = n.parent;
		}

		if (cells[0] != ch.cell)
		{
			System.err.println("Wrong way!");
			return null;
		}

		return new IsoWay(cells, costs, totalCost, completed);
	}

	public RouteNode findNear(MapCell c)
	{
		RouteNode found = null;

		for (int dx = -1; dx <= 1; dx++)
			for (int dy = -1; dy <= 1; dy++)
			{
				if (dx == 0 && dy == 0) continue;

				RouteNode n = getCalc(c.x + dx, c.y + dy, c.z);
				if (n != null && n.parent != null && n.cell.ch == null)
				{
					if (found == null || n.cost < found.cost)
						found = n;
				}
			}

		return found;
	}

	public void show()
	{
		for (RouteNode c : moveArea)
			if (c.currentTurn)
				c.cell.state |= MapCell.FLOOR_FLAG;
	}

	public void hide()
	{
		for (RouteNode c : moveArea)
			if (c.currentTurn)
				c.cell.state &= MapCell.NOT_FLOOR_FLAG;
	}
}
