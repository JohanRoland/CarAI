#include "speechRecognition.h"
#include <iostream>
/*
speechRecognition should spawn the child process RecorderSynthesiser when initilized,
then it should whait for RecorderSynthesiser.
When a .wav file is recived from he child process  RecorderSynthesiser it should be enterpreted
and sent to the parent process main.

*/

#define MODELDIR "D:/Programming projects/NIB/CarAI/CarAI/sphinxbase/pocketsphinx/model"

speechRecognition::speechRecognition()
{
}

int speechRecognition::run()
{
	return 0;
}

int speechRecognition::test()
{
	ps_decoder_t *ps;
	cmd_ln_t *config;
	FILE *fh;
	char const *hyp, *uttid;
	int16 buf[512];
	int rv;
	int32 score;
	std::cout << "preloding" << std::endl;
	config = cmd_ln_init(NULL, ps_args(), TRUE,
		"-hmm", MODELDIR "/en-us/en-us",
		"-lm", MODELDIR "/en-us/en-us.lm.dmp",
		"-dict", MODELDIR "/en-us/cmudict-en-us.dict",
		NULL);
	if (config == NULL) {
		fprintf(stderr, "Failed to create config object, see log for details\n");
		return -1;
	}

	ps = ps_init(config);
	if (ps == NULL) {
		fprintf(stderr, "Failed to create recognizer, see log for details\n");
		return -1;
	}

	fh = fopen("D:/Programming projects/NIB/CarAI/CarAI/Debug/goforward.raw", "rb");
	if (fh == NULL) {
		fprintf(stderr, "Unable to open input file goforward.raw\n");
		return -1;
	}

	rv = ps_start_utt(ps);

	while (!feof(fh)) {
		size_t nsamp;
		nsamp = fread(buf, 2, 512, fh);
		rv = ps_process_raw(ps, buf, nsamp, FALSE, FALSE);
	}

	rv = ps_end_utt(ps);
	hyp = ps_get_hyp(ps, &score);
	printf("Recognized: %s\n", hyp);

	fclose(fh);
	ps_free(ps);
	cmd_ln_free_r(config);

	return 0;
	return 0;
}
