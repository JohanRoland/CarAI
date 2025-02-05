package facerecognition;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.opencv.core.*;
import org.opencv.objdetect.*;
import org.opencv.imgproc.*;
import org.opencv.videoio.*;
import org.opencv.imgcodecs.*;
import org.opencv.face.*;

import com.google.gson.*;

import user.User;
import utils.JSONCAR;

@SuppressWarnings("unused")
public class FaceRecognition
{
	int eyedetect = 0;
	
	Window win; 
	CascadeClassifier face_cascade;
    CascadeClassifier eyes_cascade,reye_cascade,leye_cascade;
    LBPHFaceRecognizer fr;
    Mat frame;
    Mat FaceImage;
    int imWidht,imHeight;
    HashMap<Integer,Person> pers; 
    CarView cv;
    //String pathToProj; 
    String pathToUsers;
    String pathToTemp;
    int counter = 0; 
    
    public FaceRecognition() {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    	
    	File f = new File(".");
		//pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
		pathToUsers = f.getAbsoluteFile().getParentFile().getParentFile().getParent() +File.separator+ "Data"+File.separator+ "Users"+File.separator;
    	//pathToTemp = f.getAbsoluteFile().getParentFile().getParentFile().getParent() + "\\Data\\TempImgs\\";
		pathToTemp = f.getAbsoluteFile().getParentFile().getParentFile().getParent() +File.separator+ "Data"+File.separator+ "TempImgs"+File.separator;
		File tf = new File(pathToTemp);
		if(!tf.exists())
		{
			tf.mkdirs();
		}
		
	    frame = new Mat();
	    FaceImage = new Mat();  
	    //face_cascade = new CascadeClassifier("C:\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_alt.xml");
	    // Eyes cascade used to align face images
	    //eyes_cascade = new CascadeClassifier("C:\\opencv\\sources\\data\\haarcascades\\haarcascade_eye.xml"); // _tree_eyeglasses
	    
	    
	    try{
	    	String faceCascadePath = f.getCanonicalPath() +File.separator+"src"+File.separator+"haarcascade_frontalface_alt.xml";
	    	face_cascade = new CascadeClassifier(faceCascadePath);
	    	// Eyes cascade used to align face images
	    	eyes_cascade = new CascadeClassifier(f.getCanonicalPath() +File.separator+"src"+File.separator+"haarcascade_eye.xml"); // _tree_eyeglasses
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    pers = new HashMap<Integer,Person>();
	    
	    Csv cs =  loadCsv2();//loadCsv(pathToProj+"\\test.csv");
	    
	    imWidht =  cs.getImgs().get(0).width();
	    imHeight =  cs.getImgs().get(0).height();
	    
	    Mat lab = new Mat(cs.getLabels().size(),1, CvType.CV_32SC1 );
	    
	    for(int i = 0; i < cs.getLabels().size(); i++)
	    {
	    	lab.put(i, 0, cs.getLabels().get(i));
	    }
	    
	    fr=  Face.createLBPHFaceRecognizer();
	    fr.setThreshold(60.0);
	    fr.train(cs.getImgs(), lab);
	    cv = new CarView();
    }
    
    /**
     * Start a continuous facecaptureing process
     */
    public void start(boolean window)
    {
    	if(window)
    		win = new Window();
    	
    	VideoCapture vc = new VideoCapture(0);
	    if (vc.isOpened())
	    {
	    	while(true)
	    	{
	    		vc.read(frame);
	    		if(!frame.empty())
	    		{
	    			detectAndDisplay(frame,window);
	    		}
	    		else
	    		{
	    			System.out.println("Error no captured frame");
	    		}
	    	}
	    		
	    }
    }
    
    public String sample()
    {
    	CarView c = new CarView();
    	VideoCapture vc = new VideoCapture(0);
    	deleteTemp(); 
	    if (vc.isOpened())
	    {
	    	ArrayList<String> d = new ArrayList<String>();
	    	ArrayList<String> p = new ArrayList<String>();
	    	ArrayList<String> b0 = new ArrayList<String>();
	    	ArrayList<String> b1 = new ArrayList<String>();
	    	ArrayList<String> dc = new ArrayList<String>();
	    	ArrayList<String> pc = new ArrayList<String>();
	    	ArrayList<String> b0c = new ArrayList<String>();
	    	ArrayList<String> b1c = new ArrayList<String>();
	    	for(int i = 0; i < 10; i++)
	    	{
	    		counter = i;
	    		vc.read(frame);
	    		if(!frame.empty())
	    		{
	    			detectAndDisplay(frame,false);
	    			d.add(cv.exportPerson()[cv.DRIVER][0]);
	    			dc.add(cv.exportPerson()[cv.DRIVER][1]);
	    			
	    			p.add(cv.exportPerson()[cv.PASSENGER][0]);
	    			pc.add(cv.exportPerson()[cv.PASSENGER][1]);
	    			
	    			b0.add(cv.exportPerson()[cv.BACKSEAT0][0]);
	    			b0c.add(cv.exportPerson()[cv.BACKSEAT0][1]);
	    			
	    			b1.add(cv.exportPerson()[cv.BACKSEAT1][0]);
	    			b1c.add(cv.exportPerson()[cv.BACKSEAT1][1]);
	    		}
	    		else
	    		{
	    			System.out.println("Error no captured frame");
	    		}
	    	}
	    	
	    	c.setPerson(getPop(d.toArray(new String[d.size()]),dc.toArray(new String[dc.size()])),cv.DRIVER);
	    	c.setPerson(getPop(p.toArray(new String[p.size()]),pc.toArray(new String[pc.size()])),cv.PASSENGER);
	    	c.setPerson(getPop(b0.toArray(new String[b0.size()]),b0c.toArray(new String[b0c.size()])),cv.BACKSEAT0);
	    	c.setPerson(getPop(b1.toArray(new String[b1.size()]),b1c.toArray(new String[b1c.size()])),cv.BACKSEAT1);
	    }
	    vc.release();
    	return c.exportJSONPerson();
    }
    
    private String[] getPop(String[] a,String[] c)
    {
    	
    	// Empty Check
    	List<String> check1 = new ArrayList<String>();
    	Collections.addAll(check1, a);
    	check1.removeAll(Arrays.asList(""));
    	a = check1.toArray(new String[check1.size()]);
    	check1.clear();
    	Collections.addAll(check1, c);
    	check1.removeAll(Arrays.asList(""));
    	c = check1.toArray(new String[check1.size()]);
    	if(a.length != 0 && c.length != 0)
    	{
	    	int count = 1, tempc;
	    	String pop = a[0];
	    	String temp = "";
	    	
	    	HashMap<String,ArrayList<Double>> conf = new HashMap<String,ArrayList<Double>>();
	    	
	    	for(int i = 0; i < a.length -1; i++)
	    	{
	    		temp = a[i];
	    		
	    		tempc= 0;
	    		for(int j = 1; j < a.length ;j++)
	    		{
	    			if(temp.equals(a[j]))
	    			{
	    				tempc++;
	    			}
	    		}
	    		if(tempc > count)
	    		{
	    			count = tempc;
	    			pop = temp; 
	    		}
	    	}
	    	
	    	for(int i = 0; i < a.length; i++)
	    	{
	    		if(!conf.containsKey(a[i]))
	    		{
	    			conf.put(a[i], new ArrayList<Double>());
	    		}
	    		
	    		
	    		
	    		if(!c[i].equals(""))
	    		{
	    			double td = Double.parseDouble(c[i].replace(",", "."));
	    			
	    			if(td <= 100)
	    				conf.get(a[i]).add(1 - (td/100));
	    			else
	    				conf.get(a[i]).add(0.0);
	    		}
			}
	    	
	    	double oc = conf.get(temp).stream().mapToDouble(d -> d).average().getAsDouble();
	    	if(oc > 0.3)
	    		oc = 1; 
	    		
	    	String[] out = {temp,""+oc};
	    	return out;
    	}
    	else
    	{
    		String[] out = {};
	    	return out;
    	}
    }
    
    /**
     * Helper function to detect faces in an capture frame
     * @param frame Frame cam source
     */
    private void detectAndDisplay(Mat frame,boolean window)
    {
    	MatOfRect faces = new MatOfRect(); 
    	Mat frame_gray = new Mat();
    	Imgproc.cvtColor(frame, frame_gray,Imgproc.COLOR_BGR2GRAY);
    	Imgproc.equalizeHist(frame_gray, frame_gray);
    	Mat faceROI = new Mat();
    	face_cascade.detectMultiScale(frame_gray, faces);
    	cv.clearCar();
    	for(Rect face: faces.toArray())
    	{
    		Point center = new Point(face.x+face.width*0.5,face.y+face.height*0.5);
    		Imgproc.ellipse(frame, center, new Size(face.width*0.5,face.height*0.5), 0, 0, 360, new Scalar(255,0,255), 4, 8, 0);
    		Mat face_resized = frame_gray.submat(face);
    		

    		// Keeping it for reference 
    		if(eyedetect == 1)
    		{
    			int t1Y = face.y+((int)(face.height*0.2));
    			if(t1Y < 0 ) t1Y = 0;
    			
    			int drY = face.y+((int)(face.height*0.6)); 
    			if(drY > frame.rows()) drY = frame.rows();
    			Point t1 = new Point(face.x,t1Y);
    			Point dr = new Point(face.x + face.width,drY);
    			
    			
    			Rect eyeCroped = new Rect(t1,dr); 
	    		faceROI = frame_gray.submat(eyeCroped);
	    		
	    		
	    		MatOfRect eyes = new MatOfRect();
	    		//eyes_cascade.detectMultiScale(faceROI, eyes);
	        	eyes_cascade.detectMultiScale(faceROI, eyes, 1.1, 2, Objdetect.CASCADE_DO_CANNY_PRUNING, new Size(faceROI.width()*0.2,faceROI.width()*0.2), new Size(40,40));
	    		Point[] ecenters = new Point[2];
	        	for(Rect eye: eyes.toArray())
	    		{
	    			Point center2 = new Point(face.x+eye.x+eye.width*0.5,face.y+eye.y+eye.height*0.5+((int)(face.height*0.2)));
	    			int radius = (int)Math.round((eye.width + eye.height)*0.25);
	    			Imgproc.circle(frame, center2, radius, new Scalar(255,0,0));
	    			
    				if (ecenters[0] == null)
    					ecenters[0] = center2;
    				else if (ecenters[0].x > center2.x)
    					ecenters[1] = center2;
    				else
    				{
    					ecenters[1] = ecenters[0];
    					ecenters[0] = center2; 
					}
    			
	    		}
	        	
	        	if (ecenters[0] != null  && ecenters[1] != null  )
	        		face_resized = crpFace(frame_gray,ecenters[0],ecenters[1],new Point(0.2,0.2),new Size(70,70));
	        		//face_resized = cropFace(face_resized,ecenters[0],ecenters[1],face.tl(),new Point(face.width,face.height) );
	        	
    		}
    		
    		FaceImage = face_resized;
    		int[] pred = new int[1];
    		double[] conf = new double[1]; 
    		
    		fr.predict(face_resized, pred, conf);
    		
    		String nameString;
    		if(pred[0] < 0)
    			nameString = "0";
    		else
    			nameString = pers.get(pred[0]).getName();
    		String textBox = "P = " + nameString + " C =" + new DecimalFormat("#.##").format(conf[0]); 
			int pos_x = (int)Math.max(face.tl().x-10,0);
			int pos_y = (int)Math.max(face.tl().y-10,0);
		
			Imgproc.putText(frame, textBox, new Point(pos_x,pos_y), 0, 1.0, new Scalar(0,255,0));
			
			cv.parsePerson2(nameString, new DecimalFormat("#.##").format(conf[0]), center, new Point(frame.cols()/2,frame.rows()/2));
			
			String tempPath = pathToTemp+cv.getPos2(center, new Point(frame.cols()/2,frame.rows()/2))+File.separator+"t"+counter+".jpg";
			Imgcodecs.imwrite(tempPath,face_resized); 
			
    		
    	}
    	if(window)
    	{
    		win.updateImage(frame);
    		win.updateFace(FaceImage);
    	}
    }
    
    public String getFaces()
    {
    	return cv.exportJSONPerson();
    }
    
    
    private Mat cropFace (Mat Image, Point leftEye, Point rightEye, Point faceCoord,Point faceSize)
    {
    	Mat dstImg = new Mat();
    	Mat Crop = new Mat();
    	
    	if(!(leftEye.x == 0 && leftEye.y == 0))
    	{
    		double eye_directionX = rightEye.x - leftEye.x;
    		double eye_directionY = rightEye.y - leftEye.y;
    		double rot = -Math.atan2(eye_directionY,eye_directionX*(180/Math.PI));
    		dstImg = rotate(Image,rot);
    	}
    	
    	
    	return dstImg; 
    }
    
    private Mat crpFace (Mat image, Point leftEye, Point rightEye, Point offset_pct, Size outSize)
    {
    	double offset_h = Math.floor(offset_pct.x*outSize.height);
    	double offset_w = Math.floor(offset_pct.y*outSize.width);
    	
    	Point eyeDir = new Point(rightEye.x - leftEye.x,rightEye.y - leftEye.y);
    	double rot =  -Math.atan2(eyeDir.y,eyeDir.x);
    	double dist = Math.sqrt(Math.pow(rightEye.x-leftEye.x,2)+Math.pow(rightEye.y-leftEye.y,2));
    	
    	double reference = outSize.height - 2* offset_h; 
    	double scale = dist / reference; 
    	
    	Mat rotM = Imgproc.getRotationMatrix2D(leftEye, rot, 1.0);
    	
    	Mat out = new Mat();
    	
    	Imgproc.warpAffine(image, out, rotM, new Size(image.width(),image.height()));
    	
    	//Crop the image to the destination Size
    	Point crop_xy = new Point(rightEye.x - scale*offset_h, rightEye.y-scale*offset_w);
    	Size crop_size = new Size(outSize.width*scale,outSize.height*scale);
    	Rect cropRec = new Rect((int)crop_xy.x,(int)crop_xy.y,(int)crop_size.width,(int)crop_size.height);
    	
    	out = out.submat(cropRec);
    	
    	return out; 
    }
    
    private Mat rotate(Mat src, double angle)
    {
    	Mat out = new Mat();
    	int len = Math.max(src.cols(), src.rows());
    	Point pt = new Point(len/2, len/2);
    	Mat r  = Imgproc.getRotationMatrix2D(pt, angle, 1.0);
    	Imgproc.warpAffine(src, out, r, new Size(len,len));
    	return out;
    }

    /**
     * Loading a Csv file where the face detection samples are stored
     * @param path Path to file
     */
    public Csv loadCsv(String path)
    {
    	ArrayList<Mat> imgs = new ArrayList<Mat>();
    	ArrayList<Integer> labels = new ArrayList<Integer>();
    	
    	try{
    		List<String> ls = Files.readAllLines(Paths.get(path));
    		
    		for(String l : ls)
    		{
    			String[] t = l.split(";");
    			if (t.length > 0)
    			{
    				int label = Integer.parseInt(t[1]);
    				
    				if(pers.size()-1 < label)
    				{
    					
    					String[] spaht = t[0].split(Pattern.quote(File.separator));
    					String name = spaht[spaht.length-2];
    					pers.put(label, new Person(label,name));
    				}

					pers.get(label).addImage(t[0]);
    						
    				imgs.add(Imgcodecs.imread(t[0],0)); 
    				labels.add(label);
    			}
    			else
    			{
    				System.out.println("Error reading line " + l);
    			}
    		}
    	}
    	catch(Exception e)
    	{
    		System.out.println("Error reading Csv File");
    		System.out.println(e.toString());
    		System.exit(-1);
    	}
    	
    	return new Csv(imgs,labels);
    }
    
    public Csv loadCsv2()
    {
    	ArrayList<Mat> imgs = new ArrayList<Mat>();
    	ArrayList<Integer> labels = new ArrayList<Integer>();
    	
    	try{
    		
    		ArrayList<String> ls = new ArrayList<String>();
			User.getAllUserImgs(pathToUsers, ls);
    		for(String l : ls)
    		{
    			File f = new File(l);
				int label = Integer.parseInt(f.getParentFile().getName());
				
				if(pers.size()-1 < label)
				{
					
					String[] spaht = f.getAbsolutePath().split(Pattern.quote(File.separator));
					String name = spaht[spaht.length-2];
					pers.put(label, new Person(label,name));
				}

				pers.get(label).addImage(f.getAbsolutePath());
						
				imgs.add(Imgcodecs.imread(f.getAbsolutePath(),0)); 
				labels.add(label);
    			
    		}
    	}
    	catch(Exception e)
    	{
    		System.out.println("Error reading Csv File");
    		System.out.println(e.toString());
    		System.exit(-1);
    	}
    	
    	return new Csv(imgs,labels);
    }
    
    /**
     * Writes a hashmap of persons to the CSV file
     * @param ps Hashmap of persons
     */
    private void writeToCsv(HashMap<Integer,Person> ps)
    {
    	int i = 0; 
    	for(Person p : ps.values())
    	{
	    	try{
		    		boolean append;
		    		if(i == 0)
		    		{
		    			append = false;
		    			i++;
		    		}
		    		else
		    		{
		    			append = true;
		    		}
		    			
					FileWriter f = new FileWriter(Paths.get("test.csv").toFile(),append);
					f.write(p.exportToCsv());
					f.close();
					
	    	}catch(Exception e)
			{
				System.out.println("Error writing CSV for " + p.name);
			}	
    	}
    	
    }
    
    private double distance(Point p1, Point p2)
    {
    	return Math.sqrt(Math.pow(p2.x-p1.x,2)+Math.pow(p2.y-p1.y,2));
    }
    
    private void deleteTemp()
    {
    	File f = new File(pathToTemp+"Driver");
    	if(!f.exists())
    	{
    		f.mkdirs();
    	}
    	File f1 = new File(pathToTemp+"Passenger");
    	if(!f1.exists())
    	{
    		f1.mkdirs();
    	}
    	File f2 = new File(pathToTemp+"Backseat0");
    	if(!f2.exists())
    	{
    		f2.mkdirs();
    	}
    	File f3 = new File(pathToTemp+"Backseat1");
    	if(!f3.exists())
    	{
    		f3.mkdirs();
    	}
    	
    	for(File file: f.listFiles()) file.delete();
    	for(File file: f1.listFiles()) file.delete();
    	for(File file: f2.listFiles()) file.delete();
    	for(File file: f3.listFiles()) file.delete();
    }
    
    public void moveFromTemp(String fromseat,String touser)
    {
    	File uf = new File(pathToUsers+touser);
    	if(!uf.exists())
    	{
    		uf.mkdirs();
    	}
    	
    	int counter = uf.listFiles().length;
    	
    	File tf = new File(pathToTemp+fromseat);
    	for(File f :tf.listFiles())
    	{
    		f.renameTo(new File(uf.getAbsolutePath()+File.separator+touser+"-"+counter+".jpg"));
    		counter++;
    	}
    	
    	
    }
    
    /**
     * Helper class for persons
     * @author William
     *
     */
    public class Person
    {
    	private int label; 
    	private String name;
    	private ArrayList<String> imgs;
    	
    	public Person(int l, String n)
    	{
    		label = l;
    		name = n;
    		imgs = new ArrayList<String>();
    	}
    	
    	/**
    	 * add a new image source path to a person
    	 * @param i source path
    	 */
    	public void addImage(String i)
    	{
    		imgs.add(i);
    	}
    	
    	/**
    	 * Add a new reference image to a person, will write the image to a coresponding folder and add the file to the source list
    	 * @param i image frame of a face
    	 */
    	public void newImage(Mat i)
    	{
    		if(imgs.size() == 0)
    		{
    			new File(pathToUsers+name).mkdir();
    		}
    		Imgcodecs.imwrite(pathToUsers+name+File.separator+name+imgs.size() + ".jpg", i);
    		imgs.add(pathToUsers+name+File.separator+name+imgs.size() + ".jpg");
    	}
    	
    	/**
    	 * Export as String parsed for being added to a csv file
    	 * @return 
    	 */
    	public String exportToCsv()
    	{
    		String exp = "";
    		for(String i : imgs)
    		{
    			exp = exp + i +";" +label + "\n";
    		}
    		return exp;
    	}
    	
    	public String getName()
    	{
    		return name;
    	}
    	
    	public int getLabel()
    	{
    		return label;
    	}
    }
    
    /**
     * Helper class for describing a csv file
     * @author William
     *
     */
    public static class CarView
    {
    	//public enum Seat {DRIVER,PASSENGER,BACKSEAT0,BACKSEAT1};
    	public int DRIVER = 0;
    	public int PASSENGER = 1;
    	public int BACKSEAT0 = 2;
    	public int BACKSEAT1 = 3;
    	    	
    	String[][] internal; 
    	
    	//HashMap<String,Seat> internal;
    	
    	public CarView()
    	{
    		internal = new String[4][];
    		for(int i = 0; i < internal.length; i++)
    		{
    			String[] temp ={"",""} ;
    			internal[i] = temp ;
    		}
    		//internal = new HashMap<String,Seat>();
    	}
    	
    	public CarView(CarView c)
    	{
    		this.internal = c.internal;
    	}
    	
    	private void emptySeat(int s)
    	{
    		String[] temp ={"",""} ;
    		internal[s] = temp;
    	}
    	
    	private void emptyName(String s)
    	{
    		if(!s.equals("Unknown"))
    		{
	    		for(int i = 0; i < internal.length; i++)
	    		{
	    			if(internal[i].equals(s))
	    			{
	        			String[] temp ={"",""} ;
	    				internal[i] = temp;
	    			}
	    		}
    		}
    	}
    	
    	public void clearCar()
    	{
    		for(int i = 0; i < internal.length; i++)
    		{
    			String[] temp ={"",""} ;
    			internal[i] = temp; 
    		}
    	}
    	
    	public void parsePerson(String name,String conf,Point p, Point imgCenter)
    	{
    		
    		if(p.x > imgCenter.x)
    		{
    			if(p.y > imgCenter.y)
    			{
    				emptyName(name);
    				String[] temp ={name,conf} ;
    				internal[DRIVER] = temp;
    			}
    			else
    			{
    				emptyName(name);
    				String[] temp ={name,conf} ;
    				internal[BACKSEAT0] = temp;
    			}
    		}
    		else
    		{
    			if(p.y > imgCenter.y)
    			{
    				emptyName(name);
    				String[] temp ={name,conf} ;
    				internal[PASSENGER] = temp;
    			}
    			else
    			{
    				emptyName(name);
    				String[] temp ={name,conf} ;
    				internal[BACKSEAT1] = temp;
    			}
    		}
    	}
    	
    	public void parsePerson2(String name,String conf,Point p, Point imgCenter)
    	{
    		if(p.x > imgCenter.x)
    		{
				emptyName(name);
				String[] temp ={name,conf} ;
				internal[DRIVER] = temp;

    		}
    		else
    		{
				emptyName(name);
				String[] temp ={name,conf} ;
				internal[PASSENGER] = temp;
			}
    		
    	}
    	
    	
    	public String getPos(Point p,Point imgCenter)
    	{
    		if(p.x > imgCenter.x)
    		{
    			if(p.y > imgCenter.y)
    			{
    				return "Driver";
    			}
    			else
    			{
    				return "Backseat0";
    			}
    		}
    		else
    		{
    			if(p.y > imgCenter.y)
    			{
    				return "Passenger";
    			}
    			else
    			{
    				return "Backseat1";
    			}
    		}
    	}
    	
    	public String getPos2(Point p,Point imgCenter)
    	{
    		if(p.x > imgCenter.x)
    		{
    			return "Driver";
    		}
    		else
    		{
				return "Passenger";
    		}
    	}
    	
    	public void setPerson(String[] name, int pos)
    	{
    		if(name.equals(""))
    		{
    			String[] temp = {};
    			internal[pos] = temp;
    		}
    		else
    		{
    			String[] temp = name;
    			internal[pos] = temp;
    		}
    		
    	}
    	
    	public String[][] exportPerson()
    	{
    		return internal;
    	}
    	
    	public String exportJSONPerson()
    	{
    		Gson g = new Gson();
    		return g.toJson(new JSONCAR(internal[0],internal[1],internal[2],internal[3]));
    	}
    	
    	public String getSeatName(int i)
    	{
    		return internal[i][0];
    	}
    }
    
    public class Csv
    {
    	private ArrayList<Mat> imgs = new ArrayList<Mat>();
    	private ArrayList<Integer> labels = new ArrayList<Integer>();
    	
    	public Csv(ArrayList<Mat> img, ArrayList<Integer> lbs)
    	{
    		imgs = img;
    		labels = lbs;
    	}
    	
    	/**
    	 * 
    	 * @return get all images stored in the csv file
     	 */
    	public ArrayList<Mat> getImgs()
    	{
    		return imgs;
    	}
    	
    	/**
    	 * 
    	 * @return all the labels in the csv file
    	 */
    	public ArrayList<Integer> getLabels()
    	{
    		return labels;
    	}
    }
    
    /**
     * Helper class to create an debug window
     * @author William
     *
     */
    private class Window extends JFrame implements ActionListener 
    {
    	
		/**
		 * Autogenerated Serial from eclipse
		 */
		private static final long serialVersionUID = 482470343519162598L;
		JLabel imgsrc; 
    	JLabel faceImg;
    	JButton saveFace;
    	JPanel contentPane = new JPanel(new BorderLayout());
    	JPanel subPane = new JPanel();
    	JPanel carPane = new JPanel(new GridLayout(0,1));
    	JTextField ltext = new JTextField("",20);
    	JTextField ntext = new JTextField("",20); 
    	JTextField d = new JTextField("",10);
    	JTextField p = new JTextField("",10);
    	JTextField b0 = new JTextField("",10);
    	JTextField b1 = new JTextField("",10);
    	
    	
    	public Window()
    	{
    		saveFace = new JButton("Save Face");
    		saveFace.setActionCommand("saveImage");
    		saveFace.addActionListener(this);
    		this.setSize(1000, 600);
    		imgsrc = new JLabel();
    		faceImg = new JLabel();
    		faceImg.setPreferredSize(new Dimension(200,200));
    		faceImg.setMaximumSize(new Dimension(150,600));
    		subPane.add(saveFace);
    		
    		subPane.add(new InfoText("Label"));
    		subPane.add(ltext);
    		subPane.add(new InfoText("Name"));
    		subPane.add(ntext);
    		
    		carPane.add(new InfoText("Driver"));
    		carPane.add(d);
    		carPane.add(new InfoText("Passenger"));
    		carPane.add(p);
    		carPane.add(new InfoText("Backseat0"));
    		carPane.add(b0);
    		carPane.add(new InfoText("Backseat1"));
    		carPane.add(b1);
    		
    		

    		contentPane.add(faceImg,BorderLayout.WEST);
    		contentPane.add(imgsrc,BorderLayout.CENTER);
    		contentPane.add(subPane,BorderLayout.SOUTH);
    		contentPane.add(carPane, BorderLayout.EAST);
    		
    		this.setContentPane(contentPane);
            this.setVisible(true);
            //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                	System.exit(0);
                   
                }
            });
            
            this.setTitle("Title");
            this.setLocation(100, 200);
    	}
    	
    	private class InfoText extends JTextField
    	{
    		/**
			 * Auto generated Serial from eclipse
			 */
			private static final long serialVersionUID = 4142860636777943347L;

			public InfoText(String s)
    		{
    			this.setText(s);
    			this.setEditable(false);
    		}
    	}
    	
    	/**
    	 * update the displayed image of the debug window
    	 * @param img inputted frame
    	 */
    	public void updateImage(Mat img)
    	{
    		MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", img, matOfByte);
            byte[] byteArray = matOfByte.toArray();
            BufferedImage bufImage = null;

            try {
                InputStream in = new ByteArrayInputStream(byteArray);
                bufImage = ImageIO.read(in);
                imgsrc.setIcon(new ImageIcon(bufImage));
                imgsrc.revalidate();
                imgsrc.repaint();
                
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            
            // Update Seats
            d.setText(cv.getSeatName(cv.DRIVER));
            p.setText(cv.getSeatName(cv.PASSENGER));
            b0.setText(cv.getSeatName(cv.BACKSEAT0));
            b1.setText(cv.getSeatName(cv.BACKSEAT1));
            
    	}
    	public void updateFace(Mat img)
    	{
    		MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", img, matOfByte);
            byte[] byteArray = matOfByte.toArray();
            BufferedImage bufImage = null;

            try {
                InputStream in = new ByteArrayInputStream(byteArray);
                bufImage = ImageIO.read(in);
                faceImg.setIcon(new ImageIcon(bufImage));
                faceImg.revalidate();
                faceImg.repaint();
                
            }
            catch (Exception e) {
                e.printStackTrace();
            }
    	}
    	public void actionPerformed(ActionEvent e)
    	{
    		if("saveImage".equals(e.getActionCommand()))
    		{
    			
    			int t = Integer.parseInt(ltext.getText());
    			if(!pers.containsKey(t))
    			{
    				pers.put(pers.keySet().size(), new Person(t,ntext.getText()));
    			}
    			pers.get(t).newImage(FaceImage);
    			writeToCsv(pers);
    			//Imgcodecs.imwrite("william2.jpg", FaceImage);
    		}
    	}
    }
}

