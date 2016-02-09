package utils;

import java.util.ArrayList;

import interfaces.DatabaseLocation;

public class Utils {

	// TO BE CHANGED!
	public static Tuple<Double,Double> mean(ArrayList<DatabaseLocation> in)
	{
		double[] t1 = new double[2];
		for(DatabaseLocation i : in)
		{
			t1[0] += i.getLat();
			t1[1] += i.getLon();
		}
		t1[0] /= in.size();
		t1[1] /= in.size();
		return new Tuple<Double,Double>(t1[0],t1[1]);
	}
	
}
