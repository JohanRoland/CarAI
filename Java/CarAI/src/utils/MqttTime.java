package utils;

import java.util.Calendar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import interfaces.MQTTInterface;

public class MqttTime implements MQTTInterface {

	static private MqttTime instance;
	Calendar cal;
	
	MqttAsyncClient client;
	MqttConnectOptions connOpts;
	
	String utopic = "carai/time/set";
	String clientId = "timemqtt";
	
	int minute;
	int hour;
	boolean defaultTime;
	
	private MqttTime()
	{
		cal = Calendar.getInstance();
		minute = cal.get(Calendar.MINUTE);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		defaultTime = true;
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
	
	public int getMinute()
	{
		if(defaultTime)
		{
			return cal.get(Calendar.MINUTE);
		}
		return minute;
	}
	
	public int getHour()
	{
		if(defaultTime)
		{
			return cal.get(Calendar.HOUR_OF_DAY);
		}
		return hour;
	}
	
	public static MqttTime getInstance()
	{
		if(instance == null)
		{
			instance = new MqttTime();
		}
		return instance;
	}
	
	
	private class Callback implements MqttCallback
	{

		public void connectionLost(Throwable arg0) {
			System.out.println("Connection to Mqtt Server lost in Face detection");
			
		}

		public void deliveryComplete(IMqttDeliveryToken arg0) {
			
		}

		public void messageArrived(String arg0, MqttMessage arg1) throws Exception {

			if(arg0.equals(utopic))
			{
				if(new String(arg1.getPayload()).equals("default"))
				{
					defaultTime = true;
				 	cal = Calendar.getInstance();
				}
				else
				{
					defaultTime = false;
					String time = new String(arg1.getPayload());
					hour = Integer.parseInt(time.split(":")[0]);
					minute = Integer.parseInt(time.split(":")[1]);
				}
			}
			
		}
		
	}
}
