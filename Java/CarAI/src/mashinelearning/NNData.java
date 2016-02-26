package mashinelearning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
	
	public void importFromDB(int id)
	{
		ServerConnection b = ServerConnection.getInstance();
		//b= new ServerConnection();
		try {
			querry = b.getPosClass(id,1000);
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
	
	public void exportAsClustToCSV(int n)
	{
		//tree = new DBSCAN(querry, true);	
		
		//int temp = tree.cluster(0.01, 2);
		querry =  importFromFile();
		exportAsCoordsToCSV();
		PYDBSCAN py = new PYDBSCAN();
		
		ArrayList<ArrayList<DatabaseLocation>> temp2 = py.runDBSCAN(querry, 0.002, 10, n); //tree.getClusterd(true);
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
					Tuple<Double,Double> d = new Tuple<Double,Double>(dbl.getLon(),dbl.getLat());
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
					Tuple<Double,Double> coord = new Tuple<Double,Double>(dbl.getLon(),dbl.getLat());
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
				double[] pos = {temp2.get(i).get(j).getLon(),temp2.get(i).get(j).getLat()};
				double[] dest = {temp2.get(i).get(j).getNLon(),temp2.get(i).get(j).getNLat()};
				Tuple<Double,Double> dst = findNextCluster( new Tuple<Double,Double>(temp2.get(i).get(j).getNLon(),temp2.get(i).get(j).getNLat()),posToLoc);
				
				Tuple<Double,Double> meanDst = hs.get(dst); 
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
					Tuple<Double,Double> coord = new Tuple<Double,Double>(temp2.get(i).get(j).getLon(),temp2.get(i).get(j).getLat());
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
					writer.write(inputClust.get(i) + " " + hours.get(i) + " " + Math.floorMod((minutes.get(i)), 60) + " " 
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
				writer.write(querry.get(i).getLon()+ " " + querry.get(i).getLat() + " " + querry.get(i).getHTime() + " "+querry.get(i).getMTime() + " " 
						+ querry.get(i).getNLon() + " " + querry.get(i).getNLat()+ "\n");
				
			}
			writer.close();
		}catch(Exception e)
		{
			System.out.println("Error on creating csv file");
			e.printStackTrace();
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
			temp = new Tuple<Double,Double>(td.getNLon(),td.getNLat());
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
			int clustId = clusterMap.get(new Tuple<Double,Double>(dl.getLon(),dl.getLat()));
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