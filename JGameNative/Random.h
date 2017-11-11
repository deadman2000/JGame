/*
 * Random.h
 *
 *  Created on: 9 апр. 2017 г.
 *      Author: DEADMAN
 */

#ifndef RANDOM_H_
#define RANDOM_H_

#include <stdint.h>

class Random {
private:
	int next(int bits);
public:
	int64_t seed;

	Random();
	Random(int64_t seed);

	int nextInt();
	int nextInt(int bound);

	void setSeed(int64_t seed);
};

#endif /* RANDOM_H_ */
