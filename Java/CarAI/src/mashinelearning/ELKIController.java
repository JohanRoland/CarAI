package mashinelearning;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ELKIController {
	
	public static boolean runElki(String tempName)
	{
		File f = new File(".");
		
		String currentPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
    	//File file1 = new File(currentPath+File.separator+"ELKIClusters");
		//
    	File elkiTemp = new File(currentPath+File.separator+tempName);
    	if(!elkiTemp.exists())
    	{
    		elkiTemp.mkdirs();
    	}
    	//for(File file: file1.listFiles()) file.delete();
		
		double eps = 200.0;
		int minp = 3;
		Runtime rt = Runtime.getRuntime();
		//-dbc.in "D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\testELKI.txt" -dbc.parser NumberVectorLabelParser -db.index tree.spatial.rstarvariants.rstar.RStarTreeFactory -spatial.bulkstrategy SortTileRecursiveBulkSplit -algorithm clustering.DBSCAN -algorithm.distancefunction geo.LatLngDistanceFunction -geo.model WGS84SpheroidEarthModel -dbscan.epsilon 50.0 -dbscan.minpts 10 -resulthandler ResultWriter -out "D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\ELKIClusters"
		String[] commands = {"java" ,"-jar" , "elki-bundle-0.7.1.jar","KDDCLIApplication", "-dbc.in","testELKI.txt","-dbc.parser","NumberVectorLabelParser","-db.index","tree.spatial.rstarvariants.rstar.RStarTreeFactory","-spatial.bulkstrategy","SortTileRecursiveBulkSplit","-algorithm","clustering.DBSCAN","-algorithm.distancefunction","geo.LatLngDistanceFunction","-geo.model","WGS84SpheroidEarthModel","-dbscan.epsilon",""+eps,"-dbscan.minpts",""+minp,"-resulthandler","ResultWriter","-out",tempName};
		String[] com2 = {"python","convert.py","coords.csv"};
		String err = "";
		try {
			
			System.out.println("Converting");
			Process p2 = rt.exec(com2);
			p2.waitFor();
			
			

			System.out.println("Starting Clustering");
			/*for(String s : commands)
			{
				System.out.print(s+" ");
			}
			System.out.println("");*/
			Process p = rt.exec(commands);
			InputStream stderr = p.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            
            System.out.println("<ERROR>");
            while ( (line = br.readLine()) != null)
            {
                System.out.println(line);
                err += line; 
            }
            System.out.println("</ERROR>");
			int exitVal = p.waitFor();
			System.out.println("Process exitValue: " + exitVal);
			
			
			
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return err.length() > 1;
	}
	
}

