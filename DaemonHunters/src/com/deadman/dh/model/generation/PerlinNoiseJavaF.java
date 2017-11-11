package com.deadman.dh.model.generation;

import java.util.Random;

public class PerlinNoiseJavaF
{
	// Constants for setting up the Perlin-1 noise functions
	private static final int B = 0x1000;
	private static final int BM = B - 1;

	private static final int N = 0x1000;

	/** P array for perline 1 noise */
	private int[] p;
	private float[][] g3;
	private float[][] g2;
	private float[] g1;

	private Random rand;

	/**
	 * Create a new noise creator with the given seed value for the randomness
	 */
	public PerlinNoiseJavaF()
	{
		rand = new Random();

		initPerlin1();
	}
	
	/**
	 * Create a new noise creator with the given seed value for the randomness
	 *
	 * @param seed The seed value to use
	 */
	public PerlinNoiseJavaF(long seed)
	{
		rand = new Random(seed);

		initPerlin1();
	}

	/**
	 * 1-D noise generation function using the original perlin algorithm.
	 *
	 * @param x Seed for the noise function
	 * @return The noisy output
	 */
	public float noise1(float x)
	{
		float t = x + N;
		int bx0 = ((int) t) & BM;
		int bx1 = (bx0 + 1) & BM;
		float rx0 = t - (int) t;
		float rx1 = rx0 - 1;

		float sx = sCurve(rx0);

		float u = rx0 * g1[p[bx0]];
		float v = rx1 * g1[p[bx1]];

		return lerp(sx, u, v);
	}

	/**
	 * Create noise in a 2D space using the orignal perlin noise algorithm.
	 *
	 * @param x The X coordinate of the location to sample
	 * @param y The Y coordinate of the location to sample
	 * @return A noisy value at the given position
	 */
	public float noise2(float x, float y)
	{
		float t = x + N;
		int bx0 = ((int) t) & BM;
		int bx1 = (bx0 + 1) & BM;
		float rx0 = t - (int) t;
		float rx1 = rx0 - 1;

		t = y + N;
		int by0 = ((int) t) & BM;
		int by1 = (by0 + 1) & BM;
		float ry0 = t - (int) t;
		float ry1 = ry0 - 1;

		int i = p[bx0];
		int j = p[bx1];

		int b00 = p[i + by0];
		int b10 = p[j + by0];
		int b01 = p[i + by1];
		int b11 = p[j + by1];

		float sx = sCurve(rx0);
		float sy = sCurve(ry0);

		float[] q = g2[b00];
		float u = rx0 * q[0] + ry0 * q[1];
		q = g2[b10];
		float v = rx1 * q[0] + ry0 * q[1];
		float a = lerp(sx, u, v);

		q = g2[b01];
		u = rx0 * q[0] + ry1 * q[1];
		q = g2[b11];
		v = rx1 * q[0] + ry1 * q[1];
		float b = lerp(sx, u, v);

		return lerp(sy, a, b);
	}
	
	public float noise3(float x, float y, float z)
    {
    	float t = x + (float)N;
        int bx0 = ((int)t) & BM;
        int bx1 = (bx0 + 1) & BM;
        float rx0 = (float)(t - (int)t);
        float rx1 = rx0 - 1;

        t = y + (float)N;
        int by0 = ((int)t) & BM;
        int by1 = (by0 + 1) & BM;
        float ry0 = (float)(t - (int)t);
        float ry1 = ry0 - 1;

        t = z + (float)N;
        int bz0 = ((int)t) & BM;
        int bz1 = (bz0 + 1) & BM;
        float rz0 = (float)(t - (int)t);
        float rz1 = rz0 - 1;

        int i = p[bx0];
        int j = p[bx1];

        int b00 = p[i + by0];
        int b10 = p[j + by0];
        int b01 = p[i + by1];
        int b11 = p[j + by1];

        t  = sCurve(rx0);
        float sy = sCurve(ry0);
        float sz = sCurve(rz0);

        float[] q = g3[b00 + bz0];
        float u = (rx0 * q[0] + ry0 * q[1] + rz0 * q[2]);
        q = g3[b10 + bz0];
        float v = (rx1 * q[0] + ry0 * q[1] + rz0 * q[2]);
        float a = lerp(t, u, v);

        q = g3[b01 + bz0];
        u = (rx0 * q[0] + ry1 * q[1] + rz0 * q[2]);
        q = g3[b11 + bz0];
        v = (rx1 * q[0] + ry1 * q[1] + rz0 * q[2]);
        float b = lerp(t, u, v);

        float c = lerp(sy, a, b);

        q = g3[b00 + bz1];
        u = (rx0 * q[0] + ry0 * q[1] + rz1 * q[2]);
        q = g3[b10 + bz1];
        v = (rx1 * q[0] + ry0 * q[1] + rz1 * q[2]);
        a = lerp(t, u, v);

        q = g3[b01 + bz1];
        u = (rx0 * q[0] + ry1 * q[1] + rz1 * q[2]);
        q = g3[b11 + bz1];
        v = (rx1 * q[0] + ry1 * q[1] + rz1 * q[2]);
        b = lerp(t, u, v);

        float d = lerp(sy, a, b);

        return lerp(sz, c, d);
    }
 
	public float turbulence1(float x, float freq)
	{
		float t = 0;

		do
		{
			t += noise1(freq * x) / freq;
			freq *= 0.5f;
		} while (freq >= 1);

		return t;
	}

	/**
	 * Create a turbulance function in 2D using the original perlin noise
	 * function.
	 *
	 * @param x The X coordinate of the location to sample
	 * @param y The Y coordinate of the location to sample
	 * @param freq The frequency of the turbluance to create
	 * @return The value at the given coordinates
	 */
	public float turbulence2(float x, float y, float freq)
	{
		float t = 0;

		do
		{
			t += noise2(freq * x, freq * y) / freq;
			freq *= 0.5f;
		} while (freq >= 1);

		return t;
	}
	
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
    public float turbulence3(float x, float y, float z, float freq)
    {
        float t = 0;

        do
        {
            t += noise3(freq * x, freq * y, freq * z) / freq;
            freq *= 0.5f;
        }
        while (freq >= 1);

        return t;
    }
    
	/**
	 * Simple lerp function using floats.
	 */
	private float lerp(float t, float a, float b)
	{
		return a + t * (b - a);
	}

	/**
	 * S-curve function for value distribution for Perlin-1 noise function.
	 */
	private float sCurve(float t)
	{
		return (t * t * (3 - 2 * t));
	}

	/**
	 * 2D-vector normalisation function.
	 */
	private void normalize2(float[] v)
	{
		float s = (float) (1 / Math.sqrt(v[0] * v[0] + v[1] * v[1]));
		v[0] *= s;
		v[1] *= s;
	}

	/**
	 * 3D-vector normalisation function.
	 */
	private void normalize3(float[] v)
	{
		float s = (float) (1 / Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]));
		v[0] *= s;
		v[1] *= s;
		v[2] *= s;
	}

	/**
	 * Initialise the lookup arrays used by Perlin 1 function.
	 */
	private void initPerlin1()
	{
		p = new int[B + B + 2];
		g3 = new float[B + B + 2][3];
		g2 = new float[B + B + 2][2];
		g1 = new float[B + B + 2];
		int i, j, k;

		for (i = 0; i < B; i++)
		{
			p[i] = i;

			g1[i] = (float) ((rand.nextInt(Integer.MAX_VALUE) % (B + B)) - B) / B;

			for (j = 0; j < 2; j++)
				g2[i][j] = (float) ((rand.nextInt(Integer.MAX_VALUE) % (B + B)) - B) / B;
			normalize2(g2[i]);

			for (j = 0; j < 3; j++)
				g3[i][j] = (float) ((rand.nextInt(Integer.MAX_VALUE) % (B + B)) - B) / B;
			normalize3(g3[i]);
		}

		while (--i > 0)
		{
			k = p[i];
			j = (int) (rand.nextInt(Integer.MAX_VALUE) % B);
			p[i] = p[j];
			p[j] = k;
		}

		for (i = 0; i < B + 2; i++)
		{
			p[B + i] = p[i];
			g1[B + i] = g1[i];
			for (j = 0; j < 2; j++)
				g2[B + i][j] = g2[i][j];
			for (j = 0; j < 3; j++)
				g3[B + i][j] = g3[i][j];
		}
	}
}
