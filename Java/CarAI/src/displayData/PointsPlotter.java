package displayData;

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
			try {
				
				ArrayList<DatabaseLocation> points = sc.getPosClass(0);
				
				Tuple<Tuple<Double,Double>,Tuple<Double,Double>> minMax =  Utils.getGPSPlotFrame(points);
				double scalingFacX = this.getWidth()-20 / minMax.snd().fst() - minMax.fst().fst();
				double scalingFacY = this.getHeight()-20 / minMax.snd().snd() - minMax.fst().snd();
				double scalingFac = Math.min(scalingFacX, scalingFacY);
				for(DatabaseLocation l: points)
				{
					g2d.drawLine((int)(l.getLat()*scalingFac)+10, (int)(l.getLon()*scalingFac)+10, (int)(l.getLat()*scalingFac)+10, (int)(l.getLon()*scalingFac)+10);
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}
}
