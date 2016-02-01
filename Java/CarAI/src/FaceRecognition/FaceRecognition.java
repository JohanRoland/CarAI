package FaceRecognition;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    
    String pathToCsv = "D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\bin\\test.csv";
    public FaceRecognition() {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	    frame = new Mat();
	    win = new Window();
	    
	    face_cascade = new CascadeClassifier("C://opencv//build//etc//haarcascades//haarcascade_frontalface_alt.xml");
	    face_cascade.load("C://opencv//build//etc//haarcascades//haarcascade_frontalface_alt.xml");
	    
	    if(face_cascade.empty())
	    {
	    	System.out.println("error loading Face cascade");
	    }
	    
	    eyes_cascade = new CascadeClassifier("C://opencv//build//etc//haarcascades//haarcascade_eye_tree_eyeglasses.xml");
	    
	    Csv cs = loadCsv(pathToCsv);
	    
	    imWidht =  cs.getImgs().get(0).width();
	    imHeight =  cs.getImgs().get(0).height();
	    
	    Mat lab = new Mat(1,1, CvType.CV_32SC1 );
	    
	    for(int i = 0; i < cs.getLabels().size(); i++)
	    {
	    	lab.put(i, 0, cs.getLabels().get(i));
	    }
	    
	    
	    fr=  Face.createLBPHFaceRecognizer();
	    fr.train(cs.getImgs(), lab);
	    
    }
    
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
    		int prediction = fr.predict(face_resized);
    		
    		String textBox = "Prediction = " + prediction; 
    		
    		int pos_x = (int)Math.max(face.tl().x-10,0);
    		int pos_y = (int)Math.max(face.tl().y-10,0);
    		
    		Imgproc.putText(frame, textBox, new Point(pos_x,pos_y), 0, 1.0, new Scalar(0,255,0));
    		
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
    	//ShowImage(frame,"Window");
    }
    /**
     * 
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
    				imgs.add(Imgcodecs.imread(t[0],0)); 
    				labels.add(Integer.parseInt(t[1]));
    				
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
    		System.exit(-1);
    	}
    	
    	return new Csv(imgs,labels);
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
    	
    	public ArrayList<Mat> getImgs()
    	{
    		return imgs;
    	}
    	
    	public ArrayList<Integer> getLabels()
    	{
    		return labels;
    	}
    }
    
    private class Window extends JFrame implements ActionListener 
    {
    	JLabel imgsrc; 
    	JButton saveFace;
    	JPanel contentPane = new JPanel(new BorderLayout());
    	public Window()
    	{
    		saveFace = new JButton("Save Face");
    		saveFace.setActionCommand("saveImage");
    		saveFace.addActionListener(this);
    		this.setSize(800, 600);
    		imgsrc = new JLabel();
    		contentPane.add(imgsrc,BorderLayout.CENTER);
    		contentPane.add(saveFace,BorderLayout.SOUTH);
    		this.setContentPane(contentPane);
    		//this.getContentPane().add(imgsrc);
    		//this.getContentPane().add(saveFace);
            //this.pack();
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setTitle("Title");
            this.setLocation(100, 200);
    	}
    	
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
    	}
    	
    	public void actionPerformed(ActionEvent e)
    	{
    		if("saveImage".equals(e.getActionCommand()))
    		{
    			Imgcodecs.imwrite("william2.jpg", FaceImage);
    		}
    	}
    }
}
