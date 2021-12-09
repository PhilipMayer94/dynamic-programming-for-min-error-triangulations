package dynamic_programming_for_MTWT;

import java.util.HashSet;
import java.util.Hashtable;

public class Edge implements Comparable<Edge> {
	
	Vertex src;
	Vertex dst;
	
	//the associated empty left/right Circle
	UsefulCircle leftCircle;
	UsefulCircle rightCircle;
	
	
	HashSet<Triangle> incidentTriangles=new HashSet<Triangle>();
	HashSet<Triangle> incidentDelaunayTriangles=new HashSet<Triangle>();
	
	boolean hasIntersection=false;
	
	
	
	Hashtable<Edge,Edge> intersectedUsefulEdges=new Hashtable<Edge,Edge>(); //all useful edges that are intersected by this
	
	
	Edge(Vertex v1, Vertex v2){
		if(v1.id<v2.id) {
			src=v1;
			dst=v2;
		}
		else {
			src=v2;
			dst=v1;
		}
	}

	@Override
	public boolean equals(Object o) {
		if((this.src.id==((Edge)o).src.id && this.dst.id==((Edge)o).dst.id)||(this.src.id==((Edge)o).dst.id && this.dst.id==((Edge)o).src.id)) {
			return true;
		}
		return false;	
	}
	
	@Override
	public int compareTo(Edge o) {
		if((this.src.id==((Edge)o).src.id && this.dst.id==((Edge)o).dst.id)||(this.src.id==((Edge)o).dst.id && this.dst.id==((Edge)o).src.id)) {
			return 0;
		}
		else {
			if(this.src.compareTo(o.src)==-1) {
				return -1;
			}
			else {
				return 1;
			}		
		}	
	}
	
	@Override
	public int hashCode() {
		int hash = 23;
		hash = hash * 31 + this.src.id+1;
		hash = hash * 31 + this.dst.id+1;
		return hash;
	}
		
	@Override
	public String toString (){
		return "(" +this.src.id +", " +this.dst.id +")"; 
	}

}
