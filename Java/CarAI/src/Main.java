import FaceRecognition.FaceRecognition;

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
    		else 
    		{
    			System.out.println("No argument provided");
    		}
    	}
    	
    	
    	
    }
}
