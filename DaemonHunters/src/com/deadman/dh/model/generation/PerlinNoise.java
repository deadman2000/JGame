package com.deadman.dh.model.generation;

public abstract class PerlinNoise
{
	/**
	 * 1-D noise generation function using the original perlin algorithm.
	 *
	 * @param x Seed for the noise function
	 * @return The noisy output
	 */
	public abstract double noise1(double x);

	/**
	 * Create noise in a 2D space using the orignal perlin noise algorithm.
	 *
	 * @param x The X coordinate of the location to sample
	 * @param y The Y coordinate of the location to sample
	 * @return A noisy value at the given position
	 */
	public abstract double noise2(double x, double y);

	public abstract double noise3(double x, double y, double z);

	public abstract double turbulence1(double x, double freq);

	/**
	 * Create a turbulance function in 2D using the original perlin noise
	 * function.
	 *
	 * @param x The X coordinate of the location to sample
	 * @param y The Y coordinate of the location to sample
	 * @param freq The frequency of the turbluance to create
	 * @return The value at the given coordinates
	 */
	public abstract double turbulence2(double x, double y, double freq);

	/**
	 * Create a turbulance function in 3D using the original perlin noise
	 * function.
	 *
	 * @param x The X coordinate of the location to sample
	 * @param y The Y coordinate of the location to sample
	 * @param z The Z coordinate of the location to sample
	 * @param freq The frequency of the turbluance to create
	 * @return The value at the given coordinates
	 */
	public abstract double turbulence3(double x, double y, double z, double freq);

	private static final boolean USE_NATIVE = false;

	public static PerlinNoise create()
	{
		if (USE_NATIVE)
			return new PerlinNoiseNative();
		return new PerlinNoiseJava();
	}

	public static PerlinNoise create(long seed)
	{
		if (USE_NATIVE)
			return new PerlinNoiseNative(seed);
		return new PerlinNoiseJava(seed);
	}

	public abstract void buildMapTurbulence3(double[][] map, double fromX, double toX, double fromY, double toY, double z, double freq);
}
