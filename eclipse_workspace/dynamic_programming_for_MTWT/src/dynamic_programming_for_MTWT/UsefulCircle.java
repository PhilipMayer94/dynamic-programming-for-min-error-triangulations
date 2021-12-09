package dynamic_programming_for_MTWT;
import java.util.*;

public class UsefulCircle {
	double r=0;
	double x=0;
	double y=0;
	HashSet<Vertex> innerVertices; //the vertices that define the usefulness, they are later needed, since they are candidates for triangles
	
	UsefulCircle(double xval,double yval,double radius){
		r=radius;
		x=xval;
		y=yval;
	}
	
	@Override
	public String toString() {
		return "(r:"+r+"|"+x+", "+y+")";
	}

}
