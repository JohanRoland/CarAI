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

#include "speechRecognition.h"

using namespace std;

int main(int argc, char * argv[])
{
	int debug = 0;
	if (argc > 0)
	{
		string str = argv[1];
		if (str.compare("1") == 1)
		{
			debug = 1;
		}
	}

	if (debug == 1)
	{
		cout << "Speech Debug" << endl;
		cin.get();
	}


}