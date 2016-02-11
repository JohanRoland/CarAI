package serverConnection;

import interfaces.DatabaseLocation;

/**
 * @author Knarkapan
 *PointsInSpace is a container for a DatabaseLocation that adds the booleans {@value visited} and {@value noise}
 * as well as the integer cluster. It's primary use is in the modal DBSCAN. 
 *
 */
public class PointInSpace {

	DatabaseLocation d;
	private boolean visited,noise;
	private int cluster;
	/**
	 * @param dL The DatabaseLocation to be contained in the container
	 * 
	 * all values are set to false or zero by default.
	 */
	public PointInSpace(DatabaseLocation dL)
	{
		d=dL;
		visited=false;
		noise=false;
		setCluster(0);
	}
	/**
	 * sets visited to true
	 */
	public void markAsVisited(){visited=true;}
	/**
	 * sets noise to true
	 */
	public void markAsNoise(){noise=true;}
	/**
	 * 
	 * @return the value of visited
	 */
	public boolean isVisited(){return visited;}	
	/**
	 * 
	 * @return the value of noise
	 */
	public boolean isNoise(){return noise;}
	/**
	 * 
	 * @return Tthe longitude in the stored DatabaseLocation
	 */
	public double getX(){return d.getLat();}
	/**
	 * 
	 * @return The latitude stored in the DatabaseLocation
	 */
	public double getY(){return d.getLon();}
	/**
	 * 
	 * @return The value of cluster
	 */
	public int getCluster() {return cluster;}
	/**
	 * 
	 * @param cluster the value to witch to set cluster
	 */
	public void setCluster(int cluster) {this.cluster = cluster;}
	/**
	 * 
	 * @return The stored DatabaseLocation
	 */
	public DatabaseLocation getDLLoc(){return d;}
}
