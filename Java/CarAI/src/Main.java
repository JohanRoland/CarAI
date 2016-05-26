
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
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
import utils.Tuple;
import utils.Utils;
import displayData.PointsPlotter;

public class Main
{
    @SuppressWarnings("resource")
	public static void main(String[] args) {
    	//EMpty gommecnt
    	
    	//MqttTime mt = MqttTime.getInstance();
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
    				
    				for(int i=1; i<182;i++)
        			{
    					if(i!=2)
    					{
		    				LocPrediction lp = LocPrediction.getInstance(i, "coords.csv", "networkExport.eg",1);
		        			LocPrediction.clearInstance(i);
		        			
		        			LocPrediction lp2 = LocPrediction.getInstance(i, "coords.csv", "networkExport.eg",2);      			
		        			LocPrediction.clearInstance(i);
		        			
		        			LocPrediction lp3 = LocPrediction.getInstance(i, "coords.csv", "networkExport.eg",3);
		        			LocPrediction.clearInstance(i);
    					}
        			}

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
    			
    			//PYDBSCAN clusters = new PYDBSCAN();
    			//NNData nn = new NNData();

    			//nn.importFromDB(1,60000);
    			//nn.exportAsClustToCSV();
    			
    			//ArrayList<ArrayList<DatabaseLocation>> temp = clusters.runDBSCAN(nn.getQuerry(), 0.002, 10, 10000);
    			
    			int numberOfClusters=22;//nn.getNrCluster();
    			
    			for(int i=1;i<=	numberOfClusters; i++)
    			{
    				graph.addNode(i);
    			}
    				
    			Stream<String> lines;
				try {
					int dayOfTheYear=0;
					lines = java.nio.file.Files.lines(Paths.get("output.txt"));
	    			lines.forEach(ss -> {
	    				String[] s = ss.split(" " );
	    				int humm =Integer.parseInt(s[1]);
	    				if((humm!=13) && Integer.parseInt(s[3])==1)
	    					throw new Error("humm was: " + humm);
	    				graph.enterPath(Integer.parseInt(s[1]), Integer.parseInt(s[4]), (Integer.parseInt(s[3])), Integer.parseInt(s[2]), 0);
	    				
	    			});
				
    			

					double[] waightFactors = {1.0,1.0,1.0,1.0,1.0};
					Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(".//testResult3.txt"),"utf-8"));
							
						
					for(int i = 1 ; i<=22; i++)
					{
						writer.write("-----------" + i + "-----------\n");
						graph.setCurrentNode(i);
						int lastClust=-1;
						for(int k=1;k<=7;k++)
						{
							for(int j=0;j<1440;j++)
							{

								
								Tuple<Tuple<Integer, Double>, ArrayList<Tuple<Integer, Double>>> tempTemp = graph.predictNextNode(j,k,0, waightFactors);
								Tuple<Integer, Double> temp = tempTemp.fst();
								int currClust= temp.fst();
								if(currClust==13)
								 {
									 int e = 0;
									 e++;
								 }
								double conf = temp.snd();
								if(currClust!=lastClust )
								{
									lastClust=currClust;
									writer.write("Day: " + k + " Time: " + j + " Clust: "+ lastClust +"\n");
								}
							}
						
						}
					}
					writer.close();
					System.out.println("Done !!!!");
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
    			}catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    			
    			
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

    		 	//n.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\OlofLoc.kml", 0);
    		 	
    		 	//n.coordCullByBox(57, 11, 2, 8);
    		 	File f = new File(".");
				String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
				
				
    		 	File data = new File("data");
    		 	for(File fs : data.listFiles())
    		 	{
    		 		System.out.println("User: "+ fs.getName());
    		 		n.parsGeoEntry(fs.getAbsolutePath());
	    		 	System.out.println("Amount of Entries: " +n.getQuerry().size());
	    		 	if(n.getQuerry().size() != 0)
	    		 	{
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
		    		
		    		 	Date date = new Date();
		    		 	String tempName = "ELKIClusters" + date.getTime();
		    		 	n.exportAsCoordsToCSV(pathToProj+File.separator+"coords.csv");
		    			ELKIController.runElki(tempName);
		    			ArrayList<ArrayList<DatabaseLocation>> clusters = n.importFromElkiClustering(tempName+File.separator);
		    			System.out.println("Amount of Clusters " + (clusters.size()-1));
		    		 	
	    		 	}
	    		 }
    		}
    		else if(args[0].equals("9"))
    		{
    			Date date = new Date();
    			String tempName = "ELKIClusters" + date.getTime();
    			
    			File f = new File(".");
				String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
    			
    			NNData n = new NNData();
    			//n.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\OlofLoc.kml", 0);
    		 	n.parseKMLString(0);
    			n.coordCullByBox(55, 11, 14, 13.4);
    			
    			System.out.println("Amount of Entries: " +n.getQuerry().size());
    		 	double dist1 = 0;
    		 	for(DatabaseLocation d : n.getQuerry())
    		 	{
    		 		dist1 += Utils.distDB(d);
    		 	}
    		 	System.out.println("Dist before Distance culling: "+ dist1);
    		 	
    			
    			n.coordCullByDist();
    			System.out.println("Amount of Entries after dist cull: " +n.getQuerry().size());
    		 	double dist2 = 0;
    		 	for(DatabaseLocation d : n.getQuerry())
    		 	{
    		 		dist2 += Utils.distDB(d);
    		 	}
    		 	System.out.println("Dist after Distance culling: "+ dist2);
    		 	
    			n.exportAsCoordsToCSV(pathToProj+File.separator+"coords.csv");
    			ELKIController.runElki(tempName);
    			ArrayList<ArrayList<DatabaseLocation>> clusters = n.importFromElkiClustering(tempName+File.separator);
    			
    			for(ArrayList<DatabaseLocation> c : clusters)
    			{
    				System.out.println(Utils.mean(c));
    			}
    		}
    		else if(args[0].equals("10"))
    		{
    			double[] longs= {12.21473,12.00045,11.9039,11.97063,11.97607,11.94459,11.99262,11.94685,11.97925,11.96961,11.96156,11.96171,11.98528,11.99464,11.98372,12.29634,12.07617,11.97857,18.3124,11.96527,11.93096,11.98611,11.8989,12.00779};
    			double[] lats = {57.64189,57.68377,57.63886,57.69373,57.69683,57.53214,57.69577,57.65056,57.70664,57.74346,57.52621,57.53129,57.70205,57.46538,57.68424,57.66948,57.48712,57.68714,59.27767,57.52312,57.53361,57.68754,57.64275,57.48198};
    			Random r = new Random();
    			try {
    			Writer writer;
				
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(".//outputGPSCoord.txt"),"utf-8"));
			
    			Stream<String> lines = java.nio.file.Files.lines(Paths.get("output.txt"));
    			lines.forEachOrdered(ss ->
    			{
    				String[] s = ss.split(" ");
    				try {
						writer.write
						(
									(lats[Integer.parseInt(s[0])-1] + r.nextGaussian()*0.002)+" " +(longs[Integer.parseInt(s[0])-1]+ r.nextGaussian()*0.002)+ " " 
								    +(lats[Integer.parseInt(s[1])-1]+ r.nextGaussian()*0.002)+" " +(longs[Integer.parseInt(s[1])-1]+ r.nextGaussian()*0.002)+ " "
								    +Integer.parseInt(s[2])+ " " +Integer.parseInt(s[3])+ " "
								    +(lats[Integer.parseInt(s[4])-1]+ r.nextGaussian()*0.002)+" " +(longs[Integer.parseInt(s[4])-1]+ r.nextGaussian()*0.002)+"\n"
						);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			});
    			} catch (UnsupportedEncodingException | FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			
    		}
    		else if(args[0].equals("11"))
    		{
    			NNData n = new NNData();
    		 	//n.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml", 0);

    		 	//n.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\OlofLoc.kml", 0);
    		 	
    		 	//n.coordCullByBox(57, 11, 2, 8);
    		 	File f = new File(".");
				String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
				
				
    		 	File data = new File("data");
    		 	for(File fs : data.listFiles())
    		 	{
    		 		System.out.println("User: "+ fs.getName());
    		 		n.parsGeoEntry(fs.getAbsolutePath());
	    		 	System.out.println("Amount of Entries: " +n.getQuerry().size());
	    		 	if(n.getQuerry().size() != 0)
	    		 	{
		    		 	n.coordCullByDist();
		    		 	
		    		 	if(0<n.getQuerry().size())
		    		 	{
		    		 		System.out.println("#OOPS");
		    		 		n.exportToDB(Integer.parseInt(fs.getName())+3);
		    		 	}
	    		 	}
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
