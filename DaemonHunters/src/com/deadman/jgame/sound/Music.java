package com.deadman.jgame.sound;

import java.io.IOException;
import java.io.InputStream;

import org.newdawn.easyogg.OggClip;

import com.deadman.jgame.resources.ResourceManager;

public class Music
{
	private static OggClip _current;
	private static float _volume = 0.8f;

	public static void stop()
	{
		if (_current == null) return;
		_current.stop();
		_current = null;
	}

	public static void playOGG(int id)
	{
		stop();

		InputStream in = ResourceManager.getInputStream(id);
		try
		{
			_current = new OggClip(in);
			_current.loop();
			_current.setGain(_volume);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void setVolume(float value)
	{
		_volume = 0.5f + value / 2;
		System.out.println(value + " => " + _volume);
		if (_current != null)
			_current.setGain(_volume);
	}
}
