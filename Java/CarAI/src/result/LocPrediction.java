package result;

import interfaces.DatabaseLocation;
import serverConnection.DBSCAN;
import serverConnection.ServerConnection;
import serverConnection.ServerConnection.DBQuerry;
import utils.*;

import java.sql.SQLException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * 		Time 	  (Time)
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
		nd.importFromDB();
		
		nd.exportToCSV();
			
		
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		VersatileMLDataSet data =  new VersatileMLDataSet(source);

		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",0,ColumnType.continuous);		
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",1,ColumnType.continuous);		
		ColumnDefinition columnTime = data.defineSourceColumn("time",2,ColumnType.continuous);
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",3,ColumnType.continuous);		
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",4,ColumnType.continuous);	
		
		data.getNormHelper().defineUnknownValue("?");
		data.analyze();
		
		data.defineInput(columnInLat);
		data.defineInput(columnInLon);
		data.defineInput(columnTime);
		data.defineOutput(columnOutLat);
		data.defineOutput(columnOutLon);
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();
		
		model.holdBackValidation(0.3, true, 1001);
		model.selectTrainingType(data);
		MLRegression bestMethod = (MLRegression)model.crossvalidate(4, true);
		
		
		System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
		NormalizationHelper helper = data.getNormHelper();
		System.out.println(helper.toString());
		System.out.println("Final model: " + bestMethod);
		
		ReadCSV csv = new ReadCSV(new File("coords.csv"),false,format);
		String[] line = new String[3];
		MLData input = helper.allocateInputVector();
		
		while(csv.next())
		{
			StringBuilder result = new StringBuilder();
			for(int i = 0; i < 3; i++)
				line[i] = csv.get(i);
			
			helper.normalizeInputVector(line,input.getData(),false);
			MLData output = bestMethod.compute(input);
			String irisChoosen0 = helper.denormalizeOutputVectorToString(output)[0];
			String irisChoosen1 = helper.denormalizeOutputVectorToString(output)[1];
			result.append(Arrays.toString(line));
			result.append(" -> predicted: ");
			result.append(irisChoosen0 + " , " + irisChoosen1);
			result.append("(correct: ");
			result.append(csv.get(3)+ " , " +csv.get(4)); 
			result.append(")");
			System.out.println(result.toString());
		}
		
		
		Encog.getInstance().shutdown();
	}
	
	private class NNData
	{
		// Inputs
		ArrayList<double[]> input;
		
		//Outputs
		ArrayList<double[]> output;
		
		
		public NNData()
		{
			input = new ArrayList<double[]>();
			output = new ArrayList<double[]>();
		}
		
		public void importFromDB()
		{
			ServerConnection b;
			b= new ServerConnection("mydb","3306","192.168.1.26" , "car", "RigedyRigedyrektSon");
			int di =0;
			int dj =0;
			try (PrintStream out = new PrintStream(new FileOutputStream("clusterd.txt"))) 
			{
				ArrayList<DatabaseLocation> querry = b.getPosClass(0);
				
				
				DBSCAN s = new DBSCAN(querry, false);	
				int temp = s.cluster(0.01, 2);
				
				
				ArrayList<DatabaseLocation>[] temp2 = s.getClusterd(true);
				HashMap<Tuple<Double,Double>,Tuple<Double,Double>> hs = new HashMap<Tuple<Double,Double>,Tuple<Double,Double>>();
				HashMap<Tuple<Double,Double>,Integer> clust = new HashMap<Tuple<Double,Double>,Integer>();
				
				for(int i = 0; i < temp2.length;i++)
				{
					if(i == 0)
					{
						for(DatabaseLocation dbl : temp2[i])
						{
							if(dbl.getLon() == 57.6026314)
							{
								System.out.println("entered");
							}
							Tuple<Double,Double> d = new Tuple<Double,Double>(dbl.getLat(),dbl.getLon());
							hs.put(d, d);
							clust.put(d, i);
						}
					}
					else
					{
						
						Tuple<Double,Double> mean = Utils.mean(temp2[i]);
						for(DatabaseLocation dbl : temp2[i])
						{
							if(dbl.getLon() == 57.6026314)
							{
								System.out.println("entered");
							}
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
						double[] pos = {temp2[i].get(j).getLat(),temp2[i].get(j).getLon(),temp2[i].get(j).getTime()};
						double[] dest = {temp2[i].get(j).getNLat(),temp2[i].get(j).getNLon()};
						Tuple<Double,Double> dst = new Tuple<Double,Double>(temp2[i].get(j).getNLat(),temp2[i].get(j).getNLon());
						Tuple<Double,Double> meanDst = hs.get(dst);
						di = i;
						dj = j; 
						if(i == 0)
						{
							input.add(pos);
							 
							dest[0] = meanDst.fst();
							dest[1] = meanDst.snd();
							output.add(dest);
						}
						else if(clust.get(meanDst) != i)
						{
							Tuple<Double,Double> coord = new Tuple<Double,Double>(temp2[i].get(j).getLat(),temp2[i].get(j).getLon());
							pos[0] = hs.get(coord).fst();
							pos[1] = hs.get(coord).snd();
							
							input.add(pos);
							dest[0] = meanDst.fst();
							dest[1] = meanDst.snd();
							output.add(dest);
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
					 	String tmdasd= time.substring(11, time.length()-1).replace(":","");
					 	double t = Double.parseDouble(tmdasd);
					 	
					 	double lat = Double.parseDouble(eElement.getAttribute("lat"));
					 	double lon = Double.parseDouble(eElement.getAttribute("lon"));
						double[] tmp = {lat, lon , t};
						input.add(tmp);
						
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
					writer.write(input.get(i)[1] + " " + input.get(i)[0] + " " + input.get(i)[2] + " " 
							+ output.get(i)[1] + " " + output.get(i)[0] + "\n");
				}
			}catch(Exception e)
			{
				System.out.println("Error on creating csv file");
			}
		}
		
		public void exportToDB()
		{
			ServerConnection sc = new ServerConnection("mydb","3306","192.168.1.26" , "car", "RigedyRigedyrektSon");
			ArrayList<DBQuerry> querry = new ArrayList<DBQuerry>();
			
			try {
				for(int i = 0; i < input.size(); i++)
				{ 
					querry.add(new DBQuerry(input.get(i)[0], input.get(i)[1], input.get(i)[2], output.get(i)[0], output.get(i)[1]));
					
				}
				DBQuerry[] sendDB = querry.toArray(new DBQuerry[querry.size()]);
				sc.replacePosData(0, sendDB );
				//sc.addPosData(0, input.get(i)[0], input.get(i)[1], input.get(i)[2], output.get(i)[0], output.get(i)[1]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
	}
}
