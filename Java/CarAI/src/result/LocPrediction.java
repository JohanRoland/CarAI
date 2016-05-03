package result;

import interfaces.DatabaseLocation;
import serverConnection.ServerConnection;
import serverConnection.ServerConnection.DBQuerry;
import utils.*;
import mashinelearning.DBSCAN;
import mashinelearning.ELKIController;
import mashinelearning.NNData;

import java.sql.SQLException;
import java.sql.Time;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.app.analyst.AnalystFileFormat;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.app.analyst.wizard.AnalystWizard;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.util.arrayutil.VectorWindow;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.logging.EncogFormatter;
import org.encog.util.obj.SerializeObject;
import org.encog.util.simple.EncogUtility;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.genetic.MLMethodGenomeFactory;
import org.encog.ml.model.EncogModel;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Greedy;
import org.encog.ml.train.strategy.HybridStrategy;
import org.encog.ml.train.strategy.StopTrainingStrategy;
import org.encog.neural.freeform.FreeformLayer;
import org.encog.neural.freeform.FreeformNetwork;
import org.encog.neural.freeform.training.FreeformBackPropagation;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.normalizers.strategies.NormalizationStrategy;
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
	FreeformNetwork network;
	
	int nrCols = 5;
	public double sampleIn[][] = {{0.0,0.0},{1.0,0.0},{0.0,1.0},{1.0,1.0}};
	public double sampleOut[][] = {{0.0},{1.0},{1.0},{0.0}};
	
	public Tuple<Double,Double> predictedLoc;
	private MqttTime mqttTime;
	private LocPrediction()
	{
		mqttTime = MqttTime.getInstance();
		
		hyperParamLerning("coords.csv");
		//standardLearning();
		//customLearning();
		//importNetwork();
	}
	
	/**
	 * Saves the network in the selected file
	 * @param filePath
	 */
	
	private void saveNetwork(String filePath)
	{
		EncogDirectoryPersistence.saveObject(new File(filePath), bestMethod);
	}
	
	private void lern(String method)
	{
		if(method.equals("standard"))
		{
			standardLearning();
		}else if(method.equals("custom"))
		{
			customLearning("coords.csv");
		}else if(method.equals("hyperParam"))
		{
			
		}else if(method.equals(""))
		{
			
		}

	}

	private void loadHyperParamNetwork()
	{
		format = new CSVFormat('.',' ');
		
		nd.exportAsCoordsToCSV("coords.csv");
		
		ELKIController.runElki();
		
		nd.exportAsClustToCSVWithHyperTwo();
		
		bestMethod =(MLRegression)EncogDirectoryPersistence.loadObject(new File("networkExport.eg"));
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition previus = data.defineSourceColumn("prev",0,ColumnType.nominal);		
		ColumnDefinition here = data.defineSourceColumn("here",1,ColumnType.nominal);		
		ColumnDefinition columnDay = data.defineSourceColumn("day",2,ColumnType.nominal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",3,ColumnType.continuous);
		ColumnDefinition dest = data.defineSourceColumn("dest",4,ColumnType.nominal);	
		
		data.analyze();
		
		data.defineInput(previus);
		data.defineInput(here);
		data.defineInput(columnDay);
		data.defineInput(columnMTime);
		data.defineOutput(dest);
		
		data.getNormHelper().defineUnknownValue("?");
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);	
		helper = data.getNormHelper();
		
		
	}
	
	/**
	 * Trains a netwotk with the contents of coords.txt as clusterd paths,
	 * considering one path back. And loads it as the bestMethod as well
	 * as saving it.
	 */
	private void hyperParamLerning(String tempFileName)
	{
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		//nd.coordCullByDist();
		nd.exportAsCoordsToCSV(tempFileName);
		
		ELKIController.runElki();
		
		nd.exportAsClustToCSVWithHyperTwo();
		
		VersatileDataSource source = new CSVDataSource(new File(tempFileName),false,format);
		
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition previus = data.defineSourceColumn("prev",0,ColumnType.nominal);		
		ColumnDefinition here = data.defineSourceColumn("here",1,ColumnType.nominal);		
		ColumnDefinition columnDay = data.defineSourceColumn("day",2,ColumnType.nominal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",3,ColumnType.continuous);
		ColumnDefinition dest = data.defineSourceColumn("dest",4,ColumnType.nominal);	

		data.analyze();
		
		data.defineInput(previus);
		data.defineInput(here);
		data.defineInput(columnDay);
		data.defineInput(columnMTime);
		data.defineOutput(dest);

		data.getNormHelper().defineUnknownValue("?");
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();
		
		model.holdBackValidation(0.3, true, 1001);
		model.selectTrainingType(data);
		bestMethod = (MLRegression)model.crossvalidate(20, true);
		
		System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
		helper = data.getNormHelper();
		System.out.println(helper.toString());
		System.out.println("Final model: " + bestMethod);
		
		
	}
	
	private void loadStandardNetwork()
	{
		
		format = new CSVFormat('.',' ');
		
		bestMethod =(MLRegression)EncogDirectoryPersistence.loadObject(new File("networkExport.eg"));
		
		nd.coordCullByBox(57.34, 11, 1 , 4);
		
		nd.exportAsCoordsWithDateToCSV();
		
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
		ColumnDefinition columnDay = data.defineSourceColumn("Day",2,ColumnType.nominal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",3,ColumnType.continuous);
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",4,ColumnType.continuous);		
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",5,ColumnType.continuous);	
		
		data.analyze();
		
		data.defineInput(columnInLon);
		data.defineInput(columnInLat);
		data.defineInput(columnDay);
		data.defineInput(columnMTime);
		data.defineOutput(columnOutLon);
		data.defineOutput(columnOutLat);

		data.getNormHelper().defineUnknownValue("?");
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		helper = data.getNormHelper();
		
	}
	
	/**
	 * Trains a netwotk with the contents of coords.txt as coordinates to coordinates,
	 * considering one path back. And loads it as the bestMethod as well as saving it.
	 */
	private void standardLearning()
	{		
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		NNData nd = new NNData();
		
		nd.coordCullByBox(57.34, 11, 1 , 4);
		
		//nd.coordCullByDist();
		
		nd.exportAsCoordsWithDateToCSV();
		
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
		ColumnDefinition columnDay = data.defineSourceColumn("Day",2,ColumnType.nominal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",3,ColumnType.continuous);
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",4,ColumnType.continuous);		
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",5,ColumnType.continuous);	
		
		data.analyze();
		
		data.defineInput(columnInLon);
		data.defineInput(columnInLat);
		data.defineInput(columnDay);
		data.defineInput(columnMTime);
		data.defineOutput(columnOutLon);
		data.defineOutput(columnOutLat);
		data.getNormHelper().defineUnknownValue("?");
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();
		
		model.holdBackValidation(0.3, true, 1001);
		model.selectTrainingType(data);
		bestMethod = (MLRegression)model.crossvalidate(20, true);
		
		
		System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
		helper = data.getNormHelper();
		System.out.println(helper.toString());
		System.out.println("Final model: " + bestMethod);
	}
	private void customLearning(String tempFile)
	{
		mqttTime = MqttTime.getInstance();
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		nd = new NNData();

		//nd.importFromDB(0, 600000);

		nd.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
		nd.coordCullByBox(57.34, 11, 1 , 4);
		//data.cullByRDP();
		nd.coordCullByDist();
		//nd.coordCullBySpeed(15.0);
		nd.exportAsCoordsToCSV(tempFile);
		

		
		VersatileDataSource source = new CSVDataSource(new File(tempFile),false,format);
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
		ColumnDefinition columnDay = data.defineSourceColumn("day",2,ColumnType.ordinal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",2,ColumnType.continuous);
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",3,ColumnType.continuous);		
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",4,ColumnType.continuous);	
		
		data.analyze();
		
		data.defineInput(columnInLon);
		data.defineInput(columnInLat);
		data.defineInput(columnDay);
		data.defineInput(columnMTime);
		data.defineOutput(columnOutLon);
		data.defineOutput(columnOutLat);
		data.getNormHelper().defineUnknownValue("?");
		
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		
		data.normalize();
		
		helper = data.getNormHelper();
		ArrayList<double[]> in = new ArrayList<double[]>(); 
		ArrayList<double[]> out = new ArrayList<double[]>(); 
		
		data.forEach(e -> {
			in.add(e.getInputArray());
			out.add(e.getIdealArray());
		});
		
		
		MLDataSet trainingSet = new BasicMLDataSet(in.toArray(new double[in.size()][]),out.toArray(new double[out.size()][]));
		
		
		
		/////// CREATE NETWORK
		network = new FreeformNetwork(); //(new FreeformNetwork()).createElman(3, 7, 2, new ActivationTANH());
		
		
		FreeformLayer input = network.createInputLayer(3);
		FreeformLayer hiddenLayer = network.createLayer(7);
		FreeformLayer hiddenLayer2 = network.createLayer(7);
		FreeformLayer output = network.createOutputLayer(2);
		
		
		network.connectLayers(input, hiddenLayer, new ActivationTANH(), 1.0, false);
		network.connectLayers(input, hiddenLayer2, new ActivationSigmoid(), 0.0, false);
		network.connectLayers(hiddenLayer, hiddenLayer2, new ActivationTANH(), 0.0, false);
		//network.connectLayers(hiddenLayer, output, new ActivationSigmoid(), 1.0, false);
		
		network.connectLayers(hiddenLayer2, output, new ActivationTANH(), 1.0, false);
		
		network.reset();
		/////// END CREATE NETWORK
		
		TrainingSetScore score = new TrainingSetScore(trainingSet);
		
		MLTrain trainAlt = new NeuralSimulatedAnnealing(network, score, 10, 2, 100);
		
		MLTrain train = new FreeformBackPropagation(network,trainingSet,0.00001, 0.0);// 0.7, 0.9);
		
		StopTrainingStrategy stop = new StopTrainingStrategy();
		
		train.addStrategy(new Greedy());
		train.addStrategy(new HybridStrategy(trainAlt));
		train.addStrategy(stop);
		
		int epoch = 0;
		do
		{
			train.iteration();
			System.out.println("Epoch #" + (epoch++) + " Error:" + train.getError());
		}while(train.getError() > 0.0001 && epoch < 3000);
			
		
		train.finishTraining();
		
		try {
			SerializeObject.save(new File("freeformNetworkExport.eg"), network);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	
	}
	private LocPrediction(int id, String tempFile, String saveFile) throws Exception
	{
		mqttTime = MqttTime.getInstance();
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		nd = new NNData();
		
		//nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		
		
		if(nd.importFromDB(id, -1)>0)
		{
			switch(1)
			{
			case 1:
				//loadHyperParamNetwork();
				hyperParamLerning(tempFile);
				saveNetwork(saveFile);
				break;
			case 2:
				standardLearning();
				break;
			case 3:
				break;
			}
		}
		else
		{
			throw new Exception("No traning data available");
		}

	}
	
	/**
	 * Returns the specifyed user if it exsists,
	 * if it doesn't exsitst it will retrieved the user
	 * and return it.
	 * tempFile and saveFile will only be used if a new
	 * user is retrieved.
	 * 
	 * After this method is used an Encog instance will most probably be active.
	 * 
	 * @param userID
	 * @param tempFile
	 * @param saveFile
	 * @return
	 * @throws Exception
	 */
	static public LocPrediction getInstance(int userID, String tempFile, String saveFile) throws Exception
	{
		if(instanceMap == null)
		{
			instanceMap = new HashMap<Integer,LocPrediction>();
		}
		if(!instanceMap.containsKey(userID))
		{
			LocPrediction temp = new LocPrediction(userID, tempFile, saveFile);
			instanceMap.put(userID, temp);//
		}
		else
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
	public ArrayList<double[]> predict()
	{
		int temp = nd.getClosestCluster(Car.getInstance().getPos());
		//return predict(temp);
		return predictHyperTwoClust(temp,temp);
	}
	public ArrayList<double[]> predict(int cluster)
	{
		String[] line = new String[3];
		MLData input = helper.allocateInputVector();
		
		int hour = mqttTime.getHour();// c.get(Calendar.HOUR_OF_DAY);
		int minute = mqttTime.getMinute(); //c.get(Calendar.MINUTE);
		
		line[0] = ""+cluster;
		line[1] = ""+mqttTime.getDayOfWeek();
		line[2] = ""+(hour*60+minute);
		
		helper.normalizeInputVector(line,input.getData(),false);
		MLData output = bestMethod.compute(input);
		String irisChoosen0 = helper.denormalizeOutputVectorToString(output)[0];
		StringBuilder result = new StringBuilder();
		result.append("[" + line[0]+ " ( " + nd.getViewClustPos().get(Integer.parseInt(line[0])) + ")"+ ", " + line[1]+", " + line[2]+ "] ");
		result.append(" -> predicted: ");
		result.append(irisChoosen0 + " ( " + nd.getViewClustPos().get(Integer.parseInt(irisChoosen0)) + ")");
		System.out.println(result.toString());
		
		
		ArrayList<double[]> temp1 = probParing(output);
		
		return temp1;
		//return nd.getViewClustPos().get(Integer.parseInt(irisChoosen0));
	}
	public ArrayList<double[]> predictHyperTwoClust(int clust1, int clust2)// throws Exception
	{
		/*if(helper==null)
		{
			throw new Exception("No network trained");
		}*/
		String[] line = new String[4];
		MLData input = helper.allocateInputVector();
		
		int hour = mqttTime.getHour();// c.get(Calendar.HOUR_OF_DAY);
		int minute = mqttTime.getMinute(); //c.get(Calendar.MINUTE);
		
		line[0] = ""+clust1;
		line[1] = ""+clust2;
		line[2] = ""+mqttTime.getDayOfWeek();
		line[3] = ""+(hour*60+minute);
		
		helper.normalizeInputVector(line,input.getData(),false);
		MLData output = bestMethod.compute(input);
		String irisChoosen0 = helper.denormalizeOutputVectorToString(output)[0];
		
		StringBuilder result = new StringBuilder();
		result.append("Path: "+ line[0]+ " " + nd.getViewClustPos().get(Integer.parseInt(line[0]))
					 + " " + line[1] +" "+ nd.getViewClustPos().get(Integer.parseInt(line[1]))
					 +", Day: " + line[2] + " Min: " + line[3] + " ");
		result.append(" -> predicted: ");
		result.append(irisChoosen0 + " ( " + nd.getViewClustPos().get(Integer.parseInt(irisChoosen0)) + ")");
		System.out.println(result.toString());
		ArrayList<double[]>temp1 = probParing(output);
		
		return temp1;
	}
	private ArrayList<double[]> probParing(MLData output)
	{
		ArrayList<double[]> temp1= new ArrayList<double[]>();
		double[] zeroToOne = helperfunc(output.getData());
		double tot=0;
		
		for(double d : zeroToOne)
			tot+=d;
		for(int i=0; i<zeroToOne.length;i++)
		{
			double d = zeroToOne[i];
			//System.out.print(100*(d/tot) + "%, ");
			double secondTemp;
			ColumnDefinition tempbefor = (ColumnDefinition) helper.getOutputColumns().toArray()[0];
			Tuple<Double,Double> firrstTemp = nd.getViewClustPos().get(Integer.parseInt(tempbefor.getClasses().get(i)));
			secondTemp = d;//tot;
			double[] temp = {firrstTemp.fst(),firrstTemp.snd(),secondTemp};
			temp1.add(temp); //new Tuple<Tuple<Double,Double>,Double>(firrstTemp, secondTemp));
		}

		temp1.sort(new Comp());
		
		return temp1;
	}
	double[] helperfunc(double[] in)
	{
		double[] out = new double[in.length];
		
		double max = 1;
		double min = -1;
		
		for(double i : in)
		{
			if(i<min)
			{
				min=i;
			}
			if(i>max)
			{
				max=i;
			}
		}
	
		for(int i=0; i<in.length;i++)
		{
			out[i]=(in[i]-min)/(max-min)*0.5+0.5;
		}
		return out;
	}
	public Tuple<Double,Double> predictCoord()
	{
		String[] line = new String[4];
		
		//Calendar c = Calendar.getInstance();
		int hour = mqttTime.getHour();// c.get(Calendar.HOUR_OF_DAY);
		int minute = mqttTime.getMinute(); //c.get(Calendar.MINUTE);
		int dayofweek = mqttTime.getDayOfWeek();
		
		//EncogUtility.saveEGB(new File("networkExport.eg"), data);
		//EncogUtility.explainErrorMSE(bestMethod, data);
		
		//VectorWindow window = new VectorWindow(4);
		MLData input = helper.allocateInputVector();
		

		Car carData = Car.getInstance();
		
		line[0] = ""+carData.getPos().fst();
		line[1] = ""+carData.getPos().snd();
		line[2] = ""+dayofweek;
		line[3] = ""+(hour*60+minute);
		
		//line[3] = ""+minute;
		
		helper.normalizeInputVector(line,input.getData(),false);
		
		
		MLData output = bestMethod.compute(input);	// network.compute(input); // 
		
		
		
		double irisChoosen0 = Double.parseDouble(helper.denormalizeOutputVectorToString(output)[0]);
		double irisChoosen1 = Double.parseDouble(helper.denormalizeOutputVectorToString(output)[1]);

		StringBuilder result = new StringBuilder();
		
		
		
		result.append("[" + line[0]+ ", " + line[1] +", " + line[2] +", "+line[3]  + "] ");
		result.append(" -> predicted: ");
		result.append(irisChoosen0 + ", " + irisChoosen1);
		System.out.println(result.toString());
		
		return new Tuple<Double,Double>(irisChoosen0,irisChoosen1);
	}
	private class Comp implements Comparator<double[]>
	{
			 
	
			@Override
			public int compare(double[] o1, double[] o2)
			{
				if(o1[2]>o2[2])
				{
					return -1;
				}
				else if(o1[2]<o2[2])
				{
					return 1;
				}
				else
				{
					return 0;
				}
			}
	}
}