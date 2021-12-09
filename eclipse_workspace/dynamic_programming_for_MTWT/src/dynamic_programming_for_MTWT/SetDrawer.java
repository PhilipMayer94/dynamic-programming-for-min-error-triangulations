package dynamic_programming_for_MTWT;

import java.awt.*;
import java.util.*;


import javax.swing.JPanel;



public class SetDrawer extends JPanel {
	 OrderKObjects myObjects;

	 
	 void setObjects(OrderKObjects x){
		 myObjects=x;
	 }
	 
	 
	 void drawEdgeNoColor(Graphics g,Edge x) {
		    int x1=(int)x.src.x;
	    	int y1=(int)x.src.y;
	    	int x2=(int)x.dst.x;
	    	int y2=(int)x.dst.y;	
	    	g.drawLine(x1,y1,x2,y2);
		}
	
	 void drawVertexNoColor(Graphics g,Vertex vertex){
	     int x=(int)vertex.x -3;
	     int y=(int)vertex.y -3;
	     g.fillOval(x, y, 6, 6);
	 }
	 

	 @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);   
        if(myObjects==null) {
        	return;
        }
        for(Iterator<Edge> it=this.myObjects.fixedUsefulEdges.iterator();it.hasNext();) {
        	Edge cur=it.next();
        	g.setColor(Color.black);
        	this.drawEdgeNoColor(g, cur);
        }
        
    

       
    }
}