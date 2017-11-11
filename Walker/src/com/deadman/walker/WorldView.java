package com.deadman.walker;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import com.deadman.dh.global.Snow;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Control;
import com.jogamp.opengl.GL2;

public class WorldView extends Control
{
	public Map map;

	public Drawable background;

	private Snow snow;

	private Moving currentMove = Moving.NONE;
	private long startMoveTime;
	private float moveFactor;
	private final long MOVE_TIME = 5; // In frames
	private final int farYFactor = 22;

	private final int WIDTH = 198;
	private final int HEIGHT = 137;

	public WorldView()
	{
		setBounds((GameScreen.GAME_WIDTH - WIDTH) / 2, 16, WIDTH, HEIGHT);
		anchor = Control.ANCHOR_TOP;
		this.bgrColor = 0;
	}

	private Position position()
	{
		return WalkerEngine.position;
	}

	public void setSnow()
	{
		snow = new Snow(100);
		addControl(snow);
		snow.fillParent();
	}

	@Override
	public void draw()
	{
		if (currentMove != Moving.NONE)
		{
			if (needFBO)
			{
				if (currentMove != Moving.BACKWARD)
				{
					drawFBO();
					doMove(currentMove);
				}
				else
				{
					doMove(currentMove);
					drawFBO();
				}
			}

			switch (currentMove)
			{
				case FORWARD_BARRIER:
					drawFrameScaled(moveFactor < .5 ? moveFactor : 1 - moveFactor);
					break;
				case FORWARD:
					drawFrameScaled(moveFactor);
					break;
				case BACKWARD:
					drawFrameScaled(1 - moveFactor);
					break;
				case SHIFT_LEFT:
				case ROTATE_LEFT:
					drawFrame((int) (width * moveFactor));
					drawScene((int) (width * (moveFactor - 1)));
					break;
				case SHIFT_RIGHT:
				case ROTATE_RIGHT:
					drawFrame((int) (-width * moveFactor));
					drawScene((int) (width * (1 - moveFactor)));
					break;
				default:
					drawScene(0);
					break;
			}
		}
		else
		{
			clearFBO();
			drawScene(0);
		}
	}

	private void drawScene(int dx)
	{
		if (dx > 0)
		{
			if (width - dx <= 0) return;
			GameScreen.screen.enableClipping(scrX + dx, scrY, width - dx, height);
		}
		else
		{
			if (width + dx <= 0) return;
			GameScreen.screen.enableClipping(scrX, scrY, width + dx, height);
		}

		//System.out.println("draw scene; dx:" + dx + " player:" + playerX + ":" + playerY + " " + dir);

		int oldSX = scrX;
		scrX += dx;

		if (position().isOdd())
			background.drawAt(scrX, scrY);
		else
			background.drawMHAt(scrX, scrY);

		drawCell(Shift.FAR_LEFT_LEFT);
		drawCell(Shift.FAR_RIGHT_RIGHT);

		drawCell(Shift.FAR_LEFT);
		drawCell(Shift.FAR_RIGHT);

		drawCell(Shift.FAR);

		drawCell(Shift.AHEAD_LEFT);
		drawCell(Shift.AHEAD_RIGHT);

		drawCell(Shift.AHEAD);

		drawCell(Shift.NEAR_LEFT);
		drawCell(Shift.NEAR_RIGHT);

		drawCell(Shift.HERE);

		super.draw();
		scrX = oldSX;

		disableClipping();
	}

	private void drawCell(Shift pos)
	{
		Cell cell = map.getCell(position(), pos);
		if (cell == null) return;
		cell.draw(position().dir, pos, scrX, scrY);
	}

	// FBO

	boolean needFBO = false;
	int frameBuffer, colorTexture;

	private void drawFBO()
	{
		needFBO = false;
		GL2 gl = GameScreen.gl;
		clearFBO();
		if (colorTexture == 0)
		{
			colorTexture = GameScreen.screen.genTexture();
			gl.glBindTexture(GL2.GL_TEXTURE_2D, colorTexture);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, GameScreen.GAME_WIDTH * GameScreen.SCALE_FACTOR, GameScreen.GAME_HEIGHT * GameScreen.SCALE_FACTOR, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		}

		if (frameBuffer == 0)
		{
			frameBuffer = GameScreen.screen.genFrameBuffer();
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer);
			gl.glFramebufferTexture2D(GL2.GL_DRAW_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, colorTexture, 0);
			int status = gl.glCheckFramebufferStatus(GL2.GL_DRAW_FRAMEBUFFER);
			if (status != GL2.GL_FRAMEBUFFER_COMPLETE)
				System.err.println("FBO status: " + status);
		}

		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer);
		drawScene(0);
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
	}

	public void clearFBO()
	{
		GL2 gl = GameScreen.gl;
		if (colorTexture != 0)
		{
			gl.glDeleteTextures(1, new int[] { colorTexture }, 0);
			colorTexture = 0;
		}
		if (frameBuffer != 0)
		{
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
			gl.glDeleteFramebuffers(1, new int[] { frameBuffer }, 0);
			frameBuffer = 0;
		}
	}

	private void drawFrame(int dx)
	{
		enableClipping();
		int vx1 = scrX + dx;
		int vy1 = scrY;
		int vx2 = scrX + width + dx;
		int vy2 = scrY + height;

		float tx1 = (float) scrX / GameScreen.GAME_WIDTH;
		float ty1 = (float) (GameScreen.GAME_HEIGHT - scrY) / GameScreen.GAME_HEIGHT;
		float tx2 = (float) (scrX + width) / GameScreen.GAME_WIDTH;
		float ty2 = (float) (GameScreen.GAME_HEIGHT - scrY - height) / GameScreen.GAME_HEIGHT;

		GL2 gl = GameScreen.gl;
		gl.glBindTexture(GL2.GL_TEXTURE_2D, colorTexture);
		gl.glBegin(GL2.GL_QUADS);

		gl.glTexCoord2f(tx1, ty1);
		gl.glVertex2i(vx1, vy1);
		gl.glTexCoord2f(tx2, ty1);
		gl.glVertex2i(vx2, vy1);
		gl.glTexCoord2f(tx2, ty2);
		gl.glVertex2i(vx2, vy2);
		gl.glTexCoord2f(tx1, ty2);
		gl.glVertex2i(vx1, vy2);
		gl.glEnd();
		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		disableClipping();
	}

	private void drawFrameScaled(float scale)
	{
		enableClipping();
		int vx1 = (int) (scrX - width * scale / 2);
		int vy1 = (int) (scrY - height * scale / 2);
		int vx2 = (int) (vx1 + width * (scale + 1));
		int vy2 = (int) (vy1 + height * (scale + 1));

		vy1 += farYFactor * scale;
		vy2 += farYFactor * scale;

		float tx1 = (float) scrX / GameScreen.GAME_WIDTH;
		float ty1 = (float) (GameScreen.GAME_HEIGHT - scrY) / GameScreen.GAME_HEIGHT;
		float tx2 = (float) (scrX + width) / GameScreen.GAME_WIDTH;
		float ty2 = (float) (GameScreen.GAME_HEIGHT - scrY - height) / GameScreen.GAME_HEIGHT;

		GL2 gl = GameScreen.gl;
		gl.glBindTexture(GL2.GL_TEXTURE_2D, colorTexture);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(tx1, ty1);
		gl.glVertex2i(vx1, vy1);
		gl.glTexCoord2f(tx2, ty1);
		gl.glVertex2i(vx2, vy1);
		gl.glTexCoord2f(tx2, ty2);
		gl.glVertex2i(vx2, vy2);
		gl.glTexCoord2f(tx1, ty2);
		gl.glVertex2i(vx1, vy2);
		gl.glEnd();
		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		disableClipping();
	}

	public void tick(long ticks)
	{
		if (snow != null)
			snow.tick(ticks);

		int dir = position().dir.index;

		if (currentMove == Moving.NONE)
		{
			currentMove = getNextMove();
			if (currentMove != Moving.NONE)
			{
				needFBO = true;
				moveFactor = 0;
				startMoveTime = ticks;
				Compas.direction = dir;
			}
		}
		else
		{
			long dt = ticks - startMoveTime;
			if (dt > MOVE_TIME)
			{
				currentMove = getNextMove();
				if (currentMove != Moving.NONE)
				{
					needFBO = true;
					moveFactor = 0;
					startMoveTime = ticks;
					Compas.direction = dir;
				}
			}
			else
			{
				moveFactor = (float) dt / MOVE_TIME;
				if (currentMove == Moving.ROTATE_LEFT)
					Compas.direction = dir + 1 - moveFactor;
				else if (currentMove == Moving.ROTATE_RIGHT)
					Compas.direction = dir - 1 + moveFactor;
				//System.out.println("moving " + moveFactor);
			}
		}
	}

	private void doMove(Moving move)
	{
		Position p = position().translate(move);
		WalkerEngine.moveTo(p);
		if (move != Moving.ROTATE_LEFT || move != Moving.ROTATE_RIGHT)
			Compas.direction = p.dir.index;
		// TODO Падение и проч. действия
		//GameScreen.screen.setTitle(p.toString());
		if (snow != null)
			snow.init();
		//System.out.println("moving " + move + " from " + position + " to " + p);
	}

	private LinkedList<Moving> movings = new LinkedList<Moving>();

	public void move(Moving m)
	{
		if (movings.size() > 10) return;
		movings.add(m);
	}

	private Moving getNextMove()
	{
		if (movings.size() > 0)
		{
			Moving move = movings.pop();
			if (move.posChanged())
			{
				Position pos = position();
				boolean isValid = map.canMove(pos.x, pos.y, pos.dir.translate(move));
				if (!isValid)
				{
					movings.clear();
					if (move == Moving.FORWARD)
						return Moving.FORWARD_BARRIER;
					return Moving.NONE;
				}
			}
			return move;
		}
		return Moving.NONE;
	}

	@Override
	protected void onClick(Point p, MouseEvent e)
	{
		Position pos = position();
		if (p.y > 118)
		{
			Cell cell = map.getCell(pos);
			if (cell.clickHere(p, pos.dir))
				return;
		}
		else if (p.y > 90)
		{
			if (map.canMove(pos.x, pos.y, pos.dir))
			{
				Cell cell = map.getCell(pos.translate(Moving.FORWARD));
				if (cell.clickAhead(p, pos.dir))
					return;
			}
		}
		else if (p.y < 82)
		{
			if (ItemSlot.pickedItem() != null && map.canMove(pos.x, pos.y, pos.dir))
			{
				// TODO Бросок
			}
		}
		super.onClick(p, e);
	}

}
