package com.deadman.pixelgame;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class JPS
{
	static int _width;
	static float _g[];
	static float _h[];

	static float f(int x, int y)
	{
		return g(x, y) + h(x, y);
	}

	static float g(int x, int y)
	{
		return _g[x + _width * y];
	}

	static float h(int x, int y)
	{
		return _h[x + _width * y];
	}

	static private void hSet(int x, int y, float val)
	{
		_h[x + _width * y] = val;
	}

	static private void gSet(int x, int y, float val)
	{
		_g[x + _width * y] = val;
	}

	LinkedList<Node> heap = new LinkedList<Node>();

	void add(Node n)
	{
		if (n == null)
		{
			System.out.println("Error");
			return;
		}

		if (heap.size() == 0)
		{
			heap.add(n);
		}
		else
		{
			Iterator<Node> listit = heap.iterator();
			int c = 0;
			Node tmp;
			while (true)
			{
				tmp = listit.next();
				if (f(tmp.p.x, tmp.p.y) > f(n.p.x, n.p.y))
				{
					heap.add(c, n);
					break;
				}

				if (!listit.hasNext())
				{
					heap.add(n);
					break;
				}

				c++;
			}
		}
	}

	class Node
	{
		public Node(Point p)
		{
			this.p = p;
		}

		public Point p;
		public Node parent;
	}

	Matrix _mask;
	Point _to;

	public static Point[] findPath(Matrix mask, Point from, Point to)
	{
		return new JPS().createPath(mask, from, to);
	}

	public Point[] createPath(Matrix mask, Point from, Point to)
	{
		//System.out.println("From " + from + " to " + to);

		_mask = mask;
		_to = to;

		if (_g == null || _g.length != _mask.width * _mask.height)
		{
			_g = new float[_mask.width * _mask.height];
			_h = new float[_mask.width * _mask.height];
		}
		else
		{
			Arrays.fill(_g, 0);
			Arrays.fill(_h, 0);
		}

		_width = _mask.width;

		add(new Node(from));

		Node curr;
		while (true)
		{
			curr = heap.pop();
			if (curr.p.equals(to))
			{
				break;
			}

			Node[] possibleSuccess = identifySuccessors(curr);
			for (int i = 0; i < possibleSuccess.length; i++)
			{
				if (possibleSuccess[i] != null)
					add(possibleSuccess[i]);
			}

			if (heap.size() == 0)
			{
				//System.out.println("No Path....");
				curr = null;
				break;
			}
		}

		if (curr != null)
		{
			int length = 0;
			ArrayList<Point[]> lines = new ArrayList<Point[]>();
			while (curr.parent != null)
			{
				Point[] line = getLine(curr.parent.p, curr.p);
				lines.add(line);
				length += line.length;
				curr = curr.parent;
			}

			Point[] path = new Point[length];
			int pos = 0;
			for (int i = 0; i < lines.size(); i++)
			{
				Point[] pts = lines.get(lines.size() - 1 - i);
				for (int j = 0; j < pts.length; j++)
				{
					path[pos++] = pts[j];
				}
			}

			/*for (int i = 0; i < path.length; i++)
			{
				System.out.println(path[i]);
			}*/

			return path;
		}
		else
			return null;
	}

	private static Point[] getLine(Point a, Point b)
	{
		if (a.x == b.x)
		{
			Point[] pts = new Point[Math.abs(a.y - b.y)];
			int k = a.y < b.y ? 1 : -1;
			for (int i = 0; i < pts.length; i++)
			{
				pts[i] = new Point(a.x, a.y + k * (i + 1));
			}
			return pts;
		}
		else if (a.y == b.y)
		{
			Point[] pts = new Point[Math.abs(a.x - b.x)];
			int k = a.x < b.x ? 1 : -1;
			for (int i = 0; i < pts.length; i++)
			{
				pts[i] = new Point(a.x + k * (i + 1), a.y);
			}
			return pts;
		}
		else
		{
			Point[] pts = new Point[Math.abs(a.x - b.x)];
			int kx = a.x < b.x ? 1 : -1;
			int ky = a.y < b.y ? 1 : -1;
			for (int i = 0; i < pts.length; i++)
			{
				pts[i] = new Point(a.x + kx * (i + 1), a.y + ky * (i + 1));
			}
			return pts;
		}
	}

	public float toPointApprox(float x, float y, int tx, int ty)
	{
		float dx = x - tx;
		float dy = y - ty;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	private Node[] identifySuccessors(Node node)
	{
		Point pt = node.p;
		Node[] successors = new Node[8];
		Point[] neighbors = getNeighborsPrune(node);

		for (int i = 0; i < neighbors.length; i++)
		{
			if (neighbors[i] == null) continue;

			Point tmp = jump(neighbors[i].x, neighbors[i].y, pt.x, pt.y);
			if (tmp != null)
			{
				float ng = toPointApprox(tmp.x, tmp.y, pt.x, pt.y) + g(pt.x, pt.y);
				if (f(tmp.x, tmp.y) <= 0 || g(tmp.x, tmp.y) > ng)
				{
					gSet(tmp.x, tmp.y, ng);
					hSet(tmp.x, tmp.y, toPointApprox(tmp.x, tmp.y, pt.x, pt.y));
					Node n = new Node(tmp);
					n.parent = node;
					successors[i] = n;
				}
			}
		}
		return successors; //finally, successors is returned
	}

	private Point[] getNeighborsPrune(Node node)
	{
		Node parent = node.parent; //the parent node is retrieved for x,y values
		int x = node.p.x;
		int y = node.p.y;
		int px, py, dx, dy;
		Point[] neighbors = new Point[5];
		//directed pruning: can ignore most neighbors, unless forced
		if (parent != null)
		{
			px = parent.p.x;
			py = parent.p.y;
			//get the normalized direction of travel
			dx = (x - px) / Math.max(Math.abs(x - px), 1);
			dy = (y - py) / Math.max(Math.abs(y - py), 1);
			//search diagonally
			if (dx != 0 && dy != 0)
			{
				if (_mask.isMovable(x, y + dy))
				{
					neighbors[0] = new Point(x, y + dy);
				}
				if (_mask.isMovable(x + dx, y))
				{
					neighbors[1] = new Point(x + dx, y);
				}
				if (_mask.isMovable(x, y + dy) || _mask.isMovable(x + dx, y))
				{
					neighbors[2] = new Point(x + dx, y + dy);
				}
				if (!_mask.isMovable(x - dx, y) && _mask.isMovable(x, y + dy))
				{
					neighbors[3] = new Point(x - dx, y + dy);
				}
				if (!_mask.isMovable(x, y - dy) && _mask.isMovable(x + dx, y))
				{
					neighbors[4] = new Point(x + dx, y - dy);
				}
			}
			else
			{
				if (dx == 0)
				{
					if (_mask.isMovable(x, y + dy))
					{
						if (_mask.isMovable(x, y + dy))
						{
							neighbors[0] = new Point(x, y + dy);
						}
						if (!_mask.isMovable(x + 1, y))
						{
							neighbors[1] = new Point(x + 1, y + dy);
						}
						if (!_mask.isMovable(x - 1, y))
						{
							neighbors[2] = new Point(x - 1, y + dy);
						}
					}
				}
				else
				{
					if (_mask.isMovable(x + dx, y))
					{
						if (_mask.isMovable(x + dx, y))
						{
							neighbors[0] = new Point(x + dx, y);
						}
						if (!_mask.isMovable(x, y + 1))
						{
							neighbors[1] = new Point(x + dx, y + 1);
						}
						if (!_mask.isMovable(x, y - 1))
						{
							neighbors[2] = new Point(x + dx, y - 1);
						}
					}
				}
			}
		}
		else
		{//return all neighbors
			return getNeighbors(node); //adds initial nodes to be jumped from!
		}

		return neighbors; //this returns the neighbors, you know
	}

	private Point[] getNeighbors(Node node)
	{
		Point[] neighbors = new Point[8];
		int x = node.p.x;
		int y = node.p.y;
		boolean d0 = false; //These booleans are for speeding up the adding of nodes.
		boolean d1 = false;
		boolean d2 = false;
		boolean d3 = false;

		if (_mask.isMovable(x, y - 1))
		{
			neighbors[0] = new Point(x, y - 1);
			d0 = d1 = true;
		}
		if (_mask.isMovable(x + 1, y))
		{
			neighbors[1] = new Point(x + 1, y);
			d1 = d2 = true;
		}
		if (_mask.isMovable(x, y + 1))
		{
			neighbors[2] = new Point(x, y + 1);
			d2 = d3 = true;
		}
		if (_mask.isMovable(x - 1, y))
		{
			neighbors[3] = new Point(x - 1, y);
			d3 = d0 = true;
		}
		if (d0 && _mask.isMovable(x - 1, y - 1))
		{
			neighbors[4] = new Point(x - 1, y - 1);
		}
		if (d1 && _mask.isMovable(x + 1, y - 1))
		{
			neighbors[5] = new Point(x + 1, y - 1);
		}
		if (d2 && _mask.isMovable(x + 1, y + 1))
		{
			neighbors[6] = new Point(x + 1, y + 1);
		}
		if (d3 && _mask.isMovable(x - 1, y + 1))
		{
			neighbors[7] = new Point(x - 1, y + 1);
		}
		return neighbors;
	}

	Point jump(int x, int y, int px, int py)
	{
		int dx = (x - px) / Math.max(Math.abs(x - px), 1); //because parents aren't always adjacent, this is used to find parent -> child direction (for x)
		int dy = (y - py) / Math.max(Math.abs(y - py), 1); //because parents aren't always adjacent, this is used to find parent -> child direction (for y)

		if (!_mask.isMovable(x, y)) return null;

		if (x == _to.x && y == _to.y) return new Point(x, y);

		if (dx != 0 && dy != 0)
		{
			if ((_mask.isMovable(x - dx, y + dy) && !_mask.isMovable(x - dx, y)) ||
					(_mask.isMovable(x + dx, y - dy) && !_mask.isMovable(x, y - dy)))
				return new Point(x, y);
		}
		else
		{
			if (dx != 0)
			{
				if ((_mask.isMovable(x + dx, y + 1) && !_mask.isMovable(x, y + 1)) ||
						(_mask.isMovable(x + dx, y - 1) && !_mask.isMovable(x, y - 1)))
					return new Point(x, y);
			}
			else
			{
				if ((_mask.isMovable(x + 1, y + dy) && !_mask.isMovable(x + 1, y)) ||
						(_mask.isMovable(x - 1, y + dy) && !_mask.isMovable(x - 1, y)))
					return new Point(x, y);
			}
		}

		if (dx != 0 && dy != 0)
		{
			Point jpx = jump(x + dx, y, x, y);
			Point jpy = jump(x, y + dy, x, y);
			if (jpx != null || jpy != null)
				return new Point(x, y);
		}

		if (_mask.isMovable(x + dx, y) || _mask.isMovable(x, y + dy))
			return jump(x + dx, y + dy, x, y);
		else
			return null;
	}
}
