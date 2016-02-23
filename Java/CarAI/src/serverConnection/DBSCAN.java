package serverConnection;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.*;

import interfaces.DatabaseLocation;
import rx.Observable;
import rx.observables.BlockingObservable;
import rx.Observable.OnSubscribe;
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
    Observable<Entry<PointInSpace, Geometry>> recNeibors;
    
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
			points = points.add(new PointInSpace(input.get(i)), Geometries.pointGeographic(input.get(i).getLon(), input.get(i).getLat()));
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
			points = points.add(new PointInSpace(input.get(i)), Geometries.pointGeographic(input.get(i).getLon(), input.get(i).getLat()));

	}
	
	/**
	 * @param name the name that identifies the cluster file, the file thats loaded is saveClust + name + .txt
	 * @return returns the same as get clusterd
	 */
	static public ArrayList<ArrayList<DatabaseLocation>> loadCulster(String name)
	{

		ArrayList<ArrayList<DatabaseLocation>> output = new ArrayList<ArrayList<DatabaseLocation>>();
        String fileName = "saveClust" + name + ".txt";
        String line = null;
        String line2 = null;
        String line3 = null;
        String line4 = null;
        String line5 = null;
        String line6 = null;
        
        String[] lines = null;
        String[] lines2 = null;
        String[] lines3 = null;
        String[] lines4 = null;
        String[] lines5 = null;
        String[] lines6 = null;
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =  new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            int count=0;

			// double lo, double la,int hour, int min, double nlo,double nla
            while((line = bufferedReader.readLine()) != null && (line2 = bufferedReader.readLine()) != null && (line3 = bufferedReader.readLine()) != null && (line4 = bufferedReader.readLine()) != null && (line5 = bufferedReader.readLine()) != null && (line6 = bufferedReader.readLine()) != null )
            {
            	lines = line.split(" ");
            	lines2 = line2.split(" ");
            	lines3 = line.split(" ");
            	lines4 = line2.split(" ");
            	lines5 = line.split(" ");
            	lines6 = line2.split(" ");
            	
            	
                output.add(new ArrayList<DatabaseLocation>());
            	for(int i=0; i<lines.length;i++)
            	{
            		output.get(count).add(new ServerConnection.DBQuerry(Double.parseDouble(lines[i]),Double.parseDouble(lines2[i]),Integer.parseInt(lines3[i]),Integer.parseInt(lines4[i]),Double.parseDouble(lines5[i]),Double.parseDouble(lines6[i])));
            	}
                
            	count++;
            }   

            // Always close files.
            bufferedReader.close();         
        }
        catch(Error | IOException e) {
            System.out.println("An error has ocured when trying to read the cluster file");                
        }
        
        return output;
		
	}
	/**
	 * @param name the name that identifeis the cluster file, the file that will be saved is caveClust + name + .txt
	 * 
	 * invokes the getClust method and saves the result in a file with the specified name, use loadCluster to read the file later
	 * 
	 */
	public void saveCluster(String name)
	{
		ArrayList<ArrayList<DatabaseLocation>> temp = getClusterd(true);
		try(PrintStream out = new PrintStream(new FileOutputStream("saveClust" + name + ".txt"))){
			
			ArrayList<ArrayList<DatabaseLocation>> temp2 = getClusterd(true);
			for(ArrayList<DatabaseLocation> str : temp2)
			{
				
				for(DatabaseLocation v : str)
				{
					out.print(v.getLon()+" ");
					
				}
				out.print("\n");
				for(DatabaseLocation v : str)
				{
					out.print(v.getLat()+" ");	    					
				}
				out.print("\n");
				for(DatabaseLocation v : str)
				{
					out.print(v.getHTime()+" ");	    					
				}
				out.print("\n");
				for(DatabaseLocation v : str)
				{
					out.print(v.getMTime()+" ");	    					
				}
				out.print("\n");
				for(DatabaseLocation v : str)
				{
					out.print(v.getNLon()+" ");	    					
				}
				out.print("\n");
				for(DatabaseLocation v : str)
				{
					out.print(v.getNLat()+" ");	    					
				}
				out.print("\n");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		
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
			if( neibors.count().toBlocking().last() < minPoints)
			{
				e.markAsNoise();
			}
			else
			{
				c++;
				//expandCluster(e, neibors,epsilon,minPoints); // O(p^2) but marks as visited so dosn't interact mulltiplicativly with the for loop
				eClust(neibors.toList().toBlocking().first(),epsilon,minPoints);
			}
		}
	
	return c;
	}
	
	private void eClust(List<Entry<PointInSpace, Geometry>> neighbours, double epsilon, int minPoints)
	{
		//Set<Entry<PointInSpace,Geometry>> seeds = new HashSet<Entry<PointInSpace,Geometry>>();
		ArrayList<Entry<PointInSpace,Geometry>> seeds = new ArrayList<Entry<PointInSpace,Geometry>>(neighbours);
		int index = 0;
		while(index < seeds.size())
		{
			final Entry<PointInSpace, Geometry> e = seeds.get(index);
			if(!e.value().isVisited())
			{
				e.value().markAsVisited();
				final List<Entry<PointInSpace, Geometry>> currNeigbours = points.search(Geometries.circle(e.value().getX(), e.value().getY(), epsilon)).toList().toBlocking().first();
				
				if(currNeigbours.size() >= minPoints)
				{
					seeds.removeAll(currNeigbours);
					seeds.addAll(currNeigbours);
				}
			}
			if(e.value().getCluster()==0)
			{
				e.value().setCluster(c);
			}
			index++;
			//System.out.println(""+seeds.size());
		}
	}
	 
	private void expandCluster(PointInSpace p, Observable<Entry<PointInSpace, Geometry>> neibors2, double epsilon, int minPoints) 
	{
				
		do{
		recNeibors=Observable.empty();
		p.setCluster(c);
		List<Entry<PointInSpace,Geometry>> test = neibors2.toList().toBlocking().first();
		for(Entry<PointInSpace,Geometry> e : test)
		{
			if (!e.value().isVisited())
			{
				e.value().markAsVisited();
				Observable<Entry<PointInSpace, Geometry>> neiborsOfneibors = points.search(Geometries.circle(e.value().getX(), e.value().getY(), epsilon));//getNeibors(neibors.get(i),epsilon); //O(p)
				if(neiborsOfneibors.count().toBlocking().last() >=minPoints)
					recNeibors=recNeibors.mergeWith(neiborsOfneibors).distinct();
			}
			if(e.value().getCluster()==0)
			{
				e.value().setCluster(c);
			}

		}
		neibors2=recNeibors;
		System.out.println("ny nejgaphsdfasd");
		} 
		while(!recNeibors.isEmpty().toBlocking().last());
	}
	private void expandClusterHelper(PointInSpace neibor, double epsilon, int minPoints)
	{
		if (!neibor.isVisited())
		{
			neibor.markAsVisited();
			Observable<Entry<PointInSpace, Geometry>> neiborsOfneibors = points.search(Geometries.circle(neibor.getX(), neibor.getY(), epsilon));//getNeibors(neibors.get(i),epsilon); //O(p)
			if(neiborsOfneibors.count().toBlocking().last() >=minPoints)
				recNeibors=recNeibors.mergeWith(neiborsOfneibors).distinct();
		}
		if(neibor.getCluster()==0)
		{
			neibor.setCluster(c);
		}
	}

}
