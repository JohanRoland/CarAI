package predictorG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import prediction.LocPrediction;

import java.util.Set;

import predictorG.DayTime;
import utils.Tuple;
/**
 * @author Johan Ekdahl
 * Stores a graph and the paths
 */
public class PredictorG {
	
	HashMap<Integer,Node> nodes;
	int nummberOfNodes;
	Node currentNode;
	static HashMap<Integer, PredictorG> instanceMap;	
	
	private PredictorG()
	{
		nodes=new HashMap<Integer,Node>();
		currentNode=null;
		nummberOfNodes=0;
	}
	static public PredictorG getInstance(int userID)
	{

		if(instanceMap == null)
		{
			instanceMap = new HashMap<Integer,PredictorG>();
		}
		if(!instanceMap.containsKey(userID))
		{
			instanceMap.put(userID, new PredictorG());//userID
		}
		
		return instanceMap.get(userID);
	}
	/**
	 * Resets the network and loads the batch
	 * @param inputs An arrayList of lists containing fromCluster, toCluster, time of day, day, month
	 */
	public void batchLoad(ArrayList<int[]> inputs)
	{
		nodes=new HashMap<Integer,Node>();
		currentNode=null;
		nummberOfNodes=0;
		int high=Integer.MIN_VALUE;
		for(int[] inp : inputs)
		{
			if(inp[0]>high)
				high=inp[0];
			
			if(inp[1]>high)
				high=inp[1];
		}
		for(int i=1; i<=high;i++)
		{
			this.addNode(i);
		}
		for(int[] inp : inputs)
		{
			enterPath(inp[0],inp[1], inp[2],inp[3],inp[4]);
			
		}
	}
	public void loadFromCSV(String path)
	{
		try {
			ArrayList<String> reader = (ArrayList<String>) Files.readAllLines(new File(path).toPath());
			ArrayList<int[]> batch= new ArrayList<int[]>();
			for(String e : reader)
			{
				String[] arg = e.split(" ");
				int[] temp ={Integer.parseInt(arg[0]),Integer.parseInt(arg[2]),Integer.parseInt(arg[1]),0,0};
				batch.add(temp);
			}
			batchLoad(batch);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void addNode(int cluster)
	{
		if(!nodes.containsKey(cluster))
		{
			nodes.put(cluster, new Node(cluster));
			nummberOfNodes++;
		}
		else
		{
			throw new Error("Trying to create a cluster that exsists");
		}
	}
	public void setCurrentNode(int cluster)
	{
		if(nodes.containsKey(cluster))
			currentNode = nodes.get(cluster);
		else
			throw new Error("No such cluster");
	}
	public Tuple<Tuple<Integer, Double>, ArrayList<Tuple<Integer, Double>>> predictNextNode(int t,int d,int w, double[] waightFactors)
	{
		if(t==341)
		{		
			int g=0;
			g++;
		}
		 DayTime dt= new DayTime(t,d,w);
		 ArrayList<Double> temp =currentNode.neibors.proxSerch(dt, 30);
		 ArrayList<Tuple<Integer,Double>> outList = new ArrayList<Tuple<Integer,Double>>();
		 double highestFactor=0;
		 int highestNode=-1;
		 
		 for(int i=0; i<temp.size();i++)
		 {
			 if(temp.get(i)>highestFactor)
			 {
				 highestFactor=temp.get(i);
				 highestNode=i+1;
				 if(highestNode==13)
				 {
					 int e=0;
					 e++;
				 }
			 }
			 if(temp.get(i)!=0)
				 outList.add(new Tuple<Integer,Double>(i+1,temp.get(i)));
		 }
		 return new Tuple<Tuple<Integer,Double>,ArrayList<Tuple<Integer,Double>>>(new Tuple<Integer,Double>(highestNode,highestFactor), outList);
				
	}

	public void traversAndPathTo(int toCluster, int t,int d,int m)
	{
		if(nodes.containsKey(toCluster))
		{
			currentNode.neibors.addConnection(t, d, m, toCluster);	
			currentNode = nodes.get(toCluster);
		}
		else
		{
			throw new Error("Destination node does not exsist");
		}
	}
	public void enterPath(int fromCluster,int toCluster, int t,int d,int m)
	{
		if(!nodes.containsKey(fromCluster))
		{
			throw new Error("Sorce cluster dosn't exsists");
		}
		else if(!nodes.containsKey(toCluster))
		{
			throw new Error("Destination cluster dosn't exsists");
		}
		
		Node fromNode = nodes.get(fromCluster);
		fromNode.neibors.addConnection(t, d, m, toCluster);
		
	}
	private class Node
	{
		
		@SuppressWarnings("unused")
		int cluster;
		ConnectionHandeler neibors;
		
		Node(int c)
		{
			cluster=c;
			neibors = new ConnectionHandeler();
		}
		@SuppressWarnings("unused")
		Node(BufferedReader fr, int c) throws IOException
		{
			this(c);
			int numberConnections = Integer.parseInt(fr.readLine());
			for(int i=0;i<numberConnections;i++)
			{
				String[] temp =fr.readLine().split(" ");
				for(int j=0; j<Integer.parseInt(temp[4]); j++)
					neibors.addConnection(Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]), Integer.parseInt(temp[0]),Integer.parseInt(temp[5]));
			}			
		}
		private class ConnectionHandeler
		{
			//[time -> [Clust, Day of last entry]]
			ArrayList<Tuple<DayTime,ArrayList<Tuple<Integer,Integer>>>> connections2;

			
			ConnectionHandeler()
			{
				connections2 = new ArrayList<Tuple<DayTime,ArrayList<Tuple<Integer,Integer>>>>();
			}
			void addConnection(int t,int d,int m, int n)
			{
				addConnection(t, d, m, n, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
			}
			void addConnection(int t,int d,int m, int n, int timeStamp)
			{	
				 //ArrayList<Tuple<Integer, Integer>> listOfEntries= new  ArrayList<Tuple<Integer, Integer>>();
				if(n==13 && d==1)
				{
					int y=0;
					y++;
				}
				for(Tuple<DayTime, ArrayList<Tuple<Integer, Integer>>> e : connections2)
				{
					if(e.fst().getTime()==t &&e.fst().getDay()==d && e.fst().getMonth()==m )
					{
						e.snd().add(new Tuple<Integer,Integer>(n,/*timeStamp*/ null));
						return;
								
					}
				}
				DayTime pl =new DayTime(t, d,m);
				ArrayList<Tuple<Integer, Integer>> temp = new ArrayList<Tuple<Integer,Integer>>();
				temp.add(new Tuple<Integer,Integer>(n, /*Calendar.getInstance().get(Calendar.DAY_OF_YEAR)*/null));
				connections2.add(new Tuple<DayTime,ArrayList<Tuple<Integer, Integer>>>(pl, temp));
				
			}
			private ArrayList<Double> proxSerch(DayTime d, int timeInterval)
			{
				
				boolean matchingD=false;
				boolean matchingM=false;
				
				
				ArrayList<Double> out = new ArrayList<Double>();
				
				for(int i =0; i<nummberOfNodes+1;i++)
				{
					out.add(0.0);
				}
				
				
				for(int i=0; i < connections2.size();i++)
				{
					Tuple<DayTime, ArrayList<Tuple<Integer, Integer>>> e = connections2.get(i);
					
					if(e.fst().getTime()+timeInterval>d.getTime() && e.fst().getTime()-timeInterval<d.getTime())
					{
						for(Tuple<Integer, Integer> cd :e.snd())
						{
							//Calendar temp2 = Calendar.getInstance();
							//int dateDiff = temp2.get(Calendar.DAY_OF_YEAR)-cd.snd();
							/*
							if(dateDiff<0) 
							{
								int toEOY = 365 - cd.snd();
								dateDiff= toEOY+temp2.get(Calendar.DAY_OF_YEAR);
								
							}
							*/
							double factorT = e.fst().relativeDistanceT(d);//*(Math.log(0.1+cd.snd()/100)+3)/4;
							
							if(matchingD)
							{
								if(e.fst().getDay()!=d.getDay())
								{
									factorT=0;
								}
							}
							else if(e.fst().getDay()==d.getDay())
							{
								matchingD=true;
								out.set(cd.fst()-1,0.0);
							}
							/*
							if(matchingM) 
							{
								if(e.fst().getMonth()!=d.getMonth())
								{
									factorT=0;
									System.out.println("Failed after match Month");
								}
							}
							else if(e.fst().getMonth()==d.getMonth())
							{
								matchingM=true;
								out.set(cd.fst()-1,0.0);
							}*/
							//608-692 && 841
							if(factorT>0 && matchingD /*&& 14>dateDiff*/)
							{
								out.set(cd.fst()-1, out.get(cd.fst()-1)+factorT);
							}
						}
					}
				}
				//ArrayList<Double> listThathasZero = new ArrayList<Double>();
				//listThathasZero.add(0.0);
				//out.removeAll(listThathasZero);
				return out;
			}
		}
	}

}