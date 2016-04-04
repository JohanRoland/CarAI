package forReport;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

public class Analyze {

	static private HashMap<Integer,ArrayList<Integer>> testData;
	
	static public void analyzeLearningData()
	{
		testData = new HashMap<Integer,ArrayList<Integer>>();
		
		try{
			Stream<String> sss = Files.lines(Paths.get("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\coords.csv"));
			
			sss.forEach(ss -> {
				String[] s  =ss.split(" ");
				if(!testData.containsKey(Integer.parseInt(s[0])))
				{
					testData.put(Integer.parseInt(s[0]), new ArrayList<Integer>());
				}
				testData.get(Integer.parseInt(s[0])).add(Integer.parseInt(s[3]));
			});
			
			HashSet<Integer> checked; 
			for(Integer i :testData.keySet())
			{
				checked = new HashSet<Integer>();
				double size = (double)testData.get(i).size();
				for(Integer a : testData.get(i))
				{
					if(!checked.contains(a))
					{
						double amount = (double)Collections.frequency(testData.get(i), a);
						
						System.out.println(i+ " -> " + a + " : " + amount/size);
						//testData.get(i).removeAll(Collections.singleton(a));
						checked.add(a);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}
