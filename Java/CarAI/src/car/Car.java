package car;

import java.util.HashMap;

import user.User;
import utils.JSONCAR;


public class Car {

	public enum Positions {DRIVER, PASSENGER, BACKSEAT0 , BACKSEAT1};
	public static Car instance;
	
	private HashMap<Positions,User> users;
	private Car(){
		users = new HashMap<Positions,User>();
		for(Positions p : Positions.values() )
		{
			users.put(p, null);
		}
	}
	
	public static Car getInstance()
	{
		if(instance == null)
		{
			instance = new Car();
		}
		
		return instance;
	}
	
	public Positions getPosition(String s)
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
		return users.get(getPosition(position));
	}
	
	public synchronized void setUser(String pos, String usr)
	{
		users.put(getPosition(pos),new User(usr));
	}
	
	public synchronized void setCar(JSONCAR car)
	{
		users.put(Positions.DRIVER,new User(car.DRIVER));
		users.put(Positions.PASSENGER,new User(car.PASSENGER));
		users.put(Positions.BACKSEAT0,new User(car.BACKSEAT0));
		users.put(Positions.BACKSEAT1,new User(car.BACKSEAT1));
		System.out.println("Passengers updated" + users.toString());
	}
}
