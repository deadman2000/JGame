package com.deadman.gameeditor.resources;


public abstract class AnimatedDrawable extends Drawable
{
	public abstract int getFramesCount();

	public abstract void tick();

	public abstract int getCurrentFrame();

	public abstract void setCurrentFrame(int number);
}
