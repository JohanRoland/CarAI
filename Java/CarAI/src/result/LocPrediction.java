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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
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
	 * 
	 */
	
	int nrCols = 5;
	public double sampleIn[][] = {{0.0,0.0},{1.0,0.0},{0.0,1.0},{1.0,1.0}};
	public double sampleOut[][] = {{0.0},{1.0},{1.0},{0.0}};
	public LocPrediction()
	{
		CSVFormat format = new CSVFormat('.',' ');
		
		NNData nd = new NNData();
		//nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		//nd.exportToDB();
		nd.importFromDB(1);
		
		//nd.exportToCSV();
			
		String[] descreteMTime = numArray(60);
		String[] descreteHTime = numArray(24);
 		String[] descreteClust = numArray(nd.nrCluster);
		
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		VersatileMLDataSet data =  new VersatileMLDataSet(source);

		data.getNormHelper().setFormat(format); 
		/*ColumnDefinition columnInLat = data.defineSourceColumn("ilat",0,ColumnType.continuous);		
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",1,ColumnType.continuous);		
		ColumnDefinition columnHTime = data.defineSourceColumn("hours",2,ColumnType.ordinal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",3,ColumnType.ordinal);
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",4,ColumnType.continuous);		
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",5,ColumnType.continuous);	
		*/
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
		/*
		data.defineInput(columnInLat);
		data.defineInput(columnInLon);
		data.defineInput(columnHTime);
		data.defineInput(columnMTime);
		data.defineOutput(columnOutLat);
		data.defineOutput(columnOutLon);
		*/
		data.defineInput(columnInClust);
		data.defineInput(columnHTime);
		data.defineInput(columnMTime);
		data.defineOutput(columnOutClust);
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();
		
		model.holdBackValidation(0.3, true, 1001);
		model.selectTrainingType(data);
		MLRegression bestMethod = (MLRegression)model.crossvalidate(5, true);
		
		
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
			//String irisChoosen1 = helper.denormalizeOutputVectorToString(output)[1];
			result.append("[" + line[0]+ " ( " + nd.viewClustPos.get(Integer.parseInt(line[0])) + ")"+ ", " + line[1]+ ", " + line[2]+ "] ");
			result.append(" -> predicted: ");
			//result.append(irisChoosen1 + " , " +irisChoosen0);
			result.append(irisChoosen0 + " ( " + nd.viewClustPos.get(Integer.parseInt(irisChoosen0)) + ")");
			result.append(" (correct: ");
			result.append(csv.get(3)+ " ( " + nd.viewClustPos.get(Integer.parseInt(csv.get(3))) + ")"+ ") ");//; +csv.get(4)); 
			result.append("Err: " + dispError(irisChoosen0,csv.get(3)));
			//result.append(") Lat Err: " +  dispError(irisChoosen0,csv.get(4)) + " Lon Err: " + dispError(irisChoosen1,csv.get(5)));
			System.out.println(result.toString());
		}
		
		Encog.getInstance().shutdown();
	}
	
	public LocPrediction(int id)
	{
		CSVFormat format = new CSVFormat('.',' ');
		
		NNData nd = new NNData();
		//nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		//nd.exportToDB();
		nd.importFromDB(id);
		if(!nd.emptyData())
		{
			nd.exportToCSV();
				
			String[] descreteMTime = numArray(60);
			String[] descreteHTime = numArray(24);
	 		String[] descreteClust = numArray(nd.nrCluster);
			
			VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
			VersatileMLDataSet data =  new VersatileMLDataSet(source);
	
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
			
			//model.setReport(new ConsoleStatusReportable());
			
			data.normalize();
			
			model.holdBackValidation(0.3, true, 1001);
			model.selectTrainingType(data);
			MLRegression bestMethod = (MLRegression)model.crossvalidate(5, true);
			
			
			System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
			System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
			NormalizationHelper helper = data.getNormHelper();
			//System.out.println(helper.toString());
			System.out.println("Final model: " + bestMethod);
			
			ReadCSV csv = new ReadCSV(new File("coords.csv"),false,format);
			String[] line = new String[4];
			MLData input = helper.allocateInputVector();
			
			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			
			line[0] = "1";
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
			/*
			
			while(csv.next())
			{
				StringBuilder result = new StringBuilder();
				for(int i = 0; i < 4; i++)
					line[i] = csv.get(i);
				
				helper.normalizeInputVector(line,input.getData(),false);
				MLData output = bestMethod.compute(input);
				String irisChoosen0 = helper.denormalizeOutputVectorToString(output)[0];
				result.append("[" + line[0]+ " ( " + nd.viewClustPos.get(Integer.parseInt(line[0])) + ")"+ ", " + line[1]+ ", " + line[2]+ "] ");
				result.append(" -> predicted: ");
				result.append(irisChoosen0 + " ( " + nd.viewClustPos.get(Integer.parseInt(irisChoosen0)) + ")");
				result.append(" (correct: ");
				result.append(csv.get(3)+ " ( " + nd.viewClustPos.get(Integer.parseInt(csv.get(3))) + ")"+ ") ");
				result.append("Err: " + dispError(irisChoosen0,csv.get(3)));
				System.out.println(result.toString());
			}*/
		}
		Encog.getInstance().shutdown();
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
	
	public void updateDB(int id)
	{
		NNData nd = new NNData();
		nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		nd.exportToDB(id);
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
		
		public void importFromDB(int id)
		{
			ServerConnection b = ServerConnection.getInstance();
			//b= new ServerConnection();
			int di =0;
			int dj =0;
			try (PrintStream out = new PrintStream(new FileOutputStream("clusterd.txt"))) 
			{
				ArrayList<DatabaseLocation> querry = b.getPosClass(id);
				
				
				DBSCAN s = new DBSCAN(querry, false);	
				int temp = s.cluster(0.01, 2);
				
				 System.out.println(s.associateCluster(new Tuple<Double,Double>(57.69661,11.97575),0.01));
				
				ArrayList<DatabaseLocation>[] temp2 = s.getClusterd(true);
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
							Tuple<Double,Double> d = new Tuple<Double,Double>(dbl.getLat(),dbl.getLon());
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
							Tuple<Double,Double> coord = new Tuple<Double,Double>(dbl.getLat(),dbl.getLon());
							hs.put(coord,mean);
						}
						clust.put(mean, i);
					}
					
				}
				
				for(int i = 0; i < temp2.length; i++)
				{
					for(int j = 0; j < temp2[i].size(); j++)
					{
						double[] pos = {temp2[i].get(j).getLat(),temp2[i].get(j).getLon()};
						double[] dest = {temp2[i].get(j).getNLat(),temp2[i].get(j).getNLon()};
						Tuple<Double,Double> dst = findNextCluster( new Tuple<Double,Double>(temp2[i].get(j).getNLat(),temp2[i].get(j).getNLon()),posToLoc);
												
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
							Tuple<Double,Double> coord = new Tuple<Double,Double>(temp2[i].get(j).getLat(),temp2[i].get(j).getLon());
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
						double[] tmp = {lat, lon};
						input.add(tmp);
						hours.add(h);
						minutes.add(min);
						if(i+1 <nList.getLength())
						{
							Node oNode = nList.item(i+1);
							if (oNode.getNodeType() == Node.ELEMENT_NODE) {
								Element oElement = (Element) oNode;
								double[] tmp2 = {Double.parseDouble(oElement.getAttribute("lat")), Double.parseDouble(oElement.getAttribute("lon"))};
								output.add(tmp2);
							}
						}
						else
						{
							double[] tmp2 = {lat, lon};
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
		
		public void exportToCSV()
		{
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("coords.csv"),"utf-8")))
			{
				for(int i = 0; i < input.size();i++)
				{
					/*writer.write(input.get(i)[1] + " " + input.get(i)[0] + " " + hours.get(i) + " " + minutes.get(i) + " " 
							+ output.get(i)[1] + " " + output.get(i)[0] + "\n");*/
					writer.write(inputClust.get(i) + " " + hours.get(i) + " " + minutes.get(i) + " " 
							+ outputClust.get(i) + "\n");
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
				temp = new Tuple<Double,Double>(td.getNLat(),td.getNLon());
			}
			
			return temp;
		}
		
		public boolean emptyData()
		{
			return input.isEmpty() && output.isEmpty();
		}
	}
}
