package interfaces;


public interface DatabaseLocation {
	
	public double getLon();
	public double getLat();
	public int getMTime();
	public int getHTime();
	public int getDay();
	public int getMonth();
	public int getYear();
	public boolean isWeekday();
	public int getDayOfWeek();
	
	public double getNLon();
	public double getNLat();
	
	public void setPos(double lat, double lon);
	public void setNPos(double lat, double lon);
	public boolean equals(Object o);
	
}
