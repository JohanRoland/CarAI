import java.sql.ResultSet;
import java.sql.SQLException;

import FaceRecognition.FaceRecognition;
import Result.Scheduler;
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
    			ResultSet r= a.basicQuery("Call getCalHist(1)");
				try{
    			while(r.next())
    			{

    					int i = r.getInt(1);
    					String s = r.getString(2);
    					System.out.println(i+"\t"+s);

    			}
				}
				catch (SQLException e) {}   			
    			
    		}
    		else 
    		{
    			System.out.println("No argument provided");
    		}
    	}
    	
    	
    	
    }
}
