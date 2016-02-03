package serverConnection;

import java.util.ArrayList;

public class DBSCAN {
	
	ArrayList<Point> points;
	ArrayList<Point> visitedPoints;
	
	
	
	public DBSCAN(double[] longs, double[] lats) throws Error
	{
		points= new ArrayList<Point>();
		visitedPoints= new ArrayList<Point>();
		
		if (longs.length != lats.length)
			throw new Error("longs and lats has to have the same length");
		
		for(int i=0;i<longs.length;i++)
			points.add(new Point(longs[i], lats[i]));
		
	}
	
	public ArrayList<String>[] getClusterd(int nClust)
	{
		ArrayList<String>[] clusters = (ArrayList<String>[])new ArrayList[nClust];
		
		
		for(int i=0;i<nClust;i++)
			clusters[i]= new ArrayList<String>();
		
		
		
		for(Point p : points)
		{
			clusters[p.getCluster()].add("("+p.getX()+" ,"+p.getY()+")");
		}
		
		for(int i=0; i<nClust; i++)
		{
			System.out.println(clusters[i].toString());
		}
		
		return clusters;
	}
	
	
	public int cluster(double epsilon, int minPoints) // O(p^2)
	{
		int c=0;
		
		for(Point p : points)			//
		{								//	Go through every untreated node ones, worst case O(p)
			if(!p.isVisited())			//
			{
				p.markAsVisited();
				ArrayList<Point> neibors = getNeibors(p, epsilon);		//O(p)
				if(neibors.size() < minPoints)
				{
					p.markAsNoise();
				}
				else
				{
					c++;
					expandCluster(p,neibors,c,epsilon,minPoints); // O(p^2) but marks as visited so dosn't interact mulltiplicativly with the for loop
				}
			}	
		}
		
		return c;
	}
	
	private void expandCluster(Point p, ArrayList<Point> neibors, int c, double epsilon, int minPoints) 
	{
		p.setCluster(c);
		for(int i=0; i<neibors.size();i++)				// O(p)
		{
			if (!neibors.get(i).isVisited())
			{
				neibors.get(i).markAsVisited();
				ArrayList<Point> neiborsOfneibors = getNeibors(neibors.get(i),epsilon); //O(p)
				if(neiborsOfneibors.size() >=minPoints)
					neibors.addAll(neiborsOfneibors);
			}
			if(neibors.get(i).getCluster()==0)
			{
				neibors.get(i).setCluster(c);
			}
		}
	}

	// O(p)
	public ArrayList<Point> getNeibors(Point point,double epsilon)
	{
		ArrayList<Point> tempP= new ArrayList<Point>();
		
		for(Point p : points)
		{
			if(Math.sqrt(Math.pow(point.getY()-p.getY(),2) + Math.pow(point.getX()-p.getX(), 2))<=epsilon)
				tempP.add(p);
		}
		return tempP;	
	}
	
	
	
	
	
	
	
	
	
}
