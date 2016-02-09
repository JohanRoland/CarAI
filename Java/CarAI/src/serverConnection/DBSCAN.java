package serverConnection;
import java.util.ArrayList;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.*;

import interfaces.DatabaseLocation;
import rx.Observable;
import utils.Tuple;
/**************************************************************
 * DBSCAN is a clustering algorithm that is noise resistant, 
 * By adjusting the cluster threshold one can adjust what is
 * considered noise. So for example if the cluster threshold 
 * is 5 only objects surrounded by 4 objects are allowed to
 * "recruit" objects into the cluster.
 **************************************************************/
public class DBSCAN {
	
	int c=0;
    RTree<Tuple<PointInSpace,Object>, Geometry> points;
    Observable<Entry<Tuple<PointInSpace, Object>, Geometry>> neibors;
    volatile ArrayList<Tuple<Tuple<Double,Double>, Object>>[] clusters;
	
    /**
     * @param longs	An ArrayList of Double that details the longitudes.
     * @param lats An ArrayList of Double that details the latitudes.
     * @param star If set to true an R*Tree will be used instead of a RTree, gives better search at the cost of more expensive creation and inserts. 
     * @throws Error This error is thrown if the ArrayLists have different length
     *  or one of them is zero.
     * 
     * The DBSCAN constructor constructs an R*Tree from points detailed in the
     * two lists of coordinates, longs for longitude and lats for latitudes.
     * The coordinates are indexed based and both lists must be the same length
     * otherwise an generic Error it thrown "longs and lats has to have the same
     * length".
     * The time complexity is O(n^2* log n) for R*Tree and O(n^2) for RTree
     */
	public DBSCAN(ArrayList<Double> longs, ArrayList<Double> lats,ArrayList<Object> payload, boolean star) throws Error
	{
		if(star)
			points= RTree.star().create();
		else
			points= RTree.create();
		
		if(longs.size()==0 || lats.size()==0 || payload.size()==0)
		{
			throw new Error("one or more of the imputted arrays have zero inputs");
		}
		else if (longs.size() != lats.size())
		{
			throw new Error("longs and lats has to have the same length");
		}
		
			
		for(int i=0;i<longs.size();i++)
			points = points.add(new Tuple<PointInSpace,Object>(new PointInSpace(longs.get(i), lats.get(i)), payload.get(i)), Geometries.point(longs.get(i), lats.get(i)));

	}
	
	public DBSCAN(ArrayList<DatabaseLocation> input, boolean star) throws Error
	{ 	
		if(star)
			points= RTree.star().create();
		else
			points= RTree.create();
		
		if(input.size()==0)
		{
			throw new Error("the imputted arrays have zero inputs");
		}
		
			
		for(int i=0;i<input.size();i++)
			points = points.add(new Tuple<PointInSpace,Object>(new PointInSpace(input.get(i).getLon(), input.get(i).getLat()), input.get(i)), Geometries.point(input.get(i).getLon(), input.get(i).getLat()));

	}
	/**
	 * @param longs	An ArrayList of Double that details the longitudes.
     * @param lats An ArrayList of Double that details the latitudes.
     * @throws Error This error is thrown if the ArrayLists have different length
     *  or one of them is zero.
     *  Inserts points given by longs and lats
	 */
	public void addEntries(ArrayList<Double> longs, ArrayList<Double> lats,ArrayList<Object> payload)
	{
		if(longs.size()==0 || lats.size()==0)
		{
			throw new Error("one or more of the imputted arrays have zero inputs");
		}
		else if (longs.size() != lats.size())
		{
			throw new Error("longs and lats has to have the same length");
		}
		
		for(int i=0;i<longs.size();i++)
			points = points.add(new Tuple<PointInSpace,Object>(new PointInSpace(longs.get(i), lats.get(i)), payload.get(i)), Geometries.point(longs.get(i), lats.get(i)));
	}

	/**
	 * @param IncludeUnclusterd If set to True it will return the unclustered "noise points" as cluster number zero" 
	 * @return Returns an Array of ArrayList of Tuples of Doubles where each ArrayList is a cluster thats made upp of points described by Tuples.
	 * 
	 * getClusterd Is the way which one gets information from the DBSCAN class.
	 * This method goes through each entry ones so it has a ordo O(n).
	 */
	public ArrayList<Tuple<Tuple<Double, Double>, Object>>[] getClusterd(boolean IncludeUnclusterd)
	{
		
		int nClust, OneOrZero;
		
		if(IncludeUnclusterd)
		{
			OneOrZero=0;
			nClust=c+1;
		}
		else
		{
			OneOrZero=1;
		    nClust=c;	
		}
		
		clusters = (ArrayList<Tuple<Tuple<Double,Double>, Object>>[])new ArrayList[nClust];
		
		for(int i=0;i<nClust;i++)
			clusters[i]= new ArrayList<Tuple<Tuple<Double,Double>, Object>>();

		if(IncludeUnclusterd)
		{
			points.entries().forEach(a->clusters[a.value().fst().getCluster()-OneOrZero].
					add(new Tuple(new Tuple<Double,Double>( a.value().fst().getX() , a.value().fst().getY() ), a.value().snd())));
		}
		else
		{
			points.entries().filter(e-> e.value().fst().getCluster()!=0).forEach(a->clusters[a.value().fst().getCluster()-OneOrZero].
					add(new Tuple(new Tuple<Double,Double>( a.value().fst().getX() , a.value().fst().getY() ), a.value().snd())));
			}
		
		return clusters;
	}
	
	/**
	 * @param epsilon Details the maximum distance between points that are to be considerd naibors
	 * @param minPoints Details the minimum number of neighbors a point has to have for it to be able to add points to the cluster.
	 * @return returns the number of clusters found.
	 * 
	 * cluster clusters the points stored in the DBSCAN class with the parameters given.
	 * The worst time complexity is bas at O(n^2) but this only happens if all points are within
	 * epsilon of eachother witch should not happen on larger trees. so the expected time complexity is closer to
	 * between O(n^2) and O(n).
	 * 
	 */
	public int cluster(double epsilon, int minPoints) // O(p^2)
	{
		points.
			entries().
				map(
					e -> 
						{return clusterHelper(e.value().fst(),epsilon,minPoints);}
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
							expandClusterHelper(e.value().fst(), epsilon,minPoints);
						}
						);
	}
	private void expandClusterHelper(PointInSpace neibor, double epsilon, int minPoints)
	{
		if (!neibor.isVisited())
		{
			neibor.markAsVisited();
			Observable<Entry<Tuple<PointInSpace, Object>, Geometry>> neiborsOfneibors = points.search(Geometries.circle(neibor.getX(), neibor.getY(), epsilon));//getNeibors(neibors.get(i),epsilon); //O(p)
			if(neiborsOfneibors.count().toBlocking().last() >=minPoints)
				neibors.mergeWith(neiborsOfneibors);
		}
		if(neibor.getCluster()==0)
		{
			neibor.setCluster(c);
		}
	}


	 public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
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
