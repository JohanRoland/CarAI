package car;

import java.util.ArrayList;
import java.util.HashMap;

import user.User;
import utils.JSONCAR;
import utils.Tuple;


public class Car {

	public static Car instance;
	
	public enum Positions {DRIVER, PASSENGER, BACKSEAT0 , BACKSEAT1};
	
	public Tuple<Double,Double> position;
	private HashMap<Positions,User> users;
	
	
	private Car(){
		users = new HashMap<Positions,User>();		
		position = new Tuple<Double,Double>(0.0,0.0);
	}
	
	public static Car getInstance()
	{
		if(instance == null)
		{
			instance = new Car();
		}
		
		return instance;
	}
	
	public Positions getSeatPosition(String s)
	{
		if(s.toLowerCase().equals("driver"))
			return Positions.DRIVER;
		else if(s.toLowerCase().equals("passenger"))
			return Positions.PASSENGER;
		else if(s.toLowerCase().equals("backseat0"))
			return Positions.BACKSEAT0;
		else if(s.toLowerCase().equals("backseat1"))
			return Positions.BACKSEAT1;
		
		return null;
	}
	
	public synchronized User getUser(String position)
	{
		if(users.containsKey(getSeatPosition(position)))
			return users.get(getSeatPosition(position));
		else
			return null; 
	}
	/**
	 * Returns all users organized as: driver, front passenger,
	 * left back passenger, right back passenger.
	 * @return an ArrayList of the users in the car.
	 */
	public ArrayList<User> getAllUsers()
	{
		ArrayList<User> output= new ArrayList<User>();
		output.add(users.get(Positions.DRIVER));
		output.add(users.get(Positions.PASSENGER));
		output.add(users.get(Positions.BACKSEAT0));
		output.add(users.get(Positions.BACKSEAT1));
		return output;
	}
	
	public synchronized void setUser(String pos, String usr)
	{
		users.put(getSeatPosition(pos), User.getInstance(Integer.parseInt(usr)));
	}
	
	public synchronized void setCar(JSONCAR car)
	{
		if(car.DRIVER.length > 1)
		{
			users.put(Positions.DRIVER, User.getInstance(Integer.parseInt(car.DRIVER[0])));
		}
		else
		{
			users.put(Positions.DRIVER,User.getInstance(-1));
		}
		if(car.PASSENGER.length > 1)
		{
			users.put(Positions.PASSENGER,User.getInstance(Integer.parseInt(car.PASSENGER[0])));
		}
		else
		{
			users.put(Positions.PASSENGER,User.getInstance(-1));
		}
		if(car.BACKSEAT0.length > 1)
		{
			users.put(Positions.BACKSEAT0,User.getInstance(Integer.parseInt(car.BACKSEAT0[0])));
		}
		else
		{
			users.put(Positions.BACKSEAT0,User.getInstance(-1));
		}
		if(car.BACKSEAT1.length > 1)
		{
			users.put(Positions.BACKSEAT1,User.getInstance(Integer.parseInt(car.BACKSEAT1[0])));
		}
		else
		{
			users.put(Positions.BACKSEAT1,User.getInstance(-1));
		}
		
		
		
	}
	
	public synchronized void setPos(Double lon,Double lat) 
	{
		position.setFst(lon);
		position.setSnd(lat);
	}
	
	public synchronized Tuple<Double,Double> getPos()
	{
		return position;
	}
}
