package dynamic_programming_for_MTWT;

import java.util.ArrayList;


public class PolygonVertex implements Comparable<PolygonVertex>{
	Vertex originalVertex;
	int id=-1;
	ArrayList<PolygonEdge> edges=new ArrayList<PolygonEdge>();
	
	//checks if a polygon has an incident edge leading to index
	PolygonEdge hasEdge(int index) {
		int distance=edges.size()-1;
		int lowerBound=0;
		int upperBound=edges.size()-1;;
		if(edges.get(0).dst.id==index) {
			return edges.get(0);
		}
		if(edges.get(edges.size()-1).dst.id==index) {
			return edges.get(edges.size()-1);
		}
		
		while(distance>1) {
			int tmp=(int)(lowerBound+distance/2);
			if(edges.get(tmp).dst.id==index) {
				return edges.get(tmp);
			}
			else {
				if(edges.get(tmp).dst.id<index) {
					lowerBound=tmp;
					distance=upperBound-lowerBound;
				}
				else {
					upperBound=tmp;
					distance=upperBound-lowerBound;
				}
			}
		}
		return null;
	}
	
	PolygonVertex(Vertex v,int idvalue){
		originalVertex=v;
		id=idvalue;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this.id==((PolygonVertex)o).id ) {
			return true;
		}	
		return false;	
	}
	
	@Override
	public int compareTo (PolygonVertex obj){	
		//System.out.println("VertexCompTo should not be called");
	    if (this.originalVertex.x < obj.originalVertex.x) return -1;
	    if (this.originalVertex.x > obj.originalVertex.x) return +1;
	    if (this.originalVertex.y < obj.originalVertex.y) return -1;
	    if (this.originalVertex.y > obj.originalVertex.y) return +1;
	    return 0;  
	}
	
	 @Override
	 public int hashCode() {
	    int hash = this.id;
	    return hash;
	 }
	
	

	 
	
}
