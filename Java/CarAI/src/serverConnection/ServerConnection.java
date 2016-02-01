package serverConnection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

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
		} catch (SQLException e) {
			System.out.println("faild to send/recive request\n");
			e.printStackTrace();
		}		
		return rs;
	}


}
