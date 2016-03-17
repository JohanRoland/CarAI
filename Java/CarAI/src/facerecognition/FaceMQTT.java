package facerecognition;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import interfaces.MQTTInterface;

public class FaceMQTT implements MQTTInterface {

	MqttAsyncClient client;
	MqttConnectOptions connOpts;
	
	FaceRecognition fr;
	
	String utopic = "carai/face/update";
	String ctopic = "carai/face/car";
	String content = "JSON package of detected faces";
	String clientId = "Facedetect";
	
	boolean exit; 
	
	public FaceMQTT()
	{
		exit = false; 
		MemoryPersistence persistence = new MemoryPersistence();
		try
		{
			client = new MqttAsyncClient(broker,clientId,persistence); 
			connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect();
            client.setCallback(new Callback());
            while(!client.isConnected()){}
            client.subscribe(utopic,0);
            fr = new FaceRecognition();
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
	
	private class Callback implements MqttCallback
	{

		public void connectionLost(Throwable arg0) {
			System.out.println("Connection to Mqtt Server lost in Face detection");
			arg0.printStackTrace();
		}

		public void deliveryComplete(IMqttDeliveryToken arg0) {
			
		}

		public void messageArrived(String arg0, MqttMessage arg1) throws Exception {

			if(arg0.equals(utopic))
			{
				if(new String(arg1.getPayload()).equals("exit"))
				{
					System.exit(0);
				}
				else
				{
					String passPack = fr.sample();
					MqttMessage msg = new MqttMessage(passPack.getBytes());
					msg.setQos(qos);
					client.publish(ctopic, msg);
				}
			}
			
		}
		
	}
	
}
