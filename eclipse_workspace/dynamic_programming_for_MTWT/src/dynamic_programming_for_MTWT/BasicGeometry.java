package dynamic_programming_for_MTWT;

import java.util.ArrayList;
import java.util.Comparator;

public class BasicGeometry {

	static double calculateDistance(double ax,double ay,double bx,double by) {
		return Math.sqrt((ax-bx)*(ax-bx)+(ay-by)*(ay-by));
		
	}
	
	//calculates x,y,r of a circle given 3 points
	static double[] calculateCircle3Points(Vertex b, Vertex c, Vertex d) {
		
		double temp=c.x*c.x + c.y*c.y;
		double bc=(b.x*b.x + b.y*b.y -temp)/2;
		double cd=(temp- d.x*d.x - d.y*d.y)/2;
		double det=(b.x-c.x)*(c.y-d.y)-(c.x-d.x)*(b.y-c.y);
		

		
		if(Math.abs(det) < 1.0e-10){
			double[] result={0,0,-22};
		    return result;
		}
		double cx= (bc*(c.y-d.y)-cd*(b.y-c.y))/det;
		double cy= ((b.x-c.x)*cd-(c.x-d.x)*bc)/det;
		
		double radius= Math.sqrt((cx-b.x)*(cx-b.x)+(cy-b.y)*(cy-b.y));
		
		double[] result= {cx,cy,radius};
		
		return result;
		
	}
	
	
	//checks if two edges are equal
	static boolean isNotSameLine(Vertex p1, Vertex q1, Vertex p2, Vertex q2) {
		if(((p1.x==p2.x && p1.y==p2.y)&&(q1.x==q2.x && q1.y==q2.y))||((p1.x==q2.x && p1.y==q2.y)&& (q1.x==p2.x && q1.y==p2.y))){
			return false;
		}
		return true;
	}
	
	// Given three colinear points p, q, r, the function checks if 
	// point q lies on line segment 'pr' 
	static boolean onSegment(Vertex p, Vertex q, Vertex r) 
	{ 
	    if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) && 
	        q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y)) 
	    return true; 
	  
	    return false; 
	} 
	  
	//find orientation of ordered triplet (p, q, r). 
	static int orientation(Vertex p, Vertex q, Vertex r) 
	{ 
	    double val = (q.y - p.y) * (r.x - q.x) - 
	            (q.x - p.x) * (r.y - q.y); 
	    if (val == 0) return 0; // colinear 
	    return (val > 0)? 1: 2; // clock or counterclock wise 
	} 
	  
	// returns true if line segment 'p1q1' and 'p2q2' intersect 
	static boolean doIntersect(Vertex p1, Vertex q1, Vertex p2, Vertex q2) 
	{ 
		
		if(p1.id==p2.id ||p1.id==q2.id ||q1.id==p2.id ||q1.id==q2.id ) {
			return false;
		}
	    int o1 = orientation(p1, q1, p2); 
	    int o2 = orientation(p1, q1, q2); 
	    int o3 = orientation(p2, q2, p1); 
	    int o4 = orientation(p2, q2, q1); 
	  
	    // General case 
	    if (o1 != o2 && o3 != o4) 
	        return true; 
	  
	    // Special Cases  
	    if (o1 == 0 && onSegment(p1, p2, q1)) return true; 
	    if (o2 == 0 && onSegment(p1, q2, q1)) return true; 
	    if (o3 == 0 && onSegment(p2, p1, q2)) return true; 
	    if (o4 == 0 && onSegment(p2, q1, q2)) return true; 
	  
	    return false; 
	} 
	
	static boolean doIntersect( Edge e1, Edge e2) 
	{ 
		return doIntersect(e1.src, e1.dst, e2.src, e2.dst);
	    
	} 
	
	//checks if three points are colinear
	static boolean collinear(double x1, double y1, double x2,double y2, double x3, double y3) { 

		double a = x1 * (y2 - y3) +  
				x2 * (y3 - y1) +  
				x3 * (y1 - y2); 

		if (Math.abs(a) <0.00001) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	public class angleComparator implements Comparator<Vertex> {
		Vertex v;
		Edge e;
		
		angleComparator(Vertex anchor,Edge edge){
			super();
			v=anchor;
			e=edge;
			
		}
	    @Override
	    public int compare(Vertex o1, Vertex o2) {
	    	double x1= calculateAngle(e, v, o1);
	    	double x2=calculateAngle(e,v,o2);
	    	if(x1<x2) {
	    		return -1;
	    	}
	    	else {
	    		if(x1==x2) {
	    			return 0;
	    		}
	    		else {
	    			return 1;
	    		}
	    	}
	    }
	}
	
	//checks if a vertex is left of an (oriented) edge
	static boolean isLeft(Vertex v, Edge e) {
		double pos = (e.dst.x - e.src.x) * (v.y - e.src.y) - (e.dst.y - e.src.y) * (v.x - e.src.x);
		if(pos<0) {
			return true;
		}
		if(pos>0)  {
			return false;
		}
		System.out.println(v+" "+e);
		System.out.println("error point to close in isLeft");
		return false;
	}
	
	static boolean isLeftOriented(Vertex v, Vertex e1,Vertex e2) {
		double pos = (e2.x - e1.x) * (v.y - e1.y) - (e2.y - e1.y) * (v.x - e1.x);
		if(pos<0) {
			return true;
		}
		if(pos>0)  {
			return false;
		}

		return false;
	}
	
	//checks if a point is in a polygon
	static boolean vertexInPolygon(ArrayList<Vertex> polygon,Vertex v){
		double x=v.x, y=v.y;
		
		if(v.label==polygon.get(0).label) {
			return false;
		}

		boolean result=false;
		for(int i=0;i<polygon.size();i++) {
			int j=(i+1)%polygon.size();
			
			double xi=polygon.get(i).x;
			double yi=polygon.get(i).y;
			double xj=polygon.get(j).x;
			double yj=polygon.get(j).y;

			boolean intersect= (((yi > y) != (yj > y))
		            && (x < ((xj - xi) * (y - yi) / (yj - yi) + xi))); 
			if (intersect) {
				result = !result;
			}
		}
		return result;
	}
	
	
	//checks if point in polygon and gives the distance to the closest point to the border of the polygon on a ray
	static double vertexInPolygonGetIntersectionDistance(ArrayList<Vertex> polygon,Vertex v){
		double x=v.x, y=v.y;
		
		double result=Double.MAX_VALUE;
		
		if(v.label==polygon.get(0).label) {
			return Double.MAX_VALUE;
		}

		for(int i=0;i<polygon.size();i++) {
			int j=(i+1)%polygon.size();
			
			double xi=polygon.get(i).x;
			double yi=polygon.get(i).y;
			double xj=polygon.get(j).x;
			double yj=polygon.get(j).y;

			boolean intersect= (((yi > y) != (yj > y))
		            && (x < ((xj - xi) * (y - yi) / (yj - yi) + xi))); 
			
			if (intersect) {
				if((((xj - xi) * (y - yi) / (yj - yi) + xi))<result&& BasicGeometry.isLeftOriented(v, polygon.get(i),polygon.get(j))){
					
					result=((xj - xi) * (y - yi) / (yj - yi ))+ xi;
					System.out.println("happend");
					System.out.println(v+ " "+ polygon.get(i)+polygon.get(j)+result);
				}
			}
		}
		return result;
	}	
	
	
	
	//calculates the angle between (a,v) and (a,pred(a))
	static double calculateAngle(Edge e, Vertex anchor, Vertex v) {
		Vertex anchorPred;
		if(e.dst.equals(anchor)) {
			anchorPred=e.src;
		}
		else {
			anchorPred=e.dst;
		}
		if(v.equals(anchorPred)) {
			return 360;
		}
		else {
			double targetX=v.x-anchor.x;
			double targetY=v.y-anchor.y;
			double preEdgeX=anchorPred.x-anchor.x;
			double preEdgeY=anchorPred.y-anchor.y;
			
			double targetA=Math.atan2(targetY, targetX);
			
			double preEdgeA=Math.atan2(preEdgeY, preEdgeX);
			
	
			targetA=targetA-preEdgeA;
			targetA=normalizeAngle(targetA);
			
			return targetA;
			
			
		}
		
		
	}
	
	
	static double normalizeAngle(double angle) {
		
		if (angle>2*Math.PI) {
			angle=angle-2*Math.PI;
		}
		if(angle<0) {
			angle=angle+2*Math.PI;
		}
		
	    return angle;
	}
	
}
