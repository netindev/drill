#include <stdlib.h>
#include <stdio.h>

#include "jni.h"
#include "hash-ops.h"

JNIEXPORT 
void JNICALL Java_tk_netindev_drill_hasher_Hasher_slowHash(JNIEnv *env, jclass clazz, jbyteArray input, jbyteArray output, jint variant) {
	unsigned char* inputBuffer = (*env)->GetByteArrayElements(env, input, NULL);
	unsigned char* outputBuffer = (*env)->GetByteArrayElements(env, output, NULL);

	jsize inputSize = (*env)->GetArrayLength(env, input);
	jsize outputSize = (*env)->GetArrayLength(env, output);

	if (outputSize < 32) {
		jclass exception = (*env)->FindClass(env, "java/lang/Exception");

		(*env)->ThrowNew(env, exception, "length of output array is less than 32 bytes");
	}

	cn_slow_hash(inputBuffer, inputSize, outputBuffer, (int)variant, NULL, NULL);

	(*env)->ReleaseByteArrayElements(env, input, (jbyte *)inputBuffer, JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env, output, (jbyte *)outputBuffer, JNI_COMMIT);
}