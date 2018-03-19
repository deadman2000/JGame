package com.deadman.dh.model;

/**
 * Тип скила
 * @author ivamar
 *
 */
public class SkillType
{
	public final double fs, fd, fi;
	public final String name;
	

	public SkillType(String name, double fs, double fd, double fi)
	{
		this.name = name;
		this.fs = fs;
		this.fd = fd;
		this.fi = fi;
	}
}
