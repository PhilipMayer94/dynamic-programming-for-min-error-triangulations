package dynamic_programming_for_MTWT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.kdtree.KdTree;

import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.Variable;

public class AltimeterLoader {
	AltimeterLoader(){	
	}
	
	//given a year and a month; generates a string that fits the database
	public static String getTimeCode(int year,int month) {
		String result = String.valueOf(year)+"/";
		String monthS;
		if(month<10) {
			monthS="0"+String.valueOf(month);
		}
		else {
			monthS=String.valueOf(month);
		}
		String monthCode="ESACCI-SEALEVEL-L4-MSLA-MERGED-"+String.valueOf(year)+monthS+"15000000-fv02.nc";
		result=String.valueOf(year)+"/" +monthCode;
		return result;
	}
	
	//load the dataset with the given timecode; if it is available
	public static Point3D[][] loadSpecificSet(int year, int month)throws Exception{
		int i =year;
		int j =month;
		if((i==1999 && (j==3))|| (  (i==2006 && (j==3||j==4||j==5||j==6||j==7||j==8||j==9||j==10||j==11)) ||(i==2015 && (j==4))     )) {
			return null;
		}
		
		String timeCode=getTimeCode(year,month);
		String name="assets/altimeter/"+timeCode;

		return loadDataSet(name);
	}
	
	//loads the dataset with the given timecode and projects it with the currently used projection in Projection
	public static Point3D[][] loadAndProjectSpecificSet(int year,int month)throws Exception{
		Point3D[][] points=loadSpecificSet(year, month);
		if(points==null) {
			return null;
		}
		Point3D[][] result= new Point3D[points.length][points[0].length];
		for(int i=0;i<points.length;i++) {
			for(int j=0;j<points[0].length;j++) {
				Point3D cur=points[i][j];
				double[] pPoint=Projections.project(cur.x, cur.y);
				result[i][j]=new Point3D(pPoint[0],pPoint[1],cur.val);
			}
		}
		return result;
	}
	
	
	//the projected data of a month and year given as a KDtree
	public static KdTree loadAndProjectSpecificSetAsKDTree(int year, int month)throws Exception{
		Point3D[][] points=loadAndProjectSpecificSet(year,month);
		if(points==null) {
			return null;
		}
		KdTree result= new KdTree();
		for(int i=0;i<points.length;i++) {
			for(int j=0;j<points[0].length;j++) {
				Point3D cur=points[i][j];
				if(!Double.isNaN(cur.val)&&cur.val>-999999) {
					result.insert(new Coordinate(cur.x,cur.y,cur.val));
				}
			}
		}
		return result;
	}
	
	//load an NC file and safe it in a 2D matrix of points (lon,lat,height-value)
	public static Point3D[][] loadDataSet(String name) throws Exception{	
		Netcdf nc = new NetcdfFile(name, true);
	    int[] index = new int[1];

	    //get lons
	    Variable lon = nc.get("lon");
	    int nlons = lon.getLengths()[0];
	    double[] lons = new double[nlons];
	    for(int ilon = 0; ilon < nlons; ilon++) {
	      index[0] = ilon;
	      double l = lon.getDouble(index);
	      lons[ilon] = l;
	    }

	    //get lats
	    Variable lat = nc.get("lat");
	    int nlats = lat.getLengths()[0];
	    double[] lats = new double[nlats];
	    for(int ilat = 0; ilat < nlats; ilat++) {
	      index[0] = ilat;
	      lats[ilat] = lat.getDouble(index);
	    }   

	    //get height-values
	    Variable sla = nc.get("sla"); // sea level average
	    float fill = sla.getAttribute("_FillValue").getNumericValue().floatValue();
	    int[] ix = new int[3];
	    double[][] slas = new double[nlons][nlats];
	    for(int ilat = 0; ilat < nlats; ilat++) {
	      ix[0] = ilat;
	      for(int ilon = 0; ilon < nlons; ilon++) {
	        ix[1] = ilon;
	        ix[2] = 5;
	        slas[ilon][ilat] = sla.getFloat(ix);// create raster, set sea level average to its lon lat
	      }
	    }

	    Arrays.sort(lons);

	    //get Point3D 2D-Matrix with the values
	    Point3D[][] ps = new Point3D[nlons][nlats];
	    for(int i = 0; i < nlons; i++) {
	      for(int j = 0; j < nlats; j++) {
	        if(slas[i][j] != fill) {          
	          ps[i][j] = new Point3D(lons[i], lats[j], slas[i][j]*100);
	          
	        }else {
	          ps[i][j] = new Point3D(lons[i], lats[j], Double.NaN);
	        }
	        
	      }
	    }
	   return ps;
	}
	
	//load the average global altimeter data if needed
	public static Point3D[][] loadAvgAltiFromFile(){
		try(BufferedReader in = new BufferedReader(new FileReader(new File("assets/globalAltiAvg.csv")))) {
		    String str;
		    Point3D[][] result = new Point3D[1440][720];
		    while ((str = in.readLine()) != null) {
		        String[] tokens = str.split(",");
		        result[(int)Double.parseDouble(tokens[3])][(int)Double.parseDouble(tokens[4])]=new Point3D(Double.parseDouble(tokens[0]),Double.parseDouble(tokens[1]),Double.parseDouble(tokens[2]));
		    }
		    return result;
		}
		catch (IOException e) {
		    System.out.println("File Read Error");
		    return null;
		}
	}

	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////calculation and saving as csv of the global average altimeter values should never be recomputed///////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static Point3D[][] calculateGlobalAltiAvg() throws Exception{
		List<File> allFiles=getAllDataSetFiles();
		Point3D[][] result = new Point3D[1440][720]; 
		int counter =0;
		for(int i=0;i<result.length;i++) {
			for(int j=0;j<result[0].length;j++) {
				result[i][j]=new Point3D(0,0,0);
				
			}
		}
		int[][] hasGoodValue= new int[1440][720];
		for(Iterator<File> it=allFiles.iterator();it.hasNext();) {
			File cur=it.next();
			String name= cur.getPath().toString();
			Point3D[][] tmp= loadDataSet(name);
			
			for(int i=0;i<tmp.length;i++) {
				for(int j=0;j<tmp[0].length;j++) {
					result[i][j].x=tmp[i][j].x;
					result[i][j].y=tmp[i][j].y;
					double newVal=0;
					
					if(!Double.valueOf(tmp[i][j].val).isNaN()) {
						hasGoodValue[i][j]=hasGoodValue[i][j]+1;
					}
					if(Double.valueOf(tmp[i][j].val).isNaN()) {
						int dev=0;
						if(counter==0) {
							dev=1;
						}
						else {
							dev=counter;
						}
						newVal=result[i][j].val/dev;
						
					}
					else {
						newVal=tmp[i][j].val;
					}
					result[i][j].val=result[i][j].val +newVal;
				}
			}
			counter++;
			//System.out.println(counter);
		}
		for(int i=0;i<result.length;i++) {
			for(int j=0;j<result[0].length;j++) {
				
				
				result[i][j].val=result[i][j].val/counter;
				if(hasGoodValue[i][j]<40) {
					result[i][j].val=Double.NaN;
				}
				
			}
		}
		
		return result;
	}
	
	public static List<File> getAllDataSetFiles(){
		try {
			List<File> result=new ArrayList<File>();
            List<File> files = Files.list(Paths.get("assets/altimeter"))
                        .map(Path::toFile)
                        .collect(Collectors.toList());
             
            for(Iterator<File> it= files.iterator();it.hasNext();) {
            	File cur=it.next();
            	
            	List<File> filesIntern = Files.list(cur.toPath())
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            	result.addAll(filesIntern);
            }

            return result;
        } catch (IOException e) {
        	return null;
            // Error while reading the directory
        }
		
	}
	
	private static void saveAvgAsCsv(Point3D[][] points,String name) throws IOException {
		FileWriter writer = new FileWriter(new File("assets/"+name));
		for(int i=0;i<points.length;i++) {
			for(int j=0;j<points[0].length;j++) {
				Point3D tmp=points[i][j];
			writer.append(tmp.x+" , " + tmp.y+ " , " +tmp.val+ " , " + i  +" , " +j);
			writer.append("\n");
			}
				
		}
		writer.flush();
		writer.close();
		
	}
	
	public static void saveGlobalAltiavg() throws Exception{
		saveAvgAsCsv(calculateGlobalAltiAvg(),"globalAltiAvg.csv");
	}
	

}
