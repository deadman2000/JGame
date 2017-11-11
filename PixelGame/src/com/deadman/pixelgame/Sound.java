package com.deadman.pixelgame;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.newdawn.easyogg.OggClip;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

public class Sound
{
	public static void playWAV(String fileName)
	{
		try
		{
			InputStream in = new FileInputStream(fileName);
			AudioStream audioStream = new AudioStream(in);
			AudioPlayer.player.start(audioStream);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void playOGG(String fileName)
	{
		try
		{
			OggClip	ogg = new OggClip(new FileInputStream(fileName));
			ogg.loop();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
