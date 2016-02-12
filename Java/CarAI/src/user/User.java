package user;

import serverConnection.ServerConnection;

public class User {

	//User variables
	private int userID;
	String userName;
	
	//Database related
	ServerConnection sc;
	
	public User(String name)
	{
		sc = ServerConnection.getInstance();
		
		if(!querryUser())
		{
			createNewUser();
		}
		else
		{
			importFromDB();
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
	
	private void importFromDB()
	{
		
	}
}
