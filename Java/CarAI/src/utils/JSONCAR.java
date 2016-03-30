package utils;
/**
 * 
 * A class containing four fields,
 * used to represents individuals
 * in a four seat car.
 * 
 * @author William
 *
 */
public class JSONCAR {
	
	public String[] DRIVER = {};
	public String[] PASSENGER = {};
	public String[] BACKSEAT0 = {};
	public String[] BACKSEAT1 = {};
	
	public JSONCAR(String[] d,String[] p,String[] b0,String[] b1)
	{
		DRIVER = d;
		PASSENGER = p;
		BACKSEAT0 = b0;
		BACKSEAT1 = b1;
	}
	
	
	public void setDRIVER(String[] d)
	{
		DRIVER = d;
	}
	public void setPASSENGER(String[] p)
	{
		PASSENGER = p;
	}
	public void setBACKSEAT0(String[] b)
	{
		DRIVER = b;
	}
	public void setBACKSEAT1(String[] b)
	{
		DRIVER = b;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DRIVER: " );
		if(DRIVER.length > 1)
		{
			sb.append(DRIVER[0]);
		}
		sb.append(", PASSENGER: ");
		if(PASSENGER.length > 1)
		{
			sb.append(PASSENGER[0]);
		}
		sb.append(", BACKSEAT: ");
		if(BACKSEAT0.length > 1)
		{
			sb.append(BACKSEAT0[0]);
		}
		sb.append(", BACKSEAT1: ");
		if(BACKSEAT1.length > 1)
		{
			sb.append( BACKSEAT1[0]);
		}
		
		return  sb.toString(); 
	}
}


