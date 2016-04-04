package serverConnection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.mysql.jdbc.CallableStatement;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import interfaces.DatabaseLocation;
import utils.Tuple;
/**
 * @author John Ekdahl
 *
 * Keeps a connection to a mySQL database, has functions to interact smoothly with 
 * specific stored procedures aimed at the CarAI project.
 *
 */
public class ServerConnection {
	Connection connection;
	
	private static ServerConnection instance; 
	/**
	 * @param serverName The name of the database
	 * @param serverPort The servers port
	 * @param serverLocation The location of the database such as an IP
	 * @param userName The username witch will be used for the log in to the DB account
	 * @param Password The Password witch will be used for the log in to the DB account
	 * 
	 * The constructor creates an connection thats stored in the class object.
	 * 
	 */
	private ServerConnection(String serverName, String serverPort, String serverLocation, String userName, String Password)
	{
		
		try {
			String url = "jdbc:mysql://" + serverLocation +":"+ serverPort + "/" + serverName; 
			System.out.println(url);
			connection = (Connection) DriverManager.getConnection(url, userName, Password);

			System.out.println("Connected witout a hitch");
		} catch (SQLException e) {
		    throw new IllegalStateException("Cannot connect the database!", e);
		}
	}
	/**
	 * Connects to a default predifined user
	 */
	private ServerConnection()
	{
		this("mydb","3306","54.229.54.240" , "johan", "knarkapan");
	}
	/**
	 * Returns the active instance.
	 * If no such instance exists a new instance will be returned. 
	 * @return The active erverConnection 
	 */
	public static ServerConnection getInstance()
	{
		if(instance == null)
		{
			instance = new ServerConnection();
		}
		
		return instance;
		
	}
	
	
	/**
	 * 
	 * @param request the mySQL code to be executed
	 * @return the result of the mySQL code
	 * 
	 * executes the request on the connected DB
	 * 
	 */
	public ResultSet basicQuery(String request)
	{
		Statement stmt = null;
		try {
			stmt = (Statement) connection.createStatement();
		} catch (SQLException e1) {
			System.out.println("failiure in connection\n");
			e1.printStackTrace();
		}

		ResultSet rs = null;
		
		try {
			rs = stmt.executeQuery(request);
			stmt.close();
		} catch (SQLException e) {
			System.out.println("faild to send/recive request\n");
			e.printStackTrace();
		}

		return rs;
	}
	/**
	 * Using the active ServerConnection, this metod retrives the 
	 * GPS data of the user. 
	 * @param ID The user ID
	 * @return returns two ArrayLists containing Latitudes and Longitudes in that order 
	 * @throws SQLException
	 */
	public ArrayList<Double>[] getPosData(int ID) throws SQLException
	{
		Statement stmt = (Statement) connection.createStatement();
		ResultSet rs = stmt.executeQuery("CALL getPos("+ID+")");
		ArrayList<Double>[] output = (ArrayList<Double>[])new ArrayList[5];
		
		output[0] = new ArrayList<Double>();
		output[1] = new ArrayList<Double>();
		
		while(rs.next())
		{
			output[0].add(rs.getDouble("Lat"));
			output[1].add(rs.getDouble("Lon"));
		}
		stmt.close();
		return output;
		
	}
	/**
	 * Using the active ServerConnection, this method retrieves the 
	 * GPS data up to a specified limit starting from the most recent.
	 * Returning the result as DBQuerry s. 
	 * @param id The user ID
	 * @param limit The number of entries to retrieve, starting from the most recent.
	 * @return An ArrayList of DBQuerry that represents the paths the user has taken.
	 * @throws SQLException
	 */
	public ArrayList<DatabaseLocation> getPosClass(int id,int limit) throws SQLException
	{
		Statement stmt = (Statement) connection.createStatement();
		ResultSet rs = stmt.executeQuery("CALL getPos("+id+","+limit +")");
		
		ArrayList<DatabaseLocation> out = new ArrayList<DatabaseLocation>(); 
		
		while(rs.next())
		{
			out.add(new DBQuerry(rs.getDouble("Lat"),rs.getDouble("Lon"),rs.getInt("Hours"),rs.getInt("Minutes") ,rs.getDouble("nextLat"),rs.getDouble("nextLon")));
		}
		stmt.close();
		return out;
		
	}
	/**
	 * @param ID
	 * @param Long
	 * @param Lat
	 * @param time
	 * @param Long2
	 * @param Lat2
	 * @throws SQLException
	 * 
	 * 
	 * Adds an entry to the possitionhistory rable through a stored procedure
	 */
	public void addPosData(int ID, double Long, double Lat, double time, double Long2,double Lat2) throws SQLException
	{
		Statement stmt = (Statement) connection.createStatement();
		stmt.executeQuery("CALL enterPossitionData("+ID+","+Long+","+Lat+","+time+","+Long2+","+Lat2+")");
		stmt.close();
	}
	/**
	 * @param ID The ID of the entries that are to be replaced
	 * @param input List of DBQuerry that holds the information that is to be inserted on behalf of ID
	 * @throws SQLException
	 * 
	 * Removes all previous entries on the specified ID and makes a bulk insert on that ID
	 * 
	 * 
	 */
	public void replacePosData(int ID, DBQuerry[] input) throws SQLException
	{
		if(input.length==0)
			throw new SQLException("length of input must be greater than zero");
						
		
		Statement stmt = (Statement) connection.createStatement();
		String values= "INSERT INTO PositionHistoryTable (ID,Lon,Lat,Hours,Minutes,nextLon,nextLat) VALUES " ;		
		stmt.execute("DELETE FROM PositionHistoryTable WHERE ID="+ ID);
		
		int i =0;
		try{
		int ltoh=0;
		for(i=0;i<input.length;i=i+ltoh)
		{

			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO PositionHistoryTable (ID,Lon,Lat,Hours,Minutes,nextLon,nextLat) VALUES ");
			ltoh= Math.min((input.length -i), 100);
			for(int j=i;j<(i+ltoh-1);j++)
			{
				sb.append( "(" + ID + ","+ input[j].getLon() + "," + input[j].getLat() + "," + input[j].getHTime() + "," + input[j].getMTime() + "," + input[j].getNLon() + "," + input[j].getNLat() +"),");  
			}
			sb.append("(" + ID + ","+ input[(i+ltoh-1)].getLon() + "," + input[(i+ltoh-1)].getLat() + "," + input[(i+ltoh-1)].getHTime() + "," + input[(i+ltoh-1)].getMTime() + "," + input[(i+ltoh-1)].getNLon() + "," + input[(i+ltoh-1)].getNLat() +");");
			
			//System.out.println(sb.toString());
			
			
			stmt.execute(sb.toString());
		}
		}catch(Exception e)
		{
			System.out.println("Error at row " + i);
			e.printStackTrace();
		}
		System.out.println("Done sending data sting");
		stmt.close();		
		
	}
	
	public long addUserData(String name) throws SQLException
	{
		
		
		java.sql.CallableStatement cs = connection.prepareCall("CALL createUser(? , ?)");
		cs.setString(1, name);
		cs.registerOutParameter(2, Types.VARCHAR);
		cs.executeQuery();
		return cs.getInt(2);
	}
	
	public ArrayList<String> getUserData(String id) throws SQLException
	{
		Statement stmt = (Statement) connection.createStatement();
		ResultSet rs =  stmt.executeQuery("CALL getUser("+ Integer.parseInt(id) +")");
		
		ArrayList<String> out = new ArrayList<String>();
		
		while(rs.next())
		{
			out.add(rs.getString("ID"));
			out.add(rs.getString("UName"));
		}
		stmt.close();
		
		return out;
	}
	
	public static class DBQuerry implements DatabaseLocation
	{
		double lat;
		double lon;
		int minutes;
		int hours;
		double nextLon;
		double nextLat;
		int day;
		int month;
		int year;
		
		public DBQuerry(double la, double lo,int hour, int min, double nla,double nlo)
		{
			this.lat = la;
			this.lon = lo;
			minutes = min;
			hours = hour;
			nextLat = nla;
			nextLon = nlo;
		}
		public DBQuerry(double la, double lo,int year,int month,int day,int hour, int min, double nla,double nlo)
		{
			this.lat = la;
			this.lon = lo;
			minutes = min;
			hours = hour;
			nextLat = nla;
			nextLon = nlo;
			this.year = year;
			this.month = month;
			this.day = day;
		}
		
		public boolean equals(Object o)
		{
			if(o==null)
				return false;
			
			if(o==this)
				return true;
		
			DBQuerry dbq = (DBQuerry)o;
			
			if(this.getLat()==dbq.getLat() && this.getLon()==dbq.getLon() && this.getHTime()==dbq.getHTime() && this.getMTime()==dbq.getMTime() && this.getNLat()==dbq.getNLat() && this.getNLon()==dbq.getNLon())
				return true;
			else
				return false;
		}
		
		public double getLon()
		{
			return lon;
		}
		
		public double getLat()
		{
			return lat;
		}
		public int getMTime()
		{
			return minutes;
		}
		public int getHTime()
		{
			return hours;
		}
		public double getNLon()
		{
			return nextLon;
		}
		
		public double getNLat()
		{
			return nextLat;
		}
		
		public int getDay()
		{
			return day;
		}
		public int getMonth()
		{
			return month;
		}
		public int getYear()
		{
			return year;
		}
		
		public void setPos(double lon, double lat)
		{
			this.lon = lon; 
			this.lat = lat;
		}
		@Override
		public void setNPos(double lat, double lon) {
			nextLon=lon;
			nextLat=lat;
			
		}
		
		
		public boolean isWeekday() {
			Calendar c = Calendar.getInstance();
			c.set(year, month, day);
			int out = c.get(Calendar.DAY_OF_WEEK);
			return (out < 6);
		}
		public int getDayOfWeek() {
			Calendar c = new GregorianCalendar();// Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DATE, day);
			int out = c.get(Calendar.DAY_OF_WEEK);
			return out;
		}
			
	}

}
