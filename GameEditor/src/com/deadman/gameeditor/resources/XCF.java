package com.deadman.gameeditor.resources;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

// https://git.gnome.org/browse/gimp/plain/devel-docs/xcf.txt?h=gimp-2-8
// http://api.kde.org/4.x-api/kdelibs-apidocs/kimgio/html/gimp_8h.html
// https://github.com/GNOME/gimp
public class XCF
{
	private HashMap<String, Layer> allLayers = new HashMap<>();
	private ArrayList<Layer> layers = new ArrayList<>(); // Верхние слои
	private int[] palette = null;

	private boolean debug = false;

	public XCF()
	{
	}

	public Collection<Layer> getLayers()
	{
		return allLayers.values();
	}

	public Layer getLayer(String name)
	{
		for (Layer l : allLayers.values())
			if (l.name.equals(name))
				return l;
		System.err.println("Layer " + name + " not found");
		return null;
	}

	public Drawable getDrawable(String name)
	{
		Layer l = allLayers.get(name);
		if (l == null)
			return null;

		if (l.layers != null) // Группа слоев
			return l.getAnimation();
		else
			return l;
	}

	public static XCF loadFile(String fileName)
	{
		XCF file = new XCF();
		try
		{
			file.load(fileName);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
		return file;
	}

	public void loadIndexed(String fileName, int... pal) throws IOException
	{
		palette = pal;
		load(fileName);
	}

	private RandomAccessFile f;

	private void load(String fileName) throws IOException
	{
		f = new RandomAccessFile(fileName, "r");
		try
		{
			byte[] head = new byte[9];
			f.read(head);
			if (!new String(head).equals("gimp xcf "))
				throw new IOException("Is not GIMP");

			f.skipBytes(5);
			int width = f.readInt();
			int height = f.readInt();
			int base_type = f.readInt();
			if (debug)
			{
				System.out.print(String.format("Size: %d x %d   ", width, height));
				switch (base_type)
				{
					case GimpImageBaseType.RGB:
						System.out.println("RGB");
						break;
					case GimpImageBaseType.GRAY:
						System.out.println("Grayscale");
						break;
					case GimpImageBaseType.INDEXED:
						System.out.println("Indexed");
						break;

					default:
						System.err.println("Unknown " + base_type);
						break;
				}

				System.out.println("Properties:");
			}

			int[] colorMap = null;
			IndexColorModel cm = null;

			while (true) // Properties
			{
				int type_number = f.readInt();
				int size = f.readInt();
				if (type_number == 0)
					break;

				if (debug) System.out.println(String.format("Type: %d   Size: %d", type_number, size));
				switch (type_number)
				{
					case PropType.PROP_COLORMAP:
						if (palette != null)
						{
							f.skipBytes(size);
							break;
						}
						int colorsCount = f.readInt();
						colorMap = new int[colorsCount];
						byte[] rA = new byte[colorsCount];
						byte[] gA = new byte[colorsCount];
						byte[] bA = new byte[colorsCount];
						for (int i = 0; i < colorsCount; i++)
						{
							int r = (rA[i] = f.readByte()) & 0xff;
							int g = (gA[i] = f.readByte()) & 0xff;
							int b = (bA[i] = f.readByte()) & 0xff;
							colorMap[i] = (r << 16) | (g << 8) | b;
						}
						cm = new IndexColorModel(8, colorsCount, rA, gA, bA);
						break;
					default:
						if (debug)
						{
							byte[] data = readBytes(size);
							System.out.println(getHexString(data));
							System.out.println(getPrintableString(data));
						}
						else
						{
							f.skipBytes(size);
						}
						break;
				}
			}

			ArrayList<Integer> layerOffsets = new ArrayList<>();
			while (true)
			{
				int offset = f.readInt();
				if (offset == 0)
					break;
				layerOffsets.add(offset);
			}

			// Channels
			/*int chInd = 0;
			while (true)
			{
				int offset = f.readInt();
				if (offset == 0)
					break;
				chInd++;
			}*/

			for (int i = 0; i < layerOffsets.size(); i++)
			{
				f.seek(layerOffsets.get(i)); // Jump to layer

				int lW = f.readInt();
				int lH = f.readInt();
				int colorMode = f.readInt();
				String layerName = readString();

				if (debug) System.out.println(String.format("%s\t%dx%d Color mode:%d", layerName, lW, lH, colorMode));

				boolean isGroup = false;
				int posX = 0, posY = 0;
				Layer parent = null;
				float opacity = 1f;

				while (true)
				{
					int type_number = f.readInt();
					int size = f.readInt();
					if (type_number == 0)
						break;

					//if(debug)System.out.println(String.format("\tType: %d   Size: %d", type_number, size));
					switch (type_number)
					{
						case PropType.PROP_OPACITY:
							opacity = Math.round(f.readInt() * 1000 / 255) / 1000f;
							break;

						case PropType.PROP_OFFSETS:
							posX = f.readInt();
							posY = f.readInt();
							break;

						case PropType.PROP_GROUP_ITEM:
							//System.out.println(layerName);
							isGroup = true;
							break;
						case PropType.PROP_ITEM_PATH:
							for (int k = 0; k < size / 4 - 1; k++)
							{
								int p = f.readInt();
								if (parent == null)
									parent = layers.get(p);
								else
									parent = parent.layers.get(p);
							}
							f.skipBytes(4);
							break;

						default:
							if (debug)
							{
								byte[] data = readBytes(size);
								System.out.println(String.format("\tProp %d\n\t\t%s\n\t\t%s", type_number, getHexString(data), getPrintableString(data)));
							}
							else
								f.skipBytes(size);
							break;
					}
				}

				if (debug) System.out.println(String.format("\tAnchor: %d x %d   Opacity: %f", posX, posY, opacity));

				if (isGroup)
				{
					Layer group = new Layer(layerName, posX, posY);
					if (parent != null)
						parent.addLayer(group);
					else
						layers.add(group);
					allLayers.put(group.name, group);
					continue;
				}

				int hptr = f.readInt();
				//int mptr = f.readInt();

				f.seek(hptr); // Jump to hierarchy structure
				int aW = f.readInt();
				int aH = f.readInt();
				int bpp = f.readInt();

				ArrayList<Integer> levels = new ArrayList<>();
				while (true)
				{
					int lptr = f.readInt();
					if (lptr == 0)
						break;
					levels.add(lptr);
				}
				if (debug) System.out.println(String.format("\tSize: %dx%d  bpp: %d  Levels: %d", aW, aH, bpp, levels.size()));

				BufferedImage img;
				int colorType = getColorType(colorMode);
				if (colorType == BufferedImage.TYPE_BYTE_BINARY || colorType == BufferedImage.TYPE_BYTE_INDEXED)
					img = new BufferedImage(lW, lH, colorType, cm);
				else
					img = new BufferedImage(lW, lH, colorType);

				//for (int l = 0; l < levels.size(); l++)
				int l = 0;
				{
					f.seek(levels.get(l)); // Jump to level

					int lvlW = f.readInt();
					int lvlH = f.readInt();

					if (debug) System.out.println(String.format("\t\tLvl: %d  Size: %dx%d", levels.get(l), lvlW, lvlH));

					ArrayList<Integer> tiles = new ArrayList<>();
					while (true)
					{
						int tptr = f.readInt();
						if (tptr == 0)
							break;
						tiles.add(tptr);
					}

					int rX = 0, rY = 0;

					for (int t = 0; t < tiles.size(); t++)
					{
						byte[][] tileData = new byte[bpp][];
						f.seek(tiles.get(t)); // Jump to tile

						int rW = 64, rH = 64;
						if (rX + rW > lvlW)
							rW = lvlW % 64;
						if (rY + rH > lvlH)
							rH = lvlH % 64;

						if (debug) System.out.println("\t\tRect: [" + rX + ":" + rY + ":" + rW + ":" + rH + "]");

						for (int b = 0; b < bpp; b++)
						{
							int size = rW * rH;
							ByteBuffer bb = ByteBuffer.allocate(size);

							while (bb.remaining() > 0)
							{
								int opcode = f.readByte() & 0xff;

								if (opcode < 127)
								{
									opcode++;

									size -= opcode;
									if (size < 0)
										throw new IOException("Wrong RLE");

									byte v = f.readByte();
									for (int n = 0; n < opcode; n++)
										bb.put(v);
								}
								else if (opcode == 127)
								{
									int count = f.readShort() & 0xFFFF;
									byte v = f.readByte();

									size -= count;
									if (size < 0)
										throw new IOException("Wrong RLE " + bb.remaining());

									for (int n = 0; n < count; n++)
										bb.put(v);
								}
								else
								{
									int count;
									if (opcode == 128)
										count = f.readShort() & 0xFFFF;
									else
										count = 256 - opcode;

									size -= count;
									if (size < 0)
										throw new IOException("Wrong RLE");

									bb.put(readBytes(count));
								}
							}

							if (bb.remaining() != 0)
								throw new IOException("Wrong RLE " + bb.remaining());

							tileData[b] = bb.array();
						}

						for (int x = 0; x < rW; x++)
							for (int y = 0; y < rH; y++)
							{
								int ind = x + y * rW;
								int c = getColor(colorMode, tileData, ind, colorMap);
								if (opacity != 1f && c != 0)
									c = setAlpha(c, opacity);
								img.setRGB(rX + x, rY + y, c);
							}

						rX += 64;
						if (rX >= lvlW)
						{
							rX = 0;
							rY += 64;
						}
					}
				}

				Layer layer = new Layer(layerName, img, posX, posY);
				if (parent != null)
					parent.addLayer(layer);
				else
					layers.add(layer);

				allLayers.put(layer.name, layer);
			}
		}
		finally
		{
			f.close();
			f = null;
		}

		// Apply anchor
		Layer lAnchor = allLayers.get("anchor");
		if (lAnchor != null)
		{
			if (palette == null)
				lAnchor.crop();
			int dx = lAnchor.anchorX;
			int dy = lAnchor.anchorY;
			if (debug) System.out.println("Anchor: [" + dx + ":" + dy + "]");

			for (Layer l : allLayers.values())
			{
				if (l == lAnchor) continue;
				if (palette == null)
					l.crop();
				l.anchorX -= dx;
				l.anchorY -= dy;
			}
		}
		else
		{
			for (Layer l : allLayers.values())
			{
				l.anchorX = 0;
				l.anchorY = 0;
				if (palette == null)
					l.crop();
			}
		}
	}

	private int setAlpha(int c, float opacity)
	{
		return (c & 0xFFFFFF) | (((int) (((c >> 24) & 0xFF) * opacity) & 0xFF) << 24);
	}

	private int getColorType(int colorMode)
	{
		if (palette != null)
			return BufferedImage.TYPE_INT_ARGB;

		switch (colorMode)
		{
			case GimpImageType.RGB_GIMAGE: // RGB color without alpha
				return BufferedImage.TYPE_INT_RGB;
			case GimpImageType.RGBA_GIMAGE: // RGB color with alpha
				return BufferedImage.TYPE_INT_ARGB;
			case GimpImageType.GRAY_GIMAGE: // Grayscale without alpha
				return BufferedImage.TYPE_BYTE_GRAY;
			case GimpImageType.GRAYA_GIMAGE: // Grayscale with alpha
				return BufferedImage.TYPE_INT_ARGB;
			case GimpImageType.INDEXED_GIMAGE: // Indexed without alpha
				return BufferedImage.TYPE_BYTE_INDEXED;
			case GimpImageType.INDEXEDA_GIMAGE: // Indexed with alpha
				return BufferedImage.TYPE_INT_ARGB;
			default:
				System.err.println("Unknown layer color mode: " + colorMode);
				return BufferedImage.TYPE_INT_ARGB;
		}
	}

	private int getColor(int colorMode, byte[][] tileData, int ind, int[] colorMap)
	{
		if (palette != null)
		{
			int cind = tileData[0][ind] & 0xFF;
			if (cind == 0)
				return 0; // transparent
			return 0xFF000000 | palette[cind - 1];
		}

		switch (colorMode)
		{
			case GimpImageType.RGB_GIMAGE:
				return ((tileData[0][ind] & 0xFF) << 16) | ((tileData[1][ind] & 0xFF) << 8) | (tileData[2][ind] & 0xFF); // RGB
			case GimpImageType.RGBA_GIMAGE:
				return ((tileData[0][ind] & 0xFF) << 16) | ((tileData[1][ind] & 0xFF) << 8) | (tileData[2][ind] & 0xFF) | ((tileData[3][ind] & 0xFF) << 24); // ARGB
			case GimpImageType.GRAY_GIMAGE:
				return tileData[0][ind] & 0xFF;
			case GimpImageType.GRAYA_GIMAGE:
				int g = tileData[0][ind] & 0xFF;
				int a = tileData[1][ind] & 0xFF;
				return (a << 24) | (g << 16) | (g << 8) | g;
			case GimpImageType.INDEXED_GIMAGE:
				return tileData[0][ind] & 0xFF;
			case GimpImageType.INDEXEDA_GIMAGE:
				return colorMap[tileData[0][ind] & 0xFF] | ((tileData[1][ind] & 0xFF) << 24);
			default:
				return 0;
		}
	}

	private byte[] readBytes(int count) throws IOException
	{
		byte[] buff = new byte[count];
		f.read(buff);
		return buff;
	}

	private String readString() throws IOException
	{
		int length = f.readInt();
		byte[] bytes = new byte[length - 1];
		f.read(bytes);
		f.read();
		return new String(bytes);
	}

	private static String getHexString(byte[] data)
	{
		return getHexString(data, 0, data.length);
	}

	private static String getHexString(byte[] data, int from, int to)
	{
		StringBuffer str = new StringBuffer();

		for (int i = from; i < to && i < data.length; i++)
		{
			str.append(String.format("%02X", data[i]));
			if (i < to - 1 && i < data.length - 1)
				str.append(" ");
		}

		return str.toString();
	}

	private static String getPrintableString(byte[] data)
	{
		return getPrintableString(data, 0, data.length);
	}

	private static String getPrintableString(byte[] data, int from, int to)
	{
		StringBuffer str = new StringBuffer();

		for (int i = from; i < to && i < data.length; i++)
		{
			if (data[i] >= 0x20)
				str.append((char) data[i]);
			else
				str.append('.');
		}

		return str.toString();
	}

	static final class PropType
	{
		static final int PROP_END = 0;
		static final int PROP_COLORMAP = 1;
		static final int PROP_ACTIVE_LAYER = 2;
		static final int PROP_ACTIVE_CHANNEL = 3;
		static final int PROP_SELECTION = 4;
		static final int PROP_FLOATING_SELECTION = 5;
		static final int PROP_OPACITY = 6;
		static final int PROP_MODE = 7;
		static final int PROP_VISIBLE = 8;
		static final int PROP_LINKED = 9;
		static final int PROP_PRESERVE_TRANSPARENCY = 10;
		static final int PROP_APPLY_MASK = 11;
		static final int PROP_EDIT_MASK = 12;
		static final int PROP_SHOW_MASK = 13;
		static final int PROP_SHOW_MASKED = 14;
		static final int PROP_OFFSETS = 15;
		static final int PROP_COLOR = 16;
		static final int PROP_COMPRESSION = 17;
		static final int PROP_GUIDES = 18;
		static final int PROP_RESOLUTION = 19;
		static final int PROP_TATTOO = 20;
		static final int PROP_PARASITES = 21;
		static final int PROP_UNIT = 22;
		static final int PROP_PATHS = 23;
		static final int PROP_USER_UNIT = 24;
		static final int PROP_VECTORS = 25;
		static final int PROP_TEXT_LAYER_FLAGS = 26;
		static final int PROP_SAMPLE_POINTS = 27;
		static final int PROP_LOCK_CONTENT = 28;
		static final int PROP_GROUP_ITEM = 29;
		static final int PROP_ITEM_PATH = 30;
		static final int PROP_GROUP_ITEM_FLAGS = 31;
		static final int PROP_LOCK_POSITION = 32;
		static final int PROP_FLOAT_OPACITY = 33;
		static final int PROP_COLOR_TAG = 34;
	}

	static final class GimpImageType
	{
		static final int RGB_GIMAGE = 0;
		static final int RGBA_GIMAGE = 1;
		static final int GRAY_GIMAGE = 2;
		static final int GRAYA_GIMAGE = 3;
		static final int INDEXED_GIMAGE = 4;
		static final int INDEXEDA_GIMAGE = 5;
	}

	static final class GimpImageBaseType
	{
		static final int RGB = 0;
		static final int GRAY = 1;
		static final int INDEXED = 2;
	}
}
