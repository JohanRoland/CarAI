package utils;

import java.util.Objects;

/**
 * 
 * @author Johan Ekdahl
 *
 * @param <T>
 * 
 * A tuple class that makes a tuple of type T with a get and set method as well as a to string method
 * 
 */
public class Tuple<T,F>
{
	private T lat;
	private F lon;

	/**
	 * @param first The first value of the tuple
	 * @param second The second value of the tuple
	 */
	public Tuple(T first, F second)
	{
		lat = first;
		lon = second;
	
	}
	/**
	 * Overloads the toString method and prints the result as
	 * ( first , second ) 
	 */
	public String toString(){return "("+lat.toString()+","+lon.toString()+")";}
	/**
	 * @return Returns the first value
	 */
	public T fst(){return lat;}
	/**
	 * @return Returns the second value
	 */
	public F snd(){return lon;}
	/**
	 * @param in The value to set the first value of the tuple
	 * Sets the first value
	 */
	public void setFst(T in){lat=in;}
	/**
	 * @param in The value to set the second value of the tuple
	 * set the second value
	 */
	public void setSnd(F in){lon=in;}
	/**
	 * Provides a hash for the Tuple
	 */
	public int hashCode()
	{
		
		return Objects.hash(lat,lon);// Math.floorMod(a.hashCode()*17+b.hashCode()*31,Integer.MAX_VALUE) ; 
	}
	/**
	 * Two Tuples are considered equal of there elements are equal.
	 * Needless to say, this requires the elements to have an equals method
	 * to provide a sensible output.
	 */
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Tuple))
			return false;
		if(obj == this)
			return true; 
		
		Tuple rhs = (Tuple) obj;
		if(rhs.lat.equals(this.lat) && rhs.lon.equals(this.lon))
			return true;
		
		return false;
	}
}