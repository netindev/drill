#include <stdlib.h>
#include <stdio.h>

#include <iostream>
#include "jni.h"
#include "cryptonight_aesni.h"

/* Some parts are taken from XMR-STAK project, you can find here https://github.com/fireice-uk/xmr-stak*/
extern "C" {

#define SIZE 5
#define FUNC 1

	cryptonight_ctx* alloc_ctx()
	{
		cryptonight_ctx* ctx;
		alloc_msg msg = { 0 };
		ctx = cryptonight_alloc_ctx(1, 1, &msg);
		if (ctx == NULL) {
			ctx = cryptonight_alloc_ctx(1, 0, &msg);
			if (ctx == NULL) {
				if (ctx == NULL) {
					ctx = cryptonight_alloc_ctx(0, 0, NULL);
				}
				return ctx;
			}
			return ctx;
		}
		return nullptr;
	}

	unsigned char* as_unsigned_char_array(JNIEnv *env, jbyteArray array) {
		int len = env->GetArrayLength(array);
		unsigned char* buf = new unsigned char[len];
		env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte*>(buf));
		return buf;
	}

	jbyteArray as_byte_array(JNIEnv *env, unsigned char* buf, int len) {
		jbyteArray array = env->NewByteArray(len);
		env->SetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte*>(buf));
		return array;
	}

	JNIEXPORT void JNICALL Java_tk_netindev_drill_hasher_Hasher_slowHash(JNIEnv *env, jclass clazz, jbyteArray input, jbyteArray output) {
		unsigned char* inputBuffer = as_unsigned_char_array(env, input);
		unsigned char* outputBuffer = as_unsigned_char_array(env, input);
		cryptonight_ctx* ctx[SIZE];
		for (int i = 0; i < SIZE; i++) {
			ctx[i] = alloc_ctx();
		}
		Cryptonight_hash<FUNC>::template hash<cryptonight_monero_v8, false, false>(inputBuffer, env->GetArrayLength(input), outputBuffer, ctx);
		for (int i = 0; i < SIZE; i++) {
			cryptonight_free_ctx(ctx[i]);
		}
		//env->ReleaseByteArrayElements(input, (jbyte *)inputBuffer, JNI_ABORT);
		env->ReleaseByteArrayElements(output, (jbyte *)outputBuffer, JNI_COMMIT);
	}

};