package interfaces;

public interface DatabaseLocation {
	
	public double getLon();
	public double getLat();
	public int getMTime();
	public int getHTime();
	public double getNLon();
	public double getNLat();
	
	public void setPos(double lat, double lon);
	public boolean equals(Object o);
	
}
