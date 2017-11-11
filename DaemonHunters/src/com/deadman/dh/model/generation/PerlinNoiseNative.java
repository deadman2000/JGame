package com.deadman.dh.model.generation;

public class PerlinNoiseNative extends PerlinNoise
{
	static
	{
		System.loadLibrary("JGameNative");
	}

	native private static long createPerlin();

	native private static long createPerlin(long seed);

	native private static void deletePerlin(long address);

	native private static double perlinNoise1(long address, double x);

	native private static double perlinNoise2(long address, double x, double y);

	native private static double perlinNoise3(long address, double x, double y, double z);

	native private static double perlinTurbulence1(long address, double x, double freq);

	native private static double perlinTurbulence2(long address, double x, double y, double freq);

	native private static double perlinTurbulence3(long address, double x, double y, double z, double freq);

	native private static void perlinBuildMapTurbulence3(long address, double[][] map, double fromX, double toX, double fromY, double toY, double z, double freq);
	
	private long _handler;

	public PerlinNoiseNative()
	{
		_handler = createPerlin();
	}

	public PerlinNoiseNative(long seed)
	{
		_handler = createPerlin(seed);
	}

	protected void finalize()
	{
		//System.out.println("finalize " + _handler);
		deletePerlin(_handler);
	}

	@Override
	public double noise1(double x)
	{
		//System.out.println("noise1 " + _handler);
		return perlinNoise1(_handler, x);
	}

	@Override
	public double noise2(double x, double y)
	{
		//System.out.println("noise2 " + _handler);
		return perlinNoise2(_handler, x, y);
	}

	@Override
	public double noise3(double x, double y, double z)
	{
		//System.out.println("noise3 " + _handler);
		return perlinNoise3(_handler, x, y, z);
	}

	@Override
	public double turbulence1(double x, double freq)
	{
		//System.out.println("turbulence1 " + _handler);
		return perlinTurbulence1(_handler, x, freq);
	}

	@Override
	public double turbulence2(double x, double y, double freq)
	{
		//System.out.println("turbulence2 " + _handler);
		return perlinTurbulence2(_handler, x, y, freq);
	}

	@Override
	public double turbulence3(double x, double y, double z, double freq)
	{
		//System.out.println("turbulence3 " + _handler);
		return perlinTurbulence3(_handler, x, y, z, freq);
	}

	@Override
	public void buildMapTurbulence3(double[][] map, double fromX, double toX, double fromY, double toY, double z, double freq)
	{
		perlinBuildMapTurbulence3(_handler, map, fromX, toX, fromY, toY, z, freq);
	}
}
