package Result;

public class Rule 
{
	private String[][] base;
	private String[][] implies;

	
	
	public Rule(String[][] InBase, String[][] goal)
	{
		base =    InBase;
		implies = goal;
	}
	String[][] apply(String[][] stm)
	{
		for (String[] parts: stm)
			if(apply(parts) !=null)
				return implies;
		return null;
	}
	
	
	String[][] apply(String[] stm)
	{
		for(String[] part : base)
		{
			if (part == stm)
				return implies;	
		}
		return null;
	}
}

