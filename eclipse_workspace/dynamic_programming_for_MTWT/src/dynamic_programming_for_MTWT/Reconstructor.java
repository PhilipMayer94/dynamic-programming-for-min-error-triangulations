package dynamic_programming_for_MTWT;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import com.vividsolutions.jts.index.kdtree.KdTree;

public class Reconstructor {
	
	KdTree altimeterForLearning=null;
	KdTree altimeterForReconstruction=null;
	MTWTSolver solver=null;
	
	int order=2;
	
	double delValueLearning=Double.MIN_VALUE;
	double ourValueLearning=Double.MIN_VALUE;
	double delValueReconstruction=Double.MIN_VALUE;
	double ourValueReconstruction=Double.MIN_VALUE;
	
	double maxNumberOfConnectedComponents=Double.MIN_VALUE;
	double avgNumberOfConnectedComponents=Double.MIN_VALUE;
	double timePreGeo=Double.MIN_VALUE;
	double timePreWeights=Double.MIN_VALUE;
	double timeSolving=Double.MIN_VALUE;
	
	int numberOfUsedStations=Integer.MIN_VALUE;
	int numberOfUsedAltimeter=Integer.MIN_VALUE;
	
	int learningYear=Integer.MIN_VALUE;
	int learningMonth=Integer.MIN_VALUE;
	int reconstructionYear=Integer.MIN_VALUE;
	int reconstructionMonth=Integer.MIN_VALUE;
	
	Reconstructor(){	
	}
	
	
	void setLambertProjection(double long0,double lat0) {
		Projections.setProjectionLamb(Projections.LAMBERT, long0, lat0);
	}
	
	void setEquiProjection(int height,int width) {
		Projections.setProjectionEqu(Projections.EQUILIATERAL, height, width);
	}
	
	//count the number of altimeter points that  are inside of the triangulations
	void getNumberOfUsedAltimeterPoints(int yearLearning, int monthLearning)  throws Exception{
		int counter=0;		
		for(Iterator<Triangle> it=this.solver.solution.iterator();it.hasNext();) {
			Triangle cur=it.next();
			int x= cur.candidatesValues(altimeterForReconstruction).size();
			counter=counter+x;
		}

		numberOfUsedAltimeter=counter;
		
	}
		
	//set the triangle weights with respect to the learning values
	void setWeightsLearning() {
		for(Iterator<Triangle> it= solver.myKODObjects.allKODTriangles.iterator();it.hasNext();){
        	Triangle cur=it.next();
        	cur.setWeightLearning(altimeterForLearning);
        }
		
	}
	
	//set the triangle weights with respect to the  reconstruction values
	void setWeightsReconstruction() {
		for(Iterator<Triangle> it= solver.myKODObjects.allKODTriangles.iterator();it.hasNext();){
        	Triangle cur=it.next();
        	cur.setWeightReconstruction(altimeterForReconstruction);
        }
	}
		
	double getDelSolutionAfterReconstruction() {
		double dVal=0;
        for(Iterator<Triangle> it = solver.delaunaySolution().iterator();it.hasNext();) {
        	Triangle cur=it.next();
        	dVal=dVal+cur.weight;
        }
        return dVal;
		
	}
	
	double getOurSolutionAfterReconstruction() {
		double oVal=0;
        for(Iterator<Triangle> it = solver.solution.iterator();it.hasNext();) {
        	Triangle cur=it.next();
        	oVal=oVal+cur.weight;
        }
        return oVal;
		
	}
	
	void reconstruct(int yearLearning, int monthLearning, int yearReconstruction, int monthReconstruction, int order) throws Exception {
		
		learningYear=yearLearning;
		learningMonth=monthLearning;
		reconstructionYear=yearReconstruction;
		reconstructionMonth=monthReconstruction;		
        this.order=order;

        
        final long t0 = System.currentTimeMillis();
        
        //get the tide gauge set with all stations that have values for both Epochs
		double[][] tideSet=TideGaugeLoader.getSpecificSetProjected(yearLearning,monthLearning,yearReconstruction,monthReconstruction);
		this.numberOfUsedStations= tideSet.length;
		
		//get kd-trees with the altimeter Data for both epochs individually
		altimeterForLearning=AltimeterLoader.loadAndProjectSpecificSetAsKDTree(yearLearning, monthLearning);
		altimeterForReconstruction=AltimeterLoader.loadAndProjectSpecificSetAsKDTree(yearReconstruction, monthReconstruction);
		
		//some of the altimeter sets are missing; we need to skip those
		if(altimeterForLearning==null || altimeterForReconstruction==null) {
			System.out.println("altiData invalid");
			return;
		}
		
		final long t1 = System.currentTimeMillis();
		
		
		
		//calculate the optimal triangulation with the learning values
		solver=new MTWTSolver(tideSet,order);
		final long t2 = System.currentTimeMillis();
		setWeightsLearning();
		final long t3 = System.currentTimeMillis();
		solver.solve();
		
		final long t4 = System.currentTimeMillis();
		getNumberOfUsedAltimeterPoints(yearLearning,monthLearning);
		final long t5 = System.currentTimeMillis();
		
		ourValueLearning=solver.solutionValue/numberOfUsedAltimeter;
		delValueLearning=solver.delaunaySolutionValue()/numberOfUsedAltimeter;
		
		
		//calculate the value of the triangulation on the set we want to reconstruct
		final long t6 = System.currentTimeMillis();
		setWeightsReconstruction();
		final long t7 = System.currentTimeMillis();
		delValueReconstruction=getDelSolutionAfterReconstruction()/numberOfUsedAltimeter;
		ourValueReconstruction=getOurSolutionAfterReconstruction()/numberOfUsedAltimeter;
		
		
		//set the different runtimes that have been calculated
		this.timePreWeights=(t1-t0)+(t3-t2)+(t5-t4)+(t7-t6);
		this.timePreGeo=solver.preProcessingTime;
		this.timeSolving=solver.solvingTime;
		
		//get some information about the graph
		double[] graphInfo=this.solver.getFixedGraphInfoArray();
		this.maxNumberOfConnectedComponents=graphInfo[5];
		this.avgNumberOfConnectedComponents=graphInfo[ 3];		
		
		//output the information into the console
		//printInfo();
		String name = "reconstruction_experiments.csv";
		appendToCsv(name,this.infoAsString());
		

	}
	
	void reset() {
		altimeterForLearning=null;
		altimeterForReconstruction=null;
		solver=null;
		
		order=2;
		
		delValueLearning=-42;;
		ourValueLearning=-42;
		delValueReconstruction=-42;
		ourValueReconstruction=-42;
		
		maxNumberOfConnectedComponents=-42;
		avgNumberOfConnectedComponents=-42;
		timePreGeo=-42;
		timePreWeights=-42;
		timeSolving=-42;
		
		numberOfUsedStations=-42;;
		numberOfUsedAltimeter=-42;
		
		learningYear=-42;
		learningMonth=-42;
		reconstructionYear=-42;
		reconstructionMonth=-42;
		
	}
	
	void printInfo() {

		System.out.println("");
		System.out.println(learningMonth+"."+learningYear+" -> "+reconstructionMonth+"."+reconstructionYear);
		System.out.println("long0="+Projections.lon0+" and "+"lat0="+Projections.lat0);
		
		System.out.println("Used Delaunay order:               "+order  );
		System.out.println("MaxComponent:                      "+this.maxNumberOfConnectedComponents  );
		System.out.println("AvgComponent:                      "+this.avgNumberOfConnectedComponents  );
		System.out.println("Number of points used:             "+this.numberOfUsedStations );
		System.out.println("--------------------");
		
		System.out.println("Our Solution after Learning:       "+ourValueLearning );
		System.out.println("Del Solution after Learning:       "+delValueLearning );
		System.out.println("Our Solution after Reconstruction: "+ourValueReconstruction );
		System.out.println("Del Solution after Reconstruction: "+delValueReconstruction );
		System.out.println("---------------------------------");
		
		System.out.println("Weight-preprocessing time:         "+this.timePreWeights );
		System.out.println("Geometric-preprocessing time:      "+this.timePreGeo );
		System.out.println("Solving time:                      "+this.timeSolving );
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}
	
	String infoAsString() {
		String gen="";
		gen =String.valueOf(0.01*learningMonth+learningYear)+"; "+  String.valueOf(0.01*reconstructionMonth+reconstructionYear)  +"; "+ 
		String.valueOf(Projections.lon0)+"; "+String.valueOf(Projections.lat0);

		String geo=		String.valueOf(order)+"; "+ String.valueOf(this.numberOfUsedStations )  +"; "+
				String.valueOf(this.avgNumberOfConnectedComponents)  +"; "+ String.valueOf(this.maxNumberOfConnectedComponents);
		
		String vals = String.valueOf(Math.floor(ourValueLearning*100) / 100d)+"; "+String.valueOf(Math.floor(delValueLearning*100) / 100d)+
				"; "+String.valueOf(Math.floor(ourValueReconstruction*100) / 100d)+"; "+String.valueOf(Math.floor(delValueReconstruction*100) / 100d) ;
		
		String times= String.valueOf(timePreWeights)+"; "+ String.valueOf(timePreGeo)+"; "+String.valueOf(timeSolving);
		
		return gen+"; "+"; "+geo+"; "+"; "+vals+"; "+"; "+times+"; "+String.valueOf(Math.abs(learningYear-reconstructionYear));
	}
	
	public static void appendToCsv(String name,String info) throws IOException {
		FileWriter writer = new FileWriter(new File("results/"+name),true);
		writer.append(info);
		writer.append("\n");
		writer.flush();
		writer.close();
		
	}
	
	public static String doubleArrayToCsv(double[] arr) {
		String result="";
		for(int i=0;i<arr.length;i++) {
			result=result+String.valueOf(arr[i]);
			if(i<arr.length-1) {
				result=result+"; ";
			}	
		}
		return result;
	}
	
	
	

}
