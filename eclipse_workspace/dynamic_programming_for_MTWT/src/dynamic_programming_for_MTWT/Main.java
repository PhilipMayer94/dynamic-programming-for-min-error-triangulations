package dynamic_programming_for_MTWT;
//import java.lang.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;

//import com.sun.tools.javac.util.ArrayUtils;
import java.util.*;


public class Main {

	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////random point generation///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static double[][]  uniformRandomInCircle(double cx, double cy, double radius, int n ){
		double[][] result= new double[n][2];
		Random random = new Random();
		double rand=0;
		for(int i=0;i<n;i++) {
				rand=random.nextDouble();
				result[i][0]=cx-radius+rand*2*radius;
				rand=random.nextDouble();
				result[i][1]=cy-radius+rand*2*radius;
				if(BasicGeometry.calculateDistance(cx, cy, result[i][0], result[i][1])>radius) {
					i=i-1;
				}
				
			}
		return result;
	}
	
	public static double[][] oneEmptyCircle(int n,int centerX, int centerY, int rSmall, int rBig){
		double[][] result= new double[n][2];
		Random random = new Random();
		int radius1=rBig;
		int radius2=rSmall;
		double rand=0;
		double angle=0;
		double rad=0;
		for(int i=0;i<n;i++) {
			rand=random.nextDouble();
			rad=rand*(radius1-radius2)+radius1;
			rand=random.nextDouble();
			angle=rand*2*Math.PI;
			result[i][0]=centerX+Math.cos(angle)*rad;
			result[i][1]=centerY+Math.sin(angle)*rad;			
		}
		return result;	
	}
	
	public static double[][] FourUniformRandomInCircle(int n){
		double[][] result1= uniformRandomInCircle(100,400,60,(int)n/4);
		double[][] result2= uniformRandomInCircle(400,100,60,(int)n/4);
		double[][] result3= uniformRandomInCircle(100,100,60,(int)n/4);
		double[][] result4= uniformRandomInCircle(400,400,60,(int)n/4);
		double[][] both = concArrays(result1,result2);
		double[][] both1 = concArrays(both,result3);
		double[][] both2 = concArrays(both1,result4);
		return both2;
	}
	
	public static double[][] FourEmptyRandomInCircle(int n){
		double[][] result1= oneEmptyCircle((int)n/4,100,400,60,150);
		double[][] result2= oneEmptyCircle((int)n/4,400,100,60,150);
		double[][] result3= oneEmptyCircle((int)n/4,100,100,60,150);
		double[][] result4= oneEmptyCircle((int)n/4,100,100,60,150);
		double[][] both = concArrays(result1,result2);
		double[][] both1 = concArrays(both,result3);
		double[][] both2 = concArrays(both1,result4);
		


		return both2;
	}

	public static double[][] concArrays(double[][] array1,double[][] array2){
		int aLen = array1.length;
        int bLen = array2.length;
        double[][]  result = new double[aLen + bLen][2];

        System.arraycopy(array1, 0, result, 0, aLen);
        System.arraycopy(array2, 0, result, aLen, bLen);
        return result;
	}

	public static void appendToCsv(String name,String info) throws IOException {
		FileWriter writer = new FileWriter(new File(name),true); 
		writer.append(info);
		writer.append("\n");
		writer.flush();
		writer.close();
		
	}
	
	public static void main(String[] args) throws Exception{
		
		
		int which_experiment=0;
		int visualization=0;		
		if(args.length==1) {
			if(args[0].equals("fixed_edges")) {
				which_experiment=1;
			}
		}
		
		if(args.length>1) {
			if(args[0].equals("fixed_edges")) {
				which_experiment=1;
			}
			if(args[1].equals("visualization")) {
				visualization=1;
			}
		}
		
		//reconstruction
		if(which_experiment==0) {
			Projections.type=Projections.LAMBERT;
			Projections.setProjectionLamb(Projections.LAMBERT, -40, 16);
			Reconstructor reconstructor=new Reconstructor();
			reconstructor.setLambertProjection(-40, 16);
			//draw frame
			JFrame f = new JFrame();
			ReconstructionDrawer p=new ReconstructionDrawer();
			if(visualization==1) {
				p.setReconstructor(reconstructor);
       	 		Dimension size = new Dimension(900, 900);
       	 		p.setSize(size);
       	 		p.setPreferredSize(size);
       	 		p.setMinimumSize(size);
       	 		size = new Dimension(900, 900);
       	 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       	 		f.setPreferredSize(size);
       	 		f.setMinimumSize(size);
       	 		f.add(p);
       	 		f.pack();
       	 		f.setSize(size);
       	 		f.setVisible(true);
       	 		f.repaint();
			}
			
       	    
       	    //perform reconstruction
			for(int ordnung =7;ordnung>1;ordnung--) {
				for(int i=1993;i <2016;i++) {
					for(int j=1993;j<i;j++) {
						for(int m=1;m<13;m++) {
							System.out.println("l. year: "+i+", r. year: "+j+", order: "+ordnung);
        					reconstructor.reconstruct(i,m,j,m,ordnung);
        					if(visualization==1) {
        						f.repaint();
        					}
						}
					}
				}
			}			
		}
		
		
		
		
		
		
		
		//random experiments
		if(which_experiment==1) {
			String name="";
			
			//draw frame
			JFrame f = new JFrame();
            SetDrawer p=new SetDrawer();
            if(visualization==1) {
       	 		Dimension size = new Dimension(900, 900);
       	 		p.setSize(size);
       	 		p.setPreferredSize(size);
       	 		p.setMinimumSize(size);
       	 		size = new Dimension(900, 900);
       	 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       	 		f.setPreferredSize(size);
       	 		f.setMinimumSize(size);
       	 		f.add(p);
       	 		f.pack();
       	 		f.setSize(size);
       	 		f.setVisible(true);
       	 		f.repaint();
            }
       	    
            name="results/uniform.csv";
       	    for(int n=1;n<5;n++) {
       	    	for(int order=1;order<11;order++) {
       	    		System.out.println("n: "+n*500+" "+ "order: "+order);
       	    		for(int i=0;i<200;i++) {
       	    			double[][] pointset=uniformRandomInCircle(450,450,400,n*500);
       	    			MTWTSolver solver=new MTWTSolver(pointset,order);
       	    			appendToCsv(name,solver.getGraphinfo());
       	    			if(visualization==1) {
       	    				p.setObjects(solver.myKODObjects);
       	    				f.repaint();
       	    			}
       	    		}
       	    	}
       	    }
       	    
       	    name="results/oneEmptyCircle.csv";
       	    for(int n=1;n<5;n++) {
       	    	for(int order=1;order<11;order++) {
       	    		System.out.println("n: "+n*500+" "+ "order: "+order);
    	    		for(int i=0;i<200;i++) {
    	    			double[][] pointset=oneEmptyCircle( n*500, 400,  400,  300,  350);
    	    			MTWTSolver solver=new MTWTSolver(pointset,order);
    	    			appendToCsv(name,solver.getGraphinfo());
    	    			if(visualization==1) {
    	    				p.setObjects(solver.myKODObjects);
    	    				f.repaint();
    	    			}
    	    		}
    	    	}
       	    }
       	    
       	    name="results/fourCircles.csv";
       	    for(int n=1;n<5;n++) {
    	    	for(int order=1;order<11;order++) {
    	    		System.out.println("n: "+n*500+" "+ "order: "+order);
    	    		for(int i=0;i<200;i++) {
    	    			double[][] pointset=FourUniformRandomInCircle(n*500);
    	    			MTWTSolver solver=new MTWTSolver(pointset,order);
    	    			appendToCsv(name,solver.getGraphinfo());
    	    			if(visualization==1) {
    	    				p.setObjects(solver.myKODObjects);
    	    				f.repaint();
    	    			}
    	    		}
    	    	}
    	    }
		} 
		
	}     
}
