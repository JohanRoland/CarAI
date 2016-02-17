package result;

import interfaces.DatabaseLocation;
import serverConnection.DBSCAN;
import serverConnection.ServerConnection;
import serverConnection.ServerConnection.DBQuerry;
import utils.*;

import java.sql.SQLException;
import java.sql.Time;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.logging.EncogFormatter;
import org.encog.util.obj.SerializeObject;
import org.encog.util.simple.EncogUtility;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mysql.jdbc.Statement;

import car.Car;




public class LocPrediction {

	/*
	 * Input:
	 * 		Position  (Double, Double)
	 * 		Hours 	  (Descrete)
	 * 		Minutes   (Descrete)
	 * 		Weekday?  (Int)
	 * 		Monthday? (Int)
	 * 		Month?     (Int)
	 * 
	 * 
	 * Output:
	 * 		Position (Double, Double)
	 */
	
	
	static private Map<Integer,LocPrediction> instanceMap; 
	
	
	CSVFormat format;
	VersatileMLDataSet data;
	NormalizationHelper helper;
	MLRegression bestMethod;
	NNData nd;
	
	int nrCols = 5;
	public double sampleIn[][] = {{0.0,0.0},{1.0,0.0},{0.0,1.0},{1.0,1.0}};
	public double sampleOut[][] = {{0.0},{1.0},{1.0},{0.0}};
	
	public Tuple<Double,Double> predictedLoc;
	private MqttTime mqttTime;
	private LocPrediction()
	{
		format = new CSVFormat('.',' ');
		
		NNData nd = new NNData();
		//nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		//nd.exportToDB();
		nd.importFromDB(1);
		
		nd.exportAsCoordsToCSV();
		
		String[] descreteMTime = numArray(60);
		String[] descreteHTime = numArray(24);
		
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		data =  new VersatileMLDataSet(source);

		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
		ColumnDefinition columnHTime = data.defineSourceColumn("hours",2,ColumnType.ordinal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",3,ColumnType.ordinal);
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",4,ColumnType.continuous);		
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",5,ColumnType.continuous);	
		
		columnMTime.defineClass(descreteMTime);
		columnHTime.defineClass(descreteHTime);
		data.analyze();
		
		data.defineInput(columnInLon);
		data.defineInput(columnInLat);
		data.defineInput(columnHTime);
		data.defineInput(columnMTime);
		data.defineOutput(columnOutLon);
		data.defineOutput(columnOutLat);
		data.getNormHelper().defineUnknownValue("?");
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();
		
		model.holdBackValidation(0.3, false, 1001);
		model.selectTrainingType(data);
		MLRegression bestMethod = (MLRegression)model.crossvalidate(5, false);
		
		
		System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
		NormalizationHelper helper = data.getNormHelper();
		System.out.println(helper.toString());
		System.out.println("Final model: " + bestMethod);
		
		ReadCSV csv = new ReadCSV(new File("coords.csv"),false,format);
		String[] line = new String[4];
		MLData input = helper.allocateInputVector();
		
		while(csv.next())
		{
			StringBuilder result = new StringBuilder();
			for(int i = 0; i < 4; i++)
				line[i] = csv.get(i);
			
			helper.normalizeInputVector(line,input.getData(),false);
			MLData output = bestMethod.compute(input);
			String irisChoosen0 = helper.denormalizeOutputVectorToString(output)[0];
			String irisChoosen1 = helper.denormalizeOutputVectorToString(output)[1];
			result.append("[" + line[0]+ ", "+ line[1]+ " " + line[2]+ ", " + line[3]+ "] ");
			result.append(" -> predicted: ");
			result.append(irisChoosen0 + " , " +irisChoosen1);
			result.append(" (correct: ");
			result.append(csv.get(4)+ " " +csv.get(5)); 
			result.append(") Lat Err: " +  dispError(irisChoosen0,csv.get(4)) + " Lon Err: " + dispError(irisChoosen1,csv.get(5)));
			System.out.println(result.toString());
		}
		
		Encog.getInstance().shutdown();
	}
	
	private LocPrediction(int id)
	{
		mqttTime = MqttTime.getInstance();
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		nd = new NNData();
		//nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		//nd.exportToDB();
		
		nd.importFromDB(id);

		if(!nd.emptyData())
		{
			//nd.exportToCSV();
				
			String[] descreteMTime = numArray(60);
			String[] descreteHTime = numArray(24);
	 		String[] descreteClust = numArray(nd.nrCluster);
			
			VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
			data =  new VersatileMLDataSet(source);
	
			data.getNormHelper().setFormat(format); 
	
			ColumnDefinition columnInClust = data.defineSourceColumn("pos",0,ColumnType.nominal);		
			ColumnDefinition columnHTime = data.defineSourceColumn("hours",1,ColumnType.ordinal);
			ColumnDefinition columnMTime = data.defineSourceColumn("minutes",2,ColumnType.ordinal);
			ColumnDefinition columnOutClust = data.defineSourceColumn("opos",3,ColumnType.nominal);
			
			columnInClust.defineClass(descreteClust);
			columnMTime.defineClass(descreteMTime);
			columnHTime.defineClass(descreteHTime);
			columnOutClust.defineClass(descreteClust);
			data.getNormHelper().defineUnknownValue("?");
			data.analyze();
	
			data.defineInput(columnInClust);
			data.defineInput(columnHTime);
			data.defineInput(columnMTime);
			data.defineOutput(columnOutClust);
			
			EncogModel model = new EncogModel(data);
			model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
			
			model.setReport(new ConsoleStatusReportable());
			
			data.normalize();
			
			model.holdBackValidation(0.3, false, 1001);
			model.selectTrainingType(data);
			bestMethod = (MLRegression)model.crossvalidate(5, false);
			
			
			System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
			System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
			helper = data.getNormHelper();
			System.out.println(helper.toString());
			System.out.println("Final model: " + bestMethod);
			
		}
		
		//Encog.getInstance().shutdown();
	}
	
	static public LocPrediction getInstance(int userID)
	{
		if(instanceMap == null)
		{
			instanceMap = new HashMap<Integer,LocPrediction>();
		}
		if(!instanceMap.containsKey(userID))
		{
			instanceMap.put(userID, new LocPrediction());
		}
		
		return instanceMap.get(userID);
	}
	
	
	private String dispError(String x, String y)
	{
		
		return new Double(Double.parseDouble(y)-Double.parseDouble(x)).toString(); 
	}
	
	private String[] numArray(int t)
	{
		String[] out = new String[t];
		
		for(int i = 0; i < t; i++ )
			out[i] = "" +i;
		
		return out;
				
	}
	
	public Tuple<Double,Double> predict()
	{
		ReadCSV csv = new ReadCSV(new File("coords.csv"),false,format);
		String[] line = new String[4];
		MLData input = helper.allocateInputVector();
		
		//Calendar c = Calendar.getInstance();
		int hour = mqttTime.getHour();// c.get(Calendar.HOUR_OF_DAY);
		int minute = mqttTime.getMinute(); //c.get(Calendar.MINUTE);
		
		//EncogUtility.saveEGB(new File("networkExport.eg"), data);
		//EncogUtility.explainErrorMSE(bestMethod, data);
		EncogDirectoryPersistence.saveObject(new File("networkExport.eg"), bestMethod);
		Car carData = Car.getInstance();
		
		line[0] = ""+nd.getClosestCluster(carData.getPos());
		//line[0] = ""+nd.getClosestCluster(new Tuple<Double,Double>(57.69661,11.97575));
		line[1] = ""+hour;
		line[2] = ""+minute;
		
		helper.normalizeInputVector(line,input.getData(),false);
		MLData output = bestMethod.compute(input);
		String irisChoosen0 = helper.denormalizeOutputVectorToString(output)[0];
		StringBuilder result = new StringBuilder();
		result.append("[" + line[0]+ " ( " + nd.viewClustPos.get(Integer.parseInt(line[0])) + ")"+ ", " + line[1]+ ", " + line[2]+ "] ");
		result.append(" -> predicted: ");
		result.append(irisChoosen0 + " ( " + nd.viewClustPos.get(Integer.parseInt(irisChoosen0)) + ")");
		System.out.println(result.toString());
		return nd.viewClustPos.get(Integer.parseInt(irisChoosen0));
	}
	
	private class NNData
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
		public NNData()
		{
			
			input = new ArrayList<double[]>();
			output = new ArrayList<double[]>();
			minutes = new ArrayList<Integer>();
			hours = new ArrayList<Integer>();
			inputClust = new ArrayList<Integer>();
			outputClust = new ArrayList<Integer>();
			viewClustPos = new HashMap<Integer, Tuple<Double,Double>>();
			nrCluster = 0;
		}
		
		public int getClosestCluster(Tuple<Double,Double> pos)
		{
			return tree.associateCluster(pos,0.01);
		}
		
		public void importFromDB(int id)
		{
			ServerConnection b = ServerConnection.getInstance();
			//b= new ServerConnection();
			int di =0;
			int dj =0;
			try (PrintStream out = new PrintStream(new FileOutputStream("clusterd.txt"))) 
			{
				querry = b.getPosClass(id);
				
				
				tree = new DBSCAN(querry, false);	
				int temp = tree.cluster(0.01, 2);
					
				ArrayList<DatabaseLocation>[] temp2 = tree.getClusterd(true);

				HashMap<Tuple<Double,Double>,Tuple<Double,Double>> hs = new HashMap<Tuple<Double,Double>,Tuple<Double,Double>>();
				HashMap<Tuple<Double,Double>,Integer> clust = new HashMap<Tuple<Double,Double>,Integer>();
				HashMap<Tuple<Double,Double>,DatabaseLocation> posToLoc = new HashMap<Tuple<Double,Double>,DatabaseLocation>();
				nrCluster = temp2.length;
				for(int i = 0; i < temp2.length;i++)
				{
					if(i == 0)
					{
						for(DatabaseLocation dbl : temp2[i])
						{
							Tuple<Double,Double> d = new Tuple<Double,Double>(dbl.getLon(),dbl.getLat());
							hs.put(d, d);
							clust.put(d, i);
							posToLoc.put(d, dbl);
						}
					}
					else
					{
						
						Tuple<Double,Double> mean = Utils.mean(temp2[i]);
						viewClustPos.put(i, mean);
						for(DatabaseLocation dbl : temp2[i])
						{
							Tuple<Double,Double> coord = new Tuple<Double,Double>(dbl.getLon(),dbl.getLat());
							hs.put(coord,mean);
						}
						clust.put(mean, i);
					}
					
				}
				
				for(int i = 0; i < temp2.length; i++)
				{
					for(int j = 0; j < temp2[i].size(); j++)
					{
						double[] pos = {temp2[i].get(j).getLon(),temp2[i].get(j).getLat()};
						double[] dest = {temp2[i].get(j).getNLon(),temp2[i].get(j).getNLat()};
						Tuple<Double,Double> dst = findNextCluster( new Tuple<Double,Double>(temp2[i].get(j).getNLon(),temp2[i].get(j).getNLat()),posToLoc);
												
						Tuple<Double,Double> meanDst = hs.get(dst);
						di = i;
						dj = j; 
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
							Tuple<Double,Double> coord = new Tuple<Double,Double>(temp2[i].get(j).getLon(),temp2[i].get(j).getLat());
							pos[0] = hs.get(coord).fst();
							pos[1] = hs.get(coord).snd();
							
							input.add(pos);
							
							inputClust.add(i);
							dest[0] = meanDst.fst();
							dest[1] = meanDst.snd();
							output.add(dest);
							hours.add(temp2[i].get(j).getHTime());
							minutes.add(temp2[i].get(j).getMTime());
							outputClust.add(clust.get(hs.get(dst)));
						}
						
					}
				}
			
			} 
			catch (Exception e1) {
					e1.printStackTrace();
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
		
		public void exportAsClustToCSV()
		{
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("coords.csv"),"utf-8")))
			{
				for(int i = 0; i < input.size();i++)
				{
					/*writer.write(input.get(i)[1] + " " + input.get(i)[0] + " " + hours.get(i) + " " + minutes.get(i) + " " 
							+ output.get(i)[1] + " " + output.get(i)[0] + "\n");*/
					for(int noise = -5 ; noise < 5; noise++)
						writer.write(inputClust.get(i) + " " + hours.get(i) + " " + Math.floorMod((minutes.get(i)+noise), 60) + " " 
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
					writer.write(querry.get(i).getLon()+ " " + querry.get(i).getLat() + " " + querry.get(i).getHTime() + " " + querry.get(i).getMTime() + " " 
							+ querry.get(i).getNLon() + " " + querry.get(i).getNLat()+ "\n");
					
				}
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
				temp = new Tuple<Double,Double>(td.getNLon(),td.getNLat());
			}
			
			return temp;
		}
		
		public boolean emptyData()
		{
			return input.isEmpty() && output.isEmpty();
		}
	}
}
