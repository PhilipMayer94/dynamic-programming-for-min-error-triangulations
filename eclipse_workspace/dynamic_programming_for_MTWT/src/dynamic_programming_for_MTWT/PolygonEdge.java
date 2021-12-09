package dynamic_programming_for_MTWT;
import java.util.*;


//!!Oriented!! edges of the boundary of a polygon
public class PolygonEdge implements Comparable<PolygonEdge> {
	Edge originalEdge;
	PolygonVertex src;
	PolygonVertex dst;
	ArrayList<PolygonTriangle> triangles=new ArrayList<PolygonTriangle>() ;
	
	
	
	PolygonEdge(PolygonVertex source,PolygonVertex destination,Edge original){
		originalEdge=original;
		src=source;
		dst=destination;
	}
	
	
	
	//checks if the edge has an incident triangle with the requested tipAsIndex
	PolygonTriangle hasTriangle(int index) {
		int distance=triangles.size()-1;
		int lowerBound=0;
		int upperBound=triangles.size()-1;;
		if(triangles.size()==0) {
			return null;
		}
		
		if(triangles.get(0).tipId==index) {
			return triangles.get(0);
		}
		if(triangles.get(triangles.size()-1).tipId==index) {
			return triangles.get(triangles.size()-1);
		}
		
		while(distance>1) {
			int tmp=(int)(lowerBound+distance/2);
			if(triangles.get(tmp).tipId==index) {
				return triangles.get(tmp);
			}
			else {
				if(triangles.get(tmp).tipId<index) {
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

	
	//checks if vertex is left of the polygon edge	
	boolean isLeft(Vertex v) {
		double pos = (this.dst.originalVertex.x - this.src.originalVertex.x) * (v.y - this.src.originalVertex.y) - (this.dst.originalVertex.y - this.src.originalVertex.y) * (v.x - this.src.originalVertex.x);
		if(pos<0) {
			return true;
		}
		if(pos>0)  {
			return false;
		}
		return false;
	}
	
	
	@Override
	public String toString() {
		return "("+ this.src.id+" " + this.dst.id+")";
	}
	
	@Override
	public boolean equals(Object o) {
		if((this.src.id==((PolygonEdge)o).src.id && this.dst.id==((PolygonEdge)o).dst.id)) {
			return true;
		}
		return false;	
	}
	
	
	@Override
	public int compareTo(PolygonEdge o) {
		//System.out.println("compareTo should never be called!:"+ this.toString() +   "["  + this.hashCode()  +"]"   +  " " + o+ "["  + o.hashCode()  +"]");
		if((this.src.id==((PolygonEdge)o).src.id && this.dst.id==((PolygonEdge)o).dst.id)) {
			return 0;
		}
		else {
			if((this.src).compareTo(o.src)==-1) {
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
	
	
}
