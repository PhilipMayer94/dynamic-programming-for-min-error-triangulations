package dynamic_programming_for_MTWT;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.kdtree.KdTree;


public class Triangle {
	Edge[] edges;
	Vertex[] vertices;
	double weight;
	
	
	Triangle(Edge[] triangleEdges,double triangleWeight){
		this.edges=triangleEdges;
		this.weight=triangleWeight;
		Vertex v1=edges[0].src;
		Vertex v2=edges[0].dst;
		Vertex v3;
		
		//finds the third vertex of the triangle
		if(edges[1].src.id!=v1.id && edges[1].src.id!=v2.id) {
			v3=edges[1].src;
		}
		else {
			v3=edges[1].dst;
		}
		
		//inputs the vertices from smallest to largest id.
		if(v1.id<v2.id && v2.id<v3.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		if(v1.id<v3.id && v3.id<v2.id) {
			Vertex[] tmp = {v1,v3,v2};
			vertices= tmp;
			return;
		}
		if(v3.id<v1.id && v1.id<v2.id) {
			Vertex[] tmp = {v3,v1,v2};
			vertices= tmp;
			return;
		}
		if(v3.id<v2.id && v2.id<v1.id) {
			Vertex[] tmp = {v3,v2,v1};
			vertices= tmp;
			return;
		}
		if(v2.id<v1.id && v1.id<v3.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		if(v2.id<v3.id && v3.id<v1.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		
	}
	
	
	Triangle(Vertex v1,Vertex v2,Vertex v3){
		this.weight=Double.MAX_VALUE;
		
		//inputs the vertices from smallest to largest id.
		if(v1.id<v2.id && v2.id<v3.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		if(v1.id<v3.id && v3.id<v2.id) {
			Vertex[] tmp = {v1,v3,v2};
			vertices= tmp;
			return;
		}
		if(v3.id<v1.id && v1.id<v2.id) {
			Vertex[] tmp = {v3,v1,v2};
			vertices= tmp;
			return;
		}
		if(v3.id<v2.id && v2.id<v1.id) {
			Vertex[] tmp = {v3,v2,v1};
			vertices= tmp;
			return;
		}
		if(v2.id<v1.id && v1.id<v3.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		if(v2.id<v3.id && v3.id<v1.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		
	}
	
	
	Triangle(Edge e1,Edge e2,Edge e3){
		Edge[] triangleEdges = {e1,e2,e3};
		this.edges=triangleEdges;
		this.weight=Double.MAX_VALUE;
		Vertex v1=edges[0].src;
		Vertex v2=edges[0].dst;
		Vertex v3;
		
		if(edges[1].src.id!=v1.id && edges[1].src.id!=v2.id) {
			v3=edges[1].src;
		}
		else {
			v3=edges[1].dst;
		}
		
		//inputs the vertices from smallest to largest id.
		if(v1.id<v2.id && v2.id<v3.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		if(v1.id<v3.id && v3.id<v2.id) {
			Vertex[] tmp = {v1,v3,v2};
			vertices= tmp;
			return;
		}
		if(v3.id<v1.id && v1.id<v2.id) {
			Vertex[] tmp = {v3,v1,v2};
			vertices= tmp;
			return;
		}
		if(v3.id<v2.id && v2.id<v1.id) {
			Vertex[] tmp = {v3,v2,v1};
			vertices= tmp;
			return;
		}
		if(v2.id<v1.id && v1.id<v3.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		if(v2.id<v3.id && v3.id<v1.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		
	}

	
	Triangle(Edge[] triangleEdges){
		this.edges=triangleEdges;
		this.weight=Double.MAX_VALUE;
		Vertex v1=edges[0].src;
		Vertex v2=edges[0].dst;
		Vertex v3;
		
		if(edges[1].src.id!=v1.id && edges[1].src.id!=v2.id) {
			v3=edges[1].src;
		}
		else {
			v3=edges[1].dst;
		}
		
		//inputs the vertices from smallest to largest id.
		if(v1.id<v2.id && v2.id<v3.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		if(v1.id<v3.id && v3.id<v2.id) {
			Vertex[] tmp = {v1,v3,v2};
			vertices= tmp;
			return;
		}
		if(v3.id<v1.id && v1.id<v2.id) {
			Vertex[] tmp = {v3,v1,v2};
			vertices= tmp;
			return;
		}
		if(v3.id<v2.id && v2.id<v1.id) {
			Vertex[] tmp = {v3,v2,v1};
			vertices= tmp;
			return;
		}
		if(v2.id<v1.id && v1.id<v3.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
		if(v2.id<v3.id && v3.id<v1.id) {
			Vertex[] tmp = {v1,v2,v3};
			vertices= tmp;
			return;
		}
	}
	

	
	// 0 learning 1 reconstruction
	double getValueBilinearInt(double px,double py, int mode) {
		Vertex v1=this.vertices[0];
		Vertex v2=this.vertices[1];
		Vertex v3=this.vertices[2];
		if(px==v1.x&&py==v1.y) {
			return v1.learningValues[mode];
		}
		if(px==v2.x&&py==v2.y) {
			return v2.learningValues[mode];
		}
		if(px==v3.x&&py==v3.y) {
			return v3.learningValues[mode];
		}
		if(!pointInTriangle(px,py)) {
			System.out.println("point was not in triangle");
			return Double.MAX_VALUE;
		}
		double w1=0;
		double w2=0;
		double w3=0;
		w1=(   (v2.y-v3.y)*( px-v3.x)  +  ( v3.x-v2.x ) * (py-v3.y)   )  /  ( (v2.y-v3.y)* (v1.x-v3.x )  +   ( v3.x-v2.x  )  * ( v1.y-v3.y   )               );
		w2=(   (v3.y-v1.y)*( px-v3.x)  +  ( v1.x-v3.x ) * (py-v3.y)   )  /  ( (v2.y-v3.y)* (v1.x-v3.x )  +   ( v3.x-v2.x  )  * ( v1.y-v3.y   )               );
		w3=1-w2-w1;
		
		return w1*v1.learningValues[mode]+w2*v2.learningValues[mode]+w3*v3.learningValues[mode];
	}
	
	//checks, if an edge of the triangle is currently intersected by a connecting edge
	double getWeight() {
		double result=weight;
		for(int i=0;i<3;i++) {
			if(this.edges[i].hasIntersection) {
				result=Double.MAX_VALUE;
			}
		}	
		return result;
	}
	
	//tree contains the altimeterPoints as KDtree 
	//collect all points in triangle and calculate the squared error after interpolation with learning values
	void setWeightLearning(KdTree tree) {
		double weighti=0;
		Coordinate[] candidates= getAltimeterCandidates(tree);
		for(int i=0;i<candidates.length;i++) {
			Coordinate cur= candidates[i];
			if(pointInTriangle(cur.x,cur.y)) {
				if(!Double.isNaN(cur.z)) {
					double interpol=getValueBilinearInt(cur.x,cur.y, 0);
					weighti=weighti+( (interpol-cur.z)  *(interpol-cur.z) );
				}
				
			}
		}
		this.weight=weighti;
	}
	
	//tree contains the altimeterPoints as KDtree
	//collect all points in triangle 
	//and calculate the squared error after interpolation with reconstruction values
	void setWeightReconstruction(KdTree tree) {
		double weighti=0;
		Coordinate[] candidates= getAltimeterCandidates(tree);
		for(int i=0;i<candidates.length;i++) {
			Coordinate cur= candidates[i];
			if(pointInTriangle(cur.x,cur.y)) {
				if(!Double.isNaN(cur.z)) {
				double interpol=getValueBilinearInt(cur.x,cur.y, 1);
				weighti=weighti+( (interpol-cur.z)  *(interpol-cur.z) );
				}
			}
		}
		this.weight=weighti;
	}
	
	
	//collects for a triangle all altimeter points
	//and returns them as List with error as value
	ArrayList<Point3D> candidatesValues(KdTree tree){
		ArrayList<Point3D> result=new ArrayList<Point3D>();
		Coordinate[] candidates= getAltimeterCandidates(tree);
		for(int i=0;i<candidates.length;i++) {
			Coordinate cur= candidates[i];
			if(pointInTriangle(cur.x,cur.y)) {
				if(!Double.isNaN(cur.z)) {
				double interpol=getValueBilinearInt(cur.x,cur.y, 1);
					result.add(new Point3D(cur.x,cur.y,(interpol-cur.z)  *(interpol-cur.z)));
				}
			}
		}
		return result;
		
	}
	
	
	//given to vertices get the third one
	Vertex getThirdVertex(Vertex v1,Vertex v2){
		Vertex result=null;
		
		for(int i=0;i<vertices.length;i++) {
			if(vertices[i].id!=v1.id &&vertices[i].id!=v2.id ) {
				result=vertices[i];
			}
		}
		
		return result;
	}
	
	
	//checks if the point px,py is in the triangle
	boolean pointInTriangle (double px,double py){
		if((px==this.vertices[0].x &&py==this.vertices[0].y)||(px==this.vertices[1].x &&py==this.vertices[1].y)||(px==this.vertices[2].x &&py==this.vertices[2].y)) {
			return false;
		}
		
		
		double a = ((this.vertices[1].y-this.vertices[2].y) * (px-this.vertices[2].x)+(this.vertices[2].x-this.vertices[1].x)*(py-this.vertices[2].y))/
				   ((this.vertices[1].y-this.vertices[2].y) * (this.vertices[0].x-this.vertices[2].x)+(this.vertices[2].x-this.vertices[1].x)*(this.vertices[0].y-this.vertices[2].y));
		double b =((this.vertices[2].y-this.vertices[0].y) * (px-this.vertices[2].x)+(this.vertices[0].x-this.vertices[2].x)*(py-this.vertices[2].y))/
				   ((this.vertices[1].y-this.vertices[2].y) * (this.vertices[0].x-this.vertices[2].x)+(this.vertices[2].x-this.vertices[1].x)*(this.vertices[0].y-this.vertices[2].y));
		double c =1-a-b;
		//System.out.println(a+ " "+ b+ " "+ c);
		if(a<=1 && a>=0 && b<=1 && b>=0 && c<=1 && c>=0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	//query the bounding box of the triangle to the kdTree
	Coordinate[] getAltimeterCandidates(KdTree tree) {
		List candidates=tree.query(getBoundingBoxOfTriangle());
		return KdTree.toCoordinates(candidates);
	}
	
	//Axis-aligned bounding box of the triangle
	Envelope getBoundingBoxOfTriangle(){
		double xmin=Double.MAX_VALUE;
		double xmax=Double.MIN_VALUE;
		double ymin=Double.MAX_VALUE;
		double ymax=Double.MIN_VALUE;
		for(int i=0;i<3;i++) {
			double curx=this.vertices[i].x;
			double cury=this.vertices[i].y;
			if(curx<xmin) {
				xmin=curx;
			}
			if(cury<ymin) {
				ymin=cury;
			}
			
			if(curx>xmax) {
				xmax=curx;
			}
			if(cury>ymax) {
				ymax=cury;
			}
			
		}
		return new Envelope(xmax,xmin,ymax,ymin);	
	}
	
	
	//checks if the point is in the circumcircle of this
	boolean pointInTriangleCircle(double px,double py) {
		double[] circle=BasicGeometry.calculateCircle3Points(this.vertices[0], this.vertices[1], this.vertices[2]);
		if(BasicGeometry.calculateDistance(circle[0], circle[1], px, py)<circle[2]) {
			return true;
		}
		return false;
	}

	
	@Override
	public boolean equals(Object o) {
		if((this.vertices[0].id==((Triangle)o).vertices[0].id)&&(this.vertices[1].id==((Triangle)o).vertices[1].id)&&(this.vertices[2].id==((Triangle)o).vertices[2].id)) {
			return true;
		}
		return false;	
	}
	
	
	double[] midPoint() {
		double[] tmp= {0,0};
		for(int i=0;i<this.vertices.length;i++) {
			tmp[0]=tmp[0]+this.vertices[i].x;
			tmp[1]=tmp[1]+this.vertices[i].y;
			
		}
		tmp[0]=tmp[0]/3;
		tmp[1]=tmp[1]/3;
		return tmp;
	}

	
	@Override
	public int hashCode() {
		int hash = 23;
		hash = hash * 31 + this.vertices[0].id+1;
		hash = hash * 31 + this.vertices[1].id+1;
		hash = hash * 31 + this.vertices[2].id+1;
		return hash;
	}
	
	public String toString() { 
		Vertex[] x=this.vertices;
        return (x[0] +" "+x[1]+" "+ x[2]); 
    } 	
	
	//sets the weight to the sum of its edge lengths
	double setWeightMWT() {
		double result=0;
		Vertex v1=this.vertices[0];
		Vertex v2=this.vertices[1];
		Vertex v3=this.vertices[2];
		
		double tmp=BasicGeometry.calculateDistance(v1.x, v1.y,v2.x , v2.y);
		result=result+tmp;
		tmp=BasicGeometry.calculateDistance(v1.x, v1.y,v3.x , v3.y);
		result=result+tmp;
		tmp=BasicGeometry.calculateDistance(v3.x, v3.y,v2.x , v2.y);
		result=result+tmp;
		
		return result;
		
	}
	
	//sets weight as degree angles 
	double setWeightWithRespecttoAngles() {
		double[] angles=getAngles(this.vertices[0], this.vertices[1], this.vertices[2]);
		double result=Double.MAX_VALUE;
		for(int i=0;i<angles.length;i++) {
			if(angles[i]<result) {
					result=angles[i];
			}
		}
		return result/60;
	}	
	
	// returns square of distance b/w two points 
    static double lengthSquare(Vertex p1, Vertex p2) { 
        double xDiff = p1.x- p2.x; 
        double yDiff = p1.y- p2.y; 
        return xDiff*xDiff + yDiff*yDiff; 
    } 
      
    //gets angles of the triangle 
    static double[] getAngles(Vertex A, Vertex B, Vertex C) { 
    	// Square of lengths be a2, b2, c2 
    	double a2 = lengthSquare(B,C); 
    	double b2 = lengthSquare(A,C); 
    	double c2 = lengthSquare(A,B); 
      
    	// length of sides be a, b, c 
    	float a = (float)Math.sqrt(a2); 
    	float b = (float)Math.sqrt(b2); 
    	float c = (float)Math.sqrt(c2); 
      
    	// From Cosine law 
    	float alpha = (float) Math.acos((b2 + c2 - a2)/(2*b*c)); 
    	float betta = (float) Math.acos((a2 + c2 - b2)/(2*a*c)); 
    	float gamma = (float) Math.acos((a2 + b2 - c2)/(2*a*b)); 
      
    	// Converting to degree 
    	alpha = (float) (alpha * 180 / Math.PI); 
    	betta = (float) (betta * 180 / Math.PI); 
    	gamma = (float) (gamma * 180 / Math.PI); 
      
    	double[] result= {alpha,betta,gamma};
    	return result;
    } 
      
  
    
	
	
}
