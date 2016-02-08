package serverConnection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class ServerConnection {
	Connection connection;
	public ServerConnection(String serverName, String serverPort, String serverLocation, String userName, String Password)
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

	public ArrayList<double[]> getPosData(int ID) throws SQLException
	{
		Statement stmt = (Statement) connection.createStatement();
		ResultSet rs = stmt.executeQuery("CALL getPos"+ID+")");
		ArrayList<double[]> output = new ArrayList<double[]>();
		double[] temp =new double[2];
		
		while(rs.next())
		{
			temp[0]=rs.getDouble("Long");
			temp[1]=rs.getDouble("Lat");
			output.add(temp);
		}
		stmt.close();
		return output;
		
	}
	public void addPosData(int ID, double Long, double Lat, double time, double Long2,double Lat2) throws SQLException
	{
		Statement stmt = (Statement) connection.createStatement();
		stmt.executeQuery("CALL getPos"+ID+","+Long+","+Lat+","+time+","+","+Long2+","+Lat2+")");
		stmt.close();
	}
	public void replacePosData(int ID, double[] Longs, double[] Lats) throws SQLException
	{
		if(Longs.length!=Lats.length)
			throw new SQLException("Length of Longs and Lats must match");
		
		Statement stmt = (Statement) connection.createStatement();
		String values= "INSERT INTO positionhistorytable VALUES " ;		
		stmt.execute("DELETE FROM positionhistorytable WHERE ID="+ ID);
		for(int i=0;i<Longs.length-1;i++)
		{
			values += "(" + ID + ","+ Longs[i] + "," + Lats[i] + "),";  
		}
		values += "(" + ID + ","+ Longs[Longs.length-1] + "," + Lats[Longs.length-1] + ");";
		
		System.out.println(values);
		
		
		stmt.execute(values);
		stmt.close();		
		
	}
	

}
