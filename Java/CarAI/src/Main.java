
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.encog.Encog;
import org.encog.util.simple.EncogUtility;

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
import speechrecognition.Watson; 

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
    			System.out.println("Main Mode");
    			Watson mic = new Watson();
    			FaceMQTT f = new FaceMQTT();
    			CarInterface cf = new CarInterface();
    			
    		}
    		else if(args[0].equals("2"))
    		{
    			//System.out.println("FaceRecognition debug");
    			System.out.println("FaceRecog Debug");
    			FaceRecognition f = new FaceRecognition();
    			f.start(true);
    			
    			
    		}
    		else if(args[0].equals("3"))
    		{
    			System.out.println("Schedule debug");
    			try{
        			LocPrediction lp = LocPrediction.getInstance(2, "coords.csv", "networkExport.eg",5);
        			
        			LocPrediction.clearInstance(1);
				} catch (Exception e) {
					
				}
    			
    			
    			try {
					System.in.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
    				
    			List<String> lines;
				try {
					int dayOfTheYear=0;
					lines = java.nio.file.Files.readAllLines(Paths.get("output.txt"));
					
					Random r = new Random();
					for(int j=lines.size()-1;j>0;j--)
					{
						int index = r.nextInt(j+1);
						String a = lines.get(index);
						lines.set(index, lines.get(j));
						lines.set(j, a);
					}
					
					for(int i=0; i<Math.floor(lines.size()*0.7) ;i++)
					{
						String ss = lines.get(i);
						String[] s = ss.split(" " );
	    				int humm =Integer.parseInt(s[1]);
	    				graph.enterPath(Integer.parseInt(s[1]), Integer.parseInt(s[4]), (Integer.parseInt(s[3])), Integer.parseInt(s[2]), 0);
	    				
					}
					
					double[] waightFactors = {1.0,1.0,1.0,1.0,1.0};
						
					int tot=0;
					int corr=0; 
					
					for(int i=(int)Math.floor(lines.size()*0.7); i<lines.size() ;i++)
					{
						String ss = lines.get(i);
						String[] s = ss.split(" " );
						
						graph.setCurrentNode(Integer.parseInt(s[1]));
						Tuple<Tuple<Integer, Double>, ArrayList<Tuple<Integer, Double>>> predicted = graph.predictNextNode((Integer.parseInt(s[3])),  Integer.parseInt(s[2]), 0, waightFactors);
						
						if(predicted.fst().fst()== Integer.parseInt(s[4]))
						{
							tot++;
							corr++;
						}
						else
						{
							tot++;
						}
						
					}
    			
					Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(".//testResult3.txt"),"utf-8"));
					writer.write(
							"The nummber of correct predictions was: " + corr + " out of the total: "+ tot+" ratio of: " + (((double) corr)/((double) tot))
							);

					/*	
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
					*/
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
    		 	n.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml", 0);

    		 	//n.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\OlofLoc.kml", 0);
    		 	
    		 	n.coordCullByBox(57, 11, 2, 8);
    		 	File f = new File(".");
				String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
				
				
    		 	//File data = new File("data");
    		 	//for(File fs : data.listFiles())
    		 	//{
    		 		//System.out.println("User: "+ fs.getName());
    		 		//n.parsGeoEntry(fs.getAbsolutePath());
	    		 	
					System.out.println("Amount of Entries: " +n.getQuerry().size());
	    		 	int size1 = n.getQuerry().size();
	    		 	if(n.getQuerry().size() != 0)
	    		 	{
	    		 		double dist1 = 0;
		    		 	/*for(DatabaseLocation d : n.getQuerry())
		    		 	{
		    		 		dist1 += Utils.distDB(d);
		    		 	}*/
		    		 	System.out.println("Dist before Distance culling: "+ dist1);
		    		 	
		    		 	n.coordCullBySpeed(25);
		    		 	//n.coordCullByDist();
		    		 	System.out.println("Amount of Entries after dist cull: " +n.getQuerry().size());
		    		 	int size2 = n.getQuerry().size();
		    		 	double dist2 = 0;
		    		 	/*for(DatabaseLocation d : n.getQuerry())
		    		 	{
		    		 		dist2 += Utils.distDB(d);
		    		 	}*/
		    		 	System.out.println("Dist after Distance culling: "+ dist2);
		    		
		    		 	Date date = new Date();
		    		 	String tempName = "ELKIClusters" + date.getTime();
		    		 	n.exportAsCoordsToCSV(pathToProj+File.separator+"coords.csv");
		    			boolean er = ELKIController.runElki(tempName);
		    			ArrayList<ArrayList<DatabaseLocation>> clusters = n.importFromElkiClustering(tempName+File.separator);
		    			System.out.println("Amount of Clusters " + (clusters.size()-1));
		    		 	
	    		 	
		    		 	/*try {
		    				BufferedWriter bw = new BufferedWriter(new FileWriter("resultofNoclustering.txt", true));
		    				bw.write(fs.getName()+ "\t" + size1 + "\t" + dist1 +"\t"+  size2+"\t" + dist2 +"\t"+ (clusters.size()-1)+"\t" + er +"\n");
		    				bw.close();
		    			} catch (IOException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			}*/
	    		 	//}
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
    	try {
			System.in.read();
	    	Encog.getInstance().shutdown();
			System.exit(0);
    		//mt.kill();
		}
    	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    	}

    	
    	return ;		
    }
    
    
}
