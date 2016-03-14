package mashinelearning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import interfaces.DatabaseLocation;
import serverConnection.DBSCAN;
import serverConnection.ServerConnection;
import serverConnection.ServerConnection.DBQuerry;
import utils.Tuple;
import utils.Utils;

public class NNData
{
	// Inputs
	ArrayList<double[]> input;
	ArrayList<Integer> minutes;
	ArrayList<Integer> hours;
	ArrayList<Integer> inputClust;
	
	
	//Outputs
	ArrayList<double[]> output;
	ArrayList<Integer> outputClust;
	
	//View Datas
	HashMap<Integer, Tuple<Double,Double>> viewClustPos; 
	int nrCluster;
	
	DBSCAN tree;
	
	ArrayList<DatabaseLocation> querry;
	
	ArrayList<Tuple<Double,Double>> means;
	
	public NNData()
	{
		
		input = new ArrayList<double[]>();
		output = new ArrayList<double[]>();
		minutes = new ArrayList<Integer>();
		hours = new ArrayList<Integer>();
		inputClust = new ArrayList<Integer>();
		outputClust = new ArrayList<Integer>();
		viewClustPos = new HashMap<Integer, Tuple<Double,Double>>();
		means = new ArrayList<Tuple<Double,Double>>(); 
		nrCluster = 0;
	}
	
	public int getNrCluster()
	{
		return nrCluster;
	}

	public HashMap<Integer, Tuple<Double,Double>> getViewClustPos()
	{
		return viewClustPos;
	}
	
	public double[][] getInputData()
	{
		double[][] ret = new double[input.size()][]; 
		for(int i = 0; i < input.size(); i++)
		{
			ret[i] = input.get(i);
		}
		return ret;
	}
	
	public double[][] getOutputData()
	{
		double[][] ret = new double[output.size()][]; 
		for(int i = 0; i < output.size(); i++)
		{
			ret[i] = output.get(i);
		}
		return ret;
	}
	
	public int getClosestCluster(Tuple<Double,Double> pos)
	{
		double dist = Double.MAX_VALUE;
		int temp = -1;
		for(int i = 0; i <  means.size(); i++)
		{
			double t = Utils.distFrom(means.get(i).fst(), means.get(i).snd(), pos.fst(), pos.snd());
			if(dist > t)
			{
				dist = t;
				temp = i+1;
			}
			
		}
		
		return temp; //tree.associateCluster(pos,0.01);
	}
	
	public void importFromDB(int id,int n)
	{
		ServerConnection b = ServerConnection.getInstance();
		//b= new ServerConnection();
		try {
			querry = b.getPosClass(id,n);
			ArrayList<DatabaseLocation> temp = new ArrayList<DatabaseLocation>();
			for(int i = querry.size()-1; i >= 0; i--)
			{
				temp.add(querry.get(i));
			}
			querry = temp;
		} catch (SQLException e) {
			System.out.println("Error Downloading Data");
			e.printStackTrace();
		}
		System.out.println("Finished downloading data");

	}
	
	public void parseGPX(String path)
	{
		try
		{
			File xmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			Document doc  = dBuilder.parse(xmlFile);
			
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("trkpt");
			
			String builder = "";
			for(int i = 0; i < nList.getLength(); i++)
			{
				
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	
					Element eElement = (Element) nNode;
					builder = builder + eElement.getAttribute("lat") + " , " + eElement.getAttribute("lon");
					
				 	String time = eElement.getElementsByTagName("time").item(0).getTextContent();
				 	int h = Integer.parseInt(time.substring(11, time.length()-6).replace(":",""));
				 	int min = Integer.parseInt(time.substring(13, time.length()-3).replace(":",""));
				 			
				 	
				 	String tmdasd= time.substring(11, time.length()-3).replace(":","");
				 	double t = Math.floor( Double.parseDouble(tmdasd)/100);
				 	
				 	double lat = Double.parseDouble(eElement.getAttribute("lat"));
				 	double lon = Double.parseDouble(eElement.getAttribute("lon"));
					double[] tmp =  {lon,lat};
					input.add(tmp);
					hours.add(h);
					minutes.add(min);
					if(i+1 <nList.getLength())
					{
						Node oNode = nList.item(i+1);
						if (oNode.getNodeType() == Node.ELEMENT_NODE) {
							Element oElement = (Element) oNode;
							double[] tmp2 = {Double.parseDouble(oElement.getAttribute("lon")), Double.parseDouble(oElement.getAttribute("lat"))};
							output.add(tmp2);
						}
					}
					else
					{
						double[] tmp2 = {lon,lat};
						output.add(tmp2);
					}
				}
				
			}
		
		}
		catch(Exception e){}
	
		
	}
	
	public void parseKML(String path,int amount)
	{
		try{
			File xmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			Document doc  = dBuilder.parse(xmlFile);
			
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("gx:coord");
			NodeList tList = doc.getElementsByTagName("when");
			int count = amount;
			if(amount <= 0)
			{
				count = nList.getLength();
			}
			
			String builder = "";
			for(int i = count-1; i >= 0; i--)
			{
				Node nNode = nList.item(i);
				Node tNode = tList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	
					Element eElement = (Element) nNode;
					Element tElement = (Element) tNode;
					
					String[] coordinates = eElement.getTextContent().split(" ");
					String[] fullDateTime = tElement.getTextContent().substring(0, tElement.getTextContent().length()-1).split("T");
					
					//TIME PARSING
					String[] splitTime = fullDateTime[1].split(":");
					int h = Integer.parseInt(splitTime[0]);
					int min = Integer.parseInt(splitTime[1]);;
					
					
					//GPS PARSING
					double lat = ((double)Math.round(Double.parseDouble(coordinates[0])*10000000))/10000000;
				 	double lon = ((double)Math.round(Double.parseDouble(coordinates[1])*10000000))/10000000;
					double[] tmp =  {lon,lat};
					input.add(tmp);
					hours.add(h);
					minutes.add(min);
					
					if(i != 0)
					{
						Node oNode = nList.item(i-1);
						if (oNode.getNodeType() == Node.ELEMENT_NODE) {
							Element oElement = (Element) oNode;
							String[] nCoordinates = oElement.getTextContent().split(" ");
							double lat2 = ((double)Math.round(Double.parseDouble(nCoordinates[1])*10000000))/10000000;
						 	double lon2 = ((double)Math.round(Double.parseDouble(nCoordinates[0])*10000000))/10000000;
							double[] tmp2 = {lat2,lon2};
							output.add(tmp2);
						}
					}
					else
					{
						double[] tmp2 = {lon,lat};
						output.add(tmp2);
					}
					
				}
			}
			System.out.println("Done fetching data");
		}
		catch(Exception e)
		{
			
		}
	}
	
	public void exportToNN(double[][] in,double[][] out)
	{
		double[][] parsedInData = new double[input.size()][];
		for(int i = 0; i < input.size(); i++)
		{
			parsedInData[i] = input.get(i);
		}
		double[][] parsedOutData = new double[output.size()][];
		for(int i = 0; i < output.size(); i++)
		{
			parsedOutData[i] = output.get(i);
		}
		out = parsedOutData;
		in = parsedInData; 
		
	}
	
	public ArrayList<ArrayList<DatabaseLocation>> importFromElkiClustering(String path)
	{
		ArrayList<ArrayList<DatabaseLocation>> output = new ArrayList<ArrayList<DatabaseLocation>>(); 
		HashMap<Tuple<Double,Double>,Integer> clusterMap= new HashMap<Tuple<Double,Double>,Integer>();
		try {
		Files.walk(Paths.get(path)).forEach(filePath -> {
		    if (Files.isRegularFile(filePath) && !filePath.toString().contains("settings")) {
		        
		    	amountofClusts++;
		    	try {
					ArrayList<String> temp;
					temp = (ArrayList<String>) Files.readAllLines(filePath);
					
					int clustID=0;
					if(!(new File(filePath.toString())).getName().equals("noise.txt"))
					{
						
						clustID=Integer.parseInt((new File(filePath.toString())).getName().replaceFirst("cluster_", "").replaceFirst(".txt", ""))+1;
					}
					
					for(String a : temp)
					{
						if(!a.startsWith("#"))
						{
							String[] lonLat= a.split(" ");
							clusterMap.put(new Tuple<Double,Double>(Double.parseDouble(lonLat[1]),Double.parseDouble(lonLat[2])),clustID);
						}	
					}
				} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
		    }
		});
		} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		for(int i = 0 ; i <= amountofClusts; i++)
		{
			output.add(new ArrayList<DatabaseLocation>());
		}
		
		for(DatabaseLocation dl : querry)
		{
			int clustId = clusterMap.get(new Tuple<Double,Double>(dl.getLat(),dl.getLon()));
			output.get(clustId).add(dl);
		}
		
		return output;

		
	}

	
	
	/**
	 * 	Runs DBSCAN on the imported data 
	 * 
	 * @param n Amounts of datapoints to be sampled
	 */
	public void exportAsClustToCSV()
	{
		//tree = new DBSCAN(querry, true);	
		
		//int temp = tree.cluster(0.01, 2);
		//querry =  importFromFile();
		//exportAsCoordsToCSV();
		//PYDBSCAN py = new PYDBSCAN();
		
		ArrayList<ArrayList<DatabaseLocation>> temp2 = importFromElkiClustering("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\ELKIClusters\\"); //py.runDBSCAN(querry, 0.001, 20, n); //tree.getClusterd(true);
		System.out.println("Done Getting Cluster");
		HashMap<Tuple<Double,Double>,Tuple<Double,Double>> hs = new HashMap<Tuple<Double,Double>,Tuple<Double,Double>>();
		HashMap<Tuple<Double,Double>,Integer> clust = new HashMap<Tuple<Double,Double>,Integer>();
		HashMap<Tuple<Double,Double>,DatabaseLocation> posToLoc = new HashMap<Tuple<Double,Double>,DatabaseLocation>();
		nrCluster = temp2.size();
		for(int i = 0; i < temp2.size();i++)
		{
			if(i == 0)
			{
				for(DatabaseLocation dbl : temp2.get(i))
				{
					Tuple<Double,Double> d = new Tuple<Double,Double>(dbl.getLat(),dbl.getLon());
					hs.put(d, d);
					clust.put(d, i);
					posToLoc.put(d, dbl);
				}
			}
			else
			{
				
				Tuple<Double,Double> mean = Utils.mean(temp2.get(i));
				means.add(mean);
				viewClustPos.put(i, mean);
				for(DatabaseLocation dbl : temp2.get(i))
				{
					Tuple<Double,Double> coord = new Tuple<Double,Double>(dbl.getLat(),dbl.getLon());
					hs.put(coord,mean);
				}
				clust.put(mean, i);
			}
			
		}
		
		System.out.println("Done first Data Iteration");
		
		hours = new ArrayList<Integer>();
		minutes = new ArrayList<Integer>();
		
		for(int i = 0; i < temp2.size(); i++)
		{
			for(int j = 0; j < temp2.get(i).size(); j++)
			{
				double[] pos = {temp2.get(i).get(j).getLat(),temp2.get(i).get(j).getLon()};
				double[] dest = {temp2.get(i).get(j).getNLat(),temp2.get(i).get(j).getNLon()};
				Tuple<Double,Double> dst = findNextCluster( new Tuple<Double,Double>(temp2.get(i).get(j).getNLat(),temp2.get(i).get(j).getNLon()),posToLoc);
				
				Tuple<Double,Double> meanDst = hs.get(dst);
				
				if(meanDst == null || clust.get(meanDst) == null)
				{
					break;
				}
				
				if(i == 0)
				{
					/*
					input.add(pos);
					 
					dest[0] = meanDst.fst();
					dest[1] = meanDst.snd();
					output.add(dest);
					hours.add(temp2[i].get(j).getHTime());
					minutes.add(temp2[i].get(j).getMTime());
					*/
				}
				else if(clust.get(meanDst) != i)
				{
					Tuple<Double,Double> coord = new Tuple<Double,Double>(temp2.get(i).get(j).getLat(),temp2.get(i).get(j).getLon());
					pos[0] = hs.get(coord).fst();
					pos[1] = hs.get(coord).snd();
					
					input.add(pos);
					
					inputClust.add(i);
					dest[0] = meanDst.fst();
					dest[1] = meanDst.snd();
					output.add(dest);
					hours.add(temp2.get(i).get(j).getHTime());
					minutes.add(temp2.get(i).get(j).getMTime());
					outputClust.add(clust.get(hs.get(dst)));
				}
				
			}
		}
		System.out.println("Done Formatting datastructure");
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("coords.csv"),"utf-8")))
		{
			for(int i = 0; i < inputClust.size();i++)
			{
				/*writer.write(input.get(i)[1] + " " + input.get(i)[0] + " " + hours.get(i) + " " + minutes.get(i) + " " 
						+ output.get(i)[1] + " " + output.get(i)[0] + "\n");*/
				//for(int noise = -5 ; noise < 5; noise++)
					writer.write(inputClust.get(i) + " " + (hours.get(i) * 60 + minutes.get(i)) + " " 
						+ outputClust.get(i) + "\n");
			}
		}catch(Exception e)
		{
			System.out.println("Error on creating csv file");
			e.printStackTrace();
		}
		
	}
	
	public void exportAsCoordsToCSV()
	{
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("coords.csv"),"utf-8")))
		{
			for(int i = 0; i < querry.size();i++)
			{
				writer.write(querry.get(i).getLat()+ " " + querry.get(i).getLon() + " " + (querry.get(i).getHTime()*60+querry.get(i).getMTime()) + " " 
						+ querry.get(i).getNLat() + " " + querry.get(i).getNLon()+ "\n");
				
			}
			writer.close();
		}catch(Exception e)
		{
			System.out.println("Error on creating csv file");
			e.printStackTrace();
		}
	}
	public void coordClull()
	{
		ArrayList<DatabaseLocation> temp = new ArrayList<DatabaseLocation>();
		double accDist=0;
		DBQuerry db = null;
		int counterOfRemovedCoords=0;
		for(int i =0 ; i<querry.size()-1;i++)
		{
			accDist =+Math.abs(Utils.distFrom(querry.get(i).getLat(), querry.get(i).getLon(), querry.get(i).getNLat(), querry.get(i).getNLon()));
			if(accDist<5000 && i!=(querry.size()-2))
			{
				counterOfRemovedCoords++;
				if(db==null)
				{
					db=(DBQuerry) querry.get(i);
				}
				
			}
			else
			{
				accDist=0;
				if(db==null)
				{
					temp.add(querry.get(i));
				}
				else
				{
					temp.add(new DBQuerry(db.getLat(),db.getLon(),db.getHTime(),db.getMTime(),querry.get(i).getNLat(),querry.get(i).getNLon()));
					db=null;
				}

			}	
		}
		System.out.println("The nummber of culled coords wasr: "+counterOfRemovedCoords);
		temp.add(querry.get(querry.size()-1));
		querry = temp;
	}

	public void coordCullByBox(double lat,double lon, double hight ,double width)
	{
		double lonPrime = lon+width;
		double latPrime = lat+hight;
		
		ArrayList<DatabaseLocation> temp = new ArrayList<DatabaseLocation>();
		int counterOfRemovedCoords=0;
	
		double	dist =Math.abs(Utils.distFrom(querry.get(0).getLat(), querry.get(0).getLon(), querry.get(0).getNLat(), querry.get(0).getNLon()));
		int lastH=querry.get(0).getHTime();
		int lastM=querry.get(0).getMTime();	
		int lastValidPoint=0;
		
		for(int i =1 ; i<querry.size()-1;i++)
		{
			
				if(querry.get(i).getLat()>lat && querry.get(i).getLat()<latPrime && querry.get(i).getLon()>lon && querry.get(i).getLon()<lonPrime)
				{
					temp.add(new DBQuerry(querry.get(lastValidPoint).getLon(),querry.get(lastValidPoint).getLat(),lastH,lastM,querry.get(i).getLon(),querry.get(i).getLat()));
					lastH = querry.get(i).getHTime();
					lastM = querry.get(i).getMTime();
					lastValidPoint=i;
				
				}
				else
				{
					counterOfRemovedCoords++;
				}
		}
		System.out.println("The nummber of culled coords wasr: "+counterOfRemovedCoords);
		//stemp.add(new DBQuerry(temp.get(temp.size()-1).getNLat(),temp.get(temp.size()-1).getNLon(),temp.get(temp.size()-1).getHTime(),temp.get(temp.size()-1).getMTime(),temp.get(temp.size()-1).getNLat(),temp.get(temp.size()-1).getNLon()));
		querry = temp;
		
		
		
	}

	/**
	 * Removes the paths that has a lower speed than the threshold 
	 * or that has zero distance traveled or that has zero difference in time.
	 * New paths are created linking the all coordinates like a chain as it is assumed that the
	 * paths under the threshold where not traveling significant distances.
	 * 
	 */
	public void coordClullBySpeed(double threshold)
	{
		ArrayList<DatabaseLocation> temp = new ArrayList<DatabaseLocation>();
		int counterOfRemovedCoords=0;

		double	dist =Math.abs(Utils.distFrom(querry.get(0).getLat(), querry.get(0).getLon(), querry.get(0).getNLat(), querry.get(0).getNLon()));
		int lastH=querry.get(0).getHTime();
		int lastM=querry.get(0).getMTime();
		int oldTime = lastH*60+lastM;	
		int lastValidPoint=0;
		int takingAbrake=0;
		
		for(int i =1 ; i<querry.size()-1;i++)
		{
			
			int time =querry.get(i).getHTime()*60+querry.get(i).getMTime();
			
			 /* querry.get(i).getLat()>11.5 && querry.get(i).getLon()>56.5 && */
			if((time-oldTime)!=0)
			{
				if(dist!=0 && ((dist*1000)/((time-oldTime)*60)>threshold))
				{
					if(takingAbrake>0)
					{
						temp.add(new DBQuerry(querry.get(lastValidPoint).getLat(),querry.get(lastValidPoint).getLon(),lastH,lastM,querry.get(i).getLat(),querry.get(i).getLon()));
						lastH = querry.get(i).getHTime();
						lastM = querry.get(i).getMTime();
						lastValidPoint=i;
						takingAbrake=0;
					}
					else
					{
						takingAbrake=0;
						counterOfRemovedCoords++;
					}
				}
				else
				{
					takingAbrake++;
					counterOfRemovedCoords++;
					
				}
			}
			else
			{
				counterOfRemovedCoords++;
			}
			dist = Math.abs(Utils.distFrom(querry.get(i).getLat(), querry.get(i).getLon(), querry.get(i).getNLat(), querry.get(i).getNLon()));
			oldTime=time;	
			
			
		}
		System.out.println("The nummber of culled coords wasr: "+counterOfRemovedCoords);
		//stemp.add(new DBQuerry(temp.get(temp.size()-1).getNLat(),temp.get(temp.size()-1).getNLon(),temp.get(temp.size()-1).getHTime(),temp.get(temp.size()-1).getMTime(),temp.get(temp.size()-1).getNLat(),temp.get(temp.size()-1).getNLon()));
		querry = temp;
	}

	public void coordCullByDist()
	{
		ArrayList<DatabaseLocation> temp = new ArrayList<DatabaseLocation>();
 		
		boolean traveling = false;
		boolean stopping = false;
		
		Tuple<Double,Double> start = new Tuple<Double,Double>(0.0,0.0);
		Tuple<Double,Double> stop = new Tuple<Double,Double>(0.0,0.0);
		Tuple<Integer,Integer> tempTime = new Tuple<Integer,Integer>(0,0);
		
		for(int i = 0; i < querry.size(); i++)
		{
			if(Utils.distDB(querry.get(i)) < 15)
			{
				if(traveling)
				{
					stop.setFst(querry.get(i).getLat());
					stop.setSnd(querry.get(i).getLon());
					temp.add(new DBQuerry(start.fst(),start.snd(),tempTime.fst(),tempTime.snd(),stop.fst(),stop.snd()));
					
				}
				
				
			}
			else
			{
				
			}
		}
		
		
	}
	
	public void exportToDB(int id)
	{
		ServerConnection sc =  ServerConnection.getInstance(); //new ServerConnection();
		ArrayList<DBQuerry> querry = new ArrayList<DBQuerry>();
		
		try {
			for(int i = 0; i < input.size(); i++)
			{ 
				querry.add(new DBQuerry(input.get(i)[0], input.get(i)[1], hours.get(i), minutes.get(i), output.get(i)[0], output.get(i)[1]));
			}
			System.out.println("Done formatting QuerryArrayList " + querry.size());
			DBQuerry[] sendDB = querry.toArray(new DBQuerry[querry.size()]);
			sc.replacePosData(id, sendDB );
			//sc.addPosData(0, input.get(i)[0], input.get(i)[1], input.get(i)[2], output.get(i)[0], output.get(i)[1]);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<DatabaseLocation> importFromFile()
	{
		querry = new ArrayList<DatabaseLocation>();
		for(int i = 0; i < input.size(); i++)
		{ 
			querry.add(new DBQuerry(input.get(i)[0], input.get(i)[1], hours.get(i), minutes.get(i), output.get(i)[0], output.get(i)[1]));
		}
		System.out.println("Input size: " + input.size());
		return querry;
	}
	
	public Tuple<Double,Double> findNextCluster(Tuple<Double,Double> pos, HashMap<Tuple<Double,Double>,DatabaseLocation> lookup )
	{
		Tuple<Double,Double> temp = pos;
		while(lookup.containsKey(temp))
		{
			DatabaseLocation td = lookup.get(temp);
			temp = new Tuple<Double,Double>(td.getNLat(),td.getNLon());
		}
		
		return temp;
	}
	
	int amountofClusts = -1;
	public ArrayList<ArrayList<DatabaseLocation>> importClustFromFile(String path)
	{
		ArrayList<ArrayList<DatabaseLocation>> output = new ArrayList<ArrayList<DatabaseLocation>>(); 
		HashMap<Tuple<Double,Double>, Integer> clusterMap = new HashMap<Tuple<Double,Double>, Integer>();
		
		try{
			Stream<String> lines = Files.lines(Paths.get(path));
			lines.forEach(ss ->{ 
				String[] s = ss.split(" "); 
				if(s.length == 3)
				{
					int c = Integer.parseInt(s[0])+1;
					clusterMap.put(new Tuple<Double,Double>(Double.parseDouble(s[1]),Double.parseDouble(s[2])), c);
					if(c > amountofClusts)
					{
						amountofClusts = c; 
					}
				}
			});
		}
		catch(Exception e) {}
		for(int i = 0 ; i <= amountofClusts; i++)
		{
			output.add(new ArrayList<DatabaseLocation>());
		}
		
		int show =0;
		for(DatabaseLocation dl : querry)
		{
			System.out.println(show++);
			int clustId = clusterMap.get(new Tuple<Double,Double>(dl.getLat(),dl.getLon()));
			output.get(clustId).add(dl);
		}
		
		return output;
	}
	
	public boolean emptyData()
	{
		return input.isEmpty() && output.isEmpty();
	}

	public ArrayList<DatabaseLocation> getQuerry() {
		return querry;
	}
}