from sys import byteorder
from array import array
from struct import pack

import pyaudio
import wave
import os

import paho.mqtt.client as mqtt
import requests

THRESHOLD = 300
CHUNK_SIZE = 1024
FORMAT = pyaudio.paInt16
RATE = 44100
CHANNEL = 2

def is_silent(snd_data):
    "Returns 'True' if below the 'silent' threshold"
    return max(snd_data) < THRESHOLD

def normalize(snd_data):
    "Average the volume out"
    MAXIMUM = 16384
    times = float(MAXIMUM)/max(abs(i) for i in snd_data)

    r = array('h')
    for i in snd_data:
        r.append(int(i*times))
    return r

def trim(snd_data):
    "Trim the blank spots at the start and end"
    def _trim(snd_data):
        snd_started = False
        r = array('h')

        for i in snd_data:
            if not snd_started and abs(i)>THRESHOLD:
                snd_started = True
                r.append(i)

            elif snd_started:
                r.append(i)
        return r

    # Trim to the left
    snd_data = _trim(snd_data)

    # Trim to the right
    snd_data.reverse()
    snd_data = _trim(snd_data)
    snd_data.reverse()
    return snd_data

def add_silence(snd_data, seconds):
    "Add silence to the start and end of 'snd_data' of length 'seconds' (float)"
    r = array('h', [0 for i in xrange(int(seconds*RATE))])
    r.extend(snd_data)
    r.extend([0 for i in xrange(int(seconds*RATE))])
    return r

def record():
    """
    Record a word or words from the microphone and 
    return the data as an array of signed shorts.

    Normalizes the audio, trims silence from the 
    start and end, and pads with 0.5 seconds of 
    blank sound to make sure VLC et al can play 
    it without getting chopped off.
    """
    p = pyaudio.PyAudio()
    stream = p.open(format=FORMAT, channels=CHANNEL, rate=RATE,
        input=True, output=True,
        frames_per_buffer=CHUNK_SIZE)

    num_silent = 0
    snd_started = False

    r = array('h')

    while 1:
        # little endian, signed short
        snd_data = array('h', stream.read(CHUNK_SIZE))
        if byteorder == 'big':
            snd_data.byteswap()
        r.extend(snd_data)

        silent = is_silent(snd_data)

        if silent and snd_started:
            num_silent += 1
        elif not silent and not snd_started:
            snd_started = True

        if snd_started and num_silent > 30:
            break

    sample_width = p.get_sample_size(FORMAT)
    stream.stop_stream()
    stream.close()
    p.terminate()

    r = normalize(r)
    r = trim(r)
    r = add_silence(r, 0.5)
    return sample_width, r

def record_to_file(path):
    "Records from the microphone and outputs the resulting data to 'path'"
    sample_width, data = record()
    data = pack('<' + ('h'*len(data)), *data)

    wf = wave.open(path, 'wb')
    wf.setnchannels(CHANNEL)
    wf.setsampwidth(sample_width)
    wf.setframerate(RATE)
    wf.writeframes(data)
    wf.close()

def record_and_send():
  c = mqtt.Client()
  c.connect("54.229.54.240", 1883, 60)
  c.publish("talkamatic/pttevent","{\"ptt\":\"on\"}")
  c.loop(1)
  record_to_file('demo.wav')
  c.publish("talkamatic/pttevent","{\"ptt\":\"off\"}")
  c.loop(1)
  #sample_width, data = record()
  os.system("soundconverter -b -m audio/x-flac -q -s .flac 'demo.wav'")
  print("recording ended")
  with open('demo.flac','rb') as fobj:
    s = requests.Session()
    files = {'file': fobj}
    url = "https://stream.watsonplatform.net/speech-to-text/api/v1/recognize"
    headers = {"Content-Type": "audio/flac"}#,"Transfer-Encoding": "chunked"}
    auth=('f5d5460d-ae13-4290-887b-33f1c14104f6','LISdhubRqXAw')
    r = s.post(url,files=files,auth=auth,headers=headers)
    #r = requests.post(url,auth=auth,files=files,headers=headers)
    #print(r.status_code)
    text = r.json()['results'][0]['alternatives'][0]['transcript']
    conf =r.json()['results'][0]['alternatives'][0]['confidence']
    print((text,conf))
    c.publish("talkamatic/input",text)
    c.loop(1)


def main():
  def on_connect(client, userdata, rc):  
    print("connected")
    client.subscribe("talkamatic/ptt")
  def on_message(client, userdata, msg):  
    record_and_send()
  client = mqtt.Client()
  client.on_connect = on_connect
  client.on_message = on_message
  client.connect("54.229.54.240", 1883, 60)
  client.loop_forever()


if __name__ == '__main__':
  main()
    #record_and_send()
    #print("please speak a word into the microphone")
    #record_to_file('demo.wav')
   # print("done - result written to demo.wav")
