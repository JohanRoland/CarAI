package displayData;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import interfaces.DatabaseLocation;
import serverConnection.DBSCAN;
import serverConnection.ServerConnection;
import utils.Tuple;
import utils.Utils;

public class PointsPlotter extends JFrame {

	ServerConnection sc; 
	
	
	public PointsPlotter()
	{
		sc = ServerConnection.getInstance();
		
		initUI();
	}
	
	private void initUI()
	{
		final Surface surface = new Surface();
		add(surface);
		
		addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Timer timer = surface.getTimer();
                timer.stop();
            }
        });
		
		setTitle("Plotter");
		setSize(800,600);
		setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	
	
	class Surface extends JPanel implements ActionListener
	{
		private final int DELAY = 150;
	    private Timer timer;
		
		public Surface()
		{
			initTimer();
		}
		
		private void initTimer() {

	        timer = new Timer(DELAY, this);
	        timer.start();
	    }
	    
	    public Timer getTimer() {
	        
	        return timer;
	    }
		
		 @Override
	    public void paintComponent(Graphics g) {

	        super.paintComponent(g);
	        doDrawing(g);
	    }
		@Override
	    public void actionPerformed(ActionEvent e) {
	        repaint();
	    }

	    
		private void doDrawing(Graphics g)
		{
			Graphics2D g2d = (Graphics2D) g;
			g2d.setBackground(Color.white);
			try {
				
				ArrayList<DatabaseLocation> points = sc.getPosClass(1);
				DBSCAN sbs = new DBSCAN(points, true); 
				sbs.cluster(0.002, 2);
				ArrayList<DatabaseLocation>[] temp2 = sbs.getClusterd(true);
				Tuple<Tuple<Double,Double>,Tuple<Double,Double>> minMax =  Utils.getGPSPlotFrame(points);
				double maxDistX = (minMax.snd().fst() - minMax.fst().fst());
				double maxDistY = (minMax.snd().snd() - minMax.fst().snd());
				double scalingFacX = (this.getHeight()-20) / maxDistX;
				double scalingFacY = (this.getHeight()-20) /maxDistY;
				double scalingFac = Math.min(scalingFacX, scalingFacY);
				for(int i = 0; i < temp2.length; i++)
				{
					for( DatabaseLocation l: temp2[i])
					{
						
						int y = (this.getHeight()-20)-(int)((l.getLat()-minMax.fst().fst())*scalingFac)+10;
						int x = (int)((l.getLon()-minMax.fst().snd())*scalingFac)+10;
						int ny = (this.getHeight()-20)-(int)((l.getNLat()-minMax.fst().fst())*scalingFac)+10;
						int nx = (int)((l.getNLon()-minMax.fst().snd())*scalingFac)+10;
						g2d.setPaint(makeColorGradient(2.4,2.4,2.4,0,2,4,128,127,50,i));
						
						g2d.drawOval(x-4, y-4, 8, 8);
						g2d.setPaint(Color.black);
						g2d.drawLine(x, y,nx, ny);
					}
					}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		private Color makeColorGradient(double freq1,double freq2,double freq3,double phase1,double phase2,double phase3,int center, int width, int lenght,int val)
		{
			if(val == 0)
			{
				return Color.black;
			}
			int red = Math.round( (float)Math.sin(freq1*val + phase1) * width + center);
	        int grn = Math.round( (float)Math.sin(freq2*val + phase2) * width + center);
	        int blu = Math.round( (float)Math.sin(freq3*val + phase3) * width + center);
			
			float[] hsbcol = Color.RGBtoHSB(red, grn, blu, null);
			return Color.getHSBColor(hsbcol[0], hsbcol[1], hsbcol[2]);
		}
	}
}
