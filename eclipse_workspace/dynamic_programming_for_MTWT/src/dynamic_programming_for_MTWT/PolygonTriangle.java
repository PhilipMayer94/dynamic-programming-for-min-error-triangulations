package dynamic_programming_for_MTWT;

//these triangles are incident to an edge an have a tip, i.e. the third vertex given the edge
public class PolygonTriangle {
	Triangle originalTriangle;
	PolygonVertex tip;
	int tipId;
	double weight;
	
	PolygonTriangle(Triangle originalTriangleToSet, PolygonVertex tipToSet){
		originalTriangle=originalTriangleToSet;
		tip=tipToSet;
		tipId=tipToSet.id;
		weight=originalTriangle.weight;		
	}
	
	public boolean equals(Object o) {
		if(this.tipId==((PolygonTriangle)o).tipId) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "[tip: " + tip.id+ " ortip:"+ tip.originalVertex.id+"]";
	}
}
