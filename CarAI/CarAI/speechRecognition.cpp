#include "speechRecognition.h"


/*
speechRecognition should spawn the child process RecorderSynthesiser when initilized,
then it should whait for RecorderSynthesiser.
When a .wav file is recived from he child process  RecorderSynthesiser it should be enterpreted
and sent to the parent process main.

*/

#define MODELDIR "D:/Programming projects/NIB/CarAI/CarAI/sphinxbase/pocketsphinx/model"

static ps_decoder_t *ps;
static cmd_ln_t *config;


/* Sleep for specified msec */
static void
sleep_msec(int32 ms)
{
#if (defined(_WIN32) && !defined(GNUWINCE)) || defined(_WIN32_WCE)
	Sleep(ms);
#else
	/* ------------------- Unix ------------------ */
	struct timeval tmo;

	tmo.tv_sec = 0;
	tmo.tv_usec = ms * 1000;

	select(0, NULL, NULL, NULL, &tmo);
#endif
}


speechRecognition::speechRecognition()
{
	config = cmd_ln_init(NULL, ps_args(), TRUE,
		"-hmm", MODELDIR "/en-us/en-us",
		"-lm", MODELDIR "/en-us/en-us.lm.dmp",
		"-dict", MODELDIR "/en-us/cmudict-en-us.dict",
		NULL);

	ps = ps_init(config);
}

int speechRecognition::run()
{
	ad_rec_t *ad;
	int16 adbuf[2048];
	uint8 utt_started, in_speech;
	int32 k;
	char const *hyp;

	if ((ad = ad_open_dev(cmd_ln_str_r(config, "-adcdev"),
		(int)cmd_ln_float32_r(config,
			"-samprate"))) == NULL)
		E_FATAL("Failed to open audio device\n");
	if (ad_start_rec(ad) < 0)
		E_FATAL("Failed to start recording\n");

	if (ps_start_utt(ps) < 0)
		E_FATAL("Failed to start utterance\n");
	utt_started = FALSE;
	E_INFO("Ready....\n");

	for (;;) {
		if ((k = ad_read(ad, adbuf, 2048)) < 0)
			E_FATAL("Failed to read audio\n");
		ps_process_raw(ps, adbuf, k, FALSE, FALSE);
		in_speech = ps_get_in_speech(ps);
		if (in_speech && !utt_started) {
			utt_started = TRUE;
			E_INFO("Listening...\n");
		}
		if (!in_speech && utt_started) {
			/* speech -> silence transition, time to start new utterance  */
			ps_end_utt(ps);
			hyp = ps_get_hyp(ps, NULL);
			if (hyp != NULL) {
				printf("%s\n", hyp);
				fflush(stdout);
			}
			ps_set_keyphrase(ps, "keyphrase_search", "car");
			ps_set_search(ps, "keyphrase_search");
			if (ps_start_utt(ps) < 0)
				E_FATAL("Failed to start utterance\n");
			utt_started = FALSE;
			E_INFO("Ready....\n");
		}
		sleep_msec(100);
	}
	ad_close(ad);
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
}


