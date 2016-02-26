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
		 ArrayList<Tuple<Integer,ArrayList<Double>>> temp =currentNode.neibors.proxSerch(dt);
		 ArrayList<Tuple<Integer,Double>> outList = new ArrayList<Tuple<Integer,Double>>();
		 
		 double highestFactor=0;
		 int highestNode=-1;
		 for(Tuple<Integer,ArrayList<Double>> e : temp)
		 {
			 double factor=1;
			 int numberOfFactors=0;
			 Double nummberOfOccurences = e.snd().get(0);
			 for(int i=1; i < e.snd().size(); i++)
			 {
				 factor+=e.snd().get(i)*waightFactors[numberOfFactors];
				 numberOfFactors++;
			 }
			 factor=(factor/numberOfFactors)*(Math.sqrt(nummberOfOccurences));
			 if(factor>highestFactor)
			 {
				 highestFactor=factor;
				 highestNode=e.fst();
			 }
			 outList.add(new Tuple<Integer,Double>(e.fst(),factor));
		 }
		
		
		
		return new Tuple<Tuple<Integer,Double>,ArrayList<Tuple<Integer,Double>>>(new Tuple<Integer,Double>(highestNode, highestFactor),outList);
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
		public void saveNode(PrintStream out)
		{
			out.println(cluster );
			neibors.saveConnectionHandler(out);
		}

		private class ConnectionHandeler
		{
			//ArrayList<Tuple<Integer, DayTime>> connections;
			HashMap<Integer,Tuple<Integer,DayTime>> connections2;
			
			ConnectionHandeler()
			{
				//connections = new ArrayList<Tuple<Integer,DayTime>>();
				connections2 = new HashMap<Integer,Tuple<Integer,DayTime>>();
			}
			
			public void saveConnectionHandler(PrintStream out) 
			{
				Set<Entry<Integer, Tuple<Integer, DayTime>>> temp = connections2.entrySet();
				
				out.println(temp.size());
				
				for(Entry<Integer, Tuple<Integer, DayTime>> e : temp)
				{
					out.println(e.getKey()+" "+e.getValue().snd().getTime()+" "+e.getValue().snd().getDay()+ " "+e.getValue().snd().getMonth()+" "+e.getValue().fst());
				}
			}

			void addConnection(double t,int d,int m, Integer n)
			{
				
				DayTime pl =new DayTime(t, d,m);
				if(connections2.containsKey(n))
				{
					Tuple<Integer,DayTime> temp = connections2.get(n);	
					temp.setFst(temp.fst()+1);
				}
				else
				{
					connections2.put(n, new Tuple<Integer,DayTime>(1,pl));
				}
				//connections.add(new Tuple<Integer,DayTime>(n,pl));
			}


			private ArrayList<Tuple<Integer,ArrayList<Double>>> proxSerch(DayTime d)
			{
				
				ArrayList<Tuple<Integer,ArrayList<Double>>> out = new ArrayList<Tuple<Integer,ArrayList<Double>>> ();
				for(Entry<Integer, Tuple<Integer, DayTime>> t : connections2.entrySet())
				{
					ArrayList<Double> temp;	
					double time =	t.getValue().snd().relativeDistanceT(d);
					double day =	t.getValue().snd().relativeDistanceD(d);
					double month =	t.getValue().snd().relativeDistanceM(d);
					temp =new ArrayList<Double>();
					temp.add((double)t.getValue().fst());
					if(time*day*month>0)
					{		
						temp.add(time);
						temp.add(day);
						temp.add(month);
						out.add(new Tuple<Integer,ArrayList<Double>>(t.getKey(),temp));
					}
					else if(time*day>0)
					{
						temp.add(time);
						temp.add(day);
						out.add(new Tuple<Integer,ArrayList<Double>>(t.getKey(),temp));
					}else if(time>0)
					{
						temp.add(time);
						out.add(new Tuple<Integer,ArrayList<Double>>(t.getKey(),temp));
						
					}
						
				}
				return out;
			}
			
		}

	
	}

}
