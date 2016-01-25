#pragma once
#include "pocketsphinx.h"
#include "sphinxbase\err.h"
#include "sphinxbase\ad.h"
#include <iostream>
#include <assert.h>

#if defined(_WIN32) && !defined(__CYGWIN__)
#include <windows.h>
#else
#include <sys/select.h>
#endif

class speechRecognition
{
public:
	speechRecognition();
	int run();
	int test();
};

