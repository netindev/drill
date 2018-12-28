cmake_minimum_required(VERSION 3.6)
project (cryptonight)

include_directories(
	common/
)

set (HEADER_FILES
	common/int-util.h
	warnings.h
	variant4_random_math.h
	variant2_int_sqrt.h
	skein_port.h
	skein.h
	oaes_lib.h
	oaes_config.h
	keccak.h
	jh.h
	hash-ops.h
	groestl_tables.h
	groestl.h
	blake256.h
)

set (SOURCE_FILES
	hasher.c
    slow-hash.c
	aesb.c
	skein.c
	oaes_lib.c
	keccak.c
	jh.c
	hash-extra-skein.c
	hash-extra-jh.c
	hash-extra-groestl.c
	hash-extra-blake.c
	hash.c
	groestl.c
	blake256.c
)

add_definitions(-D_CRT_SECURE_NO_WARNINGS -msse4.1 -maes)

add_library(cryptonight SHARED
	${HEADER_FILES}
${SOURCE_FILES})