package mashinelearning;

import java.io.File;
import java.io.IOException;

public class ELKIController {
	
	public static void runElki()
	{
		File f = new File(".");
		
		String currentPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
    	
		File file1 = new File(currentPath+File.separator+"ELKIClusters");
    	
    	for(File file: file1.listFiles()) file.delete();
		
		double eps = 200.0;
		int minp = 3;
		Runtime rt = Runtime.getRuntime();
		//-dbc.in "D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\testELKI.txt" -dbc.parser NumberVectorLabelParser -db.index tree.spatial.rstarvariants.rstar.RStarTreeFactory -spatial.bulkstrategy SortTileRecursiveBulkSplit -algorithm clustering.DBSCAN -algorithm.distancefunction geo.LatLngDistanceFunction -geo.model WGS84SpheroidEarthModel -dbscan.epsilon 50.0 -dbscan.minpts 10 -resulthandler ResultWriter -out "D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\ELKIClusters"
		String[] commands = {"java" ,"-jar" , "elki-bundle-0.7.1.jar","KDDCLIApplication", "-dbc.in \"" +currentPath+ File.separator+"testELKI.txt\"","-dbc.parser NumberVectorLabelParser","-db.index tree.spatial.rstarvariants.rstar.RStarTreeFactory","-spatial.bulkstrategy SortTileRecursiveBulkSplit","-algorithm clustering.DBSCAN","-algorithm.distancefunction geo.LatLngDistanceFunction","-geo.model WGS84SpheroidEarthModel","-dbscan.epsilon "+eps,"-dbscan.minpts "+minp,"-resulthandler ResultWriter","-out \""+currentPath + File.separator+"ELKIClusters\""};
		String[] com2 = {"python","convert.py","coords.csv"};
		
		try {
			
			System.out.println("Converting");
			Process p2 = rt.exec(com2);
			p2.waitFor();
			
			

			System.out.println("Starting Clustering");
			for(String s : commands)
			{
				System.out.print(s+" ");
			}
			System.out.println("");
			Process p = rt.exec(commands);
			p.waitFor();
			
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}

