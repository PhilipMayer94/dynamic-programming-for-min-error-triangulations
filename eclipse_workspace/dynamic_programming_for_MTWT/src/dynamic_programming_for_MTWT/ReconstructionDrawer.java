package dynamic_programming_for_MTWT;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JPanel;



public class ReconstructionDrawer  extends JPanel{
	private Reconstructor x=null;
	
	void setReconstructor(Reconstructor inputReconstructor) {
		x=inputReconstructor;
	}
	
	void drawEdge(Graphics g,Edge x) {
	    int x1=(int)x.src.x;
    	int y1=(int)x.src.y;
    	int x2=(int)x.dst.x;
    	int y2=(int)x.dst.y;	
    	g.drawLine(x1,y1,x2,y2);
	}
	
	void drawTriangle(Graphics g, Triangle x) {
		 for(int i=0;i<x.edges.length;i++) {
			 drawEdge(g,x.edges[i]);
		 }

	}
	
	void drawVertex(Graphics g,Vertex vertex, int withText){
	     int x=(int)vertex.x -1;
	     int y=(int)vertex.y -1;
	     g.fillOval(x, y, 3, 3);
	 }
	
	void drawEdgeSet(Graphics g, Set<Edge> set) {
		for(Iterator<Edge> itE=set.iterator();itE.hasNext();) {
        	Edge cur=itE.next();
        	g.setColor(Color.black);
        	this.drawEdge(g, cur);
        }
	}
	

	
	void drawTriangleSet(Graphics g, Set<Triangle> set) {
		for(Iterator<Triangle> itE=set.iterator();itE.hasNext();) {
        	Triangle cur=itE.next();
        	g.setColor(Color.black);
        	this.drawTriangle(g, cur);
        }
	}

	@Override
    protected void paintComponent(Graphics g) {
		if(x.solver!=null) {
			super.paintComponent(g);
			g.setColor(Color.white);
	        g.fillRect(0, 0,1840 , 1040);
			g.setColor(Color.black);
			this.drawTriangleSet(g, x.solver.solution);
			for(Iterator<Edge> it=this.x.solver.myKODObjects.fixedUsefulEdges.iterator();it.hasNext();) {
		        Edge cur=it.next();
		        g.setColor(Color.red);
		        this.drawEdge(g, cur);
		   }
		}
		
		
	}

        
        
	

	
}
