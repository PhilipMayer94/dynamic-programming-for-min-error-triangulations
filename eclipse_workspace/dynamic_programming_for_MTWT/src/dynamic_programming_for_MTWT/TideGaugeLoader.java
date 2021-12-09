package dynamic_programming_for_MTWT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class TideGaugeLoader {
	static Point3D [][] avgAltiMap=null;
	static double [][] tideGaugeStandard=null;
	
	
	TideGaugeLoader(){
		avgAltiMap=AltimeterLoader.loadAvgAltiFromFile();
	}
	
	//generates the timecode that fits the database
	static String getCodeForDate(int year,int month) {
		double val=year + (month-0.5)/12.0;
		return String.format(Locale.US,"%.8g%n", val);
	}
	
	
	public static double [][] getSpecificSetProjected(int year,int month, int yearRec, int monthRec){
		//project the points with the currently chosen projection in Projections
		double [][] preProjection=getSpecificSet(year,month,yearRec,monthRec);
		double [][] withDuplicates=new double [preProjection.length][preProjection[0].length];
		for(int i=0;i<preProjection.length;i++) {
			double[] tmpPoint=Projections.project(preProjection[i][0], preProjection[i][1]);
			withDuplicates[i][0]=tmpPoint[0];
			withDuplicates[i][1]=tmpPoint[1];
			withDuplicates[i][2]=preProjection[i][2];
			withDuplicates[i][3]=preProjection[i][3];
		}
		
		//it may happen that points are very close together afer projection which leads to numerical problems
		ArrayList<double[]> noDuplicates=new ArrayList<double[]>();
		noDuplicates.add(withDuplicates[0]);
		for(int i=0;i<withDuplicates.length;i++) {
			boolean tmp = false;
			for(int j=0;j<noDuplicates.size();j++) {
				if(Math.abs(noDuplicates.get(j)[0]-withDuplicates[i][0])<0.000001 &&Math.abs(noDuplicates.get(j)[1]-withDuplicates[i][1])<0.000001) {
					tmp=true;
					}
				}
			if (tmp==false) {
				noDuplicates.add(withDuplicates[i]);
				}
			}
		return noDuplicates.toArray(new double[noDuplicates.size()][2]);
	}
	
	public static double [][] getSpecificSetProjected(int year,int month){
		//project the points with the currently chosen projection in Projections
		double [][] preProjection=getSpecificSet(year,month);
		double [][] withDuplicates=new double [preProjection.length][preProjection[0].length];
		for(int i=0;i<preProjection.length;i++) {
			double[] tmpPoint=Projections.project(preProjection[i][0], preProjection[i][1]);
			withDuplicates[i][0]=tmpPoint[0];
			withDuplicates[i][1]=tmpPoint[1];
			withDuplicates[i][2]=preProjection[i][2];
		}
		
		
		//it may happen that points are very close together afer projection which leads to numerical problems
		ArrayList<double[]> noDuplicates=new ArrayList<double[]>();
		noDuplicates.add(withDuplicates[0]);
		for(int i=0;i<withDuplicates.length;i++) {
			boolean tmp = false;
			for(int j=0;j<noDuplicates.size();j++) {
				if(Math.abs(noDuplicates.get(j)[0]-withDuplicates[i][0])<0.000001 &&Math.abs(noDuplicates.get(j)[1]-withDuplicates[i][1])<0.000001) {
					tmp=true;
					}
				}
			if (tmp==false) {
				noDuplicates.add(withDuplicates[i]);
				}
			}
		return noDuplicates.toArray(new double[noDuplicates.size()][2]);
	}
	
	//returns a set of tide gauge stations of the form (lat,lon,trainingValue)
	//for the given Epoch
	public static double [][] getSpecificSet(int year,int month){
		if(tideGaugeStandard==null) {
			loadTideGaugeStandard();
		}


		ArrayList<double[]> validPoints=new ArrayList<double[]>();
		String timeCode=getCodeForDate(year,month);
		for(int i=0;i<tideGaugeStandard.length;i++) {
			try(BufferedReader in = new BufferedReader(new FileReader("assets/rlr_monthly/data/"+(int)tideGaugeStandard[i][0]+".rlrdata"))) {
			    String str;
			    //search for the entry with the correct timecode
			    while ((str = in.readLine()) != null) {
			        String[] tokens = str.split(";");
			        double tmp= Double.parseDouble(tokens[1]);
			        double tmpCodeAsDouble=Double.parseDouble(tokens[0]);
			        String tmpCode=String.format(Locale.US,"%.8g%n", tmpCodeAsDouble);
			        //if the station has a real value and the correct timecode than we should save it
			        if(tmp>(-99990)&&tmpCode.equals(timeCode)) {
			        	//skip the entry, if some flag is 001 (see RLR documentation)
			        	if(Integer.parseInt(tokens[2])==001) {
			        		continue;
			        	}
			        	//need to divide tide-gauge values by 10 since its in mm instead of cm 
			        	//subtract the mean tide gauge value and add the mean value of the closest altimeter station
			        	double correctedValue=tmp/10-tideGaugeStandard[i][3]/10+tideGaugeStandard[i][4];
			        	double tmpLong=tideGaugeStandard[i][1];
				    	if(tideGaugeStandard[i][1]<0) {
				    		tmpLong=tmpLong+360;
				    	}
			        	double[] tmpPoint={tmpLong,tideGaugeStandard[i][2],correctedValue};
			        	validPoints.add(tmpPoint);
			        }
			        
			    }	    
			}
			catch (IOException e) {
			    System.out.println("File Read Error");
			    return null;
			}
			
		}
		System.out.println("validSize: "+ validPoints.size());
		return validPoints.toArray( new double[validPoints.size()][3]);
	
	}
	
	//returns a set of tide gauge stations of the form (lat,lon,trainingValue,reconstructionValue)
	//for the given Epochs
	public static double [][] getSpecificSet(int year,int month, int yearRec, int monthRec){
		if(tideGaugeStandard==null) {
			loadTideGaugeStandard();
		}
		
		ArrayList<double[]> validPoints=new ArrayList<double[]>();
		String timeCode=getCodeForDate(year,month);
		String timeCodeRec=getCodeForDate(yearRec,monthRec);
		for(int i=0;i<tideGaugeStandard.length;i++) {
			double correctedVal=Double.MAX_VALUE;
			double correctedValRec=Double.MAX_VALUE;
			try(BufferedReader in = new BufferedReader(new FileReader("assets/rlr_monthly/data/"+(int)tideGaugeStandard[i][0]+".rlrdata"))) {
			    String str;

			    //search for the entry with the correct timecodes
			    while ((str = in.readLine()) != null) {
			        String[] tokens = str.split(";");
			        double tmp= Double.parseDouble(tokens[1]);
			        double tmpCodeAsDouble=Double.parseDouble(tokens[0]);
			        String tmpCode=String.format(Locale.US,"%.8g%n", tmpCodeAsDouble);

			        if(tmp>(-99990)&&tmpCode.equals(timeCode)) {
			        	if(tokens[3].equals(" 001")) {
			        		continue;
			        	}
			        	//set the value of the training phase if it exists
			        	correctedVal=tmp/10-tideGaugeStandard[i][3]/10+tideGaugeStandard[i][4];
			        }
			        if(tmp>(-99990)&&tmpCode.equals(timeCodeRec)) {
			        	if(tokens[3].equals(" 001")) {
			        		continue;
			        	}
			        	//set the value of the reconstruction phase if it exists
			        	correctedValRec=tmp/10-tideGaugeStandard[i][3]/10+tideGaugeStandard[i][4];
			        }
			        
			    }
			    //if both values exist generate a station with both values
			    if(correctedVal!=Double.MAX_VALUE &&correctedValRec!=Double.MAX_VALUE) {
			    	double tmpLong=tideGaugeStandard[i][2];
			    	if(tideGaugeStandard[i][1]<0) {
			    		tmpLong=tmpLong+360;
			    	}
			    	double[] tmpPoint={tideGaugeStandard[i][1],tideGaugeStandard[i][2],correctedVal,correctedValRec};
			    	validPoints.add(tmpPoint);
			    }
			}
			catch (IOException e) {
			    System.out.println("File Read Error");
			    return null;
			}
			
		}
		return validPoints.toArray( new double[validPoints.size()][3]);	
	}
	
	
	
	
	
		

	
	//loads the average tide gauge station values and the associated average altimeter values that have already been saved in a csv
	public static void loadTideGaugeStandard() {
		try(BufferedReader in = new BufferedReader(new FileReader(new File("assets/fileListCutWithAvg.csv")))) {
		    String str;
		    ArrayList<double[]> posList = new ArrayList<double[]>();
		    while ((str = in.readLine()) != null) {
		        String[] tokens = str.split(",");
		        					//id longitude latitude 
		        double[] tmp= {Double.parseDouble(tokens[0]),Double.parseDouble(tokens[1]),Double.parseDouble(tokens[2]),Double.parseDouble(tokens[3]),Double.parseDouble(tokens[4])};
		        posList.add(tmp);
		    }
		    tideGaugeStandard= posList.toArray( new double[posList.size()][3]);
		}
		catch (IOException e) {
		    System.out.println("File Read Error");
		}
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////Tide-gauge Average calculations; should not be recomputed////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void saveAsCsv(double[][] points) throws IOException {
		FileWriter writer = new FileWriter(new File("assets/"+"fileListCutWithAvg.csv"));
		for(int i=0;i<points.length;i++) {
			writer.append((int)(points[i][0])+" , " + points[i][1]+ " , " +points[i][2]+ " , " + points[i][3]+ " , " + points[i][4]  );
			writer.append("\n");
			}

		writer.flush();
		writer.close();
	}
	
	public static double[][] LoadTideGaugeSet(){
		try(BufferedReader in = new BufferedReader(new FileReader(new File("assets/filelist.txt")))) {
		    String str;
		    ArrayList<double[]> posList = new ArrayList<double[]>();
		    while ((str = in.readLine()) != null) {
		        String[] tokens = str.split(";");
		        					//id longitude latitude 
		        double tmpLon=Double.parseDouble(tokens[2]);
		        if(tmpLon<0) {
		        	tmpLon=tmpLon+360;
		        }
		        double[] tmp= {Double.parseDouble(tokens[0]),tmpLon,Double.parseDouble(tokens[1])};
		        posList.add(tmp);
		    }
		    return posList.toArray( new double[posList.size()][3]);
		}
		catch (IOException e) {
		    System.out.println("File Read Error");
		    return null;
		}
		
	}
	
	public static int[] adjustIndex(int[] index, int toWhere,int k) {
		int[] result= {0,0};
		if(k==0) {
			return index;
		}
		if(toWhere==0) {
			result[0]=index[0]+k;
			result[1]=index[1];
		}
		if(toWhere==1) {
			result[0]=index[0];
			result[1]=index[1]+k;
		}
		if(toWhere==2) {
			result[0]=index[0]-k;
			result[1]=index[1];
		}
		if(toWhere==3) {
			result[0]=index[0];
			result[1]=index[1]-k;
		}
		if(result[0]<0) {
			result[0]=result[0]+1440;
		}
		if(result[0]>=1440) {
			result[0]=result[0]-1440;
		}
		if(result[1]<0) {
			result[1]=result[1]+720;
		}
		if(result[1]>=720) {
			result[1]=result[1]-720;
		}
		
		return result;
 	}
	
	public static double avgOfClosest(double lon,double lat) {
		int[] startingIndex=getClosestAltiToStationAsIndex(lon, lat);
		boolean hasFound=false;
		int counter=0;
		int[] adjustedIndex= {0,0};
		while(!hasFound) {
			for(int i=0;i<4;i++) {
				adjustedIndex=adjustIndex(startingIndex,i,counter);

				double tmpval=avgAltiMap[adjustedIndex[0]][adjustedIndex[1]].val;
				Double test=Double.valueOf(tmpval);
				if(!test.isNaN()) {
					return tmpval;
				}
			}
			counter=counter+1;
			
			
		}
		
		return 0;
	}
	
	public static int[] getClosestIndex(double lon,double lat) {
		if(avgAltiMap==null) {
			avgAltiMap=AltimeterLoader.loadAvgAltiFromFile();
		}
		int[] startingIndex=getClosestAltiToStationAsIndex(lon, lat);
		boolean hasFound=false;
		int counter=0;
		int[] adjustedIndex= {0,0};
		while(!hasFound) {
			for(int i=0;i<4;i++) {
				adjustedIndex=adjustIndex(startingIndex,i,counter);
				double tmpval=avgAltiMap[adjustedIndex[0]][adjustedIndex[1]].val;
				Double test=Double.valueOf(tmpval);
				if(!test.isNaN()) {
					return adjustedIndex;
				}
			}
			counter=counter+1;
		}	
		return null;
		
	}
	
	
	//long range 0,360 lat range -90,90
	public static int[] getClosestAltiToStationAsIndex(double lon,double lat) {
		
		double floorLon= Math.floor(lon);
		double floorLat=Math.floor(lat);
		double minLon=Double.MAX_VALUE;
		double minDist=Double.MAX_VALUE;
		double value=floorLon-0.125;
		for(int i=0;i<6;i++) {
			double curVal= value+0.25*i;
			if(Math.abs(curVal-lon)<minDist) {
				minLon=curVal;
				minDist=Math.abs(curVal-lon);
			}
		}
		if(minLon<0) {
			minLon=minLon+360;
		}
		if(minLon>360) {
			minLon=minLon-360;
		}
		double minLat=Double.MAX_VALUE;
		minDist=Double.MAX_VALUE;
		value=floorLat-0.125;
		for(int i=0;i<6;i++) {
			double curVal= value+0.25*i;
			if(Math.abs(curVal-lat)<minDist) {
				minLat=curVal;
				minDist=Math.abs(curVal-lat);
			}
		}
		if(minLat<-90) {
			minLat=minLat+180;
		}
		if(minLat>90) {
			minLat=minLat-180;
		}
		minLat=minLat+90;
		double indexLon=(minLon+-0.125)/0.25;
		double indexLat=(minLat-0.125)/0.25;
		
		
		
		
		
		
		int[] result= {(int)indexLon,(int)indexLat};

		
		return result ;
	}
	
	public static double[] getAllValuesOneStation(int i ) {
		try(BufferedReader in = new BufferedReader(new FileReader(new File("assets/rlr_monthly/data/"+i+".rlrdata")))) {
		    String str;
		    ArrayList<Double> values = new ArrayList<Double>();
		    while ((str = in.readLine()) != null) {
		        String[] tokens = str.split(";");
		        double tmp= Double.parseDouble(tokens[1]);
		        if(tmp>(-99990)&&Double.parseDouble(tokens[0])>1993&&Double.parseDouble(tokens[0])<2016) {
		        	values.add(tmp);
		        }
		        
		    }
		    double[] result =new double[values.size()];
		    for(int j=0;j<result.length;j++) {
		    	result[j]=values.get(j).doubleValue();
		    }
		    		  
		    return result; 
		}
		catch (IOException e) {
		    System.out.println("File Read Error");
		    return null;
		}
		
	}
	
	//id long lat avg closest
	public static double[][]  addAveragesToTideGaugeSet(){
		
		double[][] theSet= LoadTideGaugeSet();
		double[][] result=new double[theSet.length][5];
		for(int i=0;i<theSet.length;i++) {
			int currentId = (int)theSet[i][0];
			double[] tmpArr=getAllValuesOneStation(currentId);
			double tmpAvg=0;
			for(int j=0;j<tmpArr.length;j++) {

				tmpAvg=tmpAvg+tmpArr[j];
			}
			tmpAvg=tmpAvg/tmpArr.length;
			result[i][0]=theSet[i][0];
			result[i][1]=theSet[i][1];
			result[i][2]=theSet[i][2];
			result[i][3]=tmpAvg;
			result[i][4]=avgOfClosest(theSet[i][1],theSet[i][2]);
		}	
		return result;
	}
	
	public static void saveAvgForTide() throws Exception {
		if(avgAltiMap==null) {
			avgAltiMap=AltimeterLoader.loadAvgAltiFromFile();
		}
		saveAsCsv(addAveragesToTideGaugeSet());
	}
	
	


}
