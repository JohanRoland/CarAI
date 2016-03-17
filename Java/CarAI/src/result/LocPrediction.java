package result;

import interfaces.DatabaseLocation;
import serverConnection.DBSCAN;
import serverConnection.ServerConnection;
import serverConnection.ServerConnection.DBQuerry;
import utils.*;
import mashinelearning.NNData;

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
		//standardLearning();
		//customLearning();
		importNetwork();
	}
	
	private void importNetwork()
	{
		
		//network =  (FreeformNetwork)EncogDirectoryPersistence.loadObject(new File("networkExport.eg"));
		bestMethod = (MLRegression)EncogDirectoryPersistence.loadObject(new File("networkExport.eg"));	
		
		format = new CSVFormat('.',' ');
		
		NNData nd = new NNData();

		nd.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
		//nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		nd.importFromFile();
		//nd.exportToDB(1);
		//nd.importFromDB(1,600000);
		nd.coordCullByBox(57.34, 11, 1 , 4);
		//data.cullByRDP();
		nd.coordCullByDist();
		nd.repoint();
		//nd.coordCullBySpeed(15.0);
		nd.exportAsCoordsToCSV();
		
		String[] descreteMTime = numArray(60);
		String[] descreteHTime = numArray(24);
		
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
		//ColumnDefinition columnHTime = data.defineSourceColumn("hours",2,ColumnType.ordinal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",2,ColumnType.continuous);
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",3,ColumnType.continuous);		
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",4,ColumnType.continuous);	
		
		//columnMTime.defineClass(descreteMTime);
		//columnHTime.defineClass(descreteHTime);
		data.analyze();
		
		data.defineInput(columnInLon);
		data.defineInput(columnInLat);
		//data.defineInput(columnHTime);
		data.defineInput(columnMTime);
		data.defineOutput(columnOutLon);
		data.defineOutput(columnOutLat);
		data.getNormHelper().defineUnknownValue("?");
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();
		helper = data.getNormHelper();
		
	}
	
	private void standardLearning()
	{
		
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		NNData nd = new NNData();

		//nd.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
		//nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		//nd.importFromFile();
		//nd.exportToDB(1);
		nd.importFromDB(1,600000);
		nd.coordCullByBox(57.34, 11, 1 , 4);
		//data.cullByRDP();
		nd.coordCullByDist();
		nd.repoint();
		//nd.coordCullBySpeed(15.0);
		nd.exportAsCoordsToCSV();
		
		String[] descreteMTime = numArray(60);
		String[] descreteHTime = numArray(24);
		
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
		//ColumnDefinition columnHTime = data.defineSourceColumn("hours",2,ColumnType.ordinal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",2,ColumnType.continuous);
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",3,ColumnType.continuous);		
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",4,ColumnType.continuous);	
		
		//columnMTime.defineClass(descreteMTime);
		//columnHTime.defineClass(descreteHTime);
		data.analyze();
		
		data.defineInput(columnInLon);
		data.defineInput(columnInLat);
		//data.defineInput(columnHTime);
		data.defineInput(columnMTime);
		data.defineOutput(columnOutLon);
		data.defineOutput(columnOutLat);
		data.getNormHelper().defineUnknownValue("?");
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();
		
		//data.setLeadWindowSize(1);
		//data.setLagWindowSize(3);
		
		model.holdBackValidation(0.3, true, 1001);
		model.selectTrainingType(data);
		bestMethod = (MLRegression)model.crossvalidate(5, true);
		
		
		System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
		helper = data.getNormHelper();
		System.out.println(helper.toString());
		System.out.println("Final model: " + bestMethod);

		EncogDirectoryPersistence.saveObject(new File("networkExport.eg"), bestMethod);
		
		//ReadCSV csv = new ReadCSV(new File("coords.csv"),false,format);
		//String[] line = new String[4];
		//MLData input = helper.allocateInputVector();
		
		/*while(csv.next())
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
		*/
		
		//Encog.getInstance().shutdown();
	}
	
	private void customLearning()
	{
		mqttTime = MqttTime.getInstance();
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		NNData nd = new NNData();

		//nd.importFromDB(0, 600000);

		nd.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
		nd.importFromFile();
		nd.coordCullByBox(57.34, 11, 1 , 4);
		//data.cullByRDP();
		nd.coordCullByDist();
		nd.repoint();
		//nd.coordCullBySpeed(15.0);
		nd.exportAsCoordsToCSV();
		
		
		String[] descreteMTime = numArray(60);
		String[] descreteHTime = numArray(24);
		
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		data =  new VersatileMLDataSet(source);
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
		//ColumnDefinition columnHTime = data.defineSourceColumn("hours",2,ColumnType.ordinal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",2,ColumnType.continuous);
		ColumnDefinition columnOutLon = data.defineSourceColumn("olon",3,ColumnType.continuous);		
		ColumnDefinition columnOutLat = data.defineSourceColumn("olat",4,ColumnType.continuous);	
		
		//columnMTime.defineClass(descreteMTime);
		//columnHTime.defineClass(descreteHTime);
		data.analyze();
		
		data.defineInput(columnInLon);
		data.defineInput(columnInLat);
		//data.defineInput(columnHTime);
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
		network = (new FreeformNetwork()).createElman(3, 7, 2, new ActivationTANH());
		
		/*
		FreeformLayer input = network.createInputLayer(3);
		FreeformLayer hiddenLayer = network.createLayer(7);
		FreeformLayer hiddenLayer2 = network.createLayer(7);
		FreeformLayer output = network.createOutputLayer(2);
		
		
		network.connectLayers(input, hiddenLayer, new ActivationTANH(), 1.0, false);
		network.connectLayers(input, hiddenLayer2, new ActivationSigmoid(), 0.0, false);
		network.connectLayers(hiddenLayer, hiddenLayer2, new ActivationTANH(), 0.0, false);
		//network.connectLayers(hiddenLayer, output, new ActivationSigmoid(), 1.0, false);
		
		network.connectLayers(hiddenLayer2, output, new ActivationTANH(), 1.0, false);
		*/
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
		
		//EncogDirectoryPersistence.saveObject(, network);
		
		
	}
	
	private LocPrediction(int id)
	{
		mqttTime = MqttTime.getInstance();
		predictedLoc = new Tuple<Double,Double>(0.0,0.0);
		format = new CSVFormat('.',' ');
		
		nd = new NNData();
		//nd.parseGPX("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\20160204.gpx");
		//nd.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
		//nd.exportToDB(id);
		
		int n = 600000;
		
		nd.importFromDB(id,n);
		
		nd.coordCullBySpeed(15.0);

		//if(!nd.emptyData())
		//{
		nd.exportAsClustToCSV();
			
		String[] descreteMTime = numArray(60);
		String[] descreteHTime = numArray(24);
 		String[] descreteClust = numArray(nd.getNrCluster());
		
		VersatileDataSource source = new CSVDataSource(new File("coords.csv"),false,format);
		data =  new VersatileMLDataSet(source);

		data.getNormHelper().setFormat(format); 

		ColumnDefinition columnInClust = data.defineSourceColumn("pos",0,ColumnType.nominal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",1,ColumnType.continuous);
		//ColumnDefinition columnHTime = data.defineSourceColumn("hours",1,ColumnType.ordinal);
		//ColumnDefinition columnMTime = data.defineSourceColumn("minutes",2,ColumnType.ordinal);
		ColumnDefinition columnOutClust = data.defineSourceColumn("opos",2,ColumnType.nominal);
		
		columnInClust.defineClass(descreteClust);
		//columnMTime.defineClass(descreteMTime);
		//columnHTime.defineClass(descreteHTime);
		columnOutClust.defineClass(descreteClust);
		data.getNormHelper().defineUnknownValue("?");
		data.analyze();

		data.defineInput(columnInClust);
		//data.defineInput(columnHTime);
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
		
		EncogDirectoryPersistence.saveObject(new File("networkExport.eg"), bestMethod);
		
		//}
		
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
			instanceMap.put(userID, new LocPrediction());//userID
		}
		else
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
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
	
	public Tuple<Double,Double> predict()
	{
		//ReadCSV csv = new ReadCSV(new File("coords.csv"),false,format);
		String[] line = new String[2];
		MLData input = helper.allocateInputVector();
		
		//Calendar c = Calendar.getInstance();
		int hour = mqttTime.getHour();// c.get(Calendar.HOUR_OF_DAY);
		int minute = mqttTime.getMinute(); //c.get(Calendar.MINUTE);
		
		//EncogUtility.saveEGB(new File("networkExport.eg"), data);
		//EncogUtility.explainErrorMSE(bestMethod, data);
		
		Car carData = Car.getInstance();
		
		line[0] = ""+nd.getClosestCluster(carData.getPos());
		//line[0] = ""+nd.getClosestCluster(new Tuple<Double,Double>(57.69661,11.97575));
		line[1] = ""+(hour*60+minute);
		
		helper.normalizeInputVector(line,input.getData(),false);
		MLData output = bestMethod.compute(input);
		String irisChoosen0 = helper.denormalizeOutputVectorToString(output)[0];
		StringBuilder result = new StringBuilder();
		result.append("[" + line[0]+ " ( " + nd.getViewClustPos().get(Integer.parseInt(line[0])) + ")"+ ", " + line[1]+ "] ");
		result.append(" -> predicted: ");
		result.append(irisChoosen0 + " ( " + nd.getViewClustPos().get(Integer.parseInt(irisChoosen0)) + ")");
		System.out.println(result.toString());
		return nd.getViewClustPos().get(Integer.parseInt(irisChoosen0));
	}
	public Tuple<Double,Double> predictCoord()
	{
		String[] line = new String[4];
		
		//Calendar c = Calendar.getInstance();
		int hour = mqttTime.getHour();// c.get(Calendar.HOUR_OF_DAY);
		int minute = mqttTime.getMinute(); //c.get(Calendar.MINUTE);
		
		//EncogUtility.saveEGB(new File("networkExport.eg"), data);
		//EncogUtility.explainErrorMSE(bestMethod, data);
		
		//VectorWindow window = new VectorWindow(4);
		MLData input = helper.allocateInputVector();
		
		//EncogDirectoryPersistence.saveObject(new File("networkExport.eg"), bestMethod);
		Car carData = Car.getInstance();
		
		line[0] = ""+carData.getPos().fst();
		line[1] = ""+carData.getPos().snd();
		line[2] = ""+(hour*60+minute);
		//line[3] = ""+minute;
		
		helper.normalizeInputVector(line,input.getData(),false);
		MLData output = bestMethod.compute(input);	// network.compute(input); // 
		
		
		
		String irisChoosen0 = helper.denormalizeOutputVectorToString(output)[0];
		String irisChoosen1 = helper.denormalizeOutputVectorToString(output)[1];
		StringBuilder result = new StringBuilder();
		
		
		
		result.append("[" + line[0]+ ", " + line[1] +", " + line[2] + "] ");
		result.append(" -> predicted: ");
		result.append(irisChoosen0 + ", " + irisChoosen1);
		System.out.println(result.toString());
		return new Tuple<Double,Double>(Double.parseDouble(irisChoosen0),Double.parseDouble(irisChoosen1));
	}
}
