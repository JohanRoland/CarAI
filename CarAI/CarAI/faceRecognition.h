#pragma once
#ifndef faceRecognition_H
#define faceRecognition_H

#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>
#include <stdio.h>


class faceRecognition
{
public:	
	faceRecognition();
	void camera();
	int findFace();

private:
	void detectAndDisplay(cv::Mat frame);
};


#endif faceRecognition_H