package com.deadman.dh.fx;

import com.deadman.dh.R;
import com.deadman.jgame.sound.Sound;
import com.deadman.jgame.sound.SoundBuffer;

public enum SoundEffect
{
	SWORD_WHIP(R.sounds.sword_whip_wav),
	STEPS_GRASS(R.sounds.steps_grass_wav),
	STEPS_WOOD(R.sounds.steps_wood_wav),
	STEPS_DIRT(R.sounds.steps_dirt_wav),
	STEPS_ROCK(R.sounds.steps_rock_wav),
	DOOR_OPEN(R.sounds.door_open_wav, true),
	DOOR_CLOSE(R.sounds.door_close_wav, true);

	SoundEffect(int id)
	{
		_buff = new SoundBuffer(id);
	}

	SoundEffect(int id, boolean isStat)
	{
		_buff = new SoundBuffer(id);
		_isStatic = isStat;
	}

	private SoundBuffer _buff;
	private boolean _isStatic;
	private Sound _inst;

	public Sound play()
	{
		if (_isStatic)
		{
			if (_inst == null) _inst = _buff.getSound();
			_inst.play();
			return _inst;
		}

		Sound snd = _buff.getSound();
		snd.play();
		return snd;
	}

	public void stop()
	{
		if (!_isStatic)
		{
			System.err.println("Cannot stop non static!");
			return;
		}
		if (_inst == null) return;
		_inst.stop();
	}

	public Sound create()
	{
		return _buff.getSound();
	}
}
