package dynamic_programming_for_MTWT;

import java.util.*;

public class Polygon {
	
	protected OrderKObjects base;
	
	
	//the  edges on the boundary of the polygon
	ArrayList<Vertex> boundaryVertices;
	ArrayList<Edge> boundaryEdges=new ArrayList<Edge>();
	int numberOfComponents=0;
	
	//the polygon edges and vertices with new classes since the orientation is important now
	ArrayList<PolygonVertex> polygonVertices= new ArrayList<PolygonVertex>();
	ArrayList<PolygonEdge> polygonEdges= new ArrayList<PolygonEdge>();
	
	
	HashSet<PolygonEdge> allEdges=new HashSet<PolygonEdge>(); // the edges that are inside the polygon

	
	
	//objects for the discrete DP optimization
	HashSet<PolygonTriangle> finalSolution=new HashSet<PolygonTriangle>();
	Hashtable<SubProblemKey,SubProblemSolution> subProblemTable=new Hashtable<SubProblemKey,SubProblemSolution>();
	
	
	public Polygon(OrderKObjects myEdges) {
		base=myEdges;
	}
	
	public Polygon(ArrayList<Vertex> vertices,OrderKObjects myEdges) {
		base=myEdges;		
		boundaryVertices=vertices;
		getPolygonAsEdges();
		getPolygonVerticesAndEdges();
		collectInteriorEdges();
		getAllTriangles();
		
		//it may happen that a vertex is used multiple time as "polygonVertex" because of degenerecies
		for(int i=0;i<boundaryVertices.size();i++) {
			boundaryVertices.get(i).tmpIds=new ArrayList<Integer>();
		}
		
	}
	
	
	/////////////////////////////////////////////////////////////////////geometry --- representation////////////////////////////////////////////////////////////////////
	
	//generates cyclic list of directed edges
	private void getPolygonAsEdges(){
		for(int i=0;i<boundaryVertices.size();i++) {
			int j=(i+1)%boundaryVertices.size();
			Vertex v1=boundaryVertices.get(i);
			Vertex v2=boundaryVertices.get(j);
			for(Iterator<Edge> it=v1.incidentUsefulEdges.iterator();it.hasNext();) {
				Edge cur= it.next();
				if((cur.dst.equals(v1) && cur.src.equals(v2))|| (cur.src.equals(v1) &&cur.dst.equals(v2)) ) {
					boundaryEdges.add(cur);
				}
			}	
		}
	}
	
	//generates a set of Polygon-edges and polygon-vertices that are independent of the KOD-Objects (easier combinatorics later)
	private void getPolygonVerticesAndEdges() {
		for(int i=0;i<this.boundaryVertices.size();i++) {
			Vertex tmp=this.boundaryVertices.get(i);
			tmp.tmpIds.add(i); // a vertex may appear several times as a polygonvertex since it the polygon is degenerate
			this.polygonVertices.add(new PolygonVertex(tmp,i));
		}
		
		for(int i=0;i<this.boundaryVertices.size();i++) {
			PolygonVertex tmp=this.polygonVertices.get(i);
			PolygonVertex tmp2=this.polygonVertices.get((i+1)%polygonVertices.size());
			PolygonEdge tmpEdge=new PolygonEdge(tmp,tmp2,this.boundaryEdges.get(i));
			this.polygonEdges.add(tmpEdge);
			tmp.edges.add(tmpEdge);
			this.allEdges.add(tmpEdge);
			
		}	
	}
	
	//finds all edges that are connected to the vertices and INSIDE the polygon (all other are not interesting for the optimization inside of the polygon)
	private void collectInteriorEdges() {
		for(int i=0;i<this.polygonVertices.size();i++) {
			collectEdgesForOneVertex(i);
		}
	}
	
	//collects all interior edges incident to a single vertex
	private void collectEdgesForOneVertex(int vertexId) {
		PolygonVertex v=this.polygonVertices.get(vertexId);
		for(Iterator<Edge> it= v.originalVertex.incidentUsefulEdges.iterator();it.hasNext();) {
			Edge cur=it.next();
			if(isInside(vertexId,cur)) {
				//cur is the endpoint
				if(cur.dst.equals(v.originalVertex)) {
					//add the edge with all possible Polygonvertex Ids (degenerate Polygon)
					for(int i=0;i<cur.src.tmpIds.size();i++) {
						int tId=cur.src.tmpIds.get(i);
						PolygonEdge tmpEdge=new PolygonEdge(v,this.polygonVertices.get(tId),cur);
						v.edges.add(tmpEdge);
						this.allEdges.add(tmpEdge);
					}	
				}
				
				//cur is the start point
				else {	
					for(int i=0;i<cur.dst.tmpIds.size();i++) {
						int tId=cur.dst.tmpIds.get(i);
						PolygonEdge tmpEdge=new PolygonEdge(v,this.polygonVertices.get(tId),cur);
						v.edges.add(tmpEdge);
						this.allEdges.add(tmpEdge);
					}
				}
			}	
		}
		
		Collections.sort(v.edges,  new Comparator<PolygonEdge>(){
		     @Override
		     public int compare(PolygonEdge e1, PolygonEdge e2)
		     {
		         return Integer.valueOf(e1.dst.id).compareTo( Integer.valueOf(e2.dst.id));
		     }        
		 });

	}

	
	//checks if the edge is inside of the polygon
	private boolean isInside(int vertexId,Edge e) {
		int j=vertexId-1;
		if(j<0) {
			j=j+polygonVertices.size();
		}
		PolygonVertex root=polygonVertices.get(vertexId);
		PolygonVertex pred=polygonVertices.get(j);
		PolygonVertex succ=polygonVertices.get((vertexId+1)%polygonVertices.size());
		Vertex target;
		
		if(e.dst.equals(root.originalVertex)) {
			target=e.src;
		}
		else {
			target=e.dst;
		}
		
		double angleTarget=calculateAngle(pred.originalVertex,root.originalVertex,target);
		double angleNext=calculateAngle(pred.originalVertex,root.originalVertex,succ.originalVertex);
		if(angleTarget<angleNext) {
			return true;
		}
		return false;
	}
	
	//for all edges get the triangles that are incident to the edges and inside the polygon
	private void getAllTriangles() {
		for(Iterator<PolygonEdge> it =this.allEdges.iterator();it.hasNext();) {
			PolygonEdge cur= it.next();
			getAllTrianglesForOneEdge(cur);				
		}
	}
	
	
	private void getAllTrianglesForOneEdge(PolygonEdge e) {
		//we can just look at the KOD triangles 
		for(Iterator<Triangle> it=e.originalEdge.incidentTriangles.iterator();it.hasNext();) {
			Triangle cur=it.next();
			getOneTriangle(cur,e);
		}
		Collections.sort(e.triangles,  new Comparator<PolygonTriangle>(){
		     @Override
		     public int compare(PolygonTriangle e1, PolygonTriangle e2)
		     {
		         return Integer.valueOf(e1.tip.id).compareTo( Integer.valueOf(e2.tip.id));
		     }        
		 });
		
		
	}
	
	//generates a triangle with respect to an edge, i.e. it's just the edge as base and a "tip" on the boundary
	//it should be noted that we do not need to check if the triangle intersects a polygon edge since we only look
	//at fixed edges that can not be intersected as polygon edges
	private void getOneTriangle(Triangle t,PolygonEdge e) {		
		Vertex v=t.getThirdVertex(e.src.originalVertex, e.dst.originalVertex);
		//only take the triangles left of the edge!
		if(e.isLeft(v)) {
			
			for(int i=0;i<v.tmpIds.size();i++) {
				int tId=v.tmpIds.get(i);		
				PolygonTriangle tmpTriangle= new PolygonTriangle(t,polygonVertices.get(tId));
				if(!e.triangles.contains(tmpTriangle)) {
					e.triangles.add(tmpTriangle);
				}
				
			}
		}
	}
	
	private double calculateAngle(Vertex pred, Vertex anchor, Vertex v) {
		Vertex anchorPred=pred;
		if(anchorPred.equals(v)) {
			return Math.PI*2;
		}	
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
	
	


	/////////////////////////////////////////////////////////////////////optimization////////////////////////////////////////////////////////////////////
	
	
	//checks if some index is in the range (i,j) were i may be bigger than j (mod n)
	private boolean isIndexAllow(int i, int j , int allowed) {
		if(j>=i) {
			if(allowed>i && allowed<j) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if(allowed<i || allowed >j) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	
	private int indexDistance(int i,int j) {
		int n =this.polygonVertices.size();
		if(i<j) {
			return j-i;
		}
		else {
			return j+n-i;
		}
	}
	
	//calculates the optimal solution to a sub-problem using recursion
	private double solveSubproblem(int i, int j) {
		
		SubProblemKey key =new SubProblemKey(i,j);
		int n =this.polygonVertices.size();
		int k= indexDistance(i,j);
		
		//base-case edge
		if(k==1) {
			return 0;
		}	
		
		//check if this problem has already been solved
		if(this.subProblemTable.containsKey(key)) {
			return subProblemTable.get(key).value;
		}
		
		//base-case triangle
		if(k==2) {
			//check if there is an edge from j to i
			PolygonVertex vj=this.polygonVertices.get(j);
			PolygonEdge ei= vj.hasEdge(i);

			if(ei==null) {
				return Double.MAX_VALUE;
			}
			//if there is an edge check if it form a triangle with j i  i+1 
			else {
				PolygonTriangle tq=ei.hasTriangle((i+1)%n);

				if(tq==null) {
					return Double.MAX_VALUE;
				}
				//if it the case set the cost of the subproblem to the weight of the triangle and recall the splitting triangle tq
				else {
					SubProblemSolution tmpSolution=new SubProblemSolution(key,tq,tq.originalTriangle.getWeight());
					this.subProblemTable.put(key,tmpSolution);
					return tq.originalTriangle.getWeight();
				}
			}
		}
		
		//general Case:
		PolygonTriangle minTriangle=null;
		double minValue=Double.MAX_VALUE;
		
		//check if there is an edge from j to i
		PolygonVertex vj=this.polygonVertices.get(j);
		
		PolygonEdge ei= vj.hasEdge(i);
		if(ei==null) {
			this.subProblemTable.put(key,new SubProblemSolution(key,null,Double.MAX_VALUE));
			return Double.MAX_VALUE;
		}
		
		//if there is an edge: Iterate over all possible triangles
		//in the correct index range, i.e. between i and j and find the optimal one
		ArrayList<PolygonTriangle> triangles=ei.triangles;
		for(Iterator<PolygonTriangle> it=triangles.iterator();it.hasNext();) {
			PolygonTriangle currentTriangle=it.next();

			int q=currentTriangle.tipId;
			if(!this.isIndexAllow(i, j, q)) {
				continue;
			}
			else {
				//recursive formula: weight of splitting triangle + optLeft + optRight
				double triangleWeight= currentTriangle.originalTriangle.getWeight();
				double sol1= solveSubproblem(i,q);
				double sol2=solveSubproblem(q,j);
				double tmpValue=triangleWeight+sol1+sol2;
				if(tmpValue<minValue) {
					minValue=tmpValue;
					minTriangle=currentTriangle;
				}
			}
			
		}	
		
		//set the value to the best one
		SubProblemSolution tmpSolution=new SubProblemSolution(key,minTriangle,minValue);
		this.subProblemTable.put(key,tmpSolution);
		return minValue;
	}
	
	//calculates the MTWT by using the recursive procedure and saves the solution in finalSolution
	public double MTWT() {
		double tmp= solveSubproblem(0,this.polygonVertices.size()-1);
		if(tmp<Double.MAX_VALUE) {
			this.backTrackSolution();
		}
		return tmp;
	}
	
	
	//backtracks all splitting triangles and the base Triangles which results in the solution
	HashSet<PolygonTriangle> backTrackSolution(){
		HashSet<PolygonTriangle> result=new HashSet<PolygonTriangle>();
		int n= this.polygonVertices.size()-1;

		//initial call must be 0,n in our implementation!
		SubProblemSolution tmp=this.subProblemTable.get(new SubProblemKey(0,n));
		int q=tmp.solutionTriangle.tipId;
		result.add(tmp.solutionTriangle);

		HashSet<PolygonTriangle> subProblem1=backTrackSolutionRec(0, q);
		HashSet<PolygonTriangle> subProblem2=backTrackSolutionRec(q, n);
		
		
		if(subProblem1!=null) {
			result.addAll(backTrackSolutionRec(0, q));
		}
		if(subProblem2!=null) {
			result.addAll(backTrackSolutionRec(q, n));
		}
		
		this.finalSolution=result;
		return result;
		
	}
	
	//recursive collection of the splitting triangles
	HashSet<PolygonTriangle> backTrackSolutionRec(int i,int j){
		HashSet<PolygonTriangle> result=new HashSet<PolygonTriangle>();
		
		//edge-case
		if(this.subProblemTable.get(new SubProblemKey(i,j))==null) {
			return null;
		}
		
		int k= j-i;
		if(j-i<0) {
			k=j+this.polygonVertices.size()+j-i;
		}
		
		//base-triangle
		if(k==2) {
			PolygonTriangle tmp=this.subProblemTable.get(new SubProblemKey(i,j)).solutionTriangle;
			if(tmp==null) {
				return null;
			}
			else {
				result.add(this.subProblemTable.get(new SubProblemKey(i,j)).solutionTriangle);
				return result;
			}
			
		}
		
		//edge-case
		if(k==1) {
			return null;
		}
		
		//general-case
		else {
			PolygonTriangle recTriangle=this.subProblemTable.get(new SubProblemKey(i,j)).solutionTriangle;
			int q=recTriangle.tipId;
			result.add(recTriangle);
			
			HashSet<PolygonTriangle> subProblem1=backTrackSolutionRec(i, q);
			HashSet<PolygonTriangle> subProblem2=backTrackSolutionRec(q, j);
			if(subProblem1!=null) {
				result.addAll(subProblem1);
			}
			if(subProblem2!=null) {
				result.addAll(subProblem2);
			}
			return result;
			
		}
	}
	

	
	
}
