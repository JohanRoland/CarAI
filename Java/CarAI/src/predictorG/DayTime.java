package predictorG;
import java.util.ArrayList;
import utils.Tuple;
import java.lang.Double;


	public class DayTime
	{
		private double time;
		private int day;
		private int month;
		
		
		double getTime(){return time;}
		
		DayTime(double in, int d,int m)
		{
			time=in;
			day=d;
			month=m;
		}
		
		int getDay(){return day;}
		public int getMonth() {return month;}
		
		public Double relativeDistanceT(DayTime input) {
		double temp = Math.abs(input.getTime()-time)/20;	// Hopefully abut 30 tolerance
		if(temp<(Math.PI/2))
			return Math.cos(temp);
		else
			return 0.0;
		}
		public Double relativeDistanceD(DayTime input) {
			double temp = Math.abs(input.getDay()-day)/20;	// Hopefully abut 30 tolerance
			if(temp<(Math.PI/2))
				return Math.cos(temp);
			else
				return 0.0;
			}
		public Double relativeDistanceM(DayTime input) {
			double temp = Math.abs(input.getMonth()-month)/20;	// Hopefully abut 30 tolerance
			if(temp<(Math.PI/2))
				return Math.cos(temp);
			else
				return 0.0;
			}

		
		
	}
