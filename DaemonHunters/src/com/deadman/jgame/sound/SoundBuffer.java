package com.deadman.jgame.sound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.deadman.jgame.resources.ResourceManager;
import com.jogamp.openal.AL;

public class SoundBuffer
{
	static AL al = Sound.al;

	private final int _id;
	
	// Buffers hold sound data.
	private int[] buffer = new int[1];
	private int bufferId;
	
	private boolean _loaded;

	public SoundBuffer(int id)
	{
		_id = id;

		try
		{
			al.alGenBuffers(1, buffer, 0);
			Sound.checkForALError();
			bufferId = buffer[0];
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Sound getSound()
	{
		if (!_loaded)
		{
			try
			{
				loadALData();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return new Sound(this);
	}


	private void loadALData() throws Exception
	{
		InputStream in = ResourceManager.getInputStream(_id);
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(in);
		addBufferData(audioStream.getFormat(), readStreamContents(audioStream));
		_loaded = true;
	}

	private static byte[] readStreamContents(InputStream input) throws IOException
	{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = input.read(buffer)))
		{
			result.write(buffer, 0, n);
		}
		return result.toByteArray();
	}

	private void addBufferData(AudioFormat format, byte[] data) throws Exception
	{
		addBufferData(format, data, data.length);
	}

	private void addBufferData(AudioFormat format, byte[] data, int size) throws Exception
	{
		int audioFormat = AL.AL_FALSE;
		if (format.getSampleSizeInBits() == 8)
		{
			if (format.getChannels() == 1)
			{
				audioFormat = AL.AL_FORMAT_MONO8;
			}
			else if (format.getChannels() == 2)
			{
				audioFormat = AL.AL_FORMAT_STEREO8;
			}
		}
		else if (format.getSampleSizeInBits() == 16)
		{
			if (format.getChannels() == 1)
			{
				audioFormat = AL.AL_FORMAT_MONO16;
			}
			else if (format.getChannels() == 2)
			{
				audioFormat = AL.AL_FORMAT_STEREO16;
			}
		}
		if (audioFormat == AL.AL_FALSE)
			throw new Exception("Unsuppported audio format: " + format);

		addBufferData(audioFormat, data, size, (int) format.getSampleRate());
	}

	private void addBufferData(int format, byte[] data, int size, int sampleRate) throws Exception
	{
		Buffer buf = ByteBuffer.wrap(data);
		al.alBufferData(bufferId, format, buf, size, sampleRate);
		Sound.checkForALError();
	}

	public void killALData()
	{
		al.alDeleteBuffers(1, buffer, 0);
	}

	public int getId()
	{
		return bufferId;
	}
}
