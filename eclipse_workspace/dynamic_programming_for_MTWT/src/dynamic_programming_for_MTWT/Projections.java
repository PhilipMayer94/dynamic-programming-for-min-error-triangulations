package dynamic_programming_for_MTWT;

//expected longitude=0,360 latitude -90..90
public class Projections {
	static final int LAMBERT=0;
	static final int EQUILIATERAL=1;
	static int type=0;
	static int height=800;
	static int width=1100;
	static double lon0=0.0;
	static double lat0=0.0;
	
	
	//type decides which type of projection to use
	static void setProjection(int type0,double longitude0, double latitude0,int height0,int width0) {
		type=type0;
		height=height0;
		width=width0;
		lon0=longitude0;
		lat0=latitude0;
	}
	
	//set the specific variables for the Lambert projection
	static void setProjectionLamb(int type0,double longitude0, double latitude0) {
		type=type0;
		lon0=longitude0;
		lat0=latitude0;
	}
	
	//set the specific variables for the equilateral projection
	static void setProjectionEqu(int type0,int height0,int width0) {
		type=type0;
		height=height0;
		width=width0;
	}
	
	//projects with the currently defined projection
	static public double [] project(double longitude,double latitude) {
		double[] result=null;
		if(Projections.type==0) {
			result= lambertArea(longitude,latitude,lat0,lon0);
			return result;
		}
		if(Projections.type==1) {
			result=latLongToPointCenteredEurope(longitude,latitude,height,width);
			return result;
		}
		System.out.println("wrong projection was chosen");
		return result;
	}
	
	//Lambert projection; see e.g. https://en.wikipedia.org/wiki/Lambert_azimuthal_equal-area_projection
	static public double[] lambertArea(double longitude,double latitude, double lat0,double long0) {
		
		double phi1=lat0;
		double lambda1=long0;
		double phi = latitude;
		double lambda = longitude ;
		double kprime= Math.sqrt(2/    (1+     (Math.sin(phi1*Math.PI/180)*Math.sin(phi*Math.PI/180))    
				+   (Math.cos(phi1*Math.PI/180)*Math.cos(phi*Math.PI/180)*Math.cos(lambda*Math.PI/180-lambda1*Math.PI/180))   )  );
		double [] result =new double[2];
		
		int scalingfactor=2000;
		scalingfactor=200;
		
		result[0]=kprime*(Math.cos(phi*Math.PI/180)*Math.sin((lambda-lambda1)*Math.PI/180))        *scalingfactor+425;
		result[1]=-kprime*(Math.sin(phi*Math.PI/180)*Math.cos(phi1*Math.PI/180)- Math.sin(phi1*Math.PI/180)*Math.cos(phi*Math.PI/180)*Math.cos(lambda*Math.PI/180-lambda1*Math.PI/180) )     *scalingfactor+420;
		return result;
	}
	
	//Equirectangular projection shifted by 180 degree longitude
	static double[] latLongToPointCenteredInAmerica(double longitude,double  latitude,int h,int w) {
		double [] result =new double[2];
		double lati = -latitude + 90;
		double longi = longitude + 180;
        double y = lati / 180;
        double x = longi / 360;
        result[0] = (x * w)+w/2;
        if(result[0]>w) {
        	result[0]=result[0]-w;
        }
        result[1] = (y * h)-16;
        return result;
	}
	
	//Equirectangular projection
	static double[] latLongToPointCenteredEurope(double longitude,double  latitude,int h,int w) {
		double [] result =new double[2];
		double lati = -latitude + 90;
		
		
		double longi = longitude + 180;
        double y = lati / 180;
        double x = longi / 360;
        result[0] = (x * w)-w/2;
        result[1] = (y * h)-16;
        return result;
	}
}
