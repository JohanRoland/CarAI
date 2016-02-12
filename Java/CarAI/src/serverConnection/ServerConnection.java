package serverConnection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import interfaces.DatabaseLocation;
import utils.Tuple;
/**
 * @author Knarkapan
 *
 *Keeps a connection to a mySQL database, has functions to interact smoothly with 
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

			System.out.println("Weeeee");
		} catch (SQLException e) {
		    throw new IllegalStateException("Cannot connect the database!", e);
		}
	}
	
	private ServerConnection()
	{
		this("mydb","3306","192.168.1.26" , "car", "RigedyRigedyrektSon");
	}
	
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

	public ArrayList<Double>[] getPosData(int ID) throws SQLException
	{
		Statement stmt = (Statement) connection.createStatement();
		ResultSet rs = stmt.executeQuery("CALL getPos("+ID+")");
		ArrayList<Double>[] output = (ArrayList<Double>[])new ArrayList[5];
		
		output[0] = new ArrayList<Double>();
		output[1] = new ArrayList<Double>();
		
		while(rs.next())
		{
			output[1].add(rs.getDouble("Lon"));
			output[0].add(rs.getDouble("Lat"));
		}
		stmt.close();
		return output;
		
	}
	
	public ArrayList<DatabaseLocation> getPosClass(int id) throws SQLException
	{
		Statement stmt = (Statement) connection.createStatement();
		ResultSet rs = stmt.executeQuery("CALL getPos("+id+")");
		
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
		String values= "INSERT INTO positionhistorytable VALUES " ;		
		stmt.execute("DELETE FROM positionhistorytable WHERE ID="+ ID);
		for(int i=0;i<input.length-1;i++)
		{
			values += "(" + ID + ","+ input[i].getLon() + "," + input[i].getLat() + "," + input[i].getHTime() + "," + input[i].getMTime() + "," + input[i].getNLon() + "," + input[i].getNLat() +"),";  
		}
		values += "(" + ID + ","+ input[input.length-1].getLon() + "," + input[input.length-1].getLat() + "," + input[input.length-1].getHTime() + "," + input[input.length-1].getMTime() + "," + input[input.length-1].getNLon() + "," + input[input.length-1].getNLat() +");";
		
		System.out.println(values);
		
		
		stmt.execute(values);
		stmt.close();		
		
	}
	
	public void addUserData(String name) throws SQLException
	{
		Statement stmt = (Statement) connection.createStatement();
		stmt.executeQuery("CALL createUser('"+ name +"')");
		stmt.close();
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
		
		public DBQuerry(double la, double lo,int hour, int min, double nla,double nlo)
		{
			this.lat = la;
			this.lon = lo;
			minutes = min;
			hours = hour;
			nextLat = nla;
			nextLon = nlo;
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
		
		public void setPos(double lon, double lat)
		{
			this.lon = lon; 
			this.lat = lat;
		}
			
	}

}
