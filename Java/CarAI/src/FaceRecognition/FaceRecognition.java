package FaceRecognition;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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


public class FaceRecognition
{
	Window win; 
	CascadeClassifier face_cascade;
    CascadeClassifier eyes_cascade;
    LBPHFaceRecognizer fr;
    Mat frame;
    Mat FaceImage;
    int imWidht,imHeight;
    HashMap<Integer,Person> pers; 
    CarView cv;
    String pathToProj; 
    
    public FaceRecognition() {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    	
    	File f = new File(".");
		pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
    	
	    frame = new Mat();
	    win = new Window();
	    
	    face_cascade = new CascadeClassifier("C://opencv//build//etc//haarcascades//haarcascade_frontalface_alt.xml");
	    // Eyes cascade not worht it
	    //eyes_cascade = new CascadeClassifier("C://opencv//build//etc//haarcascades//haarcascade_eye_tree_eyeglasses.xml");
	    
	    pers = new HashMap<Integer,Person>();
	    
	    Csv cs = loadCsv(pathToProj+"\\bin\\test.csv");
	    
	    imWidht =  cs.getImgs().get(0).width();
	    imHeight =  cs.getImgs().get(0).height();
	    
	    Mat lab = new Mat(cs.getLabels().size(),1, CvType.CV_32SC1 );
	    
	    for(int i = 0; i < cs.getLabels().size(); i++)
	    {
	    	lab.put(i, 0, cs.getLabels().get(i));
	    }
	    
	    fr=  Face.createLBPHFaceRecognizer();
	    fr.train(cs.getImgs(), lab);
	    cv = new CarView();
    }
    
    /**
     * Start a continuous facecaptureing process
     */
    public void start()
    {
    	VideoCapture vc = new VideoCapture(0);
	    if (vc.isOpened())
	    {
	    	while(true)
	    	{
	    		vc.read(frame);
	    		if(!frame.empty())
	    		{
	    			detectAndDisplay(frame);
	    		}
	    		else
	    		{
	    			System.out.println("Error no captured frame");
	    		}
	    	}
	    		
	    }
    }
    /**
     * Helper function to detect faces in an capture frame
     * @param frame Frame cam source
     */
    private void detectAndDisplay(Mat frame)
    {
    	MatOfRect faces = new MatOfRect(); 
    	Mat frame_gray = new Mat();
    	Imgproc.cvtColor(frame, frame_gray,Imgproc.COLOR_BGR2GRAY);
    	Imgproc.equalizeHist(frame_gray, frame_gray);
    	
    	face_cascade.detectMultiScale(frame_gray, faces);
    	
    	for(Rect face: faces.toArray())
    	{
    		Point center = new Point(face.x+face.width*0.5,face.y+face.height*0.5);
    		Imgproc.ellipse(frame, center, new Size(face.width*0.5,face.height*0.5), 0, 0, 360, new Scalar(255,0,255), 4, 8, 0);
    		Mat face_resized = frame_gray.submat(face);
    		FaceImage = face_resized;

    		int[] pred = new int[1];
    		double[] conf = new double[1]; 
    		fr.predict(face_resized, pred, conf);
    		
    		String textBox = "P = " + pers.get(pred[0]).name + " C =" + conf[0]; 
    		
    		int pos_x = (int)Math.max(face.tl().x-10,0);
    		int pos_y = (int)Math.max(face.tl().y-10,0);
    		
    		Imgproc.putText(frame, textBox, new Point(pos_x,pos_y), 0, 1.0, new Scalar(0,255,0));
    		cv.parsePerson(pers.get(pred[0]).name, center, new Point(frame.cols()/2,frame.rows()/2));
    		
    		
    		// Keeping it for reference 
    		
    		/*Mat faceROI = frame_gray.submat(face);
    		MatOfRect eyes = new MatOfRect();
    		
    		eyes_cascade.detectMultiScale(faceROI, eyes);
        	
    		for(Rect eye: eyes.toArray())
    		{
    			Point center2 = new Point(face.x+eye.x+eye.width*0.5,face.y+eye.y+eye.height*0.5);
    			int radius = (int)Math.round((eye.width + eye.height)*0.25);
    			Imgproc.circle(frame, center2, radius, new Scalar(255,0,0));
    		}*/
    		
    	}
    	win.updateImage(frame);
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
    
    /**
     * Writes an hasmap of persons to the CSV file
     * @param ps Hasmap of persons
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
		    			
					FileWriter f = new FileWriter(Paths.get("bin\\test.csv").toFile(),append);
					f.write(p.exportToCsv());
					f.close();
					
	    	}catch(Exception e)
			{
				System.out.println("Error writing CSV for " + p.name);
			}	
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
    			new File(pathToProj+"\\bin\\data\\"+name).mkdir();
    		}
    		Imgcodecs.imwrite(pathToProj+"\\bin\\data\\"+name+"\\"+name+imgs.size() + ".jpg", i);
    		imgs.add(pathToProj+"\\bin\\data\\"+name+"\\"+name+imgs.size() + ".jpg");
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
    	    	
    	String[] internal; 
    	
    	//HashMap<String,Seat> internal;
    	
    	public CarView()
    	{
    		internal = new String[5];
    		for(int i = 0; i < internal.length; i++)
    		{
    			internal[i] = "";
    		}
    		//internal = new HashMap<String,Seat>();
    	}
    	
    	private void emptySeat(int s)
    	{
    		internal[s] = "";
    	}
    	
    	private void emptyName(String s)
    	{
    		for(int i = 0; i < internal.length; i++)
    		{
    			if(internal[i].equals(s))
    			{
    				internal[i] = "";
    			}
    		}
    	}
    	
    	public void parsePerson(String name,Point p, Point imgCenter)
    	{
    		
    		if(p.x > imgCenter.x)
    		{
    			if(p.y > imgCenter.y)
    			{
    				emptyName(name);
    				internal[DRIVER] = name;
    			}
    			else
    			{
    				emptyName(name);
    				internal[BACKSEAT0] = name;
    			}
    		}
    		else
    		{
    			if(p.y > imgCenter.y)
    			{
    				emptyName(name);
    				internal[PASSENGER] = name;
    			}
    			else
    			{
    				emptyName(name);
    				internal[BACKSEAT1] = name;
    			}
    		}
    	}
    	
    	public String[] exportPerson()
    	{
    		return internal;
    	}
    	
    	public String getSeatName(int i)
    	{
    		return internal[i];
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
    	JLabel imgsrc; 
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
    		this.setSize(800, 600);
    		imgsrc = new JLabel();
    		    		
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
    		
    		
    		
    		contentPane.add(imgsrc,BorderLayout.CENTER);
    		contentPane.add(subPane,BorderLayout.SOUTH);
    		contentPane.add(carPane, BorderLayout.EAST);
    		
    		this.setContentPane(contentPane);
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setTitle("Title");
            this.setLocation(100, 200);
    	}
    	
    	private class InfoText extends JTextField
    	{
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

