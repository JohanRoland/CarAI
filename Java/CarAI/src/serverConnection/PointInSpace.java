package serverConnection;

public class PointInSpace	{
	private float x,y;
	private boolean visited,noise;
	private int cluster;
	
	public PointInSpace(float a ,float b)
	{
		x=a;
		y=b;
		visited=false;
		noise=false;
		setCluster(0);
	}
	public void markAsVisited(){visited=true;}
	public void markAsNoise(){noise=true;}
	public boolean isVisited(){return visited;}	
	public boolean isNoise(){return noise;}
	public float getX(){return x;}
	public float getY(){return y;}
	public int getCluster() {return cluster;}
	public void setCluster(int cluster) {this.cluster = cluster;}
}
