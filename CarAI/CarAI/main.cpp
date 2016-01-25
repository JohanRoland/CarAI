/*
Main should spawn an instance of faceRecognition and lissen for respons.
When respons is recived, it should forward it to result.

Main should spawn an instance of speachRecognition and lissen for respons.
When respons is recived, it should forward it to result.

Result is run in main with the input of the output of ether faceRecognition or  speachRecognition or nither.

*/
#include <stdio.h>
#include <iostream>
#include <string>


#include "faceRecognition.h"
#include "speechRecognition.h"

using namespace std;

int main(int argc, char * argv[])
{
	int debug = 0;
	if (argc > 1)
	{
		string str = argv[1];
		if (str.compare("1") == 0)
		{
			debug = 1;
		}
		if (str.compare("2") == 0)
		{
			debug = 2;
		}
	}

	if (debug == 1)
	{
		cout << "Speech Debug" << endl;
		speechRecognition asr;
		asr.run();
		cin.get();
	}
	if (debug == 2)
	{
		faceRecognition faceRec;
		faceRec.findFace();
		cout << "Image Debug" << endl;
		cin.get();
	}

}