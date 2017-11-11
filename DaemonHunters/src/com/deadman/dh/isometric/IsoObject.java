package com.deadman.dh.isometric;

import com.deadman.dh.model.Element;
import com.deadman.dh.model.GameCharacter;

/**
 * Класс единицы объекта на карте
 */
public abstract class IsoObject
{
	public MapCell cell; // Координаты объекта

	// тут будут характеристики объектов
	protected byte _state; // Верхние 4 бита - поворот
	
	/**
	 * Свечение
	 * @return
	 */
	public float getLightSource()
	{
		return 0;
	}

	/**
	 * Пропускание света
	 * @return
	 */
	public float getLightRate()
	{
		return 0;
	}

	public abstract void drawAt(int x, int y);
	
	public boolean contains(int x, int y)
	{
		return false;
	}
	

	public void setState(byte st)
	{
		_state = (byte) ((st & 0x0F) | (_state & 0xF0));
	}

	public byte getState()
	{
		return (byte) (_state & 0x0F);
	}
	
	public byte getRotation()
	{
		return (byte) ((_state >> 4) & 0x0F);
	}
	
	public void setRotation(byte rotation)
	{
		_state = (byte) ((_state & 0x0F) | ((rotation & 0x0F) << 4));
	}
	
	public byte getFullState()
	{
		return _state;
	}

	public byte rotateTo(MapCell c)
	{
		if (cell == c) return -1;
		byte dir = cell.directionTo(c);
		if (dir != getRotation())
			setRotation(dir);
		return dir;
	}
	
	public boolean isRotated(MapCell c)
	{
		if (cell == c) return true;
		byte dir = cell.directionTo(c);
		return getRotation() == dir;
	}

	public abstract byte getSlotType(); // Место в ячейке (стена, объект и т.д.)

	public void remove()
	{
		cell.setObject(null, getSlotType());
		cell = null;
	}

	// Нанесение урона
	public int hitDamage(GameCharacter from, Element el, int damage)
	{
		return 0;
	}

	// Можно пройти сквозь объект
	public boolean isMovable()
	{
		return false;
	}
}
