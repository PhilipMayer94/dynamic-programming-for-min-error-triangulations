package dynamic_programming_for_MTWT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;


public class PolygonDecomposition {
	private OrderKObjects myObjects;
	private ArrayList<ArrayList<Vertex>> allPolygons=new ArrayList<ArrayList<Vertex>>();
	ArrayList<Polygon> polygons=new ArrayList<Polygon>();
	private ConnectedComponent[] connectedComponents;
	private int numberofComponents = 0;
	
	
	
	
	PolygonDecomposition(OrderKObjects orderKObjects){
		myObjects=orderKObjects;
		
		//generates a graph structure on the fixed edges and finds different connected components
		initializeConnections();
	    this.generateGraph();
	    this.labelVertices();
	    this.getComponents();
	    this.getCyclePolygons();
	    this.getPolygons();
	    this.getPolygonsWithComponentsInsideNew();
	}
	
	public void initializeConnections() {
		for(Iterator<Vertex> it=this.myObjects.vertices.iterator();it.hasNext();) {
			Vertex cur=it.next();
			cur.neighboursForAlgo=new HashSet<Vertex>();
			cur.neighboursTree=new HashSet<Vertex>();
		}
	}
	
	//add generate a graph structure by adding all of the neighbours to a vertex (with edges given by the fixed edges!)
	private void generateGraph() {
		Iterator<Edge> it=myObjects.fixedUsefulEdges.iterator();
		while(it.hasNext()) {
			Edge tmp =it.next();
			tmp.dst.fixedNeighbours.add(tmp.src);
			tmp.src.fixedNeighbours.add(tmp.dst);
		}
	}
	
	//find all connected components
	private void labelVertices() {
		Iterator<Vertex> it=myObjects.vertices.iterator();
	
		//iterate over all vertices once and check if they are already part of a connected component
		while(it.hasNext()) {
			Vertex tmp =it.next();
			if(tmp.label==0) {
				this.numberofComponents++;
				int n=this.numberofComponents;
				markVertices(tmp,n);
			}
			
		}
		
	}

	
	//DFS to mark all vertices in the same component
	private void markVertices(Vertex v,int k) {
		v.label=k;
		Iterator<Vertex> it=v.fixedNeighbours.iterator();
		while(it.hasNext()) {
			Vertex tmp =it.next();
			if(tmp.label==0) {
				markVertices(tmp,k);
			}
		}
	}
	
	//groups together all vertices with the same label
	private void getComponents() {
		this.connectedComponents=new ConnectedComponent[this.numberofComponents];
		
		Vertex[] vertexArray=myObjects.vertices.toArray(new Vertex[myObjects.vertices.size()]);
		ArrayList<ArrayList<Vertex>> componentsSplit = new ArrayList<ArrayList<Vertex>>();
		
		
		for(int i=0;i<this.numberofComponents;i++) {
			componentsSplit.add(new ArrayList<Vertex>());
		}
		
		for(int i=0;i<vertexArray.length;i++) {
			Vertex tmp=vertexArray[i];
			componentsSplit.get(tmp.label-1).add(tmp);
		}
		
		for(int i=0;i<componentsSplit.size();i++) {
			Vertex[] component= componentsSplit.get(i).toArray(new Vertex[componentsSplit.get(i).size()]);
			this.connectedComponents[i]=new ConnectedComponent(new HashSet<Vertex>(Arrays.asList(component)));
		}
		
	}

	
	//finds all of the polygons if we exclude all edges of the fixed edge graph
	//that result in tree like structures
	private void getCyclePolygons() {

		this.getCycleComponents();
		
		ArrayList<ArrayList<Vertex>> localAllPolygons=new ArrayList<ArrayList<Vertex>>();
		ArrayList<Vertex> currentPolygon=new ArrayList<Vertex>();
		
		
		boolean notFinished=true;
		Iterator<Vertex> it=myObjects.vertices.iterator();
		while(notFinished) {
			it=myObjects.vertices.iterator();
			
			//find one "Cycle Polygon"
			while(it.hasNext()) {
				currentPolygon=new ArrayList<Vertex>();
				Vertex tmp=it.next();
				if(tmp.neighboursForAlgo.size()>0) {
					
					//find the starting edge
					currentPolygon.add(tmp);
					Vertex nextVertex=tmp.neighboursForAlgo.iterator().next();
					currentPolygon.add(nextVertex);
					Edge startEdge= new Edge(tmp,nextVertex);
					
					//recursively calculate the cycle
					if(!getNextEdge(startEdge,nextVertex,tmp,nextVertex,currentPolygon)){
						continue;
					}
					
					//the last vertex has already equals the first one!
					currentPolygon.remove(currentPolygon.size()-1);
					//checks that the orientation of the polygon is correct "deletes outer Faces"
					if(this.shoelaceformula(currentPolygon)) {
						localAllPolygons.add(currentPolygon);
					}		

				}
		
			}
			notFinished=false;
			
			//checks if there is still a cycle that has not been found
			Iterator<Vertex> it2=myObjects.vertices.iterator();
			while(it2.hasNext()) {
				Vertex x=it2.next();
				if(x.neighboursForAlgo.size()>0) {
					notFinished=true;
				}
			}
		}
		
		
		this.allPolygons.addAll(localAllPolygons);
	}

	//splits the Graph into a forest and all components that are essentially cycles
	private void getCycleComponents() {
		Iterator<Vertex> it=myObjects.vertices.iterator();
		while(it.hasNext()) {
			Vertex tmp=it.next();
			tmp.neighboursForAlgo=new HashSet<Vertex>(tmp.fixedNeighbours);

		}
		it=myObjects.vertices.iterator();
		while(it.hasNext()) {
			Vertex tmp=it.next();
			
			if(tmp.neighboursForAlgo.size()==1) {
				Vertex[] tmpArray=tmp.neighboursForAlgo.toArray(new Vertex[0]);
				Vertex tmp2 =tmpArray[0];
				tmp2.neighboursForAlgo.remove(tmp);
				tmp.neighboursForAlgo.remove(tmp2);
				tmp.neighboursTree.add(tmp2);
				tmp2.neighboursTree.add(tmp);
				leafRemovalRec(tmp2);
			}
				
		}

	}

	//recursively deletes all of the leafs on one path. It may happen that some tree like structures remain, but they get pruned in a call with another vertex
	private void leafRemovalRec(Vertex v) {
		if(v.neighboursForAlgo.size()==1) {
			Vertex[] tmpArray=v.neighboursForAlgo.toArray(new Vertex[0]);
			Vertex tmp2 =tmpArray[0];
			tmp2.neighboursForAlgo.remove(v);
			v.neighboursForAlgo.remove(tmp2);
			v.neighboursTree.add(tmp2);
			tmp2.neighboursTree.add(v);
			
			leafRemovalRec(tmp2);
		}		
	}
	
	//recursively find the next cycle
	private boolean getNextEdge(Edge e, Vertex v,Vertex startSrc, Vertex startDst,ArrayList<Vertex> polygon) {
		double min=Double.MAX_VALUE;
		Vertex minVertex= null;
		if(v.neighboursForAlgo.size()==0) {
			return false;
		}
		
		//find the leftmost edge (with the smallest angle)
		for(Iterator<Vertex> it=v.neighboursForAlgo.iterator();it.hasNext();) {
			Vertex cur=it.next();
			double val=BasicGeometry.calculateAngle(e,v,cur);
			if(val<min) {
				min=val;
				minVertex=cur;
				}
		}
		//remove the forward connection since it has already been used
		v.neighboursForAlgo.remove(minVertex);
		if(minVertex.equals(startDst)&&v.equals(startSrc)) {
			return true;
		}
		//add the new edge and check for the next one recursively
		polygon.add(minVertex);
		getNextEdge(new Edge(v,minVertex),minVertex, startSrc, startDst,polygon);
		return true;

	}
	
	//check the orientation of the cycles
	private Boolean shoelaceformula(ArrayList<Vertex> polygon) {
		double area=0;
		for(int i=0;i<polygon.size();i++) {
			int iplus=i+1;
			if(iplus==polygon.size()) {
				iplus=0;
			}
			area=area+0.5*((polygon.get(iplus).x+polygon.get(i).x)  * (polygon.get(iplus).y-polygon.get(i).y));
		}
		if(area<0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	//add the treelike extensions and generate degenerate polygons
	private void getPolygons() {
		for(Iterator<ArrayList<Vertex>> it =this.allPolygons.iterator();it.hasNext();) {
			ArrayList<Vertex> cur=it.next();		
			getSinglePolygon(cur);
		}
		
		//sort them by size for convenience
		Collections.sort(this.allPolygons, new Comparator<ArrayList<Vertex>>() {
		    @Override
		    public int compare(ArrayList<Vertex> o1, ArrayList<Vertex> o2) {
		        return -Integer.valueOf((o1.size())).compareTo(Integer.valueOf(o2.size()));
		    }
		});
		
	}
	
	
	//gets one of the degenerate polygons
	private void getSinglePolygon(ArrayList<Vertex> polygon) {
		Vertex pred= polygon.get(polygon.size()-1);
		Vertex succ;
		for(ListIterator<Vertex> it =polygon.listIterator();it.hasNext();) {
			
			//find the predecessor and the successor of the vertex (exist since its a cycle)
			Vertex v=it.next();
			it.previous();
			if(it.hasPrevious()) {
				pred=it.previous();
				it.next();
			}
			else {
				pred=polygon.get(polygon.size()-1);
			}
			it.next();
			if(it.hasNext()) {
				succ=it.next();
				it.previous();
			}
			else {
				succ=polygon.get(0);
			}
					
			//checks for trees connected to the vertex in "angular order"
			ArrayList<Vertex> sortedVertices=new ArrayList<Vertex>(v.neighboursTree);
			Collections.sort(sortedVertices, new angleComparator(v,new Edge(v,pred))  );
			
			
			for(Iterator<Vertex> nb= sortedVertices.iterator();nb.hasNext();) {
				Vertex cur=nb.next();
				//we only want to attach the trees that are inside the polygon
				if(treeIsInside(cur,v,pred,succ,polygon)) {
					//recursively traverses the tree
					ArrayList<Vertex> toAdd=collectTree(v,cur);
					
					//add the tree to the polygon at the right place
					for(Iterator<Vertex> addIt=toAdd.iterator();addIt.hasNext();) {
						it.add(addIt.next());
					}	
				}
			}
		}	
	}
	
	//checks if the tree edge is inside the polygon
	private boolean treeIsInside(Vertex treeVertex,Vertex v, Vertex pred,Vertex succ,ArrayList<Vertex> justTesting) {
		double treeAngle= BasicGeometry.calculateAngle(new Edge(v,pred ), v, treeVertex);
		double succAngle= BasicGeometry.calculateAngle(new Edge(v,pred ), v, succ);
		if(succAngle>=treeAngle) {
			return true;
		}
		else {
			return false;
	
		}
	}
	
	//collects the tree as degenerate polygon (i.e. counting edges twice)
	private ArrayList<Vertex> collectTree(Vertex vertexOnCycle,Vertex treeVertex){
		ArrayList<Vertex> result=new ArrayList<Vertex>();
		result.add(treeVertex);
		
		//get starting vertex
		vertexOnCycle.neighboursTree.remove(treeVertex);
		Edge startEdge= new Edge(vertexOnCycle,treeVertex);
		collectTreeRec(startEdge,treeVertex,vertexOnCycle,result);
		return result;
	}
	
	//recursively run through the tree (always taking the leftmost available edge)
	private boolean collectTreeRec(Edge e, Vertex v,Vertex startingVertex,ArrayList<Vertex> polygon){
		double min=Double.MAX_VALUE;
		Vertex minVertex= null;
		
		if((v.equals(startingVertex)) ){
			return true;
		}
		else {
			for(Iterator<Vertex> it=v.neighboursTree.iterator();it.hasNext();) {
				Vertex cur=it.next();
				double val=BasicGeometry.calculateAngle(e,v,cur);
				if(val<min) {
					min=val;
					minVertex=cur;
				}
			}
			v.neighboursTree.remove(minVertex);
			polygon.add(minVertex);
			collectTreeRec(new Edge(v,minVertex),minVertex,startingVertex,polygon);
			return true;
		}
	}
	
	
	private void getPolygonsWithComponentsInsideNew() {
		ArrayList<ArrayList<ConnectedComponent>> componentTracker=new ArrayList<ArrayList<ConnectedComponent>> ();
		for(int i=0;i<this.allPolygons.size();i++) {
			componentTracker.add(new ArrayList<ConnectedComponent>());
		}
		
		//checks if some component is contained in a polygon (with different label)
		for(int i=0;i<this.allPolygons.size();i++) {
			for(int j=0;j<this.connectedComponents.length;j++) {
				if(BasicGeometry.vertexInPolygon(this.allPolygons.get(i),this.connectedComponents[j].leftMostVertex)
						&&this.connectedComponents[j].leftMostVertex.label!=this.allPolygons.get(i).get(0).label) {
					this.connectedComponents[j].containedIn.add(i);
				}
			}
		}
		
		//it may happen that a polygon is contained in another polygon that is again contained in another polygon.
		//Then the first is contained in the second but not the third!
		for(int j=0;j<this.connectedComponents.length;j++) {
			ConnectedComponent cur=this.connectedComponents[j];

			
			//only contained in one implies its fine
			if(cur.containedIn.size()==1) {
				cur.reallyContainedInIndex=cur.containedIn.get(0);
				componentTracker.get(cur.reallyContainedInIndex).add(cur);	
			}
			
			//>1 we need to find the innnermost one that contains it
			if(cur.containedIn.size()>1) {
				//checks which polygons contain each other; we want the one that does not contain on of the others
				for(int i=0;i<cur.containedIn.size();i++) {
					boolean isIndex=true;
					for(int k=0;k<cur.containedIn.size();k++) {
						if(k!=i) {
							if(BasicGeometry.vertexInPolygon(this.allPolygons.get(cur.containedIn.get(k)),this.allPolygons.get(cur.containedIn.get(i)).get(0))){
								isIndex=false;
							}
						}
					}
					if(isIndex) {
						cur.reallyContainedInIndex=cur.containedIn.get(i);
					}
				}
				componentTracker.get(cur.reallyContainedInIndex).add(cur);	
			}			
		}
		
		//generate Polygons with the respective components inside of them
		for(int i=0;i<this.allPolygons.size();i++) {
			ArrayList<ConnectedComponent> connectedComponentsOfPolygon=componentTracker.get(i);
			
			if(connectedComponentsOfPolygon.isEmpty()) {
				Polygon tmp=new Polygon(this.allPolygons.get(i),this.myObjects);
				this.polygons.add(tmp);
			}
			
			else {
				PolygonContainingComponents tmp=new PolygonContainingComponents(this.allPolygons.get(i),connectedComponentsOfPolygon,this.myObjects);
				this.polygons.add(tmp);
			}
		}
	
		
	}

	
	public class angleComparator implements Comparator<Vertex> {
		Vertex v;
		Edge e;
		
		angleComparator(Vertex anchor,Edge edge){
			super();
			v=anchor;
			e=edge;
			
		}
	    @Override
	    public int compare(Vertex o1, Vertex o2) {
	    	double x1= BasicGeometry.calculateAngle(e, v, o1);
	    	double x2=BasicGeometry.calculateAngle(e,v,o2);
	    	if(x1<x2) {
	    		return -1;
	    	}
	    	else {
	    		if(x1==x2) {
	    			return 0;
	    		}
	    		else {
	    			return 1;
	    		}
	    	}
	    }
	}
	
}
