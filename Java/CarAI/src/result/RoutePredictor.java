package result;

import java.util.ArrayList;
import java.util.Collections;
import car.Car;
import user.User;
import utils.Tuple;

public class RoutePredictor {
	Car car;
	
	public RoutePredictor()
	{
		car = Car.getInstance();
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<Integer>> RoutePredict(ArrayList<Double> apointments) throws Exception
	{
		//ArrayList<User> users = car.getAllUsers();
		ArrayList<Tuple<Double,Double>> predictedLocs = new ArrayList<Tuple<Double,Double>>();
		ArrayList<ArrayList<Double>> predictedETA = new ArrayList<ArrayList<Double>>();
		//ArrayList<Tuple<Tuple<Double,Double>, Double>> apointments = new ArrayList<Tuple<Tuple<Double,Double>, Double>>();
		//Double driverAppointment=Double.MAX_VALUE;
		
		for(int i=0; i<predictedLocs.size();i++)
		{
			predictedETA.add(new ArrayList<Double>());
			for(int j=0; j<predictedLocs.size();j++)
			{
				if(i!=j)
				{
					Double eTA = null; // calculate the ETA between predictedLocs.get(i) and predictedLocs.get(j)
					predictedETA.get(i).add(eTA);
				}
			}
		}
		
		ArrayList<PathInfo> allPossiblePaths = new ArrayList<PathInfo>();
		ArrayList<Integer> temp;
		for(int i=0; i<4;i++)
		{
			temp = new ArrayList<Integer>();
			temp.add(0);
			temp.add(i);
			temp.add(4);
			allPossiblePaths.add(new PathInfo(temp,predictedETA));
			for(int j=0;i<4;j++)
			{
				if(i!=j)
				{
					temp = new ArrayList<Integer>();
					temp.add(0);
					temp.add(i);
					temp.add(j);
					temp.add(4);
					allPossiblePaths.add(new PathInfo(temp,predictedETA));
					for(int k=0; k<4; k++)
					{
						if(j!=k)
						{
							temp = new ArrayList<Integer>();	
							temp.add(0);
							temp.add(i);
							temp.add(j);
							temp.add(k);
							temp.add(4);
							allPossiblePaths.add(new PathInfo(temp,predictedETA));
						}
					}
				}
			}
		}

		
		for(PathInfo app : allPossiblePaths)
		{
			app.CalcProp(apointments);
			
		}
		
		

		Collections.sort(allPossiblePaths);
		
		ArrayList<ArrayList<Integer>> out = new ArrayList<ArrayList<Integer>>();
		for(PathInfo app : allPossiblePaths)
		{
			out.add(app.getPath());
		}
		
		return out;//allPossiblePaths; //FIX THIS SHIT *********************'''''''''*************************
	}
	@SuppressWarnings("rawtypes")
	private class PathInfo implements Comparable
	{
		ArrayList<Integer> path;
		ArrayList<ArrayList<Double>> predictedETA = new ArrayList<ArrayList<Double>>();
		boolean[] inTime={false,false,false,false};
		int inTimeApointments=0;
		double accTime=0;		
		
		PathInfo(ArrayList<Integer> path, ArrayList<ArrayList<Double>> predictedETA)
		{
			this.path=path;
			this.predictedETA=predictedETA;
		}
		public void CalcProp(ArrayList<Double> apointments)
		{		
			for(int i=1; i < path.size(); i++)
			{
				Integer previus = path.get(i-1);
				Integer current = path.get(i);
				accTime=+predictedETA.get(previus).get(current);
				
				if(apointments.get(current-1)>=accTime)
				{
					inTimeApointments++;
					inTime[current]=true;
				}
			}	
		}
		public double getTime(){ return accTime;}
		@SuppressWarnings("unused")
		public boolean[] getInTime(){return inTime;}
		public int getIinTimeApointments(){return inTimeApointments;}
		public int getNummberOfVisitedNodes(){return (path.size()-1);}
		ArrayList<Integer> getPath(){return path;}
		

	 	public boolean equals(Object obj)
	 	{
	 		if(compareTo((PathInfo)obj)==0)
	 		{
	 			return true;
	 		}
	 		else
	 		{
	 			return false;
	 		}
		}

	 	public String toString()
	 	{
	 		return path.toString();
	 	}
		@Override
		public int compareTo(Object arg0) {
			
			PathInfo a = (PathInfo)arg0;
			
			if(a.getIinTimeApointments()<this.getIinTimeApointments())
			{
				return -1;
			}
			else if(a.getIinTimeApointments()<this.getIinTimeApointments())
			{
				return 1;
			}
			else
			{
				if(a.getNummberOfVisitedNodes()<this.getNummberOfVisitedNodes())
				{
					return -1;
				}
				else if(a.getNummberOfVisitedNodes()>this.getNummberOfVisitedNodes())
				{
					return 1;
				}
				else
				{
					if(a.getTime()<this.getTime())
					{
						return 1;
					}
					else if (a.getTime()>this.getTime())
					{
						return -1;
					}
					else
					{
						return 0;
					}
				}
			}
			

		}
	 	
		
	}
	
}
