package com.deadman.dh.global;

public class Glyph implements Comparable<Glyph>
{
	public int x, y;

	public int type;
	public int subType;

	public static final int T_MOUNT = 0;
	public static final int T_BIGMOUNT = 1;
	public static final int T_TREE = 2;
	public static final int T_SEA = 3;

	public static final int TYPES_COUNT = 4;

	@Override
	public int compareTo(Glyph o)
	{
		return y - o.y;
	}
}
