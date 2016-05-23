import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Stream;


import car.CarInterface;
import facerecognition.FaceMQTT;
import facerecognition.FaceRecognition;
import interfaces.DatabaseLocation;
import mashinelearning.ELKIController;
import mashinelearning.NNData;
import mashinelearning.PYDBSCAN;
import prediction.LocPrediction;
import prediction.Network;
import predictorG.PredictorG;
import serverConnection.ServerConnection;
import utils.MqttTime;
import utils.Utils;
import displayData.PointsPlotter;

public class Main
{
    public static void main(String[] args) {
    	//EMpty gommecnt
    	
    	MqttTime mt = MqttTime.getInstance();
    	//Analyze.analyzeLearningData();
    	//System.exit(0);
    	if(args.length > 0)
    	{
    		if(args[0].equals("1"))
    		{
    			System.out.println("FaceRecog Debug");
    			FaceRecognition f = new FaceRecognition();
    			f.start(true);
    		}
    		else if(args[0].equals("2"))
    		{
    			//System.out.println("FaceRecognition debug");
    			
    			FaceMQTT f = new FaceMQTT();
    			CarInterface cf = new CarInterface();
    			
    			
    		}
    		else if(args[0].equals("3"))
    		{
    			System.out.println("Schedule debug");
    		    //	Scheduler s = new Scheduler();

    			try {
        			LocPrediction lp = LocPrediction.getInstance(3, "coords.csv", "networkExport.eg",1);
        			LocPrediction.clearInstance(3);
        			LocPrediction lp2 = LocPrediction.getInstance(3, "coords.csv", "networkExport.eg",2);
        			lp2.predictHyperTwoClust(2, 4);
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    		else if(args[0].equals("4"))
    		{
    			PointsPlotter pp = new PointsPlotter();
    			pp.setVisible(true);
    			
    		}
    		else if(args[0].equals("5"))
    		{
    			PredictorG graph = PredictorG.getInstance(1);
    			
    			PYDBSCAN clusters = new PYDBSCAN();
    			NNData nn = new NNData();

    			nn.importFromDB(1,60000);
    			nn.exportAsClustToCSV();
    			
    			//ArrayList<ArrayList<DatabaseLocation>> temp = clusters.runDBSCAN(nn.getQuerry(), 0.002, 10, 10000);
    			
    			int numberOfClusters=nn.getNrCluster();
    			
    			for(int i=1;i<=	numberOfClusters; i++)
    			{
    				graph.addNode(i);
    			}
    				
    			Stream<String> lines;
				try {
					lines = java.nio.file.Files.lines(Paths.get("coords.csv"));
	    			lines.forEach(ss -> {
	    				String[] s = ss.split(" " );
	    				graph.enterPath(Integer.parseInt(s[0]), Integer.parseInt(s[3]), (Integer.parseInt(s[1])*60)+Integer.parseInt(s[2]), 0, 0);
	    				
	    			});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
				graph.setCurrentNode(2);
				double[] waightFactors = {1.0,1.0,1.0,1.0,1.0};
				for(int i = 0 ; i<1440; i=i+30)
					System.out.println("prediction at time: " + i + " "+ graph.predictNextNode(i,0,0, waightFactors ));
				
				
    			/*
    			for(int i=0; i<numberOfClusters;i++)
    			{
    				int size=temp.get(i).size();
    				for(int j=0;j<size;j++)
    				{
    					int cluster = 0; // need to fin what cluster it is goint to 
    					
    					graph.enterPath(i,cluster,temp.get(i).get(j).getHTime(),temp.get(i).get(j).getMTime(),0);
    				}
    			}
    			*/
    			
    			
    			
    		}
    		else if(args[0].equals("6"))
    		{
    			
				NNData nn=new NNData();
				nn.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml", 0);
				nn.coordCullByDist();
				
				nn.exportToDB(1);
				try {
					ServerConnection s= ServerConnection.getInstance();
					s.replacePosData(0, nn.getQuerry());
					//s.addGeoUsers("C:\\Users\\Knarkapan\\git\\CarAI\\Java\\CarAI\\data");

    				
    				
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		else if(args[0].equals("7"))
    		{
    			/**
    			 * Make the test sute thingi
    			 */
    			try {
        			LocPrediction lp = LocPrediction.getInstance(3, "output.txt", "networkExport.eg",4);

				} catch (Exception e) {
					e.printStackTrace();
				}
    			
    		}
    		else if(args[0].equals("8"))
    		{
    		 	NNData n = new NNData();
    		 	//n.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml", 0);

    		 	n.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\OlofLoc.kml", 0);
    		 	n.coordCullByBox(57, 11, 2, 8);
    		 	System.out.println("Amount of Entries: " +n.getQuerry().size());
    		 	double dist1 = 0;
    		 	for(DatabaseLocation d : n.getQuerry())
    		 	{
    		 		dist1 += Utils.distDB(d);
    		 	}
    		 	System.out.println("Dist before Distance culling: "+ dist1);
    		 	
    		 	n.coordCullBySpeed(25);;
    		 	System.out.println("Amount of Entries after dist cull: " +n.getQuerry().size());
    		 	double dist2 = 0;
    		 	for(DatabaseLocation d : n.getQuerry())
    		 	{
    		 		dist2 += Utils.distDB(d);
    		 	}
    		 	System.out.println("Dist after Distance culling: "+ dist2);
    		 	
    		 	
    		}
    		else if(args[0].equals("9"))
    		{
    			File f = new File(".");
				String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
    			
    			NNData n = new NNData();
    			n.parseKMLString(0);
    			n.coordCullByBox(57, 11, 2, 8);
    			n.coordCullByDist();
    			ELKIController.runElki();
    			ArrayList<ArrayList<DatabaseLocation>> clusters = n.importFromElkiClustering(pathToProj+"\\ELKIClusters\\");
    			
    			for(ArrayList<DatabaseLocation> c : clusters)
    			{
    				System.out.println(Utils.mean(c));
    			}
    		}
    		else
    		{
    			System.out.println("No argument provided");
    		}
    	}
    	
    	//***************Cleanup*****************
    	/*try {
    		
    		mt.kill();
		}
    	catch (MqttException e)
    	{
			e.printStackTrace();
		}*/
    	//Encog.getInstance().shutdown();
    	//*************End Cleanup****************
    	
    	return ;		
    }
    
    
}
