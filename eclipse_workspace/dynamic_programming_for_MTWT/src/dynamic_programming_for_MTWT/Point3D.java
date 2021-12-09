package dynamic_programming_for_MTWT;

public class Point3D {
	double x;
	double y;
	double val=0;
	
	Point3D(double x, double y, double val){
		this.x=x;
		this.y=y;
		this.val=val;
		
	}
	Point3D(double x, double y){
		this.x=x;
		this.y=y;
		this.val=0;
		
	}
	
	@Override
	public String toString() {
		return String.format("["+x+", "+ y+ " | "+val+"]");
	}
}
