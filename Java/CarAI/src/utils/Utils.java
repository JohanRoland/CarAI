package utils;

import java.util.ArrayList;

import interfaces.DatabaseLocation;
import predictorG.DayTime;

public class Utils {

	public class ConnectionData
	{
		private DayTime dayTime;

		public DayTime getDayTime() {
			return dayTime;
		}

		public void setDayTime(DayTime dayTime) {
			this.dayTime = dayTime;
		}
		
	}
	
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
		return new Tuple<Double,Double>(Math.floor(t1[0]*100000)/100000,Math.floor(t1[1]*100000)/100000);
	}
	
	
	public static double distDB(DatabaseLocation d)
	{
		return distFrom(d.getLat(),d.getLon(),d.getNLat(),d.getNLon());
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
	/**
	 * @param in an Arraylist containing Database Locations 
	 * @return A Tuple of Tuples containing the min x y tuple and max x y tuple as it's first and second argument respectively
	 */
	public static Tuple<Tuple<Double,Double>,Tuple<Double,Double>> getGPSPlotFrame(ArrayList<ArrayList<DatabaseLocation>> in)
	{
		double maxY=Double.MIN_VALUE;
		double maxX=Double.MIN_VALUE;
		double minY=Double.MAX_VALUE;
		double minX=Double.MAX_VALUE;
		
		
		for(int i=0; i<in.size();i++)
		{
			ArrayList<DatabaseLocation> dl = in.get(i);
			if(i!=0)
			{
				for(DatabaseLocation d : dl)
				{
					if(maxY < d.getLat())
					
						maxY=d.getLat();
						
					if(minY > d.getLat())
						minY = d.getLat();
					
					if(maxX < d.getLon())
						maxX = d.getLon();
					
					if(minX > d.getLon())
						minX = d.getLon();
				}
			}
		}
		
		
		return new Tuple<Tuple<Double,Double>,Tuple<Double,Double>>(new Tuple<Double,Double>(minX,minY),new Tuple<Double,Double>(maxX,maxY));
	}
	 
	 
	 
}
