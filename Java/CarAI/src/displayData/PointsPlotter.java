package displayData;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import interfaces.DatabaseLocation;
import jade.content.Concept;
import jade.content.onto.basic.Action;
import serverConnection.ServerConnection;
import utils.Tuple;
import utils.Utils;
import mashinelearning.DBSCAN;
import mashinelearning.ELKIController;
import mashinelearning.KmeansSortOf;
import mashinelearning.NNData;
import mashinelearning.PYDBSCAN;

public class PointsPlotter extends JFrame {

	ServerConnection sc; 
	double zoom=1,ofsetX=0,ofsetY=0;
	
	
	
	public PointsPlotter()
	{
		//sc = ServerConnection.getInstance();
		
		initUI();
	}
	
	private void initUI()
	{
		JPanel mapPane = new JPanel(new GridLayout(1,0));
		final Surface surface = new Surface(6);
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
			this.setSize(600, 800);
			try{
				points = new ArrayList<DatabaseLocation>();
				temp2 = new ArrayList<ArrayList<DatabaseLocation>>();
				NNData data = new NNData(); 
				switch(clusterType)
				{
					case 0:
						data.importFromDB(1, 600000);
						points = data.getQuerry();
						//points = sc.getPosClass(1,600000);
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
						data.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",1000);
						points = data.getQuerry();
						data.exportAsCoordsToCSV();
						temp2 = data.importClustFromFile("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\clusterFile.csv");
						ArrayList<DatabaseLocation> points2 = new ArrayList<DatabaseLocation>();
						for(int j = 1000-1; j >= 0; j--)
						{
							points2.add(points.get(j));
						}
						break;
					case 3:
						data.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",10000);
						points = data.getQuerry();
						data.exportAsCoordsToCSV();
						PYDBSCAN ps =  new PYDBSCAN();
						temp2 = ps.runDBSCAN(points,0.001,20,10000);
						break;
					case 4:
						data.importFromDB(1, 200000);//parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",10000);
						data.coordCullBySpeed(15.0);
						points = data.getQuerry();
	
						data.exportAsCoordsToCSV();
						PYDBSCAN something =  new PYDBSCAN();
						temp2 = something.runDBSCAN(points,0.001,20,20000);
						
						System.out.println("Nummber of clusters; "+ temp2.size());
						
						break;
					case 5:
						//data.importFromDB(1, 600000);//
						data.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
						data.parseCSV("dataFile.txt");
						//data.coordCullBySpeed(15.0);
						//data.coordCullByBox(57, 11, 1 , 4);
						points = data.getQuerry();
	
						data.exportAsCoordsToCSV();


						File f = new File(".");
						String pathToProj = f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-2);
				    	
						temp2 = data.importFromElkiClustering(pathToProj+"\\ELKIClusters\\");

						for(int t=1; t<temp2.size(); t++)
						{
							System.out.println(Utils.mean(temp2.get(t)));
						}
						System.out.println("Nummber of clusters; "+ temp2.size());
						
						/*
						AbstractAction anAction = new AbstractAction() {
						    public void actionPerformed(ActionEvent e) {
						        if(e.getActionCommand()=="Up")
						        {
						        	ofsetY++;
						        }
						        else if(e.getActionCommand()=="Down")
						        {
						        	ofsetY--;
						        }
						        else if(e.getActionCommand()=="Right")
						        {
						        	ofsetX++;
						        }
						        else if(e.getActionCommand()=="Left")
						        {
						        	ofsetX--;
						        }
						    }
						};
						component.getInputMap().put(KeyStroke.getKeyStroke("F2"),  "doSomething");
						component.getActionMap().put("doSomething",anAction);
						*/
						
						break;
					case 6:
						//data.importFromDB(1, 600000);//
						//data.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\OlofLoc.kml",0);
						data.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
						//data.importFromFile();
						data.coordCullByBox(57.34, 11, 1 , 4);
						//data.cullByRDP();
						data.coordCullByDist();
						//data.coordCullByDist();
						//data.repoint();
						//data.coordCullBySpeed(15.0);
						
						data.exportAsCoordsWithDateToCSV();
						
						points =  data.getQuerry();
						if(true)
						{
							data.exportAsCoordsToCSV();
							
							File f2 = new File(".");
							String pathToProj2 = f2.getAbsolutePath().substring(0, f2.getAbsolutePath().length()-2);
					    	ELKIController.runElki();
							
							temp2 = data.importFromElkiClustering(pathToProj2+"\\ELKIClusters\\");
							for(int t=1; t<temp2.size(); t++)
							{
								System.out.println(Utils.mean(temp2.get(t)));
							}
							System.out.println("Nummber of clusters; "+ temp2.size());
							data.exportAsClustToCSV();
						}
						else{
							temp2.add(null);
							temp2.add(points);
						}
						break;
					case 8:
						data.importFromDB(1, 600000);
						data.coordCullByBox(57.34, 11, 1 , 1.5);
						points=data.getQuerry();
						temp2.add(null);
						temp2.add(points);
						
						break;
					default:
						data.parseKML("D:\\Programming projects\\NIB\\CarAI\\Java\\CarAI\\Platshistorik.kml",0);
						
						points = data.getQuerry();
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

			
			Tuple<Tuple<Double,Double>,Tuple<Double,Double>> minMax =  Utils.getGPSPlotFrame(temp2);
			double maxDistX = (minMax.snd().fst() - minMax.fst().fst());
			double maxDistY = (minMax.snd().snd() - minMax.fst().snd());
			
			double scalingFacX = ( g2d.getClipBounds().width-50) / maxDistX;
			double scalingFacY = (g2d.getClipBounds().height-50) /maxDistY;
			double scalingFac = Math.min(scalingFacX, scalingFacY);
			
			//System.out.println("maxDistX: " + maxDistX + " maxDistY: " + maxDistY + "scalingFactor: "+scalingFac);
			
			for(int i = 1; i < temp2.size(); i++)
			{
				boolean hasDraw=false;
				for( DatabaseLocation l: temp2.get(i))
				{
					
					int x = (int) (((int)((l.getLon()-minMax.fst().fst())*scalingFac)+25)*zoom+ofsetX);
					int y = (int) ((( g2d.getClipBounds().height-50)-(int)((l.getLat()-minMax.fst().snd())*scalingFac)+25)*zoom+ofsetY);
					int nx = (int) (((int)((l.getNLon()-minMax.fst().fst())*scalingFac)+25)*zoom+ofsetX);
					int ny = (int) ((( g2d.getClipBounds().height-50)-(int)((l.getNLat()-minMax.fst().snd())*scalingFac)+25)*zoom+ofsetY);
					if(!hasDraw)
					{
						g2d.setPaint(Color.black);
						g2d.drawString(""+i, x, y+30);
						hasDraw=true;
					}
					g2d.setPaint(makeColorGradient(2.4,2.4,2.4,0,2,4,128,127,50,i));
					
					g2d.drawOval(x-4, y-4, 8, 8);
					
					//g2d.setPaint(Color.black);
					//g2d.drawLine(x, y,nx, ny);
				
					//int px = (int) (((int)((11.951428392713707-minMax.fst().fst())*scalingFac)+25)*zoom+ofsetX);
					//int py = (int) ((( g2d.getClipBounds().height-50)-(int)((57.61810787414581-minMax.fst().snd())*scalingFac)+25)*zoom+ofsetY);
					//g2d.setPaint(Color.green);
					//g2d.drawOval(px-4, py-4, 8, 8);
					
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
