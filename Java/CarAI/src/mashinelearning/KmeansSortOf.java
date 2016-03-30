package mashinelearning;

import java.util.ArrayList;
import java.util.Random;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;

import interfaces.DatabaseLocation;
import rx.Observable;
import serverConnection.PointInSpace;
import utils.Tuple;

public class KmeansSortOf 
{
	
	RTree<PointInSpace, Geometry> points;
    volatile ArrayList<ArrayList<DatabaseLocation>> clusters;
    int c;
    
	
	public KmeansSortOf(ArrayList<DatabaseLocation> input, boolean star) throws Error
	{ 	
		c=0;
		if(star)
			points= RTree.star().create();
		else
			points= RTree.create();
		
		if(input.size()==0)
		{
			throw new Error("the imputted arrays have zero inputs");
		}
		clusters = new ArrayList<ArrayList<DatabaseLocation>>();
		
		
			
		for(int i=0;i<input.size();i++)
			points = points.add(new PointInSpace(input.get(i)), Geometries.point(input.get(i).getLon(), input.get(i).getLat()));
	}
	
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
	
	public int cluster(double epsilon) // O(p^2)
	{
		while(!points.isEmpty())
		{
			PointInSpace p = points.entries().toBlocking().last().value();
			clusterHelper(p ,epsilon);
		}
	
		
		return c;
	}
	private int clusterHelper(PointInSpace e, double epsilon)
	{	
		ArrayList<Tuple<Double,Double>> temp = new ArrayList<Tuple<Double,Double>>();
		Tuple<Double,Double> temp2 = new Tuple<Double,Double>(e.getX(),e.getY());
		Observable<Entry<PointInSpace, Geometry>> neibors = null;
		double tempX =e.getX();
		double tempY =e.getY();
		
		do
		{
			tempX=temp2.fst();
			tempY=temp2.snd();
			neibors = points.search(Geometries.circle(tempX, tempY, epsilon));
			if(neibors.isEmpty().toBlocking().last())
				return 0;
			
			neibors.forEach(v -> temp.add(new Tuple<Double,Double>(v.value().getX(),v.value().getY())));		
			
			temp2 = new Tuple<Double,Double>(0.0,0.0);
			for(Tuple<Double,Double> x : temp)
			{
				temp2.setFst(temp2.fst()+x.fst());
				temp2.setSnd(temp2.snd()+x.snd());
			}
			temp2.setFst(temp2.fst()/temp.size());
			temp2.setSnd(temp2.snd()/temp.size());	
		}
		while(Math.abs(tempX-temp2.fst())>0.01|| Math.abs(tempY-temp2.snd())>0.01);
		
		c++;
		clusters.add(new ArrayList<DatabaseLocation>());
		neibors.forEach(f -> clusters.get(c-1).add(f.value().getDLLoc()));
		neibors.forEach(d -> helper(d));

		return c;
	}
	void helper(Entry<PointInSpace, Geometry> in)
	{
		points=points.delete(in);
	}
}


