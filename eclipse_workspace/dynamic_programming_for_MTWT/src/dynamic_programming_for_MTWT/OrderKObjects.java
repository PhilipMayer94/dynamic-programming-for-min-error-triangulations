package dynamic_programming_for_MTWT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;






public class OrderKObjects {
	public HashSet<Vertex>vertices=new HashSet<Vertex>();
	private Set<Edge> edges=new HashSet<Edge>(); //K-OD edges
	
	public Set<Edge> usefulEdges=new HashSet<Edge>(); //Useful K-OD edges
	public Set<Edge> fixedUsefulEdges=new HashSet<Edge>(); //fixed Useful K-OD edges
	public Set<Edge> variableUsefulEdges=new HashSet<Edge>(); //variable Useful K-OD edges (degree of freedom for optimization)
		
	public Set<Triangle> allKODTriangles=new HashSet<Triangle>();
	
	public Delaunay delaunay;
	
	public int order =2;
	
	OrderKObjects(double[][] points,int wantedOrder){
		//we only have the coordinates and no values
		if(points[0].length==2) {
			this.order=wantedOrder;
			for(int i=0; i<points.length;i++) {
				vertices.add(new Vertex(points[i][0],points[i][1],i));
			}
		}
		
		//we have coordinates and learning values
		if(points[0].length==3) {
			this.order=wantedOrder;
			for(int i=0; i<points.length;i++) {
				vertices.add(new Vertex(points[i][0],points[i][1],i,points[i][2]));
			}
		}
		
		//we have coordinates and learning and reconstruction values
		if(points[0].length==4) {
			this.order=wantedOrder;
			for(int i=0; i<points.length;i++) {
				vertices.add(new Vertex(points[i][0],points[i][1],i,points[i][2],points[i][3]));
			}
		}	
		
		
		delaunay= new Delaunay(vertices);
		
		
		addDelaunayObjectsToKOD();
		getAllEdges(order);
		collectUsefulEdges();
		distributeIncidentUseFulEdges();
		getUsefulTriangles();
		collectFixedEdges();
		
	
	}
	
	//add all of the Delaunay triangles do KOD since they would not be considered during the KOD tests for k>0
	private void addDelaunayObjectsToKOD() {
		for(Iterator<Edge> it=delaunay.edges.iterator();it.hasNext();) {
			Edge cur= it.next();
			edges.add(cur);
		}
		for(Iterator<Triangle> it=delaunay.triangles.iterator();it.hasNext();) {
			Triangle cur=it.next();
			
			this.allKODTriangles.add(cur);
			
			for(int i=0;i<3;i++) {
				cur.edges[i].incidentTriangles.add(cur);
			}
		}
		
	}
	
	//just adds all edges to be considered (normaly we would use higher order Voronois here for a speed up)
	private void getAllEdges(int k ) {
		Vertex[] vertexArray=vertices.toArray(new Vertex[vertices.size()]);
		int n =vertexArray.length;

		for(int i=0;i<n;i++) {
			for(int j=i+1;j<n;j++) {
				edges.add(new Edge(vertexArray[i],vertexArray[j]));
			}
		}
	}
	
	//checks if the triangle t is KOD and also adds the contained vertices to the useful circel of edge e
	private boolean isKODTriangle(int k,UsefulCircle circle,Triangle t,Edge e) {
		int counter=0;
		Vertex[] vertexArray=vertices.toArray(new Vertex[vertices.size()]);
		HashSet<Vertex> containedVertices= new HashSet<Vertex>();
		int n =vertexArray.length;
			for(int i=0;i<n;i++) {
				//checks if point is in the circumcircle (useful circle) of t
				double dist=BasicGeometry.calculateDistance(vertexArray[i].x,vertexArray[i].y,circle.x,circle.y);
				if(dist<circle.r && !vertexArray[i].equals(t.vertices[0])&& !vertexArray[i].equals(t.vertices[1])&& !vertexArray[i].equals(t.vertices[2]) ) {
					containedVertices.add(vertexArray[i]);
					counter++;
					if(counter>k) {
						return false;
					}
				}
				//here we also allow the third vertex of the triangle to be in the useful circle of e
				if(dist==circle.r && !vertexArray[i].equals(e.dst)&& !vertexArray[i].equals(e.src)) {
					containedVertices.add(vertexArray[i]);
				}
				
			}
		//not order k?
		if(counter>k) {
			return false;
		}
		circle.innerVertices=containedVertices;
		return true;
		
	}
	
	//collects all useful edges
	private void collectUsefulEdges() {		
		Iterator<Edge> it = this.edges.iterator();
	    while(it.hasNext()){
	    	Edge eTmp=it.next();
	        if(this.isEdgeUseFul(eTmp)) {
	        	this.usefulEdges.add(eTmp);
	        };
	     }
	}

	
	//checks if a given edge is useful
	private boolean isEdgeUseFul(Edge e) {	
		ArrayList<Edge> interEdges= collectDelaunayEdgeIntersectionsFast(e);
		
		
		//It may happen that the Delaunay Triangulation misses an Edge
		//because of numerical problems, the following fixes that
		if(interEdges.isEmpty()) {
			if(!this.delaunay.edges.contains(e)) {
				this.usefulEdges.add(e);
				e.src.delaunayNeighbours.add(e.dst);
				e.dst.delaunayNeighbours.add(e.src);
				e.src.incidentDelaunayEdges.add(e);
				e.dst.incidentDelaunayEdges.add(e);
				this.delaunay.edges.add(e);
				this.addDelaunayTriangle(e);
			}
			return true;
		}
			
		//check if the number edges is to big to be useful
		if(interEdges.size()>2*order-1) {
			return false;	
		}
			
		//check if the special triangles are KOD (see "Higher Order Delaunay Triangulations by Gudmundsson et al")
		return this.hullKODtest(e, interEdges);
	}

	
    //for a given edge calculate all of the Delaunay edges which intersect the edge
	//essentially we traverse the Dual Graph along the vector given by start and endpoint of the edge
	private ArrayList<Edge> collectDelaunayEdgeIntersectionsFast(Edge e) {
		ArrayList<Edge> result= new ArrayList<Edge>();
		HashSet<Triangle> alreadyVisited=new HashSet<Triangle>();//we do not want to go backwards
		Boolean hasFound=false;
		Edge nextEdge=null;
		Triangle firstTriangle=null;
		
		//Search for the starting triangle 
		//and the first edge that is intersected
		for(Iterator<Edge> itE=e.src.incidentDelaunayEdges.iterator();itE.hasNext()&& !hasFound;) {
			Edge curE= itE.next();
			for(Iterator<Triangle> itT=curE.incidentDelaunayTriangles.iterator();itT.hasNext()&& !hasFound;) {
				Triangle curT=itT.next();
				for(int i=0;i<3&& !hasFound;i++) {
					if(BasicGeometry.doIntersect(e, curT.edges[i])) {
						nextEdge=curT.edges[i];
						firstTriangle=curT;
						alreadyVisited.add(firstTriangle);
						hasFound=true;
						break;	
					}
				}
			}
		}
		
		if(nextEdge!=null) {
			result.add(nextEdge);
			traverseDelaunayIntersections(result,e,alreadyVisited,nextEdge);
		}
		return result;
	}
	
	//recursive traversal of the dual graph
	private void traverseDelaunayIntersections(ArrayList<Edge> intersections,Edge mainEdge,HashSet<Triangle> alreadyVisited,Edge currentEdge){
		Triangle currentTriangle=null;
		Edge nextEdge;
		
		//find the adjacent triangle that is not going backwards
		for(Iterator<Triangle> it=currentEdge.incidentDelaunayTriangles.iterator();it.hasNext();) {
			Triangle cur=it.next();
			if(!alreadyVisited.contains(cur)) {
				currentTriangle=cur;
			}
			
		}
		//if there is non we are done
		if(currentTriangle==null) {
			return;
		}
		//if one of the vertices is our target we are done
		if(mainEdge.dst.id==currentTriangle.vertices[0].id ||mainEdge.dst.id==currentTriangle.vertices[1].id ||mainEdge.dst.id==currentTriangle.vertices[2].id ) {
			return;
		}
		//otherwise find the edge of the triangle that is intersected and is not the one we are coming from
		for(int i=0;i<3;i++) {
			if(!currentTriangle.edges[i].equals(currentEdge)&&BasicGeometry.doIntersect(mainEdge, currentTriangle.edges[i])) {
				nextEdge=currentTriangle.edges[i];
				alreadyVisited.add(currentTriangle);
				intersections.add(nextEdge);
				traverseDelaunayIntersections(intersections,mainEdge,alreadyVisited,nextEdge);
				return;
			}
		}
		
	}

	
	
	private boolean hullKODtest(Edge e, ArrayList<Edge> intersectingEdges){
		HashSet<Vertex> allHullVertices= collectAllHullVertices(intersectingEdges);
		HashSet<Vertex> leftHull= new HashSet<Vertex>();
		HashSet<Vertex> rightHull=new HashSet<Vertex>();
		Iterator<Vertex> it = allHullVertices.iterator();
		
		//sorts the hull into left and right
	    while(it.hasNext()){
	    	Vertex v=it.next();
	        if(BasicGeometry.isLeft(v,e)) {
	        	leftHull.add(v);
	        }
	        else {
	        	rightHull.add(v);
	        }
	    }
	    
	    //check if the hull is to big to be useful
	    if(leftHull.size()>this.order || rightHull.size()>this.order) {
	    	return false;
	    }
	    
	    //find the left vertex v that does not contain any other left vertex is in the circle
	    //check if the triangle with v is kOD
	    Vertex lVertex= emptyHullVertex(leftHull, e );
	    double[] c= BasicGeometry.calculateCircle3Points(lVertex, e.dst, e.src);
	    UsefulCircle lCircle=new UsefulCircle(c[0],c[1],c[2]);
	    Triangle tmpTriangle=new Triangle (lVertex, e.dst, e.src);
	    Boolean lBool=isKODTriangle(order,lCircle,tmpTriangle,e);
	    e.leftCircle=lCircle;
	    
	    
	    //find the right vertex v that does s.t no other right vertex is in the circle
	    //check if the triangle with v is kOD
	    Vertex rVertex= emptyHullVertex(rightHull, e );
	    c= BasicGeometry.calculateCircle3Points(rVertex, e.dst, e.src);
	    UsefulCircle rCircle=new UsefulCircle(c[0],c[1],c[2]);
	    tmpTriangle=new Triangle (rVertex, e.dst, e.src);
	    Boolean rBool=isKODTriangle(order,rCircle,tmpTriangle,e);
	    e.rightCircle=rCircle;
	    
	    //if both triangles are kOD edge is useful
	    if(rBool && lBool) {
	    	return true;
	    }
		return false;
	}

	
	//given the intersecting edges calculate all the vertices which are part of the edges
	private HashSet<Vertex> collectAllHullVertices(ArrayList<Edge> edges){
		HashSet<Vertex> result= new HashSet<Vertex>();
		for(int i =0;i<edges.size();i++) {
			Edge e= edges.get(i);
			result.add(e.src);
			result.add(e.dst);
		}
		return result;
	}
	
	
	//Checks for every vertex in the hull if the circle v,e1,e2 contains any other point (O(k^2))
	private Vertex emptyHullVertex(HashSet<Vertex> vertices, Edge e ) {
		Iterator<Vertex> it=vertices.iterator();
	    Vertex emptyVertex=new Vertex(0,0,-1);
	    //check for every Hull vertex if the circle contains any other Hull vertex
	    while(it.hasNext()){
	    	int counter=0;
	    	Iterator<Vertex> it2 = vertices.iterator();
	    	Vertex v=it.next();
	    	double[] c= BasicGeometry.calculateCircle3Points(v, e.dst, e.src);	
	    	while(it2.hasNext()) {
	    		Vertex vTmp=it2.next();
	    		if(v.equals(vTmp)) {
	    			continue;
	    		}
	    		if(BasicGeometry.calculateDistance(vTmp.x, vTmp.y, c[0], c[1])<c[2]) {
	    			counter=counter+1;
	    		}
	    	}
	    	if(counter==0) {
	    		emptyVertex=v;
	    	}
	    }
	    return emptyVertex;
	
	}
	
	
	//generates a graph-structure on the vertices with respect to the useful Edges
	private void distributeIncidentUseFulEdges() {
		for(Iterator<Edge> it=this.usefulEdges.iterator();it.hasNext();) {
			Edge cur= it.next();
			cur.dst.incidentUsefulEdges.add(cur);
			cur.src.incidentUsefulEdges.add(cur);
			cur.dst.adjacentUsefulNeighbours.add(cur.src);
			cur.src.adjacentUsefulNeighbours.add(cur.dst);
		}
	}
	
	//finds all usefulTriangles
	private void getUsefulTriangles() {
		for(Iterator<Edge> it=this.usefulEdges.iterator();it.hasNext();) {
			Edge cur=it.next();
			getUsefulTrianglesOneEdge(cur);
		}
	}
	
	
	private void getUsefulTrianglesOneEdge(Edge e) {
		HashSet<Vertex> candidates=new HashSet<Vertex>();
		//Delaunay triangles have already been added
		if(e.leftCircle==null || e.rightCircle==null) {
			return;
		}
		
		//the candidates for third points are all vertices in the empty circles
		candidates.addAll(e.leftCircle.innerVertices);
		candidates.addAll(e.rightCircle.innerVertices);
		
		//checks for all possible triangles if they are KOD
		for(Iterator<Vertex> curIt=candidates.iterator();curIt.hasNext();) {
			Triangle curTriangle;
			double [] curCircle;
			Vertex cur=curIt.next();
			Edge e2= e.dst.hasincidentEdge(cur);
			Edge e3=e.src.hasincidentEdge(cur);
			if(e2==null|| e3==null) {
				continue;
			}
			else {
				curTriangle=new Triangle(e,e2,e3);
				curCircle=BasicGeometry.calculateCircle3Points(curTriangle.vertices[0],curTriangle.vertices[1],curTriangle.vertices[2]);
				if(isRelativeKODTriangle(order,curCircle,curTriangle,candidates)&&containsNoPoint(curTriangle,candidates)) {
					
					this.allKODTriangles.add(curTriangle);
					e.incidentTriangles.add(curTriangle);
					e2.incidentTriangles.add(curTriangle);
					e3.incidentTriangles.add(curTriangle);
				}
			}
	
		}	
	}
	
	//adds a Delaunay triangle to all of its incident edges as Delaunay triangle 
	private void addDelaunayTriangle(Edge e) {
		for(Iterator<Vertex> v3It=e.src.delaunayNeighbours.iterator();v3It.hasNext();) {
			Vertex cur1=v3It.next();
			for(Iterator<Vertex> v3It2=e.dst.delaunayNeighbours.iterator();v3It2.hasNext();) {
				Vertex cur2=v3It2.next();
				if(cur1.equals(cur2)) {
					Edge e2=e.src.hasincidentDelaunayEdge(cur1);
					Edge e3 =e.dst.hasincidentDelaunayEdge(cur1);
					Triangle curTriangle=new Triangle(e,e2,e3);					
					if(containsNoPoint(curTriangle,this.vertices)) {
						this.allKODTriangles.add(curTriangle);
						e.incidentTriangles.add(curTriangle);
						e2.incidentTriangles.add(curTriangle);
						e3.incidentTriangles.add(curTriangle);
						e.incidentDelaunayTriangles.add(curTriangle);
						e2.incidentDelaunayTriangles.add(curTriangle);
						e3.incidentDelaunayTriangles.add(curTriangle);
					}
				}
			}	
		}
	}
	
	//checks if triangle is K-OD triangle with respect to the other hull vertices
	private boolean isRelativeKODTriangle(int k,double[] circle,Triangle t,HashSet<Vertex> verticesForComparison) {
		int counter=0;
		Vertex[] vertexArray=verticesForComparison.toArray(new Vertex[verticesForComparison.size()]);
		int n =vertexArray.length;
			for(int i=0;i<n;i++) {
				double dist=BasicGeometry.calculateDistance(vertexArray[i].x,vertexArray[i].y,circle[0],circle[1]);
				if(dist<circle[2] && !vertexArray[i].equals(t.vertices[0])&& !vertexArray[i].equals(t.vertices[1])&& !vertexArray[i].equals(t.vertices[2]) ) {
					counter++;
					if(counter>k) {
						return false;
					}
				}
			}
		if(counter>k) {
			return false;
		}
			
		return true;
		
	}
	
	
	//checks if triangle is empty
	private boolean containsNoPoint(Triangle t, HashSet<Vertex> verticesForComparison) {
		for(Iterator<Vertex>it= verticesForComparison.iterator();it.hasNext();) {
			Vertex cur=it.next();
			if(t.pointInTriangle(cur.x, cur.y)) {
				return false;
			}
		}
		return true;
		
	}
	
	
	//O(n^2k^2) bruteforce intersection check
	private void collectFixedEdges() {
		Iterator<Edge> it=usefulEdges.iterator();
		//a useful Edge is fixed iff it does not intersect any other useful Edge
	    while(it.hasNext()){
	    	int counter=0;
	    	Iterator<Edge> it2 = usefulEdges.iterator();
	    	Edge e=it.next();
	    	while(it2.hasNext()) {
	    		Edge eTmp=it2.next();
	    		if(BasicGeometry.doIntersect(e, eTmp)) {
	    			e.intersectedUsefulEdges.put(eTmp, eTmp);
	    			counter=counter+1;
	    		}
	    	}
	    	if(counter==0) {
	    		this.fixedUsefulEdges.add(e);
	    	}
	    	else {
	    		this.variableUsefulEdges.add(e);
	    	}
	    }
	}
	
	
	
	
}
