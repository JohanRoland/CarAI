package mashinelearning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import interfaces.DatabaseLocation;
import serverConnection.ServerConnection;
import serverConnection.ServerConnection.DBQuerry;
import utils.Tuple;
import utils.Utils;

public class NNData
{
	// Inputs

	ArrayList<Integer> minutes;
	ArrayList<Integer> hours;
	ArrayList<Integer> days;
	ArrayList<Integer> inputClust;
	
	
	//Outputs
	ArrayList<double[]> output;
	ArrayList<Integer> outputClust;
	
	//View Datas
	HashMap<Integer, Tuple<Double,Double>> viewClustPos; 
	int nrCluster;
	int amountofClusts;
	
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
		amountofClusts = -1;
	}
	
	public int getNrCluster(){return nrCluster;}
	public ArrayList<Integer> getMinutes(){return minutes;}
	public ArrayList<Integer> getHours(){return hours;}
	public ArrayList<Integer> getDays(){return days;}
	public ArrayList<Integer> getInputClust(){return inputClust;}
	public ArrayList<double[]> getOutput(){return output;}
	public ArrayList<Integer> getOutputClust(){return outputClust;}
	public ArrayList<Tuple<Double,Double>> getMeans(){return means;}
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

	
	
	public void loadFromCSV(String path)
	{
		String line="";
	
		try (BufferedReader in  = new BufferedReader(new FileReader(path)))
		{
			
			line = in.readLine();
			String[] tempStringList = line.split(" ");
			
			if(!line.equals("empty"))
			{
				minutes= new ArrayList<Integer>();
				for(String e : tempStringList)
				{
					minutes.add(Integer.parseInt(e));
					
				}
			}
			
			
			line = in.readLine();
			tempStringList = line.split(" ");
			hours = new ArrayList<Integer>();
			
			if(!line.equals("empty"))
			{
				for(String e : tempStringList)
				{
					hours.add(Integer.parseInt(e));
				}
			}
			
			line = in.readLine();
			tempStringList = line.split(" ");
			days = new ArrayList<Integer>();
			if(!line.equals("empty"))
			{
				for(String e : tempStringList)
				{
					days.add(Integer.parseInt(e));
				}

			}
			
			
			
			
			line = in.readLine();
			tempStringList = line.split(" ");
			inputClust = new ArrayList<Integer>();
			
			if(!line.equals("empty"))
			{
				for(String e : tempStringList)
				{
					inputClust.add(Integer.parseInt(e));
					
				}
			}
			
			line = in.readLine();
			if(!line.equals("empty"))
			{
				tempStringList = line.split("|");
				output = new ArrayList<double[]>();
				for(String e : tempStringList)
				{
					String[] tempStringList2 = e.split(" ");
					double[] temp =  new double[tempStringList2.length];
					for(int i=0; i<tempStringList2.length;i++)
					{
						temp[i]=Integer.parseInt(tempStringList2[i]);
	
					}
					output.add(temp);
				}
			}
			
			line = in.readLine();
			tempStringList = line.split(" ");
			outputClust = new ArrayList<Integer>();
			
			if(!line.equals("empty"))
			{
				for(String e : tempStringList)
				{
					outputClust.add(Integer.parseInt(e));
				}

			}
			
			
			line = in.readLine();
			tempStringList = line.split(" ");

			viewClustPos = new HashMap<Integer,Tuple<Double,Double>>();

			if(!line.equals("empty"))
			{
				for(int i=0;  i<tempStringList.length;i=i+3)
				{
					viewClustPos.put(Integer.parseInt(tempStringList[i]), new Tuple<Double,Double>(Double.parseDouble(tempStringList[i+1]), Double.parseDouble(tempStringList[i+2])));
				}
			}			
			

			nrCluster = Integer.parseInt(in.readLine());
			amountofClusts = Integer.parseInt(in.readLine());
			
			line = in.readLine();
			tempStringList = line.split(" ");
			
			means = new ArrayList<Tuple<Double,Double>>();
			
			if(!line.equals("empty"))
			{
				for(int i=0; i<tempStringList.length;i=i+2)
				{
					means.add(new Tuple<Double,Double>(Double.parseDouble(tempStringList[i]),Double.parseDouble(tempStringList[i+1])));
	
				}
			}
			
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	public void saveAsCSV(String path)
	{
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),"utf-8")))
		{
			if(minutes.size()>0)
			{
				for(int e : minutes)
				{
					writer.write(e +" ");
				}
				writer.write("\n");	
			}
			else
			{
				writer.write("empty\n");
			}

			if(hours.size()>0)
			{
				for(int e : hours)
				{
					writer.write(e +" ");
				}
				writer.write("\n");
			}
			else
			{
				writer.write("empty\n");
			}
			
			if(days.size()>0)
			{
				for(int e : days)
				{
					writer.write(e +" ");
				}
				writer.write("\n");

			}
			else
			{
				writer.write("empty\n");
			}
			
			if(inputClust.size()>0)
			{
				for(int e : inputClust)
				{
					writer.write(e +" ");
				}
				writer.write("\n");

			}
			else
			{
				writer.write("empty\n");
			}
						
			if(output.size()>0)
			{
				for(double[] e : output)
				{
					for(double e1:e)
					{
						writer.write(e +" ");
					}
					writer.write("|");
				}
				writer.write("\n");
			}
			else
			{
				writer.write("empty\n");
			}
			
			if(outputClust.size()>0)
			{
				for(int e : outputClust)
				{
					writer.write(e +" ");
				}
				writer.write("\n");

			}
			else
			{
				writer.write("empty\n");
			}

			Set<Entry<Integer, Tuple<Double, Double>>> viewClustPosEntries = viewClustPos.entrySet();
			
			if(viewClustPosEntries.size()>0)
			{
				for(Entry<Integer, Tuple<Double, Double>> e : viewClustPosEntries)
				{
					writer.write(e.getKey() + " " + e.getValue().fst()+" "+ e.getValue().snd()+" ");
				}
				writer.write("\n");
				
			}
			else
			{
				writer.write("empty\n");
			}
			

			writer.write(nrCluster+"\n");
			writer.write(amountofClusts+"\n");
			
			//ArrayList<DatabaseLocation> querry;
			if(means.size()>0)
			{
				for(Tuple<Double, Double> e : means)
				{
					writer.write(e.fst() + " " + e.snd() + " ");
				}
				writer.write("\n");	
			}
			else
			{
				writer.write("empty\n");
			}
						
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	
	public HashMap<Integer, Tuple<Double,Double>> getViewClustPos()
	{
		return viewClustPos;
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
	
	public int importFromDB(int id,int n)
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
		System.out.println("Finished downloading data, " + querry.size() +" entires was added");
		return querry.size();
	}

	public void parsGeoEntry(String path)
	{
		querry = new ArrayList<DatabaseLocation>();
		try {
			Files.walk(Paths.get(path)).
				forEach(filePath -> {
					
					if(Files.isRegularFile(filePath) && !filePath.endsWith("labels"))
					{
						ArrayList<String> lines;
						try {
							lines = (ArrayList<String>) Files.readAllLines(filePath);
							
							for(int i=6; i<lines.size()-1;i++)
							{
								String[] params = lines.get(i).split(",");
								
								if(params.length>6)
								{
									String[] paramsN = lines.get(i+1).split(",");
									
									String[] date = params[5].split("-");
									String[] time = params[6].split(":");
									
									querry.add(
												new DBQuerry
												   (
														   	Double.parseDouble(params[0]),
															Double.parseDouble(params[1]),
															Integer.parseInt(date[0]),
															Integer.parseInt(date[1]),
															Integer.parseInt(date[2]),
															Integer.parseInt(time[0]),
															Integer.parseInt(time[1]),
															Double.parseDouble(paramsN[0]),
															Double.parseDouble(paramsN[1])
												   )
											);
								}
								else
								{
									System.out.println("Error when trying to read the line: \n"+ lines.get(i));
								}
							}
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				}
				
			);
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
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
		System.out.println("Reading KML File");
		try{
			querry = new ArrayList<DatabaseLocation>();
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
					String[] splitDate = fullDateTime[0].split("-");
					int y = Integer.parseInt(splitDate[0]);
					int m = Integer.parseInt(splitDate[1]);
					int d = Integer.parseInt(splitDate[2]);
					
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
					querry.add(new DBQuerry(tmp[0], tmp[1],y,m,d, h,min, tmp2[0],tmp2[1]));
				}
			}
			System.out.println("Done fetching data");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void parseKMLString(int amount)
	{
		System.out.println("Reading KML File");
		try{
			querry = new ArrayList<DatabaseLocation>();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			//InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
			
			
			Document doc  = dBuilder.parse(System.in);

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
					String[] splitDate = fullDateTime[0].split("-");
					int y = Integer.parseInt(splitDate[0]);
					int m = Integer.parseInt(splitDate[1]);
					int d = Integer.parseInt(splitDate[2]);
					
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
					querry.add(new DBQuerry(tmp[0], tmp[1],y,m,d, h,min, tmp2[0],tmp2[1]));
				}
			}
			System.out.println("Done fetching data");
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
	
	public void parseTestCSV(String path)
	{
		querry = new ArrayList<DatabaseLocation>();
		try {
			Calendar cal = Calendar.getInstance();
			Stream<String> lines = Files.lines(Paths.get(path));
			lines.forEach(ss ->{ 
				String[] s = ss.split(" "); 
				
				int year = Integer.parseInt(s[0]);
				int dayOfYear = Integer.parseInt(s[1]);
				int startClust = Integer.parseInt(s[2]);
				int endClust = Integer.parseInt(s[4]);
				int minuteOfDay = Integer.parseInt(s[3]);
				
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.DAY_OF_YEAR, dayOfYear);
				
				
				querry.add(new DBQuerry(minuteOfDay, minuteOfDay, minuteOfDay, minuteOfDay, minuteOfDay, minuteOfDay));				
				}
			);			
			
			
			
			
		} catch (IOException e) {

			e.printStackTrace();
		}		
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
			if(clusterMap.containsKey(new Tuple<Double,Double>(dl.getLat(),dl.getLon())))
			{
				int clustId = clusterMap.get(new Tuple<Double,Double>(dl.getLat(),dl.getLon()));//39.984441 116.321692
				output.get(clustId).add(dl);
			}
		}		
		
		ArrayList<DatabaseLocation> empty = new ArrayList<DatabaseLocation>();
		empty.clear();
		ArrayList<ArrayList<DatabaseLocation>> emptyList = new ArrayList<ArrayList<DatabaseLocation>>();
		emptyList.add(empty);
		output.removeAll(emptyList);
		return output;	
	}

	/**
	 * Imports clusters formated as Elki does and updates
	 * hours, minutes, days, outputClust, inputClust accordingly 
	 * Lastly the list of list of DBLocations is returned
	 * @return 
	 */
	public ArrayList<ArrayList<DatabaseLocation>> impElkAndReroutFromNoise(String path)
	{
		ArrayList<ArrayList<DatabaseLocation>> possitionsByClusters = importFromElkiClustering(path);
		ArrayList<ArrayList<DatabaseLocation>> output = new ArrayList<ArrayList<DatabaseLocation>>();
		
		HashMap<Tuple<Double,Double>,Tuple<Double,Double>> coordsToMean = new HashMap<Tuple<Double,Double>,Tuple<Double,Double>>();
		HashMap<Tuple<Double,Double>,Integer> clust = new HashMap<Tuple<Double,Double>,Integer>();
		HashMap<Tuple<Double,Double>,DatabaseLocation> posToOutliers = new HashMap<Tuple<Double,Double>,DatabaseLocation>();
		nrCluster = possitionsByClusters.size();

		hours = new ArrayList<Integer>();
		minutes = new ArrayList<Integer>();
		days = new ArrayList<Integer>();
		outputClust = new ArrayList<Integer>();
		inputClust = new ArrayList<Integer>();

		System.out.println("Done Getting Cluster");

		for(DatabaseLocation dbl : possitionsByClusters.get(0))
		{
			Tuple<Double,Double> d = new Tuple<Double,Double>(dbl.getLat(),dbl.getLon());
			coordsToMean.put(d, d);
			clust.put(d, 0);
			posToOutliers.put(d, dbl);
		}
		for(int i = 1; i < possitionsByClusters.size();i++)
		{	
			Tuple<Double,Double> mean = Utils.mean(possitionsByClusters.get(i));
			if(mean.fst().isNaN())
			{	
				int j=0;
				j++;
			
			}
			means.add(mean);
			viewClustPos.put(i, mean);
			for(DatabaseLocation dbl : possitionsByClusters.get(i))
			{
				Tuple<Double,Double> coord = new Tuple<Double,Double>(dbl.getLat(),dbl.getLon());
				coordsToMean.put(coord,mean);
				
			}
			clust.put(mean, i);
		}
		
		System.out.println("Done first Data Iteration");
		
		
		for(int i = 1; i < possitionsByClusters.size(); i++)
		{
			for(int j = 0; j < possitionsByClusters.get(i).size(); j++)
			{
				double[] pos = {possitionsByClusters.get(i).get(j).getLat(),possitionsByClusters.get(i).get(j).getLon()};
				double[] dest = {possitionsByClusters.get(i).get(j).getNLat(),possitionsByClusters.get(i).get(j).getNLon()};
				Tuple<Double,Double> destination = findNextCluster( new Tuple<Double,Double>(possitionsByClusters.get(i).get(j).getNLat(),possitionsByClusters.get(i).get(j).getNLon()),posToOutliers);
				int nextCluster = getClosestCluster(destination);
				
				Tuple<Double,Double> meanDst = viewClustPos.get(nextCluster);// coordsToMean.get(dst);
				
				if(meanDst == null)
				{
					System.out.println("mean was null for" + destination);
				}
				
				if(clust.get(meanDst) != i)
				{
					Tuple<Double,Double> coord = new Tuple<Double,Double>(possitionsByClusters.get(i).get(j).getLat(),possitionsByClusters.get(i).get(j).getLon());
					pos[0] = coordsToMean.get(coord).fst();
					pos[1] = coordsToMean.get(coord).snd();
					
					
					inputClust.add(i);
					
					dest[0] = meanDst.fst();
					dest[1] = meanDst.snd();
					
					
					hours.add(possitionsByClusters.get(i).get(j).getHTime());
					minutes.add(possitionsByClusters.get(i).get(j).getMTime());
					days.add(possitionsByClusters.get(i).get(j).getDayOfWeek());
					outputClust.add(clust.get(meanDst));
				}
				else
				{
					//System.out.println("Path has been removed as it connected to itself: " + i);
				}
				
			}
		}
		System.out.println("Done Formatting datastructure");
		return possitionsByClusters;
	}
	
	/**
	 * 	Runs DBSCAN on the imported data 
	 * 
	 * @param n Amounts of datapoints to be sampled
	 */
	public void exportAsClustToCSV()
	{
		File f = new File(".");
		String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
		impElkAndReroutFromNoise(pathToProj+"\\ELKIClusters\\");
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("coords.csv"),"utf-8")))
		{
			for(int i = 0; i < inputClust.size();i++)
			{
				writer.write(inputClust.get(i) + " "+ days.get(i) + " " + (hours.get(i) * 60 + minutes.get(i)) + " " 
					+ outputClust.get(i) + "\n");
			}
		}catch(Exception e)
		{
			System.out.println("Error on creating csv file");
			e.printStackTrace();
		}
		
	}
	public void exportAsClustToCSVWithHyperTwo(String tempFileName)
	{
		
		File f = new File(".");
		String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
		impElkAndReroutFromNoise(pathToProj+"\\ELKIClusters\\");
		
		int tempFirstInputClust=0, tempSecondInputClust=0, tempFirstOutputClust=0, tempSecondOutputClust=0;
		
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFileName),"utf-8")))
		{
			for(int i = 0; i < inputClust.size();i++)
			{
				if(days.get(i)==4)
				{
					int j = 0;
					j++;
				}
				
				if(inputClust.get(i)!=0 && outputClust.get(i)!=0)
				{
					tempSecondInputClust  = inputClust.get(i);
					tempSecondOutputClust = outputClust.get(i);
					if(tempSecondInputClust==tempFirstInputClust && tempFirstInputClust!=0)
					{
						writer.write(tempFirstOutputClust + " " + tempSecondInputClust +" "+ days.get(i) +" " + (hours.get(i) * 60 + minutes.get(i)) + " " 
								+ tempSecondOutputClust + "\n");
					}
					else
					{
						writer.write(tempSecondInputClust + " " + tempSecondInputClust +" "+ days.get(i) + " " + (hours.get(i) * 60 + minutes.get(i)) + " " 
								+ tempSecondOutputClust + "\n");
					}
					tempFirstInputClust=tempSecondInputClust;
					tempFirstOutputClust=tempSecondOutputClust;
				}
			}
		}catch(Exception e)
		{
			System.out.println("Error on creating csv file");
			e.printStackTrace();
		}
		
	}
	public void exportAsCoordsToCSV(String fileName)
	{
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"utf-8")))
		{
			for(int i = 0; i < querry.size();i++)
			{
				writer.write(querry.get(i).getLat()+ " " + querry.get(i).getLon()
							+" " + querry.get(i).getDayOfWeek() +  " " + (querry.get(i).getHTime()*60+querry.get(i).getMTime()) + " " 
						+ querry.get(i).getNLat() + " " + querry.get(i).getNLon() + "\n");
				
			}
			writer.close();
		}catch(Exception e)
		{
			System.out.println("Error on creating csv file");
			e.printStackTrace();
		}
	}
	public void exportAsCoordsWithDateToCSV()
	{
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("coords.csv"),"utf-8")))
		{
			for(int i = 0; i < querry.size();i++)
			{
				writer.write(querry.get(i).getLat()+ " " + querry.get(i).getLon() + " " + querry.get(i).getDayOfWeek() +" " + (querry.get(i).getHTime()*60+querry.get(i).getMTime()) + " " 
						+ querry.get(i).getNLat() + " " + querry.get(i).getNLon() + "\n");

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
		int lastY=querry.get(0).getYear();
		int lastMo=querry.get(0).getMonth();
		int lastD=querry.get(0).getDay();
		int lastValidPoint=0;
		
		for(int i =1 ; i<querry.size()-1;i++)
		{
			
				if(querry.get(i).getLat()>lat && querry.get(i).getLat()<latPrime && querry.get(i).getLon()>lon && querry.get(i).getLon()<lonPrime)
				{
					temp.add(new DBQuerry(querry.get(lastValidPoint).getLat(),querry.get(lastValidPoint).getLon(),lastY,lastMo,lastD,lastH,lastM,querry.get(i).getLat(),querry.get(i).getLon()));
					lastH = querry.get(i).getHTime();
					lastM = querry.get(i).getMTime();
					lastY=querry.get(i).getYear();
					lastMo=querry.get(i).getMonth();
					lastD=querry.get(i).getDay();
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
		int lastY=querry.get(0).getYear();
		int lastMo=querry.get(0).getMonth();
		int lastD=querry.get(0).getDay();
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
							temp.add(new DBQuerry(querry.get(lastValidPoint).getLat(),querry.get(lastValidPoint).getLon(),lastY,lastMo,lastD,lastH,lastM,querry.get(i).getLat(),querry.get(i).getLon()));
							lastH = querry.get(i).getHTime();
							lastM = querry.get(i).getMTime();
							lastY=querry.get(i).getYear();
							lastMo=querry.get(i).getMonth();
							lastD=querry.get(i).getDay();
							lastValidPoint=i;						
						}
						else
						{
							lastH = querry.get(i).getHTime();
							lastM = querry.get(i).getMTime();
							lastY=querry.get(i).getYear();
							lastMo=querry.get(i).getMonth();
							lastD=querry.get(i).getDay();
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
		System.out.println("Has " + querry.size() + " befor cull");
		ArrayList<DatabaseLocation> temp = new ArrayList<DatabaseLocation>();

 		ArrayList<Tuple<Double,Double>> median =  new ArrayList<Tuple<Double,Double>>();
		

		boolean first = true;
		boolean newTravel = false;
		
		int counter = 0;
		int tCounter = 0;
		
		
		Tuple<Double,Double> start = new Tuple<Double,Double>(0.0,0.0);
		Tuple<Double,Double> stop = new Tuple<Double,Double>(0.0,0.0);
		Tuple<Integer,Integer> tempTime = new Tuple<Integer,Integer>(0,0);
		int day =0;
		int month =0;
		int year =0;
		
		double traveledDist = 0.0; 
		for(int i = 0; i < querry.size(); i++)
		{
			if(Utils.distDB(querry.get(i)) < 25)
			{
				//traveledDist += Utils.distDB(querry.get(i));

				
				median.add(new Tuple<Double,Double>(querry.get(i).getLat(),querry.get(i).getLon()));

				tempTime.setFst(querry.get(i).getHTime());
				tempTime.setSnd(querry.get(i).getMTime());
				day = querry.get(i).getDay();
				month = querry.get(i).getMonth();
				year = querry.get(i).getYear();
				
				
				counter++; 				

			}
			else
			{
				//traveledDist += Utils.distDB(querry.get(i));
				
				if(counter > 10)
				{	
					double p1 = 0.0;
					double p2 = 0.0;
					
					for(Tuple<Double,Double> d: median)
					{
						p1 += d.fst();
						p2 += d.snd();
					}
					
					if(first)
					{
						start.setFst(Math.floor((p1/median.size())*1000000)/1000000);
						start.setSnd(Math.floor((p2/median.size())*1000000)/1000000);
						
					}
					else
					{
						stop.setFst(Math.floor((p1/median.size())*1000000)/1000000);
						stop.setSnd(Math.floor((p2/median.size())*1000000)/1000000);
						
					}
					newTravel = true;
				}
				else
				{
					newTravel = false;
				}
				median.clear();	
			
				//tCounter++;
				counter = 0; 
			}
			
			if(newTravel)
			{
				
				//temp.add(new DBQuerry(median.get(median.size()/2).fst(),median.get(median.size()/2).snd(),year,month,day,tempTime.fst(),tempTime.snd(),stop.fst(),stop.snd()));
				if(!first)
				{
					if(Utils.distFrom(start.fst(),start.snd(), stop.fst(),stop.snd()) > 100)
					{
						temp.add(new DBQuerry(start.fst(),start.snd(),year,month,day,tempTime.fst(),tempTime.snd(),stop.fst(),stop.snd()));
					}
					
					start.setFst(stop.fst());
					start.setSnd(stop.snd());
				}
				
				first = false;
				newTravel = false;
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
		ServerConnection sc =  ServerConnection.getInstance();		
		try {
			
			System.out.println("Done formatting QuerryArrayList " + querry.size());
			//DBQuerry[] sendDB = querry.toArray(new DBQuerry[querry.size()]);
			sc.replacePosData(id, querry );
			//sc.addPosData(0, input.get(i)[0], input.get(i)[1], input.get(i)[2], output.get(i)[0], output.get(i)[1]);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Finds the next none out-lier, put in a another way finds the next cluster.
	 * Takes a position and returns the next cluster given a map between positions and corresponding path (DatabaseLocation)
	 * @param pos The start position  
	 * @param lookup The table containing start positions to idler paths
	 * @return 
	 */
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