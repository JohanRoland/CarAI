package car;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.*;

import interfaces.MQTTInterface;
import prediction.LocPrediction;
import predictorG.PredictorG;
import utils.JSONCAR;
import utils.Tuple;
import utils.Utils;

public class CarInterface implements MQTTInterface
{
	MqttAsyncClient client;
	MqttConnectOptions connOpts;
	
	String utopic = "carai/car";
	String ftopic = "carai/face/car";
	String desttopic = "carai/car/dest";
	String gpstopic = "carai/car/gps";
	String graphTopic = "carai/graph";
	
	String content = "Java to car interface";
	String clientId = "CarIf";
	
	Car car; 
	boolean graphNotCalled=true;
	
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
	        client.subscribe(desttopic,0);
	        client.subscribe(graphTopic, 0);
	        
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
			arg0.printStackTrace();
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
			// TODO Auto-generated method stub
			LocPrediction lp;
			if(arg0.equals(ftopic))
			{
				Gson gs = new Gson(); 
				JSONCAR carjs =  gs.fromJson(new String(arg1.getPayload()), JSONCAR.class );
				System.out.println(carjs.toString());
				
				
				car.setCar(carjs);
				
				if(car.getUser("DRIVER").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("DRIVER").getUserID(),"coords.csv", "networkExport.eg",2);
				}
				if(car.getUser("PASSENGER").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("PASSENGER").getUserID(),"coords.csv", "networkExport.eg",2);
				}
				if(car.getUser("BACKSEAT0").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("BACKSEAT0").getUserID(),"coords.csv", "networkExport.eg",2);
				}
				if(car.getUser("BACKSEAT1").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("BACKSEAT1").getUserID(),"coords.csv", "networkExport.eg",2);
				}
				
			}
			
			if(arg0.equals(desttopic))
			{
				Gson gs = new Gson();
				if(car.getUser("DRIVER").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("DRIVER").getUserID(),"coords.csv", "networkExport.eg",2);
					ArrayList<double[]> pred = lp.predict();
					client.publish("carai/car/driverPred", new MqttMessage(gs.toJson(pred).getBytes()));//("{\"lat\":\""+pred.fst()+"\",\"lon\":\""+pred.snd() +"\"}").getBytes()));
				}
				if(car.getUser("PASSENGER").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("PASSENGER").getUserID(),"coords.csv", "networkExport.eg",2);
					ArrayList<double[]> pred = lp.predict();
					client.publish("carai/car/passPred", new MqttMessage(gs.toJson(pred).getBytes()));//("{\"lat\":\""+pred.fst()+"\",\"lon\":\""+pred.snd() +"\"}").getBytes()));
				}
				if(car.getUser("BACKSEAT0").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("BACKSEAT0").getUserID(),"coords.csv", "networkExport.eg",2);
					ArrayList<double[]> pred= lp.predict();
					client.publish("carai/car/back0Pred", new MqttMessage(gs.toJson(pred).getBytes()));//("{\"lat\":\""+pred.fst()+"\",\"lon\":\""+pred.snd() +"\"}").getBytes()));
				}
				if(car.getUser("BACKSEAT1").userExists())
				{
					lp = LocPrediction.getInstance(car.getUser("BACKSEAT1").getUserID(),"coords.csv", "networkExport.eg",2);
					ArrayList<double[]> pred= lp.predict();
					client.publish("carai/car/back1Pred", new MqttMessage(gs.toJson(pred).getBytes()));//("{\"lat\":\""+pred.fst()+"\",\"lon\":\""+pred.snd() +"\"}").getBytes()));
				}
			}
			if(arg0.equals(gpstopic))
			{
				Gson gs = new Gson();
				Tuple<String,String> gpsPos = gs.fromJson(new String(arg1.getPayload()), Tuple.class);
				//System.out.println("lon: " + gpsPos.fst() + " lat: " + gpsPos.snd());
				car.setPos(Double.parseDouble(gpsPos.fst()),Double.parseDouble( gpsPos.snd()));
			}
			if(arg0.equals(graphTopic))
			{
				

				PredictorG graph;
				graph = PredictorG.getInstance(1);
				if(graphNotCalled)
				{
					graph.loadFromCSV("fabricatedData.csv");
					graphNotCalled=false;
				}
				Gson gs = new Gson();
				Tuple<String,String> args = gs.fromJson(new String(arg1.getPayload()), Tuple.class);
				graph.setCurrentNode(Integer.parseInt(args.fst()));
				Tuple<Tuple<Integer, Double>, ArrayList<Tuple<Integer, Double>>> temp = graph.predictNextNode(Integer.parseInt(args.snd()), 0, 0, null);
				System.out.println(temp);
				switch(temp.fst().fst())
				{
				case 1:
					client.publish("carai/car/driverPred", new MqttMessage(("{\"lat\":\""+57.685289+"\",\"lon\":\""+11.946477+"\"}").getBytes())); // hem
					break;
				case 2:
					client.publish("carai/car/driverPred", new MqttMessage(("{\"lat\":\""+57.696878+"\",\"lon\":\""+11.975853+"\"}").getBytes())); // jobb
					break;
				case 3:
					client.publish("carai/car/driverPred", new MqttMessage(("{\"lat\":\""+57.699042+"\",\"lon\":\""+11.977489+"\"}").getBytes())); // mat 1
				break;
				case 4:
					client.publish("carai/car/driverPred", new MqttMessage(("{\"lat\":\""+57.706636+"\",\"lon\":\""+11.979626+"\"}").getBytes())); // mat 2
					break;
				case 5:
					client.publish("carai/car/driverPred", new MqttMessage(("{\"lat\":\""+57.699489+"\",\"lon\":\""+11.952700+"\"}").getBytes())); // mat 3
					break;
				case 6:
					client.publish("carai/car/driverPred", new MqttMessage(("{\"lat\":\""+57.703559+"\",\"lon\":\""+11.964807+"\"}").getBytes())); // Gym
				break;
				case 7:
					client.publish("carai/car/driverPred", new MqttMessage(("{\"lat\":\""+57.700774+"\",\"lon\":\""+11.950863+"\"}").getBytes()));// Matafär
				break;
				default:
				break;
				}
				
				
			}

		}

	}
	
}