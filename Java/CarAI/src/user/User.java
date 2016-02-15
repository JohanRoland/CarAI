package user;

import java.util.ArrayList;

import serverConnection.ServerConnection;

public class User {

	//User variables
	private int userID;
	String userName;
	
	//Database related
	ServerConnection sc;
	
	public User(String id)
	{
		sc = ServerConnection.getInstance();
		if(!id.equals(""))
			importFromDB(id);
		
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
			System.exit(-1);
		}
		return false;
	}
}
