package com.deadman.gameeditor.resources;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class Layer extends Picture {
	public final String name;

	public int posX = 0, posY = 0;

	public int globalX = -1, globalY = -1; // Позиция на общем пано

	public LayerEntry res;

	public Layer(String name, BufferedImage img, int x, int y) {
		super(convertToSWT(img), -x, -y);
		this.name = name;

		posX = x;
		posY = y;
	}
	
	public int absoluteX()
	{
		return posX - anchorX;
	}
	
	public int absoluteY()
	{
		return posY - anchorY;
	}

	static Image toImage(ImageData data) {
		return new Image(Display.getDefault(), data);
	}

	static Image convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(),
					colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int rgb = bufferedImage.getRGB(x, y);
					int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
					data.setPixel(x, y, pixel);
					if (colorModel.hasAlpha()) {
						data.setAlpha(x, y, (rgb >> 24) & 0xFF);
					}
				}
			}
			return toImage(data);
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return toImage(data);
		}
		System.err.println("Unsupported color model: " + bufferedImage.getColorModel());
		return null;
	}

	// Группа

	public ArrayList<Layer> layers;

	@Override
	public String getLabel() {
		return name;
	}

	/**
	 * Конструктор для группы
	 * 
	 * @param name
	 */
	public Layer(String name, int x, int y) {
		super(null, 0, 0);
		this.name = name;
		posX = x;
		posY = y;

		layers = new ArrayList<>();
	}

	public void addLayer(Layer layer) {
		layers.add(layer);
	}

	public void printTree(int level) {
		for (int i = 0; i < level; i++)
			System.out.print("\t");

		if (layers != null) {
			System.out.println("[" + name + "]");
			for (Layer layer : layers)
				layer.printTree(level + 1);
		} else
			System.out.println(name);
	}

	static class LayerHeightComparator implements Comparator<Layer> {
		@Override
		public int compare(Layer o1, Layer o2) {
			int i = o2.height - o1.height;
			if (i != 0)
				return i;
			return o2.width - o1.width;
		}

	}

	public static void sortByHeight(ArrayList<Layer> layers) {
		Collections.sort(layers, new LayerHeightComparator());
	}

	// Анимация

	public AnimationImages getAnimation() {
		ArrayList<Frame> frames = new ArrayList<>();
		for (Layer l : layers) {
			int num = -1, delay = -1;

			String[] parts = l.name.split("-");
			for (int i = 1; i < parts.length; i++) {
				String part = parts[i];
				if (part.startsWith("f")) {
					try {
						num = Integer.parseInt(part.substring(1));
					} catch (Exception ex) {
						continue;
					}
				} else if (part.startsWith("d")) {
					try {
						delay = Integer.parseInt(part.substring(1));
					} catch (Exception ex) {
						continue;
					}
				}
			}

			if (num == -1 || delay == -1)
				continue;
			// System.err.println("Wrong animation layer " + l.name);

			frames.add(new Frame(num, delay, l));
		}
		if (frames.size() == 0)
			return null;

		Collections.sort(frames);
		return new AnimationImages(frames);
	}

	@Override
	public String toString() {
		return name + " " + posX + "x" + posY + " " + width + "x" + height + " " + anchorX + ":" + anchorY;
	}
}
