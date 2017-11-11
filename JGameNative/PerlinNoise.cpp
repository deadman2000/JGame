/*
 * PerlinNoiseGenerator.cpp
 *
 *  Created on: 8 апр. 2017 г.
 *      Author: DEADMAN
 */

#include "PerlinNoise.h"

#include <cmath>
#include <iostream>
#include <fstream>
using namespace std;

#define B 0x1000
#define B2 (B + B + 2)
#define BM (B - 1)
#define N 0x1000

double inline lerp(double t, double a, double b)
{
	return a + t * (b - a);
}

double inline sCurve(double t)
{
	return (t * t * (3 - 2 * t));
}

void inline normalize2(double v[])
{
	double s = 1.0 / sqrt(v[0] * v[0] + v[1] * v[1]);
	v[0] *= s;
	v[1] *= s;
}

void inline normalize3(double v[])
{
	double s = 1.0 / sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
	v[0] *= s;
	v[1] *= s;
	v[2] *= s;
}

PerlinNoise::PerlinNoise() : rand() {
	initPerlin1();
}

PerlinNoise::PerlinNoise(long seed) : rand(seed) {
	initPerlin1();
}

PerlinNoise::~PerlinNoise() {
	std::cout << "Destroy" << std::endl;
}

void PerlinNoise::initPerlin1() {
	//ofstream myfile("example.txt");

	p = new int[B2];
	g3 = new double*[B2];
	for (int i=0; i<B2; i++)
		g3[i] = new double[3];

	g2 = new double*[B2];
	for (int i=0; i<B2; i++)
		g2[i] = new double[2];

	g1 = new double[B2];
	int i, j, k;

	for (i = 0; i < B; i++)
	{
		p[i] = i;

		g1[i] = (double) ((rand.nextInt() % (B + B)) - B) / B;

		for (j = 0; j < 2; j++)
			g2[i][j] = (double) ((rand.nextInt() % (B + B)) - B) / B;
		normalize2(g2[i]);

		for (j = 0; j < 3; j++)
			g3[i][j] = (double) ((rand.nextInt() % (B + B)) - B) / B;
		normalize3(g3[i]);
	}

	while (--i > 0)
	{
		k = p[i];
		j = (int) (rand.nextInt(0x7fffffff) % B);
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

	/*for (int i=0; i<100; i++){
		cout << i << " : " << p[i] << endl;
	}*/

	/*cout << "flushing" << endl;
	myfile.flush();
	cout << "closing" << endl;
	myfile.close();
	cout << "step 5" << endl;*/
}

double PerlinNoise::noise1(double x) {
	double t = x + N;
	int bx0 = ((int) t) & BM;
	int bx1 = (bx0 + 1) & BM;
	double rx0 = t - (int) t;
	double rx1 = rx0 - 1;

	double sx = sCurve(rx0);

	double u = rx0 * g1[p[bx0]];
	double v = rx1 * g1[p[bx1]];

	return lerp(sx, u, v);
}

double PerlinNoise::noise2(double x, double y){
	double t = x + N;
	int bx0 = ((int) t) & BM;
	int bx1 = (bx0 + 1) & BM;
	double rx0 = t - (int) t;
	double rx1 = rx0 - 1;

	t = y + N;
	int by0 = ((int) t) & BM;
	int by1 = (by0 + 1) & BM;
	double ry0 = t - (int) t;
	double ry1 = ry0 - 1;

	int i = p[bx0];
	int j = p[bx1];

	int b00 = p[i + by0];
	int b10 = p[j + by0];
	int b01 = p[i + by1];
	int b11 = p[j + by1];

	double sx = sCurve(rx0);
	double sy = sCurve(ry0);

	double* q = g2[b00];
	double u = rx0 * q[0] + ry0 * q[1];
	q = g2[b10];
	double v = rx1 * q[0] + ry0 * q[1];
	double a = lerp(sx, u, v);

	q = g2[b01];
	u = rx0 * q[0] + ry1 * q[1];
	q = g2[b11];
	v = rx1 * q[0] + ry1 * q[1];
	double b = lerp(sx, u, v);

	return lerp(sy, a, b);
}

double PerlinNoise::noise3(double x, double y, double z){
	double t = x + N;
	int it = (int) t;
	int bx0 = it & BM;
	int bx1 = (bx0 + 1) & BM;
	double rx0 = t - it;
	double rx1 = rx0 - 1;

	t = y + N;
	it = (int) t;
	int by0 = it & BM;
	int by1 = (by0 + 1) & BM;
	double ry0 = t - it;
	double ry1 = ry0 - 1;

	t = z + N;
	it = (int) t;
	int bz0 = it & BM;
	int bz1 = (bz0 + 1) & BM;
	double rz0 = t - it;
	double rz1 = rz0 - 1;

	int i = p[bx0];
	int j = p[bx1];

	int b00 = p[i + by0];
	int b10 = p[j + by0];
	int b01 = p[i + by1];
	int b11 = p[j + by1];

	t = sCurve(rx0);
	double sy = sCurve(ry0);
	double sz = sCurve(rz0);

	double* q = g3[b00 + bz0];
	double u = (rx0 * q[0] + ry0 * q[1] + rz0 * q[2]);
	q = g3[b10 + bz0];
	double v = (rx1 * q[0] + ry0 * q[1] + rz0 * q[2]);
	double a = lerp(t, u, v);

	q = g3[b01 + bz0];
	u = (rx0 * q[0] + ry1 * q[1] + rz0 * q[2]);
	q = g3[b11 + bz0];
	v = (rx1 * q[0] + ry1 * q[1] + rz0 * q[2]);
	double b = lerp(t, u, v);

	double c = lerp(sy, a, b);

	q = g3[b00 + bz1];
	u = rx0 * q[0] + ry0 * q[1] + rz1 * q[2];
	q = g3[b10 + bz1];
	v = rx1 * q[0] + ry0 * q[1] + rz1 * q[2];
	a = lerp(t, u, v);

	q = g3[b01 + bz1];
	u = rx0 * q[0] + ry1 * q[1] + rz1 * q[2];
	q = g3[b11 + bz1];
	v = rx1 * q[0] + ry1 * q[1] + rz1 * q[2];
	b = lerp(t, u, v);

	double d = lerp(sy, a, b);

	return lerp(sz, c, d);
}

double PerlinNoise::turbulence1(double x, double freq){
	double t = 0;
	do
	{
		t += noise1(freq * x) / freq;
		freq *= 0.5f;
	} while (freq >= 1);

	return t;
}

double PerlinNoise::turbulence2(double x, double y, double freq){
	double t = 0;
	do
	{
		t += noise2(freq * x, freq * y) / freq;
		freq *= 0.5f;
	} while (freq >= 1);

	return t;
}

double PerlinNoise::turbulence3(double x, double y, double z, double freq){
	double t = 0;
	do
	{
		t += noise3(freq * x, freq * y, freq * z) / freq;
		freq *= 0.5f;
	} while (freq >= 1);

	return t;
}
