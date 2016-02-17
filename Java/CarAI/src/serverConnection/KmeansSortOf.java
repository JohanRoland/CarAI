package serverConnection;

import java.util.ArrayList;
import java.util.Random;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;

import interfaces.DatabaseLocation;
import rx.Observable;
import utils.Tuple;

public class KmeansSortOf 
{
	
	RTree<PointInSpace, Geometry> points;
    volatile ArrayList<DatabaseLocation>[] clusters;
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
		
			
		for(int i=0;i<input.size();i++)
			points = points.add(new PointInSpace(input.get(i)), Geometries.point(input.get(i).getLon(), input.get(i).getLat()));
		int jasdf = 0;
		jasdf++;
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
		Tuple<Double,Double> temp2 = new Tuple<Double,Double>(Double.MIN_VALUE,Double.MIN_VALUE);
		Observable<Entry<PointInSpace, Geometry>> neibors = Observable.empty();
		
		while(e.getX()!=temp2.fst() && e.getY()!=temp2.snd())
		{

			neibors = points.search(Geometries.circle(e.getX(), e.getY(), epsilon));
					
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
		c++;
		neibors.forEach(f -> f.value().setCluster(c));
		neibors.forEach(d -> points.delete(d));
		return c;
	}
}

