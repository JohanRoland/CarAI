package predictorG;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import predictorG.DayTime;
import utils.Tuple;
/**
 * @author Knarkapan
 * Stores a graph and the paths
 */
public class PredictorG {
	
	HashMap<Integer,Node> nodes;
	int nummberOfNodes;
	Node currentNode;
	
	
	public PredictorG()
	{
		nodes=new HashMap<Integer,Node>();
		currentNode=null;
		nummberOfNodes=0;
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
	public Tuple<Tuple<Integer, Double>, ArrayList<Tuple<Integer, Double>>> predictNextNode(double t,int d,int w, double[] waightFactors)
	{
		 DayTime dt= new DayTime(t,d,w);
		 ArrayList<Double> temp =currentNode.neibors.proxSerch(dt);
		 ArrayList<Tuple<Integer,Double>> outList = new ArrayList<Tuple<Integer,Double>>();
		 double highestFactor=0;
		 int highestNode=-1;
		 
		 for(int i=0; i<temp.size();i++)
		 {
			 if(temp.get(i)>highestFactor)
			 {
				 highestFactor=temp.get(i);
				 highestNode=i;
			 }
			 if(temp.get(i)!=0)
				 outList.add(new Tuple<Integer,Double>(i,temp.get(i)));
		 }
		 return new Tuple<Tuple<Integer,Double>,ArrayList<Tuple<Integer,Double>>>(new Tuple<Integer,Double>(highestNode,highestFactor), outList);
				
	}

	public void traversAndPathTo(int toCluster, double t,int d,int m)
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
	public void enterPath(int fromCluster,int toCluster, double t,int d,int m)
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
/*	
	public void savePredictorG(String dirPath, String fileName)
	{
		try (PrintStream out = new PrintStream(new FileOutputStream(dirPath + fileName))) {
		out.println(nummberOfNodes);
		
		for(Node e: nodes.values())
		{
			e.saveNode(out);
		}	
		out.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
		
		
	}
*/
	/*
	public void loadPredictorG(String dirPath, String fileName)
	{

		
		try  {
			FileReader fileReader =  new FileReader(dirPath+fileName);
			BufferedReader in = new BufferedReader(fileReader);
			nummberOfNodes=Integer.parseInt(in.readLine());
			
			for(int i=0; i<nummberOfNodes; i++)
			{
				int cluster = Integer.parseInt(in.readLine());
				nodes.put(cluster, new Node(in, cluster));
			}
			in.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}	
		
	}
	*/
	private class Node
	{
		int cluster;
		ConnectionHandeler neibors;
		
		Node(int c)
		{
			cluster=c;
			neibors = new ConnectionHandeler();
		}
		Node(BufferedReader fr, int c) throws IOException
		{
			this(c);
			int numberConnections = Integer.parseInt(fr.readLine());
			for(int i=0;i<numberConnections;i++)
			{
				String[] temp =fr.readLine().split(" ");
				for(int j=0; j<Integer.parseInt(temp[4]); j++)
					neibors.addConnection(Double.parseDouble(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]), Integer.parseInt(temp[0]));
				
			}
				
			
			
		}
/*
	 	public void saveNode(PrintStream out)
		{
			out.println(cluster );
			neibors.saveConnectionHandler(out);
		}
*/
		private class ConnectionHandeler
		{
			//[time -> [Clust, times]]
			ArrayList<Tuple<DayTime,ArrayList<Tuple<Integer,Integer>>>> connections2;
			//HashMap<Integer,HashMap<Integer, Tuple<Integer,DayTime>>> connections2;
			
			ConnectionHandeler()
			{
				//connections = new ArrayList<Tuple<Integer,DayTime>>();
				//connections2 = new HashMap<Integer,HashMap<Integer, Tuple<Integer,DayTime>>>();
				connections2 = new ArrayList<Tuple<DayTime,ArrayList<Tuple<Integer,Integer>>>>();
			}
		/*	
			public void saveConnectionHandler(PrintStream out) 
			{
				Set<Entry<Integer, ArrayList<Tuple<Integer, DayTime>>>> temp = connections2.entrySet();
				
				out.println(temp.size());
				
				for(Entry<Integer, ArrayList<Tuple<Integer, DayTime>>> e2 : temp)
				{
					for( Tuple<Integer, DayTime> e : e2.getValue())
					{
						out.println(e2.getKey()+" "+e.snd().getTime()+" "+e.snd().getDay()+ " "+e.snd().getMonth()+" "+e.fst());
					}
				}
			}
		 */
			void addConnection(double t,int d,int m, Integer n)
			{
				

				
				for(Tuple<DayTime, ArrayList<Tuple<Integer, Integer>>> e : connections2)
				{
					if(e.fst().getTime()==t &&e.fst().getDay()==d && e.fst().getMonth()==m )
					{
						ArrayList<Tuple<Integer, Integer>> temp = e.snd();
						for(Tuple<Integer, Integer> listE : temp)
						{
							if(listE.fst()==n)
							{
								listE.setSnd(listE.snd()+1);
								return;
								
							}		
						}

					}
					
				}
				DayTime pl =new DayTime(t, d,m);
				ArrayList<Tuple<Integer, Integer>> temp = new ArrayList<Tuple<Integer,Integer>>();
				temp.add(new Tuple<Integer,Integer>(n,1));
				connections2.add(new Tuple<DayTime,ArrayList<Tuple<Integer, Integer>>>(pl, temp));
				
			}


			private ArrayList<Double> proxSerch(DayTime d)
			{
				
				ArrayList<Double> out = new ArrayList<Double>();
				
				for(int i =0; i<connections2.size();i++)
				{
					out.add(0.0);
				}
				
				
				for(int i=0; i < connections2.size();i++)
				{
					Tuple<DayTime, ArrayList<Tuple<Integer, Integer>>> e = connections2.get(i);
					for(Tuple<Integer, Integer> cd :e.snd())
					{
							
							double factor = e.fst().relativeDistanceT(d)*(Math.log(0.1+cd.snd()/100)+3)/4;
							if(factor>0)
							{
								out.set(cd.fst(), factor);
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
