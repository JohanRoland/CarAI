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

import facerecognition.FaceMQTT;
import result.Scheduler;
import serverConnection.DBSCAN;
import serverConnection.ServerConnection;
import serverConnection.DBSCAN.Tupple;

/**
 *  Main class for CarAI
 * @author Axhav
 *
 */
public class Main
{
	
    public static void main(String[] args) {
    	
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
    			
    			//FaceRecognition f = new FaceRecognition();
    	    	//f.start(true);
    		}
    		else if(args[0].equals("3"))
    		{
    			System.out.println("Schedule debug");
    			Scheduler s = new Scheduler();
    		}
    		else if(args[0].equals("4"))
    		{
    			ServerConnection a;
    			a= new ServerConnection("mydb","3306","localhost" , "car", "RigedyRigedyrektSon");
    			double[] b = {2,7};
    			double[] c = {3,3};
    			try {
					a.replacePosData(1,b,c);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    		else if(args[0].equals("5"))
    		{
    			try {
    			FileReader fileReader = new FileReader("PhoneData.txt");
    			BufferedReader bufferedReader = new BufferedReader(fileReader);
    			String line=null;
    			ArrayList<Float> longs = new ArrayList<Float>();
    			ArrayList<Float> lats = new  ArrayList<Float>();
    			int counter=0;

					while((line = bufferedReader.readLine()) != null) {
					        String[] temp = line.split(",");
					        longs.add(Float.parseFloat(temp[2]));
					        lats .add(Float.parseFloat(temp[3]));
					        if (counter<100)
					        	counter++;
					        else
					        	break;
					    }
				
    			
    			DBSCAN s = new DBSCAN(longs,lats);	
    			int temp = s.cluster(0.001, 20);
    			
    			try (PrintStream out = new PrintStream(new FileOutputStream("clusterd.txt"))) {
    				ArrayList<Tupple<Float>>[] temp2 = s.getClusterd(temp);
    				int count=0;
    				for(ArrayList<Tupple<Float>> str : temp2)
    				{
    					count++;
    					out.print("x"+count +" = [");
    					for(Tupple<Float> v : str)
    					{
	    					out.print(v.fst().toString()+" ");
	    					
    					}
    					out.print("];\n");
    					out.print("y"+count +" = [");
    					for(Tupple<Float> v : str)
    					{
	    					out.print(v.snd().toString()+" ");
	    					
    					}
    					out.print("];\n");
    				}
    				out.print("plot(");
    				for(int i=count; i>1;i--)
    				{
    					out.print("x"+i+" ,");
    					out.print("y"+i+" ,");
    				}
    				out.print("x1,");
					out.print("y1)");
    				
    			} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			bufferedReader.close();
    			} catch (Exception e1) {
					e1.printStackTrace();
				}
    			
    		}
    		else 
    		{
    			System.out.println("No argument provided");
    		}
    	}
    	
    	
    	
    }
}
