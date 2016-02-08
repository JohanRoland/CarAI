package result;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import interfaces.MQTTInterface;

class CarInterface implements MQTTInterface
{
	MqttAsyncClient client;
	MqttConnectOptions connOpts;
	
	String utopic = "carai/car";
	String content = "Java to car interface";
	String clientId = "CarIf";
	
	public CarInterface()
	{
		
		MemoryPersistence persistence = new MemoryPersistence();
		
		try{
			client = new MqttAsyncClient(broker,clientId,persistence); 
			connOpts = new MqttConnectOptions();
	        connOpts.setCleanSession(true);
	        client.connect();
	        client.subscribe(utopic,0);
		}
		catch(MqttException me) {
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
		@Override
		public void connectionLost(Throwable arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
			// TODO Auto-generated method stub
			
		}
	}
	
}