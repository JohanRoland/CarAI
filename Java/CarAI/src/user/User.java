package user;

import java.util.ArrayList;

import serverConnection.ServerConnection;
/**
 * A class for representing a user
 * with an ID and an alias.
 * This class can also store a ServerConnection,
 * so that the user and his data can be collected
 *  from the server.
 * @author William
 */
public class User {

	//User variables
	private int userID;
	String userName;
	
	//Database related
	ServerConnection sc;
	
	public User(String id)
	{
		sc = ServerConnection.getInstance();
		if(!id.equals("") && !id.equals("0"))
		{
			//userID = 1;
			//userName = "William";
			importFromDB(id);
		}
		else if (id.equals("0"))
		{
			userID = 0;
			userName = "Unknown";
		}
		else
		{
			userID = -1;
			userName = "Empty";
		}
	}
	
	public int getUserID()
	{
		return userID;
	}
	
	public String getUserName()
	{
		return userName;
	}
	
	public boolean userExists()
	{
		return (userID >0);
	}
	
	private boolean querryUser()
	{
		return true;
	}
	
	private boolean createNewUser()
	{
		return true; 
	}
	
	private void exportDB()
	{
		
	}
	
	private boolean importFromDB(String id)
	{
		try{
			ArrayList<String> userData = sc.getUserData(id);
			if(userData != null)
			{
				userID = Integer.parseInt(userData.get(0)); 
				userName = userData.get(1);
			}
			
			return true; 
		}
		catch(Exception e)
		{
			System.out.println("Exception fetchin user "+ id);
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}
}
