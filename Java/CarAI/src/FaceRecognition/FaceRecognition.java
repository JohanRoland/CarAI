package FaceRecognition;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.opencv.core.*;
import org.opencv.objdetect.*;
import org.opencv.imgproc.*;
import org.opencv.videoio.*;
import org.opencv.imgcodecs.*;

public class FaceRecognition
{
	Window win; 
	CascadeClassifier face_cascade;
    CascadeClassifier eyes_cascade;
    Mat frame;
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
    
    private class Window extends JFrame
    {
    	JLabel imgsrc; 
    	public Window()
    	{
    		this.setSize(800, 600);
    		imgsrc = new JLabel();
    		this.getContentPane().add(imgsrc);
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
    }
}

