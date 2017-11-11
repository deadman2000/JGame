/*
 * native.cpp
 *
 *  Created on: 9 апр. 2017 г.
 *      Author: DEADMAN
 */

#include "com_deadman_dh_model_generation_PerlinNoiseNative.h"
#include "PerlinNoise.h"

#include <iostream>
using namespace std;

JNIEXPORT jlong JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_createPerlin__
  (JNIEnv *, jclass) {
	PerlinNoise* gen = new PerlinNoise();
	return (intptr_t)gen;
}


JNIEXPORT jlong JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_createPerlin__J
  (JNIEnv *, jclass, jlong seed) {
	PerlinNoise* gen = new PerlinNoise(seed);
	return (intptr_t)gen;
}

JNIEXPORT void JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_deletePerlin
  (JNIEnv *, jclass, jlong address) {
	PerlinNoise* inst = (PerlinNoise*)address;
	delete inst;
}

JNIEXPORT jdouble JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_perlinNoise1
  (JNIEnv *, jclass, jlong address, jdouble x) {
	return ((PerlinNoise*)address)->noise1(x);
}

JNIEXPORT jdouble JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_perlinNoise2
  (JNIEnv *, jclass, jlong address, jdouble x, jdouble y) {
	return ((PerlinNoise*)address)->noise2(x, y);
}

JNIEXPORT jdouble JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_perlinNoise3
  (JNIEnv *, jclass, jlong address, jdouble x, jdouble y, jdouble z) {
	return ((PerlinNoise*)address)->noise3(x, y, z);
}

JNIEXPORT jdouble JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_perlinTurbulence1
  (JNIEnv *, jclass, jlong address, jdouble x, jdouble freq) {
	return ((PerlinNoise*)address)->turbulence1(x, freq);
}

JNIEXPORT jdouble JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_perlinTurbulence2
  (JNIEnv *, jclass, jlong address, jdouble x, jdouble y, jdouble freq){
	return ((PerlinNoise*)address)->turbulence2(x, y, freq);
}

JNIEXPORT jdouble JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_perlinTurbulence3
  (JNIEnv *, jclass, jlong address, jdouble x, jdouble y, jdouble z, jdouble freq) {
	return ((PerlinNoise*)address)->turbulence3(x, y, z, freq);
}

JNIEXPORT void JNICALL Java_com_deadman_dh_model_generation_PerlinNoiseNative_perlinBuildMapTurbulence3
  (JNIEnv *env, jclass, jlong address, jobjectArray map, jdouble fromX, jdouble toX, jdouble fromY, jdouble toY, jdouble z, jdouble freq) {
	PerlinNoise* n = ((PerlinNoise*)address);

	int w = env->GetArrayLength(map);
	jdoubleArray dim = (jdoubleArray)env->GetObjectArrayElement(map, 0);
	int h = env->GetArrayLength(dim);

	double buf[h];

	for (int mx=0; mx<w; mx++){
		dim = (jdoubleArray)env->GetObjectArrayElement(map, mx);
		double x = fromX + (toX - fromX) * mx / w;

		for (int my=0; my<h; my++){
			double y = fromY + (toY - fromY) * my / h;

			double val = n->turbulence3(x, y, z, freq);
			buf[my] = val;
		}

		env->SetDoubleArrayRegion(dim, 0, h, buf);
	}
}
