package dynamic_programming_for_MTWT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class MTWTSolver {
	int order=0;
	OrderKObjects myKODObjects;
	PolygonDecomposition myDecomposition;
	HashSet<Vertex> vertices;
	
	double solutionValue=-1; //calculated value of the solution
	HashSet<Triangle> solution=new HashSet<Triangle>(); //the solution as triangles

	
	
	double preProcessingTime=-1;
	double solvingTime=-1;
	
	
	MTWTSolver(double[][] points,int wantedOrder){
		this.order=wantedOrder;
		final double t0 = (System.currentTimeMillis());
		
		//calculate the polygon decomposition and the useful edge
		this.myKODObjects=new OrderKObjects(points,wantedOrder);
		this.vertices=myKODObjects.vertices;
		this.myDecomposition=new PolygonDecomposition(myKODObjects);
	    
		//the weights do not need to be set randomly, but for the random data its better to do so
		this.setTriangleValuesRandomly();
	    
	    final double t2 = (System.currentTimeMillis());
		preProcessingTime=(t2-t0);
	}
	
	//solve the minimum triangle weighted triangulation problem for the given pointset and the set triangle weights
	void solve() {
		
		//ignore the cases where the algorithms times out
		if(getMaxComponent()>9) {
			System.out.println("Did not solve because maxComponent is "+ getMaxComponent());
			return;
		}
		
		final long t0 = System.currentTimeMillis();
		
		HashSet<PolygonTriangle> polySolution=new HashSet<PolygonTriangle>();
		double overAllValue=0;
		
		//add up all of the solutions of the decompositons
	    for(int i=0;i<myDecomposition.polygons.size();i++) {
	    	double curSolValue=myDecomposition.polygons.get(i).MTWT();	
	    	polySolution.addAll(myDecomposition.polygons.get(i).finalSolution);
	    	overAllValue+=curSolValue;
	    }
	    
	    //transform the triangles into standard triangles
	    for(Iterator<PolygonTriangle> it=polySolution.iterator();it.hasNext();) {
	    	PolygonTriangle cur=it.next();
	    	solution.add(cur.originalTriangle);
	    }
	    solutionValue=overAllValue;

	    
	    final long t1 = System.currentTimeMillis();
	    solvingTime=(t1-t0); 
	}
	
	
	//collects the triangles of  the Delaunay triangulation
	HashSet<Triangle> delaunaySolution(){
		HashSet<Triangle> result= new HashSet<Triangle>();
		for(Iterator<Edge> it= myKODObjects.delaunay.edges.iterator();it.hasNext();) {
			Edge cur=it.next();
			for(Iterator<Triangle>itTri=cur.incidentTriangles.iterator();itTri.hasNext();) {
				Triangle curT=itTri.next();
				Vertex tmp=curT.getThirdVertex(cur.dst, cur.src);
				if(cur.src.delaunayNeighbours.contains(tmp)&&cur.dst.delaunayNeighbours.contains(tmp)) {
					result.add(curT);
				}
			}
		}
		return result;
	}
	
	//collects the values of the Delaunay solution with respect to the set weights
	double delaunaySolutionValue() {
		double result=0;
		HashSet<Triangle> solution=delaunaySolution();
		for(Iterator<Triangle> it=solution.iterator();it.hasNext();) {
			Triangle cur=it.next();
			result=result+cur.getWeight();
			
		}
		return result;
	}
	
	
	// gets the value c_max, i.e. the maximal number of connected components in the 
	int getMaxComponent() {
		int max=0;
		for(int i=0;i<this.myDecomposition.polygons.size();i++) {
			if(this.myDecomposition.polygons.get(i).numberOfComponents>max) {
				max=this.myDecomposition.polygons.get(i).numberOfComponents;
			}
		}
		return max;
	}
	
	
	// double array with information about the instance
	// {order,instanceSize, overallNumberOfConnectedComponents, avgNrOfConnectedComponentsIncluding0s, avgNrOfCCwithout0s,c_max}
	double [] getFixedGraphInfoArray(){
		double[] result=new double[6];
		int ord=order;
		int size=this.vertices.size();
		ArrayList<Integer> nrOfComp=new ArrayList<Integer>();
		for(int i=0;i<this.myDecomposition.polygons.size();i++) {
			if(this.myDecomposition.polygons.get(i).numberOfComponents>0) {
				nrOfComp.add(this.myDecomposition.polygons.get(i).numberOfComponents);
			}
		}
		double sum=0;
		double avg=0;
		double avgWithZeros=0;
		int maximum=0;
		for(int i=0;i<nrOfComp.size();i++) {
			sum=sum+nrOfComp.get(i);
			if(maximum<nrOfComp.get(i)) {
				maximum=nrOfComp.get(i);
			}
		}
		if(nrOfComp.size()>0) {
			avgWithZeros=sum/this.myDecomposition.polygons.size();
			avg=sum/nrOfComp.size();
		}
		avg=Math.floor(avg*1000) / 1000;
		avgWithZeros=Math.floor(avgWithZeros*1000) / 1000;
		result[ 0]=ord ;
		result[ 1]=size ;
		result[ 2]= sum +1;
		result[ 3]= avgWithZeros;
		result[ 4]= avg;
		result[5]= maximum;
		if(avg>maximum) {
			System.out.println("müll");
		}
		return result;
	}
	
	//some information as String
	String getGraphinfo() {
		int ord=order;
		int size=this.vertices.size();
		ArrayList<Integer> nrOfComp=new ArrayList<Integer>();
		for(int i=0;i<this.myDecomposition.polygons.size();i++) {
			if(this.myDecomposition.polygons.get(i).numberOfComponents>0) {
				nrOfComp.add(this.myDecomposition.polygons.get(i).numberOfComponents);
			}
		}
		double sum=0;
		double avg=0;
		int maximum=0;
		for(int i=0;i<nrOfComp.size();i++) {
			sum=sum+nrOfComp.get(i);
			if(maximum<nrOfComp.get(i)) {
				maximum=nrOfComp.get(i);
			}
		}
		if(nrOfComp.size()>0) {
			avg=sum/nrOfComp.size();
		}
		avg=Math.floor(avg*1000) / 1000;
		
		return String.valueOf(ord)+" ; "+String.valueOf(size)+" ; "
		+String.valueOf(sum)+" ; "+String.valueOf(avg)+" ; "+String.valueOf(maximum);
		
		
		
		
	}
	
	
	//set weights to be sum of edge lengths of triangle edges
	void setTriangleValuesMWT() {
		for(Iterator<Triangle> it=this.myKODObjects.allKODTriangles.iterator();it.hasNext();) {
			Triangle cur=it.next();
			cur.weight=cur.setWeightMWT();
		}
	}
	
	//set weights randomly
	void setTriangleValuesRandomly() {
		Random rand=new Random();
		for(Iterator<Triangle> it=this.myKODObjects.allKODTriangles.iterator();it.hasNext();) {
			Triangle cur=it.next();
			cur.weight=1.0+rand.nextDouble();
		}
	}
	
	//set weights to be the sum of the triangles angles
	void setTriangleValuesAngle() {
		for(Iterator<Triangle> it=this.myKODObjects.allKODTriangles.iterator();it.hasNext();) {
			Triangle cur=it.next();
			cur.weight=cur.setWeightWithRespecttoAngles();
		}
	}
	
	//sets the weight with respect to the number of points in the circumcircle of the triangle -> optimal solution is Delaunay
	void setTriangleValuesNumberOfPoints() {
		for(Iterator<Triangle> it=this.myKODObjects.allKODTriangles.iterator();it.hasNext();) {
			Triangle cur=it.next();
			int counter=1;
			for(Iterator<Vertex> iter=this.myKODObjects.vertices.iterator();iter.hasNext();) {
				Vertex curV=iter.next();
				if(!(cur.vertices[0].id==curV.id) &&!(cur.vertices[1].id==curV.id)  &&!(cur.vertices[2].id==curV.id)  &&cur.pointInTriangleCircle(curV.x,curV.y)){
					counter=counter+1;
				}
			}
			cur.weight=counter;
		}
	}

}
