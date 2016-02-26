import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import car.CarInterface;
import facerecognition.FaceMQTT;
import forReport.Analyze;
import interfaces.DatabaseLocation;
import mashinelearning.NNData;
import mashinelearning.PYDBSCAN;
import predictorG.PredictorG;
import result.LocPrediction;
import result.Scheduler;
import serverConnection.DBSCAN;
import serverConnection.KmeansSortOf;
import serverConnection.ServerConnection;
import utils.MqttTime;
import utils.Tuple;
import displayData.PointsPlotter;

/**
 *  Main class for CarAI
 * @author Axhav
 *
 */
public class Main
{
	
    public static void main(String[] args) {
    	
    	MqttTime mt = MqttTime.getInstance();
    	//Analyze.analyzeLearningData();
    	//System.exit(0);
    	if(args.length > 0)
    	{
    		if(args[0].equals("1"))
    		{
    			System.out.println("SpeechRecognition Debug");
    			System.out.println("TO BE IMPLEMENTED");
    		}
    		else if(args[0].equals("2"))
    		{
    			System.out.println("FaceRecognition debug");
    			
    			FaceMQTT f = new FaceMQTT();
    			CarInterface cf = new CarInterface();
    			
    			//FaceRecognition f = new FaceRecognition();
    	    	//f.start(true);
    		}
    		else if(args[0].equals("3"))
    		{
    			System.out.println("Schedule debug");
    		    //	Scheduler s = new Scheduler();
    			LocPrediction lp = LocPrediction.getInstance(1);
    			
    		}
    		else if(args[0].equals("4"))
    		{
    			ServerConnection sc= ServerConnection.getInstance();
    			
    			try {
					System.out.println("Created ID: " +sc.addUserData("Johan"));
				} catch (SQLException e) {
					System.out.println("You SUCK!!");
					e.printStackTrace();
				}
    			
    		}
    		else if(args[0].equals("5"))
    		{
    			/*
    			try {
    			FileReader fileReader = new FileReader("PhoneData.txt");
    			BufferedReader bufferedReader = new BufferedReader(fileReader);
    			String line=null;
    			ArrayList<Double> longs = new ArrayList<Double>();
    			ArrayList<Double> lats = new  ArrayList<Double>();
    			int counter=0;

					while((line = bufferedReader.readLine()) != null) {
					        String[] temp = line.split(",");
					        longs.add(Double.parseDouble(temp[2]));
					        lats .add(Double.parseDouble(temp[3]));
					        if (counter<50000)
					        	counter++;
					        else
					        	break;
					    }
				*/
    			ServerConnection b = ServerConnection.getInstance();
    			//b= new ServerConnection("mydb","3306","localhost" , "car", "RigedyRigedyrektSon");
    			try (PrintStream out = new PrintStream(new FileOutputStream("clusterd.txt"))) {
    			ArrayList<DatabaseLocation> longLat = b.getPosClass(1,10000);
    			
    			
    			DBSCAN s = new DBSCAN(longLat, true);	
    			int temp = s.cluster(0.002,2);

    			
    			
    				ArrayList<ArrayList<DatabaseLocation>> temp2 = s.getClusterd(true);
    				int count=0;
    				for(ArrayList<DatabaseLocation> str : temp2)
    				{
    					count++;
    					out.print("x"+count +" = [");
    					for(DatabaseLocation v : str)
    					{
	    					out.print(v.getLat()+" ");
	    					
    					}
    					out.print("];\n");
    					out.print("y"+count +" = [");
    					for(DatabaseLocation v : str)
    					{
	    					out.print(v.getLon()+" ");	    					
    					}
    					out.print("];\n");
    				}
    				out.print("plot(");
    				for(int i=count; i>1;i--)
    				{
    					out.print("x"+i+" ,");
    					out.print("y"+i+",'o' ,");
    				}
    				out.print("x1,");
					out.print("y1,'o')");
    				
    			/*} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			bufferedReader.close();
    			*/
    			} catch (Exception e1) {
					e1.printStackTrace();
				}
    			
    		}
    		else if(args[0].equals("6"))
    		{
    			PointsPlotter pp = new PointsPlotter();
    			pp.setVisible(true);
    			
    		}
    		else if(args[0].equals("7"))
    		{
    			PredictorG graph = new PredictorG();
    			
    			PYDBSCAN clusters = new PYDBSCAN();
    			NNData nn = new NNData();
    			nn.importFromDB(1,10000);
    			
    			ArrayList<ArrayList<DatabaseLocation>> temp = clusters.runDBSCAN(nn.getQuerry(), 0.002, 10, 10000);
    			
    			int numberOfClusters=temp.size();
    			
    			for(int i=0;i<numberOfClusters; i++)
    			{
    				//graph.addNode(i);
    			}
    				
    			
    			
    			
    			for(int i=0; i<numberOfClusters;i++)
    			{
    				int size=temp.get(i).size();
    				for(int j=0;j<size;j++)
    				{
    					int cluster = 0; // need to fin what cluster it is goint to 
    					graph.enterPath(i,cluster,temp.get(i).get(j).getHTime(),temp.get(i).get(j).getMTime(),0);
    				}
    			}
    			
    			
    			
    			
    		}
    		else
    		{
    			System.out.println("No argument provided");
    		}
    	}
    	
    	
    	
    }
}
