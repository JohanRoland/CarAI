package predictorG;

import java.util.ArrayList;
import java.util.HashMap;
import predictorG.DayTime;
import utils.Tuple;

public class PredictorG {
	
	HashMap<Integer,Node> nodes;
	Node currentNode;
	
	
	public PredictorG()
	{
		nodes=new HashMap<Integer,Node>();
		currentNode=null;
	}
	public void addNode(Node node, int cluster)
	{
		nodes.put(cluster, node);
		
	}
	public void setCurrentNode(int cluster)
	{
		if(nodes.containsKey(cluster))
			currentNode = nodes.get(cluster);
		else
			throw new Error("No such cluster");
	}
	public Tuple<Tuple<Node, Double>, ArrayList<Tuple<Node, Double>>> predictNextNode(DayTime dt, double[] waightFactors)
	{		
		 ArrayList<Tuple<Node,ArrayList<Double>>> temp =currentNode.neibors.proxSerch(dt);
		 ArrayList<Tuple<Node,Double>> outList = new ArrayList<Tuple<Node,Double>>();
		 
		 double highestFactor=0;
		 Node highestNode=null;
		 for(Tuple<Node,ArrayList<Double>> e : temp)
		 {
			 double factor=1;
			 int numberOfFactors=0;
			 for(Double e2 : e.snd())
			 {
				 factor+=e2*waightFactors[numberOfFactors];
				 numberOfFactors++;
			 }
			 factor=factor/numberOfFactors;
			 if(factor>highestFactor)
			 {
				 highestFactor=factor;
				 highestNode=e.fst();
			 }
			 outList.add(new Tuple<Node,Double>(e.fst(),factor));
		 }
		
		
		
		return new Tuple<Tuple<Node,Double>,ArrayList<Tuple<Node,Double>>>(new Tuple<Node,Double>(highestNode, highestFactor),outList);
	}

	public void traversAndPathTo(int toCluster, double t,int d,int m)
	{
		Node toNode = nodes.get(toCluster);
		currentNode.neibors.addConnection(t, d, m, toNode);	
		currentNode = toNode;
	}
	public void enterPath(int fromCluster,int toCluster, double t,int d,int m)
	{
		Node fromNode = nodes.get(fromCluster);
		Node toNode = nodes.get(toCluster);
		fromNode.neibors.addConnection(t, d, m, toNode);
		
	}
	 
	
	private class Node
	{
		ConnectionHandeler neibors;
		
		
		
		
		
		private class ConnectionHandeler
		{
			ArrayList<Tuple<Node, DayTime>> connections;
			
			
			ConnectionHandeler()
			{
				connections = new ArrayList<Tuple<Node,DayTime>>();
			}
			
			void addConnection(double t,int d,int m, Node n)
			{
				DayTime pl =new DayTime(t, d,m);
				connections.add(new Tuple<Node,DayTime>(n,pl));
			}


			private ArrayList<Tuple<Node,ArrayList<Double>>> proxSerch(DayTime d)
			{
				ArrayList<Tuple<Node,ArrayList<Double>>> out = new ArrayList<Tuple<Node,ArrayList<Double>>> ();
				for(Tuple<Node,DayTime> t : connections)
				{
					ArrayList<Double> temp;	
					double time =	t.snd().relativeDistanceT(d);
					double day =	t.snd().relativeDistanceD(d);
					double month =	t.snd().relativeDistanceM(d);
					if(time*day*month>0)
					{		
						temp =new ArrayList<Double>();
						temp.add(time);
						temp.add(day);
						temp.add(month);
						out.add(new Tuple<Node,ArrayList<Double>>(t.fst(),temp));
					}
					else if(time*day>0)
					{
						temp =new ArrayList<Double>();
						temp.add(time);
						temp.add(day);
						out.add(new Tuple<Node,ArrayList<Double>>(t.fst(),temp));
					}else if(time>0)
					{
						temp =new ArrayList<Double>();
						temp.add(time);
						out.add(new Tuple<Node,ArrayList<Double>>(t.fst(),temp));
						
					}
						
				}
				return out;
			}
			
		}

	
	}

}
