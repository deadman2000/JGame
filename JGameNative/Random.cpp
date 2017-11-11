/*
 * Random.cpp
 *
 *  Created on: 9 апр. 2017 г.
 *      Author: DEADMAN
 */

#include "Random.h"

#include <iostream>
#include <chrono>

#define multiplier 0x5DEECE66DLL
#define addend 0xBLL
#define mask ((1LL << 48) - 1)

int64_t initialScramble(int64_t seed) {
	return (seed ^ multiplier) & mask;
}

int64_t randomSeed() {
	int64_t r30 = RAND_MAX*rand()+rand();
	int64_t s30 = RAND_MAX*rand()+rand();
	int64_t t4  = rand() & 0xf;
	return (r30 << 34) + (s30 << 4) + t4;
}


Random::Random(){
	setSeed(randomSeed());
}


Random::Random(int64_t s) {
	setSeed(s);
}

void Random::setSeed(int64_t s) {
	seed = initialScramble(s);
}

int Random::next(int bits) {
	seed = (seed * multiplier + addend) & mask;
    return (int)(seed >> (48 - bits));
}

int Random::nextInt() {
	return next(32);
}

int Random::nextInt(int bound) {
    int r = next(31);
    int m = bound - 1;
    if ((bound & m) == 0)
        r = (int)((bound * (long)r) >> 31);
    else {
        for (int u = r;
             u - (r = u % bound) + m < 0;
             u = next(31))
            ;
    }
    return r;
}
