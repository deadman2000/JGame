package com.deadman.dh.guild;

import java.util.ArrayList;

import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.isometric.MoveArea;
import com.deadman.dh.isometric.RouteNode;
import com.deadman.dh.model.GPoint;
import com.deadman.dh.model.Rectangle;
import com.deadman.dh.resources.GameResources;

public class GuildMapBuilder
{
	public static IsoMap buildMap(Guild guild)
	{
		return new GuildMapBuilder().build(guild);
	}

	public static IsoMap buildPreview(GuildBuildingType type)
	{
		return new GuildMapBuilder().preview(type);
	}

	public static void append(Guild guild, GuildBuilding build)
	{
		new GuildMapBuilder(guild.getMap()).build(guild, build);
	}

	public static void append(Guild guild, ArrayList<GPoint> tunnel)
	{
		new GuildMapBuilder(guild.getMap()).build(guild, tunnel);
	}

	private final IsoSprite floorRock = GameResources.main.floors.get(8); // Каменный пол
	private final IsoSprite wallWood = GameResources.main.walls.get(2); // Деревянные стены
	private final IsoSprite wallRock = GameResources.main.walls.get(13); // Каменные стены
	private final IsoSprite door = GameResources.main.walls.get(5); // Дверь
	private final IsoSprite wallShadow = GameResources.main.walls.get(14); // Каменные стены

	private final IsoSprite build = GameResources.main.objects.get(10005); // Строящееся помещение

	private IsoMap map;
	private MapCell[][] cells;
	private byte[][] buildMap; // 0 - пусто; 1 - тунель; 2 - вход; 3 - строение
	private MoveArea area;

	private GuildMapBuilder()
	{
	}

	private GuildMapBuilder(IsoMap map)
	{
		setMap(map);
	}

	private void setMap(IsoMap map)
	{
		this.map = map;
		buildMap = new byte[map.width][map.height];
		cells = map.cells[0];
	}

	private IsoMap build(Guild guild)
	{
		setMap(new IsoMap(guild.width, guild.height, 1, 0));

		// Ставим пол для туннелей
		for (GPoint p : guild.tunnels)
		{
			buildMap[p.x][p.y] = 1;
			placeFloor(p);
		}

		// Ставим пол для входа, чтобы работал MoveArea
		GuildBuilding entry = null;
		for (GuildBuilding b : guild.buildings)
			if (!b.isBuild() && b.type.isEntry())
			{
				entry = b;
				placeFloor(b);
			}
		int ex = entry.rect.x + entry.rect.width / 2;
		int ey = entry.rect.y + entry.rect.height / 2;
		area = new MoveArea(map, cells[ex][ey]);
		area.build();

		// Ставим пол для остальных помещений
		for (GuildBuilding b : guild.buildings)
			if (!b.isBuild() && !b.type.isEntry())
				placeFloor(b);

		// Ставим все остальное для помещений
		for (GuildBuilding b : guild.buildings)
			place(b);

		// Ставим все остальное для туннелей
		for (GPoint p : guild.tunnels)
			placeWalls(p);

		return map;
	}

	private IsoMap preview(GuildBuildingType type)
	{
		setMap(new IsoMap(6, 6, 1, 0));
		GuildBuilding b = new GuildBuilding(null, new Rectangle(0, 0, 6, 6), type);
		b.build_progress = -1;

		placeFloor(b);
		place(b);

		return map;
	}

	private void build(Guild guild, GuildBuilding build)
	{
		buildGuildMap(guild);

		Rectangle rect = build.rect;

		for (int x = rect.x; x <= rect.right(); x++)
			for (int y = rect.y; y <= rect.bottom(); y++)
			{
				buildMap[x][y] = 0;

				// Удаляем пол и стены туннеля
				MapCell c = cells[x][y];
				if (c.floor != null) c.floor = null;
				if (c.obj != null) c.obj = null;
				if (c.x != build.rect.x)
				{
					if (c.wall_l != null) c.wall_l = null;
					if (c.obj_l != null) c.obj_l = null;
				}
				if (c.y != build.rect.y)
				{
					if (c.wall_r != null) c.wall_r = null;
					if (c.obj_r != null) c.obj_r = null;
				}
			}

		if (!build.isBuild())
		{
			GuildBuilding entry = null; // Находим вход для MoveArea
			for (GuildBuilding b : guild.buildings)
				if (!b.isBuild() && b.type.isEntry())
					entry = b;
			int ex = entry.rect.x; // Считать из центра не получится, там стоят колонны
			int ey = entry.rect.y;
			area = new MoveArea(map, cells[ex][ey]);
			area.build();

			placeFloor(build);
		}

		place(build);

		/*int fx = rect.x - 1;
		int fy = rect.y - 1;
		int rx = rect.right() + 1;
		int ry = rect.bottom() + 1;
		for (GPoint p : guild.tunnels)
		{
			if (p.x == rx || p.y == ry || p.x == fx || p.y == fy)
				placeWalls(p);
		}*/
	}

	private void build(Guild guild, ArrayList<GPoint> tunnel)
	{
		buildGuildMap(guild);

		for (GPoint p : tunnel)
		{
			buildMap[p.x][p.y] = 1;
			placeFloor(p);

			if (p.x > 0 && isTunnelOrEntry(p.x - 1, p.y))
				cells[p.x][p.y].wall_l = null;

			if (p.y > 0 && isTunnelOrEntry(p.x, p.y - 1))
				cells[p.x][p.y].wall_r = null;

			if (p.x + 1 < map.width && isTunnelOrEntry(p.x + 1, p.y))
				cells[p.x + 1][p.y].wall_l = null;

			if (p.y + 1 < map.height && isTunnelOrEntry(p.x, p.y + 1))
				cells[p.x][p.y + 1].wall_r = null;
		}

		for (GPoint p : tunnel)
		{
			placeWalls(p);
		}
	}

	private void buildGuildMap(Guild guild)
	{
		for (GPoint p : guild.tunnels)
		{
			buildMap[p.x][p.y] = 1;
		}

		for (GuildBuilding b : guild.buildings)
		{
			byte v = (byte) (b.type.isEntry() ? 2 : 3);
			for (int x = b.rect.x; x <= b.rect.right(); x++)
				for (int y = b.rect.y; y <= b.rect.bottom(); y++)
				{
					buildMap[x][y] = v;
				}
		}
	}

	private void placeFloor(GPoint p)
	{
		MapCell c = cells[p.x][p.y];
		floorRock.setTo(c);
	}

	private void placeWalls(GPoint p)
	{
		wallUR(p.x, p.y, true);
		wallBR(p.x, p.y, true);
		wallUL(p.x, p.y, true);
		wallBL(p.x, p.y, true);
	}

	private void placeFloor(GuildBuilding b)
	{
		byte v = (byte) (b.type.isEntry() ? 2 : 3);
		for (int x = b.rect.x; x <= b.rect.right(); x++)
			for (int y = b.rect.y; y <= b.rect.bottom(); y++)
			{
				MapCell c = cells[x][y];
				floorRock.setTo(c);
				buildMap[x][y] = v;
			}
	}

	private GuildBuilding building; // Текущая комната
	private boolean[][] partMap; // Карта для текущей комнаты
	private int doorWayCost;
	private MapCell doorCell;
	private int doorPos;

	private void place(GuildBuilding b)
	{
		int minX = b.rect.x;
		int maxX = b.rect.right();
		int minY = b.rect.y;
		int maxY = b.rect.bottom();

		if (b.isBuild())
		{
			for (int x = minX; x <= maxX; x++)
				for (int y = minY; y <= maxY; y++)
				{
					byte st = 8;
					if (x == minX)
					{
						if (y == minY)
							st = 0;
						else if (y == maxY)
							st = 6;
						else
							st = 7;
					}
					else if (x == maxX)
					{
						if (y == minY)
							st = 2;
						else if (y == maxY)
							st = 4;
						else
							st = 3;
					}
					else if (y == minY)
						st = 1;
					else if (y == maxY)
						st = 5;

					build.setTo(cells[x][y], st);
				}
			return;
		}

		building = b;
		boolean isTunnel = b.type.isEntry();

		for (int x = minX; x <= maxX; x++)
		{
			wallUR(x, minY, isTunnel);
			wallBR(x, maxY, isTunnel);
		}

		for (int y = minY; y <= maxY; y++)
		{
			wallUL(minX, y, isTunnel);
			wallBL(maxX, y, isTunnel);
		}

		if (!b.isBuild() && !b.type.isEntry())
			placeDoor(true);

		// Расставляем обстановку
		partMap = new boolean[b.rect.width][b.rect.height];
		b.type.reset();
		while (true)
		{
			boolean end = true;
			for (IGuildPatternSource s : b.type.mapPatterns)
			{
				while (true)
				{
					GuildBuildPattern m = s.getPattern();
					if (m == null) break;

					if (!isTunnel && !m.isDoorPass(doorPos)) // Проходит по положению относительно двери
					{
						s.disable(m);
						continue;
					}

					// Поиск места
					Rectangle place = findPlace(m);
					if (place == null)
					{
						s.disable(m);
						continue;
					}

					// Постановка
					m.map.mixToRandom(map, minX + place.x, minY + place.y);

					end = false;
					break;
				}

				if (!end) break; // Крутим паттерны по-новой
			}

			if (end)
				break;
		}
		partMap = null;
	}

	// Пытается поставить дверь в области
	// strong - true, если рядом не должно быть стены 
	private void placeDoor(boolean strong) // 0 - top, 1 - right, 2 - bottom, 3 - left
	{
		doorPos = -1;
		doorWayCost = -1;
		doorCell = null;

		if (strong)
			checkDoorsStrong();
		else
			checkDoors();

		if (doorCell == null)
		{
			if (strong)
				placeDoor(false);
			else
				System.err.println("No door for " + building + "  " + strong);
			return;
		}

		Rectangle rect = building.rect;
		if (doorCell.x == rect.x - 1)
		{
			doorCell = cells[rect.x][doorCell.y];
			door.setToL(doorCell);
			doorPos = DOOR_LEFT;
		}
		else if (doorCell.x == rect.right() + 1)
		{
			door.setToL(doorCell);
			doorCell = cells[rect.right()][doorCell.y];
			doorPos = DOOR_RIGHT;
		}
		else if (doorCell.y == rect.y - 1)
		{
			doorCell = cells[doorCell.x][rect.y];
			door.setToR(doorCell);
			doorPos = DOOR_TOP;
		}
		else
		{
			door.setToR(doorCell);
			doorCell = cells[doorCell.x][rect.bottom()];
			doorPos = DOOR_BOTTOM;
		}
	}

	private void checkDoors()
	{
		Rectangle rect = building.rect;
		int x, y;
		y = building.rect.y - 1;
		if (y >= 0)
			for (x = rect.x; x <= rect.right(); x++)
			checkDoor(x, y);

		y = rect.bottom() + 1;
		if (y < map.height)
			for (x = rect.x; x <= rect.right(); x++)
			checkDoor(x, y);

		x = rect.x - 1;
		if (x >= 0)
			for (y = rect.y; y <= rect.bottom(); y++)
			checkDoor(x, y);

		x = rect.right() + 1;
		if (x < map.width)
			for (y = rect.y; y <= rect.bottom(); y++)
			checkDoor(x, y);
	}

	private void checkDoorsStrong()
	{
		Rectangle rect = building.rect;
		int x, y;
		y = building.rect.y - 1;
		if (y >= 0)
			for (x = rect.x + 1; x <= rect.right() - 1; x++)
			if (isTunnelOrEntry(x - 1, y) && isTunnelOrEntry(x + 1, y))
				checkDoor(x, y);

		y = rect.bottom() + 1;
		if (y < map.height)
			for (x = rect.x + 1; x <= rect.right() - 1; x++)
			if (isTunnelOrEntry(x - 1, y) && isTunnelOrEntry(x + 1, y))
				checkDoor(x, y);

		x = rect.x - 1;
		if (x >= 0)
			for (y = rect.y + 1; y <= rect.bottom() - 1; y++)
			if (isTunnelOrEntry(x, y - 1) && isTunnelOrEntry(x, y + 1))
				checkDoor(x, y);

		x = rect.right() + 1;
		if (x < map.width)
			for (y = rect.y + 1; y <= rect.bottom() - 1; y++)
			if (isTunnelOrEntry(x, y - 1) && isTunnelOrEntry(x, y + 1))
				checkDoor(x, y);
	}

	/*
	 * Проверяет, можно ли поставить сюда дверь
	 */
	private void checkDoor(int x, int y)
	{
		if (!isTunnelOrEntry(cells[x][y])) return;
		MapCell cell = cells[x][y];
		RouteNode node = area.getCalc(cell);
		if (node != null)
		{
			int cost = node.cost;
			if (doorWayCost == -1 || cost < doorWayCost)
			{
				doorWayCost = cost;
				doorCell = cell;
			}
		}
	}

	private boolean isTunnelOrEntry(MapCell c)
	{
		byte v = buildMap[c.x][c.y];
		return v == 1 || v == 2;
	}

	private boolean isTunnelOrEntry(int x, int y)
	{
		byte v = buildMap[x][y];
		return v == 1 || v == 2;
	}

	private void wallUL(int x, int y, boolean tunnel)
	{
		MapCell c = cells[x][y];

		MapCell n;
		if (x == 0)
			n = null;
		else
			n = cells[x - 1][y];

		if (n == null || n.floor == null)
		{
			if (c.wall_l == null)
				wallRock.setToL(c);
		}
		else if (!(tunnel && isTunnelOrEntry(n)))
		{
			if (c.wall_l == null || c.wall_l.sprite != wallWood)
				wallWood.setToL(c);
		}
	}

	private void wallUR(int x, int y, boolean tunnel)
	{
		MapCell c = cells[x][y];
		MapCell n;
		if (y == 0)
			n = null;
		else
			n = cells[x][y - 1];

		if (n == null || n.floor == null)
		{
			if (c.wall_r == null)
				wallRock.setToR(c);
		}
		else if (!(tunnel && isTunnelOrEntry(n))) // Не ставим стены между входом и тунелем
		{
			if (c.wall_r == null || c.wall_r.sprite != wallWood)
				wallWood.setToR(c);
		}
	}

	private void wallBL(int x, int y, boolean tunnel)
	{
		if (x >= map.width - 1) return;
		MapCell n = cells[x + 1][y];

		if (n.floor == null)
			wallShadow.setToL(n);
		else if (!(tunnel && isTunnelOrEntry(n)))
		{
			if (n.wall_l == null || n.wall_l.sprite != wallWood)
				wallWood.setToL(n);
		}
	}

	private void wallBR(int x, int y, boolean tunnel)
	{
		if (y >= map.height - 1) return;
		MapCell n = cells[x][y + 1];

		if (n.floor == null)
			wallShadow.setToR(n);
		else if (!(tunnel && isTunnelOrEntry(n)))
		{
			if (n.wall_r == null || n.wall_r.sprite != wallWood)
				wallWood.setToR(n);
		}
	}

	private GuildBuildPattern pattern; // Текущий шаблон

	public Rectangle findPlace(GuildBuildPattern p)
	{
		if (p.max != 0 && p.used >= p.max)
			return null;

		pattern = p;
		Rectangle rect = find();
		pattern = null;

		if (rect == null)
			return null;

		p.used++;

		for (int x = rect.x; x <= rect.right(); x++)
			for (int y = rect.y; y <= rect.bottom(); y++)
				partMap[x][y] = true;

		return rect;
	}

	private Rectangle find()
	{
		switch (pattern.anchor)
		{
			case ANCHOR_NONE:
				return findNone();
			case ANCHOR_LEFT:
				return findLeft();
			case ANCHOR_RIGHT:
				return findRight();
			case ANCHOR_TOP:
				return findTop();
			case ANCHOR_BOTTOM:
				return findBottom();
			case ANCHOR_CENTER:
				return findCenter();
			default:
				return null;
		}
	}

	private boolean canPlacePattern(int x, int y)
	{
		for (int dx = 0; dx < pattern.map.width; dx++)
		{
			int tx = x + dx;
			if (tx >= building.rect.width) return false;
			int gx = building.rect.x + tx; // Глобальные координаты

			for (int dy = 0; dy < pattern.map.height; dy++)
			{
				int ty = y + dy;
				if (ty >= building.rect.height) return false;
				if (partMap[tx][ty]) return false;

				int gy = building.rect.y + ty; // Глобальные координаты
				if (doorCell != null && doorCell.x == gx && doorCell.y == gy)
				{
					MapCell c = pattern.map.cells[0][dx][dy]; // Ячейка в шаблоне
					if (c.obj != null) // Нельзя закрывать дверь объектом
						return false;
					if (c.wall_l != null && doorPos == DOOR_LEFT)
						return false;
					if (c.wall_r != null && doorPos == DOOR_TOP)
						return false;
				}
			}
		}
		return true;
	}

	private Rectangle findCenter()
	{
		int x = building.rect.width / 2 - pattern.map.width / 2;
		int y = building.rect.height / 2 - pattern.map.height / 2;

		if (!canPlacePattern(x, y))
			return null;

		return new Rectangle(x, y, pattern.map.width, pattern.map.height);
	}

	private Rectangle findNone()
	{
		int w = partMap.length;
		int h = partMap[0].length;

		for (int x = 0; x < w - pattern.map.width + 1; x++)
			for (int y = 0; y < h - pattern.map.height + 1; y++)
			{
				if (!canPlacePattern(x, y))
					continue;

				return new Rectangle(x, y, pattern.map.width, pattern.map.height);
			}

		return null;
	}

	private Rectangle findLeft()
	{
		int h = partMap[0].length;
		int x = 0;

		for (int y = 0; y < h - pattern.map.height + 1; y++)
		{
			if (!canPlacePattern(x, y))
				continue;

			return new Rectangle(x, y, pattern.map.width, pattern.map.height);
		}

		return null;
	}

	private Rectangle findRight()
	{
		int h = partMap[0].length;
		int x = partMap.length - 1;

		for (int y = 0; y < h - pattern.map.height + 1; y++)
		{
			if (!canPlacePattern(x, y))
				continue;

			return new Rectangle(x, y, pattern.map.width, pattern.map.height);
		}

		return null;
	}

	private Rectangle findTop()
	{
		int w = partMap.length;
		int y = 0;

		for (int x = 0; x < w - pattern.map.width + 1; x++)
		{
			if (!canPlacePattern(x, y))
				continue;

			return new Rectangle(x, y, pattern.map.width, pattern.map.height);
		}

		return null;
	}

	private Rectangle findBottom()
	{
		int w = partMap.length;
		int y = partMap[0].length - 1;

		for (int x = 0; x < w - pattern.map.width + 1; x++)
		{
			if (!canPlacePattern(x, y))
				continue;

			return new Rectangle(x, y, pattern.map.width, pattern.map.height);
		}

		return null;
	}

	public static final int ANCHOR_NONE = 0;
	public static final int ANCHOR_LEFT = 1;
	public static final int ANCHOR_TOP = 2;
	public static final int ANCHOR_RIGHT = 4;
	public static final int ANCHOR_BOTTOM = 8;
	public static final int ANCHOR_CENTER = 16;

	public static final int DOOR_TOP = 1;
	public static final int DOOR_BOTTOM = 2;
	public static final int DOOR_LEFT = 4;
	public static final int DOOR_RIGHT = 1;
	public static final int DOOR_ALL = DOOR_TOP | DOOR_BOTTOM | DOOR_LEFT | DOOR_RIGHT;
}
