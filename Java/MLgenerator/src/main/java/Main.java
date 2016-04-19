import java.io.File;

import org.encog.ConsoleStatusReportable;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {

	public static void main(String[] args) 
	{
		CSVFormat format = new CSVFormat('.',' ');	
		VersatileDataSource source = new CSVDataSource(new File(args[1]),false,format);
		VersatileMLDataSet data = new VersatileMLDataSet(source);
		EncogModel model=null;
		if(args[0].toLowerCase()==null)
		{
			throw new Error("no Args");
		}
		if(args[0].toLowerCase().equals("clusttoclust"))
		{
			model = clustToClust(data, format);
		}
		else if(args[0].toLowerCase().equals("pointtopoint"))
		{
			model = pointToPoint(data, format);
		}
		else if(args[0].toLowerCase().equals("--help") || 
				args[0].toLowerCase().equals("-help")  ||
				args[0].toLowerCase().equals("help")     )
		{
			System.out.println(" X Y Z\n"
							 + "X sets the type of ML network X: clusterToCluster, pointToPoint.\n"
							 + "Y sets the input file\n"
							 + "Z sets the output file");
		}else
		{
			throw new Error("Invalid mode");
		}
		MLRegression bestMethod = (MLRegression)model.crossvalidate(5, true);
		
		NormalizationHelper helper = data.getNormHelper();

		String[] line = new String[2];
		MLData input = helper.allocateInputVector();
		

		EncogDirectoryPersistence.saveObject(new File(args[2]), bestMethod);
		
		/*
		System.out.println(helper.toString());
		System.out.println("Final model: " + bestMethod);
		String oldRes="";
		String res = "";

		for(int node=1; node<=7;node++)
		{
			System.out.println("Scanning node " + node);
			System.out.println("---------------------");
			
			for(int time=0; time<1440 ; time++)
			{
				 String temp;
					
				 line[0] =""+ node;
				 line[1] =""+time;
				 
				 bestMethod.compute(input);
				
				 helper.normalizeInputVector(line,input.getData(),false);
				 MLData output = bestMethod.compute(input);
				 res = helper.denormalizeOutputVectorToString(output)[0];
				 
				 //System.out.print(res+ " AtTime: " + time);
				 if(!res.equals(oldRes))
				 {
					 System.out.println(oldRes+" "+time+ " MLD: "+ output.getData().toString());
					 oldRes=res;
				 }
				 
				 
			}
			System.out.println(res+" "+1440);
			
			
		}
		

		final String EXIT_COMMAND = "exit";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    System.out.println("Enter some text, or '" + EXIT_COMMAND + "' to quit");
		try {
		    while (true)
		    {
	
		         System.out.print("> ");
		         String input2;
					input2 = br.readLine();
				
		         System.out.println(input2);
		
		         if (input2.length() == EXIT_COMMAND.length() && input2.toLowerCase().equals(EXIT_COMMAND))
		         {
		            System.out.println("Exiting.");
		            return;
		         }
				 String temp;
				
				 temp = input2;
				 line = temp.split(" ");
				 bestMethod.compute(input);
				
				 helper.normalizeInputVector(line,input.getData(),false);
				 MLData output = bestMethod.compute(input);
				 String res = helper.denormalizeOutputVectorToString(output)[0];
				 System.out.print(res);
	
		     }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	   }
	public static EncogModel pointToPoint(VersatileMLDataSet data, CSVFormat format)
	{
		
		data.getNormHelper().setFormat(format); 
		ColumnDefinition columnInLon = data.defineSourceColumn("ilon",0,ColumnType.continuous);		
		ColumnDefinition columnInLat = data.defineSourceColumn("ilat",1,ColumnType.continuous);		
		ColumnDefinition columnDay = data.defineSourceColumn("day",2,ColumnType.nominal);
		ColumnDefinition columnMTime = data.defineSourceColumn("time",3,ColumnType.continuous);
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

		
		
		return model;
	}
	public static EncogModel clustToClust(VersatileMLDataSet data,	CSVFormat format)
	{	
		data.getNormHelper().setFormat(format); 

		ColumnDefinition columnInClust = data.defineSourceColumn("pos",0,ColumnType.nominal);
		ColumnDefinition columnMTime = data.defineSourceColumn("minutes",1,ColumnType.continuous);
		ColumnDefinition columnDay = data.defineSourceColumn("day",2,ColumnType.continuous);
		ColumnDefinition columnOutClust = data.defineSourceColumn("opos",3,ColumnType.nominal);
		
		data.getNormHelper().defineUnknownValue("?");
		data.analyze();
		data.defineInput(columnInClust);
		data.defineInput(columnMTime);
		data.defineInput(columnDay);
		data.defineOutput(columnOutClust);
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();
		
		model.holdBackValidation(0.3, true, 1001);
		model.selectTrainingType(data);
		
		
		
		return model;
	}
 
	
	
}
