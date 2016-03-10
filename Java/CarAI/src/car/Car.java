package car;

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
	
	public synchronized void setUser(String pos, String usr)
	{
		users.put(getSeatPosition(pos),new User(usr));
	}
	
	public synchronized void setCar(JSONCAR car)
	{
		if(car.DRIVER.length > 1)
		{
			users.put(Positions.DRIVER,new User(car.DRIVER[0]));
		}
		else
		{
			users.put(Positions.DRIVER,new User(""));
		}
		if(car.PASSENGER.length > 1)
		{
			users.put(Positions.PASSENGER,new User(car.PASSENGER[0]));
		}
		else
		{
			users.put(Positions.PASSENGER,new User(""));
		}
		if(car.BACKSEAT0.length > 1)
		{
			users.put(Positions.BACKSEAT0,new User(car.BACKSEAT0[0]));
		}
		else
		{
			users.put(Positions.BACKSEAT0,new User(""));
		}
		if(car.BACKSEAT1.length > 1)
		{
			users.put(Positions.BACKSEAT1,new User(car.BACKSEAT1[0]));
		}
		else
		{
			users.put(Positions.BACKSEAT1,new User(""));
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
