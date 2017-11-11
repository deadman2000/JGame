package com.deadman.jgame.drawing;

public class AnimationPlayer extends Drawable
{
	public Animation animation;
	public boolean loop = false;

	private long start;
	private long stop;
	private boolean isStopped = false;
	private Drawable lastFrame;
	public IAnimationCallback endCallback;

	public AnimationPlayer(Animation a, IAnimationCallback callback)
	{
		animation = a;
		lastFrame = animation.frames[animation.frames.length - 1];
		endCallback = callback;
	}

	private Drawable getCurrentFrame()
	{
		if (isStopped)
			return lastFrame;

		long t = animation.tickSource.getTicks();

		if (start == 0)
		{
			start = t;
			stop = start + animation.duration;
			return animation.frames[0];
		}

		if (!loop && t > stop)
		{
			isStopped = true;
			if (endCallback != null)
				endCallback.onAnimationStopped();
			return lastFrame;
		}

		int n = (int) (t - start);
		if (loop) n = n % animation.duration;

		for (int i = 0; i < animation.delays.length; i++)
			if (animation.delays[i] > n)
				return animation.frames[i];

		if (!loop)
			return lastFrame;

		return animation.frames[0];
	}

	@Override
	protected void draw(int x, int y)
	{
		getCurrentFrame().drawAt(x, y);
	}

}
