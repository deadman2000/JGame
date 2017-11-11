package com.deadman.dh.isometric;

import java.io.IOException;
import java.util.HashMap;

import com.deadman.jgame.io.DataInputStreamBE;
import com.deadman.jgame.io.DataOutputStream;

public abstract class MapFormat
{
	protected static int HEADER = 0x4d415033; // MAP3

	private static MapFormat[] ALL = { new MapFormat1() };
	private static MapFormat0 MF_ZERO = new MapFormat0();
	private static MapFormat MF_LAST;
	private static HashMap<Integer, MapFormat> MF_BYVER;

	static
	{
		MF_LAST = ALL[0];
		MF_BYVER = new HashMap<Integer, MapFormat>();
		for (MapFormat f : ALL)
		{
			if (f.version() > MF_LAST.version())
				MF_LAST = f;
			MF_BYVER.put(f.version(), f);
		}
	}

	protected abstract int version();

	public static void save(IsoMap map, DataOutputStream out) throws IOException
	{
		out.writeInt(HEADER);
		out.writeShort(MF_LAST.version());
		MF_LAST.saveMap(map, out);
	}

	protected abstract void saveMap(IsoMap map, DataOutputStream out) throws IOException;

	public static IsoMap load(DataInputStreamBE in) throws Exception
	{
		MapFormat format;

		int head = in.readInt();
		if (head != HEADER)
			return MF_ZERO.loadMap(head, in);

		int version = in.readShort();
		format = MF_BYVER.get(version);
		if (format == null)
			throw new Exception("Wrong version number " + version);

		return format.loadMap(in);
	}

	protected abstract IsoMap loadMap(DataInputStreamBE in) throws IOException;
}
