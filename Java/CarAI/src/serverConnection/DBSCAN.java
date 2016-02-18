package serverConnection;
import java.util.ArrayList;
import java.util.List;

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
    RTree<PointInSpace, Geometry> points;
    Observable<Entry<PointInSpace, Geometry>> neibors;
    ArrayList<ArrayList<DatabaseLocation>> clusters;
	
    /**
     * @param input A ArrayList of a Class object that extends DatabaseLocation such that it has long and lat coordinates, it can have any number of other parameters.
     * @param star If set to true an R*Tree will be used instead of a RTree, gives better search at the cost of more expensive creation and inserts. 
     * @throws Error This error is thrown if the ArrayLists have different length
     *  or one of them is zero.
     * 
     * The DBSCAN constructor constructs an RTree or R*Tree from points detailed in the
     * classes that extends DatabaseLocation.
     * If star is set to True it will be a R*Tree, if it is set to false it will be a RTree 
     * The time complexity is O(n^2* log n) for R*Tree and O(n^2) for RTree
     */

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
			points = points.add(new PointInSpace(input.get(i)), Geometries.point(input.get(i).getLon(), input.get(i).getLat()));
		int jasdf = 0;
		jasdf++;
	}
	/**
	 * @param longs	An ArrayList of Double that details the longitudes.
     * @param lats An ArrayList of Double that details the latitudes.
     * @throws Error This error is thrown if the ArrayLists have different length
     *  or one of them is zero.
     *  Inserts points given by longs and lats
	 */
	public void addEntries(ArrayList<DatabaseLocation> input)
	{
		if(input.size()==0)
		{
			throw new Error("the imputted arrays have zero inputs");
		}
		
			
		for(int i=0;i<input.size();i++)
			points = points.add(new PointInSpace(input.get(i)), Geometries.point(input.get(i).getLon(), input.get(i).getLat()));

	}
	/**
	 * @param IncludeUnclusterd If set to True it will return the unclustered "noise points" as cluster number zero" 
	 * @return Returns an Array of ArrayList DatabaseLocation that contains among other things a point in (long,lat).
	 * 
	 * getClusterd Is the way which one gets information from the DBSCAN class.
	 * This method goes through each entry ones so it has a O(n).
	 */
	public ArrayList<ArrayList<DatabaseLocation>> getClusterd(boolean IncludeUnclusterd)
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
		
		clusters = new ArrayList<ArrayList<DatabaseLocation>>();
		
		for(int i=0;i<nClust;i++)
			clusters.add(new ArrayList<DatabaseLocation>());

		if(IncludeUnclusterd)
		{
			points.entries().forEach(a->clusters.get(a.value().getCluster()-OneOrZero).
					add(a.value().getDLLoc()));
		}
		else
		{
			points.entries().filter(e-> e.value().getCluster()!=0).forEach(a->clusters.get(a.value().getCluster()-OneOrZero).
					add(a.value().getDLLoc()));
		
		}
		
		return clusters;
	}

	public int associateCluster(Tuple<Double,Double> point,double rad)
	{
		Observable<Entry<PointInSpace, Geometry>> res = points.search(Geometries.circle(point.fst(), point.snd(), rad));
		
		ArrayList<Integer> clustC = new ArrayList<Integer>();
		for(int i2=0;i2<(c+1);i2++)
			clustC.add(0);
		
		
		if(!res.isEmpty().toBlocking().last())
			res.forEach(g -> clustC.set(g.value().getCluster(), clustC.get(g.value().getCluster())+1));
		
		
		
		return gG(clustC);
		
	}
	private int gG(ArrayList<Integer> in)
	{
		int temp=0,index=0;
		for(int i=1; i<in.size();i++)
			if(in.get(i)>temp)
			{
				temp=in.get(i);
				index=i;
			}
		
		return index;
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
			entries().forEach(
					e -> 
						clusterHelper(e.value(),epsilon,minPoints)
				);

		
	
		
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

}
