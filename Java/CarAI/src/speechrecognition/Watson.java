package speechrecognition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;

import interfaces.MQTTInterface;

public class Watson implements MQTTInterface {

	int THRESHOLD  = 55;
	SpeechToText service;
	RecognizeOptions options;
	
	AudioFormat format;
	
	
	MqttAsyncClient client;
	MqttConnectOptions connOpts;
	String ppttopic = "talkamatic/ptt";
	String itopic = "talkamatic/input";
	String pptetopic = "talkamatic/pttevent";
	String clientId = "SpeechRecognition";
	
	public Watson() 
	{
		service = new SpeechToText();
		service.setUsernameAndPassword("9410f86f-d98e-48b0-bff3-9f2244cc1dff", "I5XLjWZSUwF3");
		
		service.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");

		options = new RecognizeOptions().contentType("audio/wav").timestamps(true).wordAlternativesThreshold(0.9);
		 // .continuous(false).interimResults(false);
		
		format = new AudioFormat(44100.0f, 16, 2, true, true);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
			format);
		
		clientId += System.currentTimeMillis();
		//MQTT Connections
		try
		{	
			MemoryPersistence persistence = new MemoryPersistence();
			client = new MqttAsyncClient(broker,clientId,persistence); 
			connOpts = new MqttConnectOptions();
	        connOpts.setCleanSession(true);
	        client.connect();
	        client.setCallback(new Callback());
	        while(!client.isConnected()){}
	        client.subscribe(ppttopic,0);
		}
		catch(MqttException me)
		{
			System.out.println("reason "+me.getReasonCode());
	        System.out.println("msg "+me.getMessage());
	        System.out.println("loc "+me.getLocalizedMessage());
	        System.out.println("cause "+me.getCause());
	        System.out.println("excep "+me);
	        me.printStackTrace();
		}
	}
	
	public void recordAndSend()
	{
		byte b1, b2;
		TargetDataLine microphone;
		try {
			microphone = AudioSystem.getTargetDataLine(format);
			
			 // Assume that the TargetDataLine, line, has already
			 // been obtained and opened.
			ByteArrayOutputStream out  = new ByteArrayOutputStream();
			int numBytesRead;
			byte[] data = new byte[microphone.getBufferSize() / 5];
		
			
			int num_silent = 0;
		 	boolean snd_started = false;
			
			// Begin audio capture.
		 	MqttMessage msg = new MqttMessage("{\"ptt\":\"on\"}".getBytes());
			msg.setQos(qos);
		 	client.publish("talkamatic/pttevent", msg);
			
		 	microphone.open(format);
			microphone.start();
			
			int i= 0;
			
			while(true)
			{
				numBytesRead =  microphone.read(data, 0, data.length);
			    // Save this chunk of data.
			    out.write(data, 0, numBytesRead);
			    int level =  calculateRMSLevel(data);
			    boolean silent = calculateRMSLevel(data) < THRESHOLD;
			    if ( silent && snd_started)
			    {
			    	num_silent += 1;
			    }
			    else if(!silent && !snd_started)
			    {
			    	snd_started = true;
			    }
			    
			    if(snd_started && num_silent > 15)
			    {
			    	break;
			    }
			    i++;
			    System.out.println(num_silent + " level: " + level);
			    
			}
			
			msg = new MqttMessage("{\"ptt\":\"off\"}".getBytes());
			msg.setQos(qos);
		 	client.publish("talkamatic/pttevent", msg);
			microphone.close();
		    InputStream is = new ByteArrayInputStream(out.toByteArray());
		    
			AudioInputStream ais = new AudioInputStream(is, format, out.toByteArray().length); 
			AudioSystem.write(
                ais
               ,AudioFileFormat.Type.WAVE
               ,new File("audio-file.wav"));
			
			
			
			SpeechResults results = service.recognize(new File("audio-file.wav"), options);
			System.out.println(results);
			String result = results.getResults().get(0).getAlternatives().get(0).getTranscript();
    		msg = new MqttMessage(result.getBytes());
			msg.setQos(qos);
		 	try {
				client.publish("talkamatic/input", msg);
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			
			service.recognizeUsingWebSockets(new FileInputStream("audio-file.wav"),
			  options, new BaseRecognizeDelegate()
			  {
				
			    @Override
			    public void onMessage(SpeechResults speechResults) {
			    	if(speechResults.isFinal())
			    	{
			    		//System.out.println(speechResults);
			    		
			    		String result = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
			    		MqttMessage msg = new MqttMessage(result.getBytes());
						msg.setQos(qos);
					 	try {
							client.publish("talkamatic/pttevent", msg);
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			    	}

			    }
			    @Override
			    public void onError(Exception e)	
			    {
			      e.printStackTrace();
			    }
			  }
			);*/
		} catch (LineUnavailableException | IOException | MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public int calculateRMSLevel(byte[] audioData)
	{ 
	    long lSum = 0;
	    for(int i=0; i < audioData.length; i++)
	        lSum = lSum + audioData[i];

	    double dAvg = lSum / audioData.length;
	    double sumMeanSquare = 0d;

	    for(int j=0; j < audioData.length; j++)
	        sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);

	    double averageMeanSquare = sumMeanSquare / audioData.length;

	    return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
	}
	private boolean isSilent(byte[] snd_data)
	{
		short[] snd_data2 = new short[snd_data.length/2];
		byte b1= 0,b2 = 0;
		for(int i = 0 ; i< snd_data.length; i++)
		{
			if(i%2 == 0)
			{
				b1 = snd_data[i];
			}
			else
			{
				b2 = snd_data[i];
				snd_data2[i/2] = (short) ((b1 << 8) + b2);
			}
		}
		
		List<Short> b = Arrays.asList(ArrayUtils.toObject(snd_data2));

		
		return Collections.max(b) > THRESHOLD;
        /*
         * boolean isLower = false;
		
		for(int i = 0; i < snd_data.length; i++)
		{
			if(snd_data[i] > THRESHOLD )
				isLower = true;
		}
		return isLower;*/
	}
	
	private AudioFormat getFormat() {
	    float sampleRate = 8000;
	    int sampleSizeInBits = 8;
	    int channels = 1;
	    boolean signed = true;
	    boolean bigEndian = true;
	    return new AudioFormat(sampleRate, 
	      sampleSizeInBits, channels, signed, bigEndian);
	  }
	
	
	private class Callback implements MqttCallback
	{
		@Override
		public void connectionLost(Throwable arg0) {
			System.out.println("Connection to Mqtt Server lost in Face detection");
			arg0.printStackTrace();
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			
		}

		@Override
		public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
			// TODO Auto-generated method stub
			
			recordAndSend();
			
		}
		
	}
	
}
