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
			t1[0] += i.getLon();
			t1[1] += i.getLat();
		}
		t1[0] /= in.size();
		t1[1] /= in.size();
		return new Tuple<Double,Double>(Math.floor(t1[0]*100000)/100000,Math.floor(t1[1]*100000)/100000);
	}


	 public static double distFrom(double lat1, double lng1, double lat2, double lng2) 
	 {
		    double earthRadius = 6371000; //meters
		    double dLat = Math.toRadians(lat2-lat1);
		    double dLng = Math.toRadians(lng2-lng1);
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		               Math.sin(dLng/2) * Math.sin(dLng/2);
		    double c1 = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    double dist = (double) (earthRadius * c1);

		    return dist;
	}
	
}
