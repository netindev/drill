cmake_minimum_required(VERSION 3.6)
project (cryptonight)

set (HEADER_FILES
	algos.hpp
	c_blake256.h
	c_groestl.h
        c_jh.h
        c_keccak.h
        c_skein.h
        cryptonight.h
        cryptonight_aesni.h
        groestl_tables.h
        hash.h
        int-util.h
        skein_port.h
        soft_aes.hpp
)

set (SOURCE_FILES
	c_blake256.c
        c_groestl.c
	c_jh.c
        c_keccak.c
        c_skein.c
        cryptonight_common.cpp
        hasher.cpp
)

add_definitions(-D_CRT_SECURE_NO_WARNINGS -msse4.1 -maes)

add_library(cryptonight SHARED
	${HEADER_FILES}
${SOURCE_FILES})