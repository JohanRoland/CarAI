package interfaces;

public interface DatabaseLocation {
	
	public double getLon();
	public double getLat();
	public double getTime();
	public double getNLon();
	public double getNLat();
	
	public void setPos(double lat, double lon);

}
