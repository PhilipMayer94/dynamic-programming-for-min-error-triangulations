package dynamic_programming_for_MTWT;
import io.github.jdiemke.triangulation.*;
import java.util.*;



public class Delaunay {
	Set<Vertex>vertices=new HashSet<Vertex>(); //vertices in our class
	Set<Edge> edges=new HashSet<Edge>(); //edges in our class
	Set<Triangle> triangles=new HashSet<Triangle>(); //triangles in our class
	
	private Hashtable<Edge,Edge> tmpEdges=new Hashtable<Edge,Edge>();
	
	List<Triangle2D> triangleSoup; //triangles generated by the Delaunay-triangulation
	
	
			
	Delaunay(Set<Vertex> inputVertices){
		vertices=inputVertices; //set needed for our algorithms
		int n =vertices.size();
		if(n<3) {
			System.out.println("something went wrong initializing");
			return;
			} 
		try {
			
			//generate 2D vector pointset
			 Vector<Vector2D> pointSet=new Vector<Vector2D>(); 
			 Vertex[] tmpArray=vertices.toArray(new Vertex[vertices.size()]);
			 for(int i=0;i<tmpArray.length;i++) {
			    pointSet.add(new Vector2D(tmpArray[i].x,tmpArray[i].y));
			 }
			 
			 //use the Triangulation subroutine
			 DelaunayTriangulator delaunayTriangulator = new DelaunayTriangulator(pointSet);
			 delaunayTriangulator.triangulate();
			 
			 //collect our DelaunayObjects and adds a Graphstructure with respect to the Delaunay stuff
			List<Triangle2D> triangleSoup = delaunayTriangulator.getTriangles();
			SetDelaunayObjects(triangleSoup);
			} catch (NotEnoughPointsException e) {}
			
			
		}
	
	
	void SetDelaunayObjects(List<Triangle2D> triangleOfAlgo) {
		for (int i = 0; i < triangleOfAlgo.size(); i++) {
			getTriangleFromTriangle(triangleOfAlgo.get(i));
		}
		edges.addAll(tmpEdges.values());
	}
	
	Vertex[] getVerticesFromTriangles(Triangle2D t) {
		int counter=0;
		Vertex[] result =new Vertex[3];
		Vertex[] tmpArray=vertices.toArray(new Vertex[vertices.size()]);
		
		for(int i=0; i<tmpArray.length;i++){		
			if(tmpArray[i].x==t.a.x && tmpArray[i].y==t.a.y) {
				result[counter]=tmpArray[i];
				counter++;
			}
			if(tmpArray[i].x==t.b.x && tmpArray[i].y==t.b.y) {
				result[counter]=tmpArray[i];
				counter++;
			}
			if(tmpArray[i].x==t.c.x && tmpArray[i].y==t.c.y) {
				result[counter]=tmpArray[i];
				counter++;
			}

			if(counter==3) {
				break;
			}
		}
		return result;
	}
	
	//add the edges corresponding to a Delaunay Triangle
	Edge[] getEdgesFromTriangle(Triangle2D t) {
		Vertex[] tmpVertices =getVerticesFromTriangles(t);
		if(tmpVertices==null) {
			return null;
		}
		Edge[] result= new Edge[3];
		Edge cur=null;
		
		result[0]=new Edge (tmpVertices[0],tmpVertices[1]);
		cur=tmpEdges.get(result[0]);
		if(cur!=null){
			result[0]=cur;
		}
		else {
			tmpEdges.put(result[0],result[0]);
		}
		result[1]=new Edge (tmpVertices[1],tmpVertices[2]);
		cur=tmpEdges.get(result[1]);
		if(cur!=null){
			result[1]=cur;
		}
		else {
			tmpEdges.put(result[1],result[1]);
		}
		result[2]=new Edge (tmpVertices[0],tmpVertices[2]);
		cur=tmpEdges.get(result[2]);
		if(cur!=null){
			result[2]=cur;
		}
		else {
			tmpEdges.put(result[2],result[2]);
		}
		
		for(int i=0;i<3;i++) {
			cur= result[i];
			cur.dst.delaunayNeighbours.add(cur.src);
			cur.src.incidentDelaunayEdges.add(cur);
			cur.src.delaunayNeighbours.add(cur.dst);
			cur.dst.incidentDelaunayEdges.add(cur);
		}
		
		
		return result;
		
	}

	
	
	//add the edges corresponding to a Delaunay Triangle
	Edge[] getEdgesFromTriangle2(Triangle2D t) {
			Vertex[] tmpVertices =getVerticesFromTriangles(t);
			if(tmpVertices==null) {
				return null;
			}
			Edge[] result= new Edge[3];
			result[0]=new Edge (tmpVertices[0],tmpVertices[1]);
			result[1]=new Edge (tmpVertices[1],tmpVertices[2]);
			result[2]=new Edge (tmpVertices[0],tmpVertices[2]);
			return result;
			
		}
	
	//transforms a 2Dtriangle to one we use
	Triangle getTriangleFromTriangle(Triangle2D t) {
		Edge[] edges=getEdgesFromTriangle(t);
		
		Triangle result=new Triangle(edges[0],edges[1],edges[2]);
		for(int i=0;i<3;i++) {
			edges[i].incidentDelaunayTriangles.add(result);
		}
		triangles.add(result);
		return result;
	}
	
	//get the edges from all Delaunay Triangles
	void collectDelaunayEdges(List<Triangle2D> triangles) {
		for (int i = 0; i < triangles.size(); i++) {
			Edge[] newEdges=getEdgesFromTriangle2(triangles.get(i));
			if(newEdges==null) {
				continue;
			}
			edges.add(newEdges[0]);
			edges.add(newEdges[1]);
			edges.add(newEdges[2]);
        }
		
	}; 
	
	

}