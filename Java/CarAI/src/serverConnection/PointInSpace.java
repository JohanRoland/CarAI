package serverConnection;

public class PointInSpace	{
	private double x,y;
	private boolean visited,noise;
	private int cluster;
	
	public PointInSpace(double a ,double b)
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
	public double getX(){return x;}
	public double getY(){return y;}
	public int getCluster() {return cluster;}
	public void setCluster(int cluster) {this.cluster = cluster;}
}
