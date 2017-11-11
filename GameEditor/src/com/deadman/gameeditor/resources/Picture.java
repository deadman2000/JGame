package com.deadman.gameeditor.resources;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class Picture extends Drawable {
	public Image image;
	public int anchorX, anchorY;
	public int width, height;

	public Picture(Image image, int anchorX, int anchorY) {
		this.image = image;
		this.anchorX = anchorX;
		this.anchorY = anchorY;

		if (image != null) {
			ImageData imgData = image.getImageData();
			width = imgData.width;
			height = imgData.height;
		}
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(-anchorX, -anchorY, width, height);
	}

	/**
	 * Автокадрирование
	 */
	public void crop() {
		if (image == null)
			return;
		
		int top = 0, left = 0, bottom = height - 1, right = width - 1;

		ImageData imgData = image.getImageData();

		// Find top
		boolean isBreak = false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (imgData.getAlpha(x, y) != 0) {
					top = y;
					isBreak = true;
					break;
				}
			}

			if (isBreak)
				break;
		}

		if (!isBreak)
			return; // is full transparent image

		// Find bottom
		isBreak = false;
		for (int y = height - 1; y > top; y--) {
			for (int x = 0; x < width; x++) {
				if (imgData.getAlpha(x, y) != 0) {
					bottom = y;
					isBreak = true;
					break;
				}
			}

			if (isBreak)
				break;
		}
		if (!isBreak)
			bottom = top;

		// Find left
		isBreak = false;
		for (int x = 0; x < width; x++) {
			for (int y = top; y <= bottom; y++) {
				if (imgData.getAlpha(x, y) != 0) {
					left = x;
					isBreak = true;
					break;
				}
			}

			if (isBreak)
				break;
		}

		// Find right
		isBreak = false;
		for (int x = width - 1; x > left; x--) {
			for (int y = top; y <= bottom; y++) {
				if (imgData.getAlpha(x, y) != 0) {
					right = x;
					isBreak = true;
					break;
				}
			}

			if (isBreak)
				break;
		}
		if (!isBreak)
			right = left;

		if (left == 0 && top == 0 && right == width && bottom == height)
			return;

		width = right - left + 1;
		height = bottom - top + 1;

		ImageData croppedId = new ImageData(width, height, imgData.depth, imgData.palette);

		for (int x = left; x <= right; x++)
			for (int y = top; y <= bottom; y++) {
				croppedId.setPixel(x - left, y - top, imgData.getPixel(x, y));
				croppedId.setAlpha(x - left, y - top, imgData.getAlpha(x, y));
			}

		Image cropped = new Image(Display.getDefault(), croppedId);

		image = cropped;
		anchorX -= left;
		anchorY -= top;
	}

	public void save(String path) throws IOException {
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] { image.getImageData() };
		imageLoader.save(path, SWT.IMAGE_PNG);
	}

}
