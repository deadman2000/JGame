package com.deadman.pixelgame;

public class GameCursor extends SpriteObject
{
	public GameCursor(Sprite sprite)
	{
		super(sprite);
	}

	public GameObject draggedObject;

	@Override
	public void draw()
	{
		GameScreen.T_TOP = true;
		
		if (draggedObject != null)
			draggedObject.draw();
		else
			super.draw();
		
		GameScreen.T_TOP = false;
	}
}
