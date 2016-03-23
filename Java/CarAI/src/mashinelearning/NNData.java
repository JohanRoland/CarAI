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
import java.util.List;
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

	double[] normFromQToIn()
	{
		ArrayList<double[]> tempInput = new ArrayList<double[]>();
		ArrayList<double[]> tempOut = new ArrayList<double[]>();

		double[] tempOutS = {0,0};
		double[] tempInputS={0,0,0};
		
		
		for(DatabaseLocation e : querry)
		{
			tempInputS[0]=+e.getLon();
			tempInputS[1]=+e.getLat();
			tempInputS[2]=+e.getHTime()*60+e.getMTime();
			
			tempOutS[0]=+e.getNLon();
			tempOutS[1]=+e.getNLat();
			
		}
		tempInputS[0]=tempInputS[0]/querry.size();
		tempInputS[1]=tempInputS[2]/querry.size();
		tempInputS[2]=tempInputS[3]/querry.size();
		
		tempOutS[0]=tempOutS[0]/querry.size();
		tempOutS[1]=tempOutS[1]/querry.size();
		
		for(int i=0;i<querry.size();i++)
		{
			tempInput.get(i)[0]=querry.get(i).getLon()/tempInputS[0];
			tempInput.get(i)[1]=querry.get(i).getLat()/tempInputS[1];
			tempInput.get(i)[2]=(querry.get(i).getHTime()*60+querry.get(i).getMTime())/tempInputS[2];
			
			tempOut.get(i)[0]=querry.get(i).getNLon()/tempOutS[0];
			tempOut.get(i)[1]=querry.get(i).getNLat()/tempOutS[1];
		}
		double[] out = {tempInputS[0],tempInputS[1],tempInputS[3],tempOutS[0],tempOutS[1]};
		return out;
	}
	
	public HashMap<Integer, Tuple<Double,Double>> getViewClustPos()
	{
		return viewClustPos;
	}
	/*
	public double[][] getInputData()
	{
		double[][] ret = new double[input.size()][]; 
		for(int i = 0; i < input.size(); i++)
		{
			ret[i] = input.get(i);
		}
		return ret;
	}
	*/
	
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
					double[] tmp2= new double[2];
					if(i+1 <nList.getLength())
					{
						Node oNode = nList.item(i+1);
						if (oNode.getNodeType() == Node.ELEMENT_NODE) {
							Element oElement = (Element) oNode;
							tmp2[0] = Double.parseDouble(oElement.getAttribute("lon"));
							tmp2[0]	= Double.parseDouble(oElement.getAttribute("lat"));
						}
					}
					else
					{
						tmp2[0] = lon;
						tmp2[1] = lat;

					}
					querry.add(new DBQuerry(tmp[0], tmp[1], min, h, tmp2[0],tmp2[1]));
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
					double[] tmp2=new double[2];
					if(i != 0)
					{
						Node oNode = nList.item(i-1);
						if (oNode.getNodeType() == Node.ELEMENT_NODE) {
							Element oElement = (Element) oNode;
							String[] nCoordinates = oElement.getTextContent().split(" ");
							double lat2 = ((double)Math.round(Double.parseDouble(nCoordinates[1])*10000000))/10000000;
						 	double lon2 = ((double)Math.round(Double.parseDouble(nCoordinates[0])*10000000))/10000000;
							tmp2[0]	=lat2;
							tmp2[1] = lon2;

						}
					}
					else
					{
						tmp2[0]=lon;
						tmp2[1]= lat;
						
					}
					querry.add(new DBQuerry(tmp[0], tmp[1], min, h, tmp2[0],tmp2[1]));
				}
			}
			System.out.println("Done fetching data");
		}
		catch(Exception e)
		{
			
		}
	}

	public void parseCSV(String path)
	{
		querry = new ArrayList<DatabaseLocation>();
		try {
			Stream<String> lines = Files.lines(Paths.get(path));
			lines.forEach(ss ->{ 
				String[] s = ss.split(" "); 
				double lat =  Double.parseDouble(s[0]);//((double)Math.round(Double.parseDouble(s[0])*10000000))/10000000;
			 	double lon =  Double.parseDouble(s[1]);//((double)Math.round(Double.parseDouble(s[1])*10000000))/10000000;
				double nlat = Double.parseDouble(s[3]);//((double)Math.round(Double.parseDouble(s[3])*10000000))/10000000;
			 	double nlon = Double.parseDouble(s[4]);//((double)Math.round(Double.parseDouble(s[4])*10000000))/10000000;
				querry.add(new DBQuerry(lat,lon,((int)Double.parseDouble(s[2]))/60,((int)(Double.parseDouble(s[2])))%60,nlat,nlon));				
				}
			);			
			
			
			
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}
	
	/*
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
		
	}*/
	
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
		
		File f = new File(".");
		String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
    	
		
		ArrayList<ArrayList<DatabaseLocation>> temp2 = importFromElkiClustering(pathToProj+"\\ELKIClusters\\"); //py.runDBSCAN(querry, 0.001, 20, n); //tree.getClusterd(true);
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
					
					
					inputClust.add(i);
					
					dest[0] = meanDst.fst();
					dest[1] = meanDst.snd();
					
					
					hours.add(temp2.get(i).get(j).getHTime());
					minutes.add(temp2.get(i).get(j).getMTime());
					
					querry.add(new DBQuerry(pos[0], pos[1], temp2.get(i).get(j).getHTime(), temp2.get(i).get(j).getMTime(), dest[0], dest[1]));
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
						+ (querry.get(i).getNLat()-querry.get(i).getLat()) + " " + (querry.get(i).getNLon() - querry.get(i).getLon())+ "\n");
				
			}
			writer.close();
		}catch(Exception e)
		{
			System.out.println("Error on creating csv file");
			e.printStackTrace();
		}
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
					temp.add(new DBQuerry(querry.get(lastValidPoint).getLat(),querry.get(lastValidPoint).getLon(),lastH,lastM,querry.get(i).getLat(),querry.get(i).getLon()));
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
	public void coordCullBySpeed(double threshold)
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
						if(1000.0<Math.abs(Utils.distFrom(querry.get(lastValidPoint).getLat(),querry.get(lastValidPoint).getLon(), querry.get(i).getLat(),querry.get(i).getLon())))
						{
							temp.add(new DBQuerry(querry.get(lastValidPoint).getLat(),querry.get(lastValidPoint).getLon(),lastH,lastM,querry.get(i).getLat(),querry.get(i).getLon()));
							lastH = querry.get(i).getHTime();
							lastM = querry.get(i).getMTime();
							lastValidPoint=i;						}
						else
						{
							lastH = querry.get(i).getHTime();
							lastM = querry.get(i).getMTime();
							lastValidPoint=i;
							//System.out.println("Soo small!!!!");
						}
					}
					else
					{
						counterOfRemovedCoords++;
					}
					takingAbrake=0;

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

 		ArrayList<Tuple<Double,Double>> median =  new ArrayList<Tuple<Double,Double>>();
		
		boolean traveling = false;
		
		int counter = 0;
		int tCounter = 0;
		
		
		Tuple<Double,Double> start = new Tuple<Double,Double>(0.0,0.0);
		Tuple<Double,Double> stop = new Tuple<Double,Double>(0.0,0.0);
		Tuple<Integer,Integer> tempTime = new Tuple<Integer,Integer>(0,0);
		
		
		double traveledDist = 0.0; 
		for(int i = 0; i < querry.size(); i++)
		{
			if(Utils.distDB(querry.get(i)) < 10)
			{
				//traveledDist += Utils.distDB(querry.get(i));
				counter++; 
					
				traveledDist = 0.0; 
				

				median.add(new Tuple<Double,Double>(querry.get(i).getLat(),querry.get(i).getLon()));
				//start.setFst(querry.get(i).getLat());
				//start.setSnd(querry.get(i).getLon());
				tempTime.setFst(querry.get(i).getHTime());
				tempTime.setSnd(querry.get(i).getMTime());
				//tCounter = 0;
				traveling = false;
			}
			else
			{
				//traveledDist += Utils.distDB(querry.get(i));
				if(counter > 15)
				{
					stop.setFst(querry.get(i).getNLat());
					stop.setSnd(querry.get(i).getNLon());
					
					/*double p1 = 0.0;
					double p2 = 0.0;
					
					for(Tuple<Double,Double> d: median)
					{
						p1 += d.fst();
						p2 += d.snd();
					}*/
					
					temp.add(new DBQuerry(median.get(median.size()/2).fst(),median.get(median.size()/2).snd(),tempTime.fst(),tempTime.snd(),stop.fst(),stop.snd()));
					//temp.add(new DBQuerry(p1/median.size(),p2/median.size(),tempTime.fst(),tempTime.snd(),stop.fst(),stop.snd()));
				}
				median.clear();
				traveling = true;
				//tCounter++;
				counter = 0; 
			}
			
			
		}
		System.out.println("Removed coords: " +(querry.size()- temp.size()));
		querry = temp;
	}
	
	public ArrayList<DatabaseLocation> RDPALG(List<DatabaseLocation> pointList,double eps)
	{
		double dmax = 0;
		int index = 0; 
		
		int end = pointList.size()-1;
		
		for(int i = 1; i < end-1; i++)
		{
			double d = Utils.perpendicularDistance(pointList.get(i), pointList.get(0), pointList.get(end));
			if(d > dmax)
			{
				index = i;
				dmax = d;
			}
		}
		ArrayList<DatabaseLocation> Res = new ArrayList<DatabaseLocation>();
		
		if(dmax > eps)
		{
			ArrayList<DatabaseLocation> Res1 = (ArrayList<DatabaseLocation>)RDPALG(pointList.subList(0, index),eps);
			ArrayList<DatabaseLocation> Res2 = (ArrayList<DatabaseLocation>)RDPALG(pointList.subList(index, end),eps);
		
			Res.addAll(Res1);
			Res.addAll(Res2);
			
		}
		else
		{
			Res.add(pointList.get(0));
			Res.add(pointList.get(end));
		}
		
		return Res;
	}
	
	public void repoint()
	{
		for(int i = 0; i < querry.size()-1; i++)
		{
			querry.get(i).setNPos(querry.get(i+1).getLat(), querry.get(i+1).getLon());
		}
	}
	
	public void cullByRDP()
	{
		ArrayList<DatabaseLocation> temp  = RDPALG(querry,0.00001);
		System.out.println("After RDP: " + temp.size());
		
		querry = temp;
		//repoint(); 
	}
	
	public void exportToDB(int id)
	{
		ServerConnection sc =  ServerConnection.getInstance(); //new ServerConnection();
		ArrayList<DBQuerry> querry = new ArrayList<DBQuerry>();
		
		try {
			
			System.out.println("Done formatting QuerryArrayList " + querry.size());
			DBQuerry[] sendDB = querry.toArray(new DBQuerry[querry.size()]);
			sc.replacePosData(id, sendDB );
			//sc.addPosData(0, input.get(i)[0], input.get(i)[1], input.get(i)[2], output.get(i)[0], output.get(i)[1]);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
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
	

	public ArrayList<DatabaseLocation> getQuerry() {
		return querry;
	}
}