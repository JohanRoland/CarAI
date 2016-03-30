package mashinelearning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import interfaces.DatabaseLocation;
import utils.Tuple;

public class PYDBSCAN {
	
	
	Runtime rt;
	
	public PYDBSCAN()
	{
		
		
	}
	
	public ArrayList<ArrayList<DatabaseLocation>> runDBSCAN(ArrayList<DatabaseLocation> querry,double eps, int minClust,int sample)
	{
		int amountofClusts = -1;
		rt = Runtime.getRuntime();
		
		String[] commands = {"python" , "dbscan.py", ""+eps,""+minClust,""+sample};
		
		ArrayList<String> sb =new ArrayList<String>();
		try {
			Process p = rt.exec(commands);
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String s = null;
			
			while((s = stdInput.readLine()) != null)
			{
				sb.add(s);
			}
			
			while ((s = stdError.readLine()) != null) {
			    System.out.println(s);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		ArrayList<ArrayList<DatabaseLocation>> output = new ArrayList<ArrayList<DatabaseLocation>>(); 
		HashMap<Tuple<Double,Double>, Integer> clusterMap = new HashMap<Tuple<Double,Double>, Integer>();
		
		for(String ss : sb)
		{
				String[] s = ss.split(" "); 
				if(s.length == 3)
				{
					int c = Integer.parseInt(s[0])+1;
					clusterMap.put(new Tuple<Double,Double>(Double.parseDouble(s[1]),Double.parseDouble(s[2])), c);
					if(c > amountofClusts)
					{
						amountofClusts = c; 
					}
				}
		}
		
		
		for(int i = 0 ; i <= amountofClusts; i++)
		{
			output.add(new ArrayList<DatabaseLocation>());
		}
		
		for(DatabaseLocation dl : querry)
		{
			int clustId = clusterMap.get(new Tuple<Double,Double>(dl.getLon(),dl.getLat()));
			output.get(clustId).add(dl);
		}
		
		return output;
		
	}

}
