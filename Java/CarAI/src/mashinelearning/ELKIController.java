package mashinelearning;

import java.io.IOException;

public class ELKIController {
	
	public static void runElki()
	{
		double eps = 50.0;
		int minp = 10;
		Runtime rt = Runtime.getRuntime();
		//-dbc.in "D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\testELKI.txt" -dbc.parser NumberVectorLabelParser -db.index tree.spatial.rstarvariants.rstar.RStarTreeFactory -spatial.bulkstrategy SortTileRecursiveBulkSplit -algorithm clustering.DBSCAN -algorithm.distancefunction geo.LatLngDistanceFunction -geo.model WGS84SpheroidEarthModel -dbscan.epsilon 50.0 -dbscan.minpts 10 -resulthandler ResultWriter -out "D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\ELKIClusters"
		String[] commands = {"java" ,"-jar" , "elki-bundle-0.7.1.jar","KDDCLIApplication", "-dbc.in","\"D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\testELKI.txt\"","-dbc.parser","NumberVectorLabelParser","-db.index","tree.spatial.rstarvariants.rstar.RStarTreeFactory","-spatial.bulkstrategy","SortTileRecursiveBulkSplit","-algorithm","clustering.DBSCAN","-algorithm.distancefunction","geo.LatLngDistanceFunction","-geo.model","WGS84SpheroidEarthModel","-dbscan.epsilon",""+eps,"-dbscan.minpts",""+minp,"-resulthandler","ResultWriter","-out","\"D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\ELKIClusters\""};
		String[] com2 = {"python","convert.py"};
		
		try {
			
			System.out.println("Converting");
			Process p2 = rt.exec(com2);
			p2.waitFor();
			
			System.out.println("Starting Clustering");
			Process p = rt.exec(commands);
			p.waitFor();
			
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}

