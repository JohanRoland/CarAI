package prediction;

import utils.*;
import mashinelearning.ELKIController;
import mashinelearning.NNData;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.encog.ConsoleStatusReportable;
import org.encog.EncogError;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.util.csv.CSVFormat;
import org.encog.util.obj.SerializeObject;
import org.encog.util.simple.EncogUtility;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
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
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnType;

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
	
	public void saveNetwork(String filePath)
	{
		EncogDirectoryPersistence.saveObject(new File(filePath), bestMethod);
	}
	/**
	 * 
	 * 
	 * 
	 * @param networkPath
	 * @param dataPath
	 * @param networkType 0: clusters, 1: hyper param 2 cluster ,2: coords
	 */
	public void loadNetwork(String networkPath, String dataPath, int networkType)
	{
		bestMethod = (MLRegression) EncogDirectoryPersistence.loadObject(new File(networkPath));
		
		VersatileDataSource source = new CSVDataSource(new File(dataPath),false,format);
		data =  new VersatileMLDataSet(source);

		data.getNormHelper().setFormat(format);
		
		ColumnDefinition previus;		
		ColumnDefinition here;
		ColumnDefinition columnDay;
		ColumnDefinition columnMTime;
		ColumnDefinition dest;	

		switch(networkType)
		{
			case 0:
				here = data.defineSourceColumn("here",0,ColumnType.nominal);		
				columnDay = data.defineSourceColumn("day",1,ColumnType.nominal);
				columnMTime = data.defineSourceColumn("minutes",2,ColumnType.continuous);
				dest = data.defineSourceColumn("dest",3,ColumnType.nominal);	
				data.analyze();
				data.defineInput(here);
				data.defineInput(columnDay);
				data.defineInput(columnMTime);
				data.defineOutput(dest);
				break;
			case 1:
				previus = data.defineSourceColumn("prev",0,ColumnType.nominal);		
				here = data.defineSourceColumn("here",1,ColumnType.nominal);		
				columnDay = data.defineSourceColumn("day",2,ColumnType.nominal);
				columnMTime = data.defineSourceColumn("minutes",3,ColumnType.continuous);
				dest = data.defineSourceColumn("dest",4,ColumnType.nominal);	
				data.analyze();
				data.defineInput(previus);
				data.defineInput(here);
				data.defineInput(columnDay);
				data.defineInput(columnMTime);
				data.defineOutput(dest);
				break;
			case 2:
				data.getNormHelper().setFormat(format); 
				ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
				ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
				columnDay = data.defineSourceColumn("Day",2,ColumnType.nominal);
				columnMTime = data.defineSourceColumn("minutes",3,ColumnType.continuous);
				ColumnDefinition columnOutLon = data.defineSourceColumn("olon",4,ColumnType.continuous);		
				ColumnDefinition columnOutLat = data.defineSourceColumn("olat",5,ColumnType.continuous);	
				data.analyze();
				data.defineInput(columnInLon);
				data.defineInput(columnInLat);
				data.defineInput(columnDay);
				data.defineInput(columnMTime);
				data.defineOutput(columnOutLon);
				data.defineOutput(columnOutLat);
				break;
		}
		data.getNormHelper().defineUnknownValue("?");
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		helper = data.getNormHelper();
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();

		
	}
	
	private void learn(String method)
	{
		if(method.equals("standard"))
		{
			standardLearning("coords.csv");
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
		
		Date date = new Date();
		String tempName = "ELKIClusters" + date.getTime();
		ELKIController.runElki(tempName);
		
		nd.exportAsClustToCSVWithHyperTwo("coords.csv",tempName);
		
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

	public void loadHyperParamNetworkFromCSV(String tempFileName)
	{
		File f = new File(".");
		String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
		
		nd.impElkAndReroutFromNoise(pathToProj+"\\ELKIClusters\\");
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
		
		Date date = new Date();
		String tempName = "ELKIClusters" + date.getTime();
		ELKIController.runElki(tempName);
		
		nd.exportAsClustToCSVWithHyperTwo(tempFileName, tempName);
		
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
		
		//EncogUtility.evaluate(bestMethod, model.getValidationDataset());
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("logFile.txt", true));
			bw.write("\n Network: hyperParamLerning \t Traning error: " + EncogUtility.calculateRegressionError(bestMethod, model.getTrainingDataset())+"\t");
			bw.write("Validation error: " + EncogUtility.calculateRegressionError(bestMethod, model.getValidationDataset())+"\t");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
		*/
		helper = data.getNormHelper();
		System.out.println(helper.toString());
		System.out.println("Final model: " + bestMethod);
				
	}
	public void hyperParamLernTestLoad(String tempFileName, String network)
	{
		bestMethod =(MLRegression)EncogDirectoryPersistence.loadObject(new File(network));
		VersatileDataSource source = new CSVDataSource(new File(tempFileName),false,format);
		
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		//ColumnDefinition previus = data.defineSourceColumn("prev",0,ColumnType.nominal);		
		ColumnDefinition here = data.defineSourceColumn("here",1,ColumnType.nominal);		
		ColumnDefinition columnDay = data.defineSourceColumn("day",2,ColumnType.nominal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",3,ColumnType.continuous);
		ColumnDefinition dest = data.defineSourceColumn("dest",4,ColumnType.nominal);	

		data.analyze();
		
		//data.defineInput(previus);
		data.defineInput(here);
		data.defineInput(columnDay);
		data.defineInput(columnMTime);
		data.defineOutput(dest);

		data.getNormHelper().defineUnknownValue("?");
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();

	}
	public void hyperParamLernTestTrain(String tempFileName, String network)
	{
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
		
		System.out.println("Training Error: " + EncogUtility.calculateRegressionError(bestMethod, model.getTrainingDataset()));
		System.out.println("Validation Error:  " + EncogUtility.calculateRegressionError(bestMethod, model.getValidationDataset()));
		
		saveNetwork(network);
		helper = data.getNormHelper();
		System.out.println(helper.toString());
		System.out.println("Final model: " + bestMethod);
	}
	
	
	private void loadStandardNetwork(String tempFile)
	{
		
		format = new CSVFormat('.',' ');
		
		bestMethod =(MLRegression)EncogDirectoryPersistence.loadObject(new File("networkExport.eg"));
		
		nd.coordCullByBox(57.34, 11, 1 , 4);
		
		nd.exportAsCoordsWithDateToCSV(tempFile);
		
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
	private void standardLearning(String tempFile)
	{		
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		//NNData nd = new NNData();
		
		//nd.coordCullByBox(57.34, 11, 1 , 4);
		
		//nd.coordCullByDist();
		
		nd.exportAsCoordsWithDateToCSV(tempFile);
		
		VersatileDataSource source = new CSVDataSource(new File(tempFile),false,format);
		
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
		
		
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("logFile.txt", true));
			bw.write("\n Network: standardLearning \t Traning error: " + EncogUtility.calculateRegressionError(bestMethod, model.getTrainingDataset())+"\t");
			bw.write("Validation error: " + EncogUtility.calculateRegressionError(bestMethod, model.getValidationDataset())+"\t");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		/*
		System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
		*/
		helper = data.getNormHelper();
		System.out.println(helper.toString());
		System.out.println("Final model: " + bestMethod);
	}
	private void customLearning(String tempFile)
	{
		mqttTime = MqttTime.getInstance();
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		//nd = new NNData();

		//nd.importFromDB(0, 600000);

		//nd.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
		//nd.coordCullByBox(57.34, 11, 1 , 4);
		//data.cullByRDP();
		//nd.coordCullByDist();
		//nd.coordCullBySpeed(15.0);
		nd.exportAsCoordsToCSV(tempFile);
		

		
		VersatileDataSource source = new CSVDataSource(new File(tempFile),false,format);
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
		ColumnDefinition columnDay = data.defineSourceColumn("day",2,ColumnType.ordinal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",3,ColumnType.continuous);
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",4,ColumnType.continuous);		
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",5,ColumnType.continuous);	
		
		columnDay.defineClass(new String[] {"1","2","3","4","5","6","7"});
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
		
		model.holdBackValidation(0.3, true, 101);
		

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("logFile.txt", true));
			bw.write("\n Network: customLearning \t Traning error: " + EncogUtility.calculateRegressionError(bestMethod, model.getTrainingDataset())+"\t");
			bw.write("Validation error: " + EncogUtility.calculateRegressionError(bestMethod, model.getValidationDataset())+"\t");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			SerializeObject.save(new File("freeformNetworkExport.eg"), network);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	
	}
	private LocPrediction(int id, String tempFile, String saveFile, int mode) throws UserNotLoaded
	{
		mqttTime = MqttTime.getInstance();
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		nd = new NNData();
		
		
		//nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		
		//nd.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml", 0);
		//nd.coordCullByDist();
		
		
		
		BufferedWriter bw;		
		if(nd.importFromDB(id, -1) > 25)
		{
			switch(mode)
			{
			case 1:
				try
				{
					standardLearning(tempFile);
				}
				catch(EncogError e)
				{

					try {
						bw = new BufferedWriter(new FileWriter("logFile.txt", true));
						bw.write("\n Failed to train id: " + id +  ". Using standardLerning (mode 1) at time: "+ System.currentTimeMillis());
						bw.close();
					} catch (IOException e1) {

						e1.printStackTrace();
					}

					throw new UserNotLoaded("Failed in standard lerning, network FAIL ID: " + id);

				}
				break;
			case 2:
				try
				{
					hyperParamLerning(tempFile);
				}
				catch(EncogError e)
				{

					try {
						bw = new BufferedWriter(new FileWriter("logFile.txt", true));
						bw.write("\n Failed to train id: " + id +  ". Using hyperParamLerning (mode 2) at time: "+ System.currentTimeMillis());
						bw.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					throw new UserNotLoaded("Failed in hyperParamLerning, network FAIL ID: " + id);
				}
				//nd.saveAsCSV(".//temp.txt");
				//saveNetwork(saveFile);
				break;	
			case 3:
				try
				{
				customLearning(tempFile);
				}
				catch(EncogError e)
				{

					try {
						bw = new BufferedWriter(new FileWriter("logFile.txt", true));
						bw.write("\n Failed to train id: " + id +  ". Using customLearning (mode 3) at time: "+ System.currentTimeMillis());
						bw.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					throw new UserNotLoaded("Failed in hyperParamLerning, network FAIL ID: " + id);
				}
				break;
			case 4:
				nd.loadFromCSV(".//temp.txt");
				try{
					loadNetwork(saveFile,tempFile, 1);
				}
				catch(Error e)
				{
					try {
						bw = new BufferedWriter(new FileWriter("logFile.txt", true));
						bw.write("\n Failed to load temp.txt (mode 4). At time: "+ System.currentTimeMillis());
						bw.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					throw new Error("\n Failed to load temp.txt (mode 4). At time: "+ System.currentTimeMillis());
				}
				break;
			case 5:
				final int maxClust=22;
				try{
					hyperParamLernTestTrain("output.txt", "testSaveFile.eg");
				}catch(Error e)
				{
					try {
						bw = new BufferedWriter(new FileWriter("logFile.txt", true));
						bw.write("\n Failed test (mode 5) "+ System.currentTimeMillis());
						bw.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					throw new UserNotLoaded("\n Failed test (mode 5) "+ System.currentTimeMillis());
				}
				break;
			default:
				throw new Error("Unimplemented network mode");
			}
		}
		else
		{
			try {
				bw = new BufferedWriter(new FileWriter("logFile.txt", true));
				bw.write("\t\t");
				bw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			throw new UserNotLoaded("No traning data available");
		}
	}
	double getMaxDoubleFromList(double[] in)
	{
		double max=Double.MIN_VALUE;
		
		for(double e : in)
			if(e>max)
				max=e;
		
		return max;
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
	static public LocPrediction getInstance(int userID, String tempFile, String saveFile,int mode) throws UserNotLoaded
	{
		if(instanceMap == null)
		{
			instanceMap = new HashMap<Integer,LocPrediction>();
		}
		if(!instanceMap.containsKey(userID))
		{
			LocPrediction temp = new LocPrediction(userID, tempFile, saveFile, mode);
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
	
	static public void clearInstance(int userID)
	{
		instanceMap.remove(userID);
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

public class UserNotLoaded extends Exception {

	public UserNotLoaded(String string) {
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}

}