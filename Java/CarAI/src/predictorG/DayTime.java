package predictorG;
import java.util.ArrayList;
import utils.Tuple;
import java.lang.Double;


	public class DayTime
	{
		private int time;
		private int day;
		private int month;
		
		
		int getTime(){return time;}
		
		DayTime(int in, int d,int m)
		{
			time=in;
			day=d;
			month=m;
		}
		
		int getDay(){return day;}
		public int getMonth() {return month;}
		
		public Double relativeDistanceT(DayTime input) {
		double temp = Math.abs(input.getTime()-time);	// Hopefully abut 30 tolerance
		//if(temp<500.0)
			return 1/(temp+1); //Math.cos(temp);
		//else
			//return 0.0;
		}
		public Double relativeDistanceD(DayTime input) {
			double temp = Math.abs(input.getDay()-day);	// Hopefully abut 30 tolerance
			//if(temp<(Math.PI/2))
				return 1/(Math.pow(temp/30, 3)+1);//Math.cos(temp);
			//else
				//return 0.0;
			}
		public Double relativeDistanceM(DayTime input) {
			double temp = Math.abs(input.getMonth()-month);	// Hopefully abut 30 tolerance
			//if(temp<(Math.PI/2))
				return 1/(temp+1);//Math.cos(temp);
			//else
				//return 0.0;
			}

		
		
	}
