import java.sql.ResultSet;
import java.sql.SQLException;

import FaceRecognition.FaceRecognition;
import Result.Scheduler;
import serverConnection.DBSCAN;
import serverConnection.ServerConnection;

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
    			FaceRecognition f = new FaceRecognition();
    	    	f.start();
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
    			double [] longs={1,2,1,2,1,3,2,3,10,11,10,11,20};
    			double [] lats= {1,2,2,1,3,1,3,2,10,11,11,10,20};
    			DBSCAN s = new DBSCAN(longs,lats);
    			int temp = s.cluster(2.0, 2);
    			s.getClusterd(temp+1);
	
    		}
    		else 
    		{
    			System.out.println("No argument provided");
    		}
    	}
    	
    	
    	
    }
}
