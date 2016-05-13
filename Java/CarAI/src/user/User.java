package user;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import serverConnection.ServerConnection;
import utils.Tuple;
/**
 * A class for representing a user
 * with an ID and an alias.
 * This class can also store a ServerConnection,
 * so that the user and his data can be collected
 *  from the server.
 * @author William
 */
public class User {

	static HashMap<Integer,User> instances;
	
	//User variables
	private int userID;
	String userName;
	ArrayList<String> imgPaths;
	String imgPath;
	//Database related
	static ServerConnection sc;
	
	private User(int id)
	{
		sc = ServerConnection.getInstance();
		
		if(id != -1 && id != 0)
		{
			importFromDB(""+id);

			
			//   ADD THE IMAGE MATCHING IMAGES
			imgPaths = new ArrayList<String>(); 
			File f = new File(".");
			String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
			imgPath = pathToProj + File.separator+"Data" +File.separator+"Users" +File.separator+ id;
			File folder = new File(imgPath);
			File[] imgs = folder.listFiles(new FilenameFilter() {@Override public boolean accept(File dir,String name){return name.endsWith(".jpg");}});
			for(File file : imgs)
			{
				imgPaths.add(file.getAbsolutePath());
			}
			// END ADDING IMAGE MATCHING
			
		}
		else if (id == 0)
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
	
	
	public static User getInstance(int id)
	{
		if(instances == null)
		{
			instances = new HashMap<Integer,User>();
		}
		if(!instances.containsKey(id))
		{
			instances.put(id, new User(id));
		}
		return instances.get(id);
	}
	
	public static void getAllUserImgs(String dirName,ArrayList<String> files )
	{
		File directory = new File(dirName);
		File[] fList = directory.listFiles();
		for(File file : fList)
		{
			if(file.isFile())
			{
				boolean temp = file.getName().endsWith("jpg");
				if(temp)
				{
					files.add(file.getAbsolutePath());
				}
			}
			else if(file.isDirectory())
			{
				getAllUserImgs(file.getAbsolutePath(),files);
			}
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
	/**
	 * TODO
	 */
	public void loadPredictionNetwork()
	{
		
	}
	/**
	 *  TODO
	 * @return
	 */
	
	/**
	 * TODO
	 */
	public Tuple<Tuple<Double,Double>,Double> getNextApointment()
	{//					GPS, 			time
		
		return null;
	}
	
	public Tuple<Double,Double> predicLoc()
	{
		return null;
	}
	
	public boolean userExists()
	{
		return (userID >0);
	}
	
	public ArrayList<String> getImgPath()
	{
		return imgPaths;
	}
	
	/**
	 * Export as String parsed for being added to a csv file
	 * @return 
	 */
	public String exportToCsv()
	{
		String exp = "";
		for(String i : imgPaths)
		{
			exp = exp + i +";" + userID + "\n";
		}
		return exp;
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
