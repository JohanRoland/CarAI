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
	private T a;
	private F b;

	/**
	 * @param first The first value of the tuple
	 * @param second The second value of the tuple
	 */
	public Tuple(T first, F second)
	{
		a = first;
		b = second;
	
	}
	/**
	 * Overloads the toString method and prints the result as
	 * ( first , second ) 
	 */
	public String toString(){return "("+a.toString()+","+b.toString()+")";}
	/**
	 * @return Returns the first value
	 */
	public T fst(){return a;}
	/**
	 * @return Returns the second value
	 */
	public F snd(){return b;}
	/**
	 * @param in The value to set the first value of the tuple
	 * Sets the first value
	 */
	public void setFst(T in){a=in;}
	/**
	 * @param in The value to set the second value of the tuple
	 * set the second value
	 */
	public void setSnd(F in){b=in;}
	
	public int hashCode()
	{
		
		return Objects.hash(a,b);// Math.floorMod(a.hashCode()*17+b.hashCode()*31,Integer.MAX_VALUE) ; 
	}
	
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Tuple))
			return false;
		if(obj == this)
			return true; 
		
		Tuple rhs = (Tuple) obj;
		if(rhs.a.equals(this.a) && rhs.b.equals(this.b))
			return true;
		
		return false;
	}
}