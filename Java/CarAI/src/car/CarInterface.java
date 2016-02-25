package car;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.*;

import interfaces.MQTTInterface;
import result.LocPrediction;
import utils.JSONCAR;
import utils.Tuple;

public class CarInterface implements MQTTInterface
{
	MqttAsyncClient client;
	MqttConnectOptions connOpts;
	
	String utopic = "carai/car";
	String ftopic = "carai/face/car";
	String gpstopic = "carai/car/gps";
	
	String content = "Java to car interface";
	String clientId = "CarIf";
	
	Car car; 
	
	public CarInterface()
	{
		car = Car.getInstance(); 
		MemoryPersistence persistence = new MemoryPersistence();
		
		try{
			client = new MqttAsyncClient(broker,clientId,persistence); 
			connOpts = new MqttConnectOptions();
	        connOpts.setCleanSession(true);
	        client.connect();
	        client.setCallback(new Callback());
	        while(!client.isConnected()){}
	      //client.subscribe(utopic,0);
	        client.subscribe(ftopic, 0);
	        client.subscribe(gpstopic,0);
	        
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
			System.out.println("CarInterface mqtt connection closed");
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
			// TODO Auto-generated method stub
			if(arg0.equals(ftopic))
			{
				Gson gs = new Gson(); 
				JSONCAR carjs =  gs.fromJson(new String(arg1.getPayload()), JSONCAR.class );
				System.out.println(carjs.toString());
				LocPrediction lp;
				car.setCar(carjs);
				if(car.getUser("DRIVER").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("DRIVER").getUserID());
					Tuple<Double,Double> pred = lp.predict();
					client.publish("carai/car/driverPred", new MqttMessage(("\"lon\":\""+pred.fst()+"\",\"lat\":\""+pred.snd() +"\"").getBytes()));
				}
				if(car.getUser("PASSENGER").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("PASSENGER").getUserID());
					Tuple<Double,Double> pred = lp.predict();
					client.publish("carai/car/passPred", new MqttMessage(("\"lon\":\""+pred.fst()+"\",\"lat\":\""+pred.snd() +"\"").getBytes()));
				}
				if(car.getUser("BACKSEAT0").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("BACKSEAT0").getUserID());
					Tuple<Double,Double> pred = lp.predict();
					client.publish("carai/car/back0Pred", new MqttMessage(("\"lon\":\""+pred.fst()+"\",\"lat\":\""+pred.snd() +"\"").getBytes()));
				}
				if(car.getUser("BACKSEAT1").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("BACKSEAT1").getUserID());
					Tuple<Double,Double> pred = lp.predict();
					client.publish("carai/car/back1Pred", new MqttMessage(("\"lon\":\""+pred.fst()+"\",\"lat\":\""+pred.snd() +"\"").getBytes()));
				}
			}
			if(arg0.equals(gpstopic))
			{
				Gson gs = new Gson();
				Tuple<String,String> gpsPos = gs.fromJson(new String(arg1.getPayload()), Tuple.class);
				System.out.println("lon: " + gpsPos.fst() + " lat: " + gpsPos.snd());
				car.setPos(Double.parseDouble(gpsPos.fst()),Double.parseDouble( gpsPos.snd()));
			}

		}
	}
	
}