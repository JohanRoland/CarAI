package result;

import java.io.BufferedWriter;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

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




public class LocPrediction {

	/*
	 * Input:
	 * 		Position  (Double, Double)
	 * 		Time 	  (Time)
	 * 		Weekday?  (Int)
	 * 		Monthday? (Int)
	 * 		Month     (Int)
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
		
		gpxParser("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
			
		
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
	
	
	void gpxParser(String path)
	{
		ArrayList<double[]> inData = new ArrayList<double[]>();
		
		ArrayList<double[]> outData = new ArrayList<double[]>();
		
		try
		{
		File xmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		Document doc  = dBuilder.parse(xmlFile);
		
		doc.getDocumentElement().normalize();
		
		NodeList nList = doc.getElementsByTagName("trkpt");
		
		String builder = "";
		for(int i = 0; i < nList.getLength()-1; i++)
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
				inData.add(tmp);
				
				if(i+1 <nList.getLength())
				{
					Node oNode = nList.item(i+1);
					if (oNode.getNodeType() == Node.ELEMENT_NODE) {
						double[] tmp2 = {Double.parseDouble(eElement.getAttribute("lat")), Double.parseDouble(eElement.getAttribute("lon"))};
						outData.add(tmp2);
					}
				}
			}
			
		}
		
		}
		catch(Exception e){}
		
		double[][] parsedInData = new double[inData.size()][];
		for(int i = 0; i < inData.size(); i++)
		{
			parsedInData[i] = inData.get(i);
		}
		double[][] parsedOutData = new double[inData.size()][];
		for(int i = 0; i < inData.size(); i++)
		{
			parsedOutData[i] = outData.get(i);
		}
		
		// Write to CSV file; 
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("coords.csv"),"utf-8")))
		{
			for(int i = 0; i < inData.size();i++)
			{
				writer.write(inData.get(i)[0] + " " + inData.get(i)[1] + " " + inData.get(i)[2] + " " 
						+ outData.get(i)[0] + " " + outData.get(i)[1] + "\n");
			}
		}catch(Exception e)
		{
			System.out.println("Error on creating csv file");
		}
		sampleIn = parsedInData;
		sampleOut = parsedOutData;
		
	}
	
	
}
