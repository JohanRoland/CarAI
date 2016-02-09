package utils;

import java.util.ArrayList;

public class Utils {

	// TO BE CHANGED!
	public static double[] mean(ArrayList<Tuple<Double,Double>> in)
	{
		double[] t1 = new double[2];
		for(Tuple<Double,Double> i : in)
		{
			t1[0] += i.fst();
			t1[1] += i.snd();
		}
		t1[0] /= in.size();
		t1[1] /= in.size();
		return t1;
	}
	
}
