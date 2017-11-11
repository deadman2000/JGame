/*
 * PerlinNoiseGenerator.h
 *
 *  Created on: 8 апр. 2017 г.
 *      Author: DEADMAN
 */

#ifndef PERLINNOISE_H_
#define PERLINNOISE_H_

#include "Random.h"

class PerlinNoise {
private:
	int* p;
	double** g3;
	double** g2;
	double* g1;
	Random rand;
	void initPerlin1();
public:
	PerlinNoise();
	PerlinNoise(long seed);
	~PerlinNoise();

	double noise1(double x);
	double noise2(double x, double y);
	double noise3(double x, double y, double z);
	double turbulence1(double x, double freq);
	double turbulence2(double x, double y, double freq);
	double turbulence3(double x, double y, double z, double freq);
};

#endif /* PERLINNOISE_H_ */
