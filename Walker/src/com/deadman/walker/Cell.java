package com.deadman.walker;

import java.awt.Point;

import com.jogamp.opengl.GL2;

import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;

public class Cell
{
	/**
	 * Стены
	 */
	public Wall[] walls;

	/**
	 * Предметы
	 */
	private ItemStack[] items;

	public Cell()
	{
	}

	public boolean canMove(Direction dir)
	{
		return true;
	}

	public boolean isEmpty()
	{
		return true;
	}

	public void setWall(int dir, Wall wall)
	{
		if (walls == null)
			walls = new Wall[4];
		walls[dir] = wall;
	}

	public void setWall(int dir, WallType type)
	{
		setWall(dir, new Wall(type));
	}

	public void addItem(int corner, Item it)
	{
		if (items == null)
			items = new ItemStack[4];
		if (items[corner] == null)
			items[corner] = new ItemStack();

		items[corner].add(it);
	}

	// Drawing

	/**
	 * Отрисовка ячейки
	 * @param dir Направление обзора
	 * @param pos Позиция ячейки относительно игрока
	 * @param x Координата отрисовки X
	 * @param y Координата отрисовки Y
	 */
	public void draw(Direction dir, Shift pos, int x, int y)
	{
		if (walls != null)
		{
			switch (pos)
			{
				case NEAR_LEFT:
					drawWall(dir.index, 1, x - 148, y);
					break;
				case HERE:
					drawWall(dir.index, 1, x, y);
					drawWall(dir.left().index, 0, x, y);
					drawWallM(dir.right().index, 0, x + 173, y);
					break;
				case NEAR_RIGHT:
					drawWall(dir.index, 1, x + 148, y);
					break;

				case AHEAD_LEFT:
					drawWall(dir.index, 3, x - 76, y);
					break;
				case AHEAD:
					drawWall(dir.index, 3, x, y);
					drawWall(dir.left().index, 2, x, y);
					drawWallM(dir.right().index, 2, x + 112, y);
					break;
				case AHEAD_RIGHT:
					drawWall(dir.index, 3, x + 76, y);
					break;

				case FAR_LEFT_LEFT:
					drawWall(dir.index, 6, x - 80, y);
					break;
				case FAR_LEFT:
					drawWall(dir.index, 6, x - 40, y);
					drawWall(dir.left().index, 4, x, y);
					break;
				case FAR:
					drawWall(dir.index, 6, x, y);
					drawWall(dir.left().index, 5, x, y);
					drawWallM(dir.right().index, 5, x + 58, y);
					break;
				case FAR_RIGHT:
					drawWall(dir.index, 6, x + 40, y);
					drawWallM(dir.right().index, 4, x + 158, y);
					break;
				case FAR_RIGHT_RIGHT:
					drawWall(dir.index, 6, x + 80, y);
					break;

				default:
					break;
			}
		}

		if (items != null)
		{
			switch (pos)
			{
				case NEAR_LEFT:
					drawItems(dir.right().index, 0, x - 24, y + 127); // draw right
					break;
				case HERE:
					drawItems(dir.index, 0, x + 57, y + 127); // draw left
					drawItems(dir.right().index, 0, x + 140, y + 127); // draw right
					break;
				case NEAR_RIGHT:
					drawItems(dir.index, 0, x + 221, y + 127); // draw left
					break;

				case AHEAD_LEFT:
					drawItems(dir.right().index, 2, x + 29, y + 90); // draw far right
					drawItems(dir.opposite().index, 1, x + 7, y + 105); // draw near right
					break;
				case AHEAD:
					drawItems(dir.index, 2, x + 76, y + 90); // draw far left
					drawItems(dir.right().index, 2, x + 121, y + 90); // draw far right
					drawItems(dir.left().index, 1, x + 68, y + 105); // draw near left
					drawItems(dir.opposite().index, 1, x + 129, y + 105); // draw near right
					break;
				case AHEAD_RIGHT:
					drawItems(dir.index, 2, x + 167, y + 90); // draw far left
					drawItems(dir.left().index, 1, x + 190, y + 105); // draw near left
					break;

				case FAR_LEFT:
					drawItems(dir.index, 4, x + 39, y + 68); // draw far left
					drawItems(dir.right().index, 4, x + 62, y + 68); // draw far right
					drawItems(dir.left().index, 3, x + 17, y + 76); // draw near left
					drawItems(dir.opposite().index, 3, x + 50, y + 76); // draw near right
					break;
				case FAR:
					drawItems(dir.index, 4, x + 87, y + 68); // draw far left
					drawItems(dir.right().index, 4, x + 110, y + 68); // draw far right
					drawItems(dir.left().index, 3, x + 83, y + 76); // draw near left
					drawItems(dir.opposite().index, 3, x + 114, y + 76); // draw near right
					break;
				case FAR_RIGHT:
					drawItems(dir.index, 4, x + 135, y + 68); // draw far left
					drawItems(dir.right().index, 4, x + 158, y + 68); // draw far right
					drawItems(dir.left().index, 3, x + 147, y + 76); // draw near left
					drawItems(dir.opposite().index, 3, x + 180, y + 76); // draw near right
					break;

				default:
					break;
			}
		}
	}

	private void drawItems(int index, int distance, int x, int y)
	{
		ItemStack st = items[index];
		if (st == null) return;
		if (WalkerEngine.fog)
			setFog(distance);
		else
			setShadow(distance);

		st.drawAt(x, y, distance);

		if (WalkerEngine.fog)
		{
			GameScreen.gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
			GameScreen.gl.glColor4f(1.0f, 1.0f, 1.0f, 1f);
		}
		else
			GameScreen.screen.setBrightness(1f);
	}

	private void setFog(int distance)
	{
		GameScreen.gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
		GameScreen.gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_ADD_SIGNED);
		switch (distance)
		{
			case 1:
				GameScreen.gl.glColor3f(0.6f, 0.6f, 0.62f);
				break;
			case 2:
				GameScreen.gl.glColor3f(0.65f, 0.65f, 0.67f);
				break;
			case 3:
				GameScreen.gl.glColor3f(0.7f, 0.7f, 0.71f);
				break;
			case 4:
				GameScreen.gl.glColor3f(0.8f, 0.8f, 0.82f);
				break;
			default:
				GameScreen.gl.glColor3f(0.5f, 0.5f, 0.5f);
				break;
		}
	}

	private void setShadow(int distance)
	{
		switch (distance)
		{
			case 1:
				GameScreen.screen.setBrightness(0.7f);
				return;
			case 2:
				GameScreen.screen.setBrightness(0.6f);
				return;
			case 3:
				GameScreen.screen.setBrightness(0.5f);
				return;
			case 4:
				GameScreen.screen.setBrightness(0.4f);
				return;
			default:
				return;
		}
	}

	private void drawWall(int index, int pic_index, int x, int y)
	{
		Wall wall = walls[index];
		if (wall == null || wall.type == null) return;
		Drawable pic = wall.type.pic[pic_index];
		if (pic != null)
			pic.drawAt(x, y);
	}

	private void drawWallM(int index, int pic_index, int x, int y)
	{
		Wall wall = walls[index];
		if (wall == null || wall.type == null) return;
		Drawable pic = wall.type.pic[pic_index];
		if (pic != null)
			pic.drawMHAt(x, y);
	}

	// END Drawing

	public boolean clickHere(Point p, Direction dir)
	{
		if (ItemSlot.pickedItem() != null) // В руках предмет
		{
			if (p.x < 99)
				addItem(dir.index, ItemSlot.pickedItem());
			else
				addItem(dir.right().index, ItemSlot.pickedItem());
			ItemSlot.drop();
			return true;
		}

		if (items == null) return false; // Предметов на земле нет

		if (pick(dir.index, p.x - 57, p.y - 127))
			return true;

		if (pick(dir.right().index, p.x - 140, p.y - 127))
			return true;

		return false;
	}

	public boolean clickAhead(Point p, Direction dir)
	{
		if (ItemSlot.pickedItem() != null) // В руках предмет
		{
			if (p.x < 99)
				addItem(dir.left().index, ItemSlot.pickedItem());
			else
				addItem(dir.opposite().index, ItemSlot.pickedItem());
			ItemSlot.drop();
			return true;
		}

		if (items == null) return false; // Предметов на земле нет

		if (pick(dir.left().index, p.x - 68, p.y - 105))
			return true;

		if (pick(dir.opposite().index, p.x - 129, p.y - 105))
			return true;

		return false;
	}

	private boolean pick(int corner, int x, int y)
	{
		ItemStack st = items[corner];
		if (st == null || st.size() == 0) return false;
		return st.pick(x, y);
	}
}
