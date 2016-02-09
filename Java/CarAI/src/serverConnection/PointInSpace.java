package serverConnection;

import interfaces.DatabaseLocation;

public class PointInSpace {

	DatabaseLocation d;
	private boolean visited,noise;
	private int cluster;
	
	public PointInSpace(DatabaseLocation dL)
	{
		d=dL;
		visited=false;
		noise=false;
		setCluster(0);
	}
	public void markAsVisited(){visited=true;}
	public void markAsNoise(){noise=true;}
	public boolean isVisited(){return visited;}	
	public boolean isNoise(){return noise;}
	
	public double getX(){return d.getLat();}
	public double getY(){return d.getLon();}
	
	public int getCluster() {return cluster;}
	public void setCluster(int cluster) {this.cluster = cluster;}
	public DatabaseLocation getDLLoc(){return d;}
}
