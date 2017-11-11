package com.deadman.gameeditor.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class PicPartEntry extends ResourceEntry {
	public ArrayList<PicturePart> parts = new ArrayList<>();

	public PicPartEntry(GameResources res, String name, String path) {
		super(res, PICPARTS, name, path);
	}

	@Override
	public void writeCSV(StringBuilder str) {
		super.writeCSV(str);
		for (PicturePart p : parts)
			str.append(';').append(p.x).append(',').append(p.y).append(',').append(p.width).append(',').append(p.height).append(',').append(p.aX).append(',').append(p.aY);
	}

	public static PicPartEntry fromPpr(GameResources resources, String entryName, IFile file) {
		String picName = file.getName();
		picName = picName.substring(0, picName.length() - file.getFileExtension().length()) + "png";
		IResource pic = file.getParent().findMember(picName);
		if (pic == null) {
			System.err.println("PPR picture not found " + picName);
			return null;
		}

		PicPartEntry ent = new PicPartEntry(resources, entryName, pic.getProjectRelativePath().toString());

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(";");
				if (parts.length == 6) {
					PicturePart pp = new PicturePart(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
					ent.parts.add(pp);
				}
			}
			br.close();
		} catch (CoreException | NumberFormatException | IOException e) {
			e.printStackTrace();
			return null;
		}

		return ent;
	}

	public static PicPartEntry fromLayers(GameResources resources, String entryName, String path, Layer layer, Layer source) {
		int x1 = layer.absoluteX() - source.absoluteX();
		int x2 = x1 + layer.width;
		int w3 = source.width - x2;
		
		int y1 = layer.absoluteY() - source.absoluteY();
		int y2 = y1 + layer.height;
		int h3 = source.height - y2;

		PicPartEntry ent = new PicPartEntry(resources, entryName, path);

		ent.parts.add(new PicturePart(0, 0, x1, y1, 0, 0));  // 0:0
		ent.parts.add(new PicturePart(x1, 0, layer.width, y1, 0, 0)); // 1:0
		ent.parts.add(new PicturePart(x2, 0, w3, y1, 0, 0)); // 2:0

		ent.parts.add(new PicturePart(x2, y1, w3, layer.height, 0, 0)); // 2:1

		ent.parts.add(new PicturePart(x2, y2, w3, h3, 0, 0)); // 2:2
		ent.parts.add(new PicturePart(x1, y2, layer.width, h3, 0, 0)); // 1:2
		ent.parts.add(new PicturePart(0, y2, x1, h3, 0, 0));  // 0:2
		
		ent.parts.add(new PicturePart(0, y1, x1, layer.height, 0, 0));  // 0:1
		
		ent.parts.add(new PicturePart(x1, y1, layer.width, layer.height, 0, 0)); // 1:1

		return ent;
	}
}
