package dynamic_programming_for_MTWT;


import java.util.*;


public class PolygonContainingComponents extends Polygon {
	
	
	ArrayList<ConnectedComponent> containedComponents; //the connected components in the polygon
	
	double solutionValue=Double.MAX_VALUE;

	
	int overAllLeftMostComponentIndex; //the index of the overall leftmost CC
	int[] currentConnections; //the indices of the current set of connections
	
	
	HashSet<Edge> temporaryIntersectedEdges=new HashSet<Edge>(); //the edges that are currently intersected by connections
	
	
	PolygonContainingComponents(ArrayList<Vertex> vertices,ArrayList<ConnectedComponent> components,OrderKObjects myEdges){
		super(myEdges);
		boundaryVertices=vertices;
		containedComponents=components;
		this.numberOfComponents=components.size();

		
		this.getAllPossibleConnections();
		this.currentConnections=new int[this.numberOfComponents];
		
		
		double min=Double.MAX_VALUE;
		int minComponentIndex=-1;
		for(int i=0;i<this.numberOfComponents;i++) {
			if(this.containedComponents.get(i).leftMostVertex.x<min) {
				minComponentIndex=i;
				min=this.containedComponents.get(i).leftMostVertex.x;
			}
		}
		this.overAllLeftMostComponentIndex=minComponentIndex;
	}
	
	//get all allowed connections for all components
	private void getAllPossibleConnections() {
		for(int i=0;i<this.containedComponents.size();i++) {
			getAllPossibleConnectionsOneComponent(containedComponents.get(i));
		}
	}
	
	//get all possible connections to the left that may be
	//part of an optimal triangulation for one component
	private void getAllPossibleConnectionsOneComponent(ConnectedComponent c) {
		Vertex left=c.leftMostVertex;
		Vertex delaunayNeighbour=new Vertex(Double.MAX_VALUE,0,Integer.MAX_VALUE);
		
		//find the delaunay edge that goes furthest to the left
		for(Iterator<Vertex> it =left.delaunayNeighbours.iterator();it.hasNext();) {
			Vertex cur=it.next();
			if(cur.x<left.x&&cur.x<delaunayNeighbour.x) {
				delaunayNeighbour=cur;
			}
		}

		if (delaunayNeighbour.id==Integer.MAX_VALUE) {
			System.out.println("no delaunay neighour found!");
		}
		c.possibleConnections.add(delaunayNeighbour);
		
		
		
		//find all edges that intersect the Delaunay edge AND take the leftmost of the two Endpoints
		for(Iterator<Edge> it= this.base.usefulEdges.iterator();it.hasNext();) {
			Edge cur=it.next();
			if(BasicGeometry.doIntersect(cur.src,cur.dst,left,delaunayNeighbour)) {
				if(cur.src.x<cur.dst.x) {
					c.possibleConnections.add(cur.src);
				}
				else {
					c.possibleConnections.add(cur.dst);
				}
				
				
			}
		}
		
		HashSet<Vertex> wrongConnections=new HashSet<Vertex>();
		for(Iterator<Vertex> it= c.possibleConnections.iterator();it.hasNext();) {
			Vertex cur= it.next();
			//we can ignore edges that are right of the leftmost vertex of the component
			if(cur.x>left.x) {
				wrongConnections.add(cur);
			}
			//if the edge connecting the left most vertex of the component and the candidate vertex is not useful we can ignore it
			Edge curEdge=new Edge (cur,c.leftMostVertex);
			if(!this.base.usefulEdges.contains(curEdge)) {
				wrongConnections.add(cur);
			}
		}

		c.possibleConnections.removeAll(wrongConnections);
	}

	
	
	@Override
	//calculates the optimal triangulation
	public double MTWT() {
		backTrack(this.numberOfComponents-1);
		return this.solutionValue;
	}
	
	//backtrack over all possible sets of connections to find the ones containing in the optimal solution
	private void backTrack(int level) {
		//if level=-1 we have set all connections and need to perform our calculations
		if(level==-1) {		
			this.setAllTemporaryEdges();//set connections
			//if any connecting edges the set can be skipped
			if(checkForIntersectionOfConnectingEdges()) {
				deleteAllTemporaryEdges();
				return;
			}
			//checks for intersections since intersected edges need to be ignored
			checkForIntersections();
			//generate polygon with the connections and solve
			Polygon tmp=new Polygon(generateTemporaryPolygon(),this.base);
			double val=tmp.MTWT();
			if(val<this.solutionValue) {
				this.solutionValue=val;
				this.finalSolution=tmp.finalSolution;
			}
			//Delete everything that was added for the set of conections
			deleteAllTemporaryEdges();
			deleteIntersections();
		}
		//recursively fix the connections for the different components
		else {
			for(int i=0;i<this.containedComponents.get(level).possibleConnections.size();i++) {
				currentConnections[level]=i;
				backTrack(level-1);
			}
			
		}
		
		
	}

	
	//temporarily adds the connecting edges to the fixedneighbours
	private void setAllTemporaryEdges() {
		ArrayList<Edge> tmpForTesting=new ArrayList<Edge>();
		for(int i=0;i<currentConnections.length;i++) {
			int index=currentConnections[i];
			Vertex currentComponentVertex=this.containedComponents.get(i).leftMostVertex;
			Vertex currentConnectionVertex=this.containedComponents.get(i).possibleConnections.get(index);
			currentConnectionVertex.fixedNeighbours.add(currentComponentVertex);
			currentComponentVertex.fixedNeighbours.add(currentConnectionVertex);
			tmpForTesting.add(new Edge(currentComponentVertex,currentConnectionVertex));
		}
	}
	
	//deletes the connections from the fixedneighbours
	private void deleteAllTemporaryEdges() {
		for(int i=0;i<currentConnections.length;i++) {
			int index=currentConnections[i];
			Vertex currentComponentVertex=this.containedComponents.get(i).leftMostVertex;
			Vertex currentConnectionVertex=this.containedComponents.get(i).possibleConnections.get(index);
			currentConnectionVertex.fixedNeighbours.remove(currentComponentVertex);
			currentComponentVertex.fixedNeighbours.remove(currentConnectionVertex);		
		}
	}
	
	
	//checks if any of the connecting edges intersect. If this happens the set is not valid
	private boolean checkForIntersectionOfConnectingEdges() {
		for(int i=0;i<currentConnections.length;i++) {
			for(int j=0;j<currentConnections.length;j++) {
				Vertex currentComponentVertex1=this.containedComponents.get(i).leftMostVertex;
				Vertex currentConnectionVertex1=this.containedComponents.get(i).possibleConnections.get(currentConnections[i]);
				Vertex currentComponentVertex2=this.containedComponents.get(j).leftMostVertex;
				Vertex currentConnectionVertex2=this.containedComponents.get(j).possibleConnections.get(currentConnections[j]);
				if(BasicGeometry.doIntersect(currentComponentVertex1,currentConnectionVertex1,currentComponentVertex2,currentConnectionVertex2)) {
					return true;
				}
				
			}
		}
		return false;
	}
	
	
	//finds all edges that are intersected by the newly fixed edges, since they can not be used
	private void checkForIntersections() {
		for(int i=0;i<currentConnections.length;i++) {
			int index=currentConnections[i];
			Vertex currentComponentVertex=this.containedComponents.get(i).leftMostVertex;
			Vertex currentConnectionVertex=this.containedComponents.get(i).possibleConnections.get(index);
			Edge tmp=currentComponentVertex.hasincidentEdge(currentConnectionVertex);
			for(Iterator<Edge> it =tmp.intersectedUsefulEdges.values().iterator();it.hasNext();) {
				Edge toSet=it.next();
				toSet.hasIntersection=true;
			}
		}
	}
	
	//deletes the intersection markers
	private void deleteIntersections() {
		for(Iterator<Edge> it=this.base.usefulEdges.iterator();it.hasNext();) {
			Edge cur=it.next();
			cur.hasIntersection=false;
		}
	}
	
	//given the connections we get a single degenerate polygon we can calculate
	//Algo only works because it is one single Polygon!
	private ArrayList<Vertex> generateTemporaryPolygon() {
		ArrayList<Vertex> polygon=new ArrayList<Vertex>();
		
		//start at leftmost point on a component
		Vertex start=this.containedComponents.get(this.overAllLeftMostComponentIndex).possibleConnections.get(this.currentConnections[this.overAllLeftMostComponentIndex]);
		int index=this.boundaryVertices.indexOf(start);
		Vertex first=this.boundaryVertices.get((index+1)%this.boundaryVertices.size());
		
		
		polygon.add(start);
		polygon.add(first);
		this.getNextEdge(new Edge(start,first), first,start,first, polygon);
		
		
		polygon.remove(polygon.size()-1);
		return polygon;
	}	
	
	//recursive procedure that always takes the left most edge
	private boolean getNextEdge(Edge e, Vertex v ,Vertex startSrc, Vertex startDst,ArrayList<Vertex> polygon) {
		double min=Double.MAX_VALUE;
		Vertex minVertex= null;
		
		if(v.fixedNeighbours.size()==0) {
			System.out.println("there were no neighbours");
			return false;
		}
		
		for(Iterator<Vertex> it=v.fixedNeighbours.iterator();it.hasNext();) {
			Vertex cur=it.next();
			double val=calculateAngle(e,v,cur);
			if(val<min) {
				min=val;
				minVertex=cur;
				}
		}

		if((minVertex.equals(startDst)&&v.equals(startSrc) )){
			return true;
		}
		polygon.add(minVertex);
		
		getNextEdge(new Edge(v,minVertex),minVertex,startSrc,startDst,polygon);
		return true;

	}
	
	private double calculateAngle(Edge e, Vertex anchor, Vertex v) {
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
			targetA=BasicGeometry.normalizeAngle(targetA);
			
			return targetA;
			
			
		}
		
		
	}
	
	
}
