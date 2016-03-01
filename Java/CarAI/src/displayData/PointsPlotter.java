package displayData;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
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
import serverConnection.KmeansSortOf;
import serverConnection.ServerConnection;
import utils.Tuple;
import utils.Utils;
import mashinelearning.NNData;
import mashinelearning.PYDBSCAN;

public class PointsPlotter extends JFrame {

	ServerConnection sc; 
	
	
	public PointsPlotter()
	{
		//sc = ServerConnection.getInstance();
		
		initUI();
	}
	
	private void initUI()
	{
		JPanel mapPane = new JPanel(new GridLayout(1,0));
		final Surface surface = new Surface(4);
		//final Surface surface1 = new Surface(1);
		mapPane.add(surface);
		//mapPane.add(surface1);
		add(mapPane);
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
		private int clusterType;
		
		ArrayList<DatabaseLocation> points;
		ArrayList<ArrayList<DatabaseLocation>> temp2;
		
		public Surface(int i)
		{
			initTimer();
			clusterType = i;
			try{
				points = new ArrayList<DatabaseLocation>();
				temp2 = new ArrayList<ArrayList<DatabaseLocation>>();
				
				switch(clusterType)
				{
					case 0:
						points = sc.getPosClass(1,20000);
						DBSCAN sbs = new DBSCAN(points, true); 
						sbs.cluster(0.002,2);
						temp2 = sbs.getClusterd(true);
						break;
					case 1:
						points = sc.getPosClass(1,20000);
						KmeansSortOf sbs2 = new KmeansSortOf(points, true); 
						sbs2.cluster(0.002);
						temp2 = sbs2.getClusterd(true);
						break;
						
					case 2:
						NNData test = new NNData();
						test.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",1000);
						points = test.importFromFile();
						test.exportAsCoordsToCSV();
						temp2 = test.importClustFromFile("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\clusterFile.csv");
						ArrayList<DatabaseLocation> points2 = new ArrayList<DatabaseLocation>();
						for(int j = 1000-1; j >= 0; j--)
						{
							points2.add(points.get(j));
						}
						break;
					case 3:
						NNData test3 = new NNData();
						test3.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",10000);
						points = test3.importFromFile();
						test3.exportAsCoordsToCSV();
						PYDBSCAN ps =  new PYDBSCAN();
						temp2 = ps.runDBSCAN(points,0.001,20,10000);
						break;
					case 4:
						NNData test4 = new NNData();
						test4.importFromDB(1, 200000);//parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",10000);
						test4.coordClull();
						points = test4.getQuerry();
						test4.exportAsCoordsToCSV();
						PYDBSCAN something =  new PYDBSCAN();
						temp2 = something.runDBSCAN(points,0.001,20,200000);
						break;						
					default:
						NNData test2 = new NNData();
						test2.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
						
						points = test2.importFromFile();
						ArrayList<DatabaseLocation> points3 = new ArrayList<DatabaseLocation>();
						for(int j = points.size()-1; j >= 0; j--)
						{
							points3.add(points.get(j));
						}
						temp2.add(new ArrayList<DatabaseLocation>(points3.subList(0, 10000)));
						break;
					
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
			
			Tuple<Tuple<Double,Double>,Tuple<Double,Double>> minMax =  Utils.getGPSPlotFrame(temp2.get(0));
			double maxDistX = (minMax.snd().fst() - minMax.fst().fst());
			double maxDistY = (minMax.snd().snd() - minMax.fst().snd());
			double scalingFacX = (this.getHeight()-20) / maxDistX;
			double scalingFacY = (this.getHeight()-20) /maxDistY;
			double scalingFac = Math.min(scalingFacX, scalingFacY);
			for(int i = 1; i < temp2.size(); i++)
			{
				for( DatabaseLocation l: temp2.get(i))
				{
					
					int x = (this.getHeight()-20)-(int)((l.getLat()-minMax.fst().fst())*scalingFac)+10;
					int y = (int)((l.getLon()-minMax.fst().snd())*scalingFac)+10;
					int nx = (this.getHeight()-20)-(int)((l.getNLat()-minMax.fst().fst())*scalingFac)+10;
					int ny = (int)((l.getNLon()-minMax.fst().snd())*scalingFac)+10;
					g2d.setPaint(makeColorGradient(2.4,2.4,2.4,0,2,4,128,127,50,i));
					
					g2d.drawOval(x-4, y-4, 8, 8);
					//g2d.setPaint(Color.black);
					//g2d.drawLine(x, y,nx, ny);
				}
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
