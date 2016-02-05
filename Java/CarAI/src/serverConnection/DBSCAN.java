package serverConnection;

import java.lang.ref.Reference;
import java.util.ArrayList;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.*;
import rx.internal.*;
import rx.Observable;
import rx.functions.Func1;

public class DBSCAN {
	
	int c=0;
    RTree<PointInSpace, Geometry> points;
    Observable<Entry<PointInSpace, Geometry>> neibors;
    volatile ArrayList<Tupple<Float>>[] clusters;

    //RTree<String, Point> tree;
	
	
	public DBSCAN(ArrayList<Float> longs, ArrayList<Float> lats) throws Error
	{
		//tree =  RTree.create();
		
		points= RTree.create();//new ArrayList<PointInSpace>();
		
		
		if (longs.size() != lats.size())
			throw new Error("longs and lats has to have the same length");
		
		for(int i=0;i<longs.size();i++)
			points = points.add(new PointInSpace(longs.get(i), lats.get(i)), Geometries.point((float)longs.get(i), (float)lats.get(i)));//points.add(new PointInSpace(longs.get(i), lats.get(i)));

	}
	
	public ArrayList<Tupple<Float>>[] getClusterd(int nClust)
	{
		nClust++;
		clusters = (ArrayList<Tupple<Float>>[])new ArrayList[nClust];
		
		
		for(int i=0;i<nClust;i++)
			clusters[i]= new ArrayList<Tupple<Float>>();
		
		
		points.entries().filter(e-> e.value().getCluster()!=0).forEach(a->clusters[a.value().getCluster()].
								add(new Tupple<Float>(a.value().getX(), a.value().getY())));
		
		
		/*{
			clusters[p.getCluster()].add(new Tupple<Float>(p.getX(), p.getY()));
		}*/
		return clusters;
	}
	
	
	public int cluster(double epsilon, int minPoints) // O(p^2)
	{
		
		
		
		points.
			entries().
				map(
					e -> 
						{return clusterHelper(e.value(),epsilon,minPoints);}
				).toBlocking().last();

		
	
		
		return c;
	}
	private int clusterHelper(PointInSpace e, double epsilon,int minPoints)
	{
		if(!e.isVisited())			//
		{
			e.markAsVisited();
			neibors = points.search(Geometries.circle(e.getX(), e.getY(), epsilon));//(e, epsilon);		//O(p)
			if(neibors.count().toBlocking().last() < minPoints)
			{
				e.markAsNoise();
			}
			else
			{
				c++;
				expandCluster(e,epsilon,minPoints); // O(p^2) but marks as visited so dosn't interact mulltiplicativly with the for loop
			}
		}
	
	return c;
	}
	
	private void expandCluster(PointInSpace p, double epsilon, int minPoints) 
	{
		p.setCluster(c);
		neibors.forEach(
					e-> 
						{
							expandClusterHelper(e.value(), epsilon,minPoints);
						}
						);
	}
	private void expandClusterHelper(PointInSpace neibor, double epsilon, int minPoints)
	{
		if (!neibor.isVisited())
		{
			neibor.markAsVisited();
			Observable<Entry<PointInSpace, Geometry>> neiborsOfneibors = points.search(Geometries.circle(neibor.getX(), neibor.getY(), epsilon));//getNeibors(neibors.get(i),epsilon); //O(p)
			if(neiborsOfneibors.count().toBlocking().last() >=minPoints)
				neibors.mergeWith(neiborsOfneibors);
		}
		if(neibor.getCluster()==0)
		{
			neibor.setCluster(c);
		}
	}

/*
	// O(p)
	public ArrayList<PointInSpace> getNeibors(PointInSpace point,double epsilon)
	{
		ArrayList<PointInSpace> tempP= new ArrayList<PointInSpace>();
		
		points.
		entries().
			map(
				e ->
					{
						if(distFrom(point.getY(),point.getX(),e.value().getY(),e.value().getX())<=epsilon)
							tempP.add(e.value());
					});
		return tempP;	
	}
*/
	
	 public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
		    double earthRadius = 6371000; //meters
		    double dLat = Math.toRadians(lat2-lat1);
		    double dLng = Math.toRadians(lng2-lng1);
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		               Math.sin(dLng/2) * Math.sin(dLng/2);
		    double c1 = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    float dist = (float) (earthRadius * c1);

		    return dist;
		    }
	
	
	
	
	
	
	public class Tupple<T>
	{
		T a;
		T b;
		
		public Tupple(T first, T second)
		{
			a = first;
			b = second;
			
		}
		public String toString(){return "("+a.toString()+","+b.toString()+")";}
		public T fst(){return a;}
		public T snd(){return b;}
		public void setFst(T in){a=in;}
		public void setSnd(T in){b=in;}
		
	}
	
}
