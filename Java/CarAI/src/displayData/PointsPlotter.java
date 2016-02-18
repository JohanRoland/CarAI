package displayData;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import interfaces.DatabaseLocation;
import serverConnection.ServerConnection;

public class PointsPlotter extends JFrame {

	ServerConnection sc; 
	
	
	public PointsPlotter()
	{
		sc = ServerConnection.getInstance();
		
		
	}
	
	private void initUI()
	{
		final Surface surface = new Surface();
		setTitle("Plotter");
		setSize(800,600);
		setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	
	
	class Surface extends JPanel 
	{
		
		public Surface()
		{
			initUI();
		}
		
		private void doDrawing(Graphics g)
		{
			Graphics2D g2d = (Graphics2D) g;
			try {
				ArrayList<DatabaseLocation> points = sc.getPosClass(0);
				double scalingFacX = this.getWidth()-20 / getMaximum().x - getMinimum().x;
				double scalingFacY = this.getHeight()-20 / getMaximum().y - getMinimum().y;
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
