package dynamic_programming_for_MTWT;

import java.util.*;

public class ConnectedComponent {
	
	HashSet<Vertex> vertices =new HashSet<Vertex>();
	Vertex leftMostVertex; 
	Vertex rightMostVertex;
	int label=0; //the unique label of the component
	
	
	
	ArrayList<Vertex> possibleConnections=new ArrayList<Vertex>(); //the possible connections that the CC has to the polygon
	
	
	
	ArrayList<Integer> containedIn=new ArrayList<Integer>();//tracks in which polygons the component is contained
	int reallyContainedInIndex;// for nested polygons we are only interested in the innermost
	
	
	
	ConnectedComponent(HashSet<Vertex> verticesOftheComponent){
		this.vertices=verticesOftheComponent;
		Iterator<Vertex> it= vertices.iterator();
		Vertex minVertex=new Vertex(0,0,0);
		double minValue=Double.MAX_VALUE;
		Vertex maxVertex=null;
		double maxValue=-Double.MAX_VALUE;
		while(it.hasNext()) {
			Vertex x= it.next();
			if(x.x<minValue) {
				minValue=x.x;
				minVertex=x;	
			}
			if(x.x>maxValue) {
				maxValue=x.x;
				maxVertex=x;	
			}	
		}
		this.rightMostVertex=maxVertex;
		this.leftMostVertex=minVertex;
		this.label=minVertex.label;
		
		
	}
	
	
	@Override
	public String toString() {
		String result= "";
		
		return result;
		
	}
	
	
	


}
