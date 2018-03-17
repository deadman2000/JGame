package com.deadman.jgame.sound;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;

public class Sound
{
	public static AL al;

	public static void init()
	{
		try
		{
			al = ALFactory.getAL();
			ALut.alutInit();
			checkForALError();
			
			setListenerValues();

			Runtime runtime = Runtime.getRuntime();
			runtime.addShutdownHook(new Thread(new Runnable()
			{
				public void run()
				{
					ALut.alutExit();
				}
			}));
			System.out.println("OpenAL is init");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// Position of the listener.
	static float[] listenerPos = { 0.0f, 0.0f, 0.0f };

	// Velocity of the listener.
	static float[] listenerVel = { 0.0f, 0.0f, 0.0f };

	// Orientation of the listener. (first 3 elements are "at", second 3 are "up")
	static float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };

	static void setListenerValues() throws Exception
	{
		al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
		al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
		al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
		al.alListenerf(AL.AL_GAIN, 1f);
		checkForALError();
	}

	
	private final SoundBuffer _buff;
	
	// Sources are points emitting sound.
	private int sourceId;
	private int[] source = new int[1];

	// Position of the source sound.
	private float[] sourcePos = { 0.0f, 0.0f, 0.0f };

	// Velocity of the source sound.
	private float[] sourceVel = { 0.0f, 0.0f, 0.0f };

	public Sound(SoundBuffer buff)
	{
		_buff = buff;
		
		try
		{
			al.alGenSources(1, source, 0);
			checkForALError();
			sourceId = source[0];

			al.alSourcei(sourceId, AL.AL_BUFFER, _buff.getId());
			checkForALError();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setLooping(boolean looping)
	{
		al.alSourcei(sourceId, AL.AL_LOOPING, looping ? AL.AL_TRUE : AL.AL_FALSE);
	}

	public void setPosition(float x, float y, float z)
	{
		if (sourcePos[0] == x && sourcePos[1] == y && sourcePos[2] == z) return;
		
		//System.out.println(String.format("Set pos %f %f %f", x, y, z));
		sourcePos[0] = x;
		sourcePos[1] = y;
		sourcePos[2] = z;
		al.alSourcefv(sourceId, AL.AL_POSITION, sourcePos, 0);
	}

	public void setVelocity(float x, float y, float z)
	{
		sourceVel[0] = x;
		sourceVel[1] = y;
		sourceVel[2] = z;
		al.alSourcefv(sourceId, AL.AL_VELOCITY, sourceVel, 0);
	}
	
	public void play()
	{
		al.alSourcePlay(sourceId);
		try
		{
			checkForALError();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	boolean _deleted = false;

	public void stop()
	{
		al.alSourceStop(sourceId);
	}
	
	protected void finalize()
	{
		delete();
	}
	
	public void delete()
	{
		if (_deleted) return;
		
		_deleted = true;
		al.alDeleteSources(1, source, 0);
	}

	static void checkForALError() throws Exception
	{
		int errorCode = al.alGetError();
		if (errorCode != AL.AL_NO_ERROR)
			throw new Exception(String.format("AL Error 0x%x: %s", errorCode, al.alGetString(errorCode)));
	}

}
