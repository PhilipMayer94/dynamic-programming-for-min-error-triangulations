package dynamic_programming_for_MTWT;
import java.util.*;


public class Vertex implements Comparable<Vertex>  {
	//geometry
	double x=0;
	double y=0;
	int id=0;
	
	
	double [] learningValues={Double.MAX_VALUE,Double.MAX_VALUE};// 0 learning 1 reconstruction
	
	
	ArrayList<Integer> tmpIds=new ArrayList<Integer>();//needed for processing in the polygon class

	
	int label=0; //connected component label
	boolean isPartOfaPolygon=false; //indicator, that it was already used in a polygon
	
	
	
	//graph structures given by useful KODObjects
	HashSet<Vertex> fixedNeighbours =new HashSet<Vertex>(); //neighbours in the fixed useful edge graph
	HashSet<Vertex> adjacentUsefulNeighbours=new HashSet<Vertex>(); //all neighbours given the useful edges
	HashSet<Edge> 	incidentUsefulEdges= new HashSet<Edge>();		//all incident useful edges (as edges and not as vertices)
	
	
	//graph structures given by Delauany
	HashSet<Vertex> delaunayNeighbours=new HashSet<Vertex>(); //all neighbours in the Delaunay Triangulation
	HashSet<Edge> 	incidentDelaunayEdges= new HashSet<Edge>();
	
	
	//used by PolygonDecomposition
	HashSet<Vertex> neighboursTree=new HashSet<Vertex>();
	HashSet<Vertex> neighboursForAlgo =new HashSet<Vertex>();
	
	
	//checks if given a vertex v there is an edge with endpoint v
	Edge hasincidentEdge(Vertex v) {
			for(Iterator<Edge> it= this.incidentUsefulEdges.iterator();it.hasNext();) {
				Edge cur=it.next();
				if(cur.dst.equals(v)||cur.src.equals(v)) {
					return cur;
				}
			}
			return null;

	}
	
	//checks if given a vertex v there is a Delaunay edge with endpoint v
	Edge hasincidentDelaunayEdge(Vertex v) {
		for(Iterator<Edge> it= this.incidentDelaunayEdges.iterator();it.hasNext();) {
			Edge cur=it.next();
			if(cur.dst.equals(v)||cur.src.equals(v)) {
				return cur;
			}
		}
		return null;

}
	
	public Vertex(double xval,double yval,int idVal) {
		x=xval;
		y=yval;
		id=idVal;
	}
	
	//Vertex with learningValue lval
	public Vertex(double xval,double yval,int idVal,double lVal) {
		x=xval;
		y=yval;
		id=idVal;
		this.learningValues[0]=lVal;
	}
	
	//Vertex with learningValue lval and reconstructionValue rVal
	public Vertex(double xval,double yval,int idVal,double lVal,double rVal) {
		x=xval;
		y=yval;
		id=idVal;
		this.learningValues[0]=lVal;
		this.learningValues[1]=rVal;
		
	}
	
	@Override
    public String toString() { 
        return String.format("("+ (int)x + ", " + (int)y+"|"+id+ ")"); 
    }
	
	@Override
	public boolean equals(Object o) {
		if(this.id==((Vertex)o).id ) {
			return true;
		}	
		return false;	
	}
	

	public int compareTo (Vertex obj){	
	    if (this.x < obj.x) return -1;
	    if (this.x > obj.x) return +1;
	    if (this.y < obj.y) return -1;
	    if (this.y > obj.y) return +1;
	    return 0;  
	}
	
	@Override
	public int hashCode() {
	    int hash = this.id;
	    return hash;
	 }
	 
}
