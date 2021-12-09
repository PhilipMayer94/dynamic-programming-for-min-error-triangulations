
package dynamic_programming_for_MTWT;

//solution of a sub-problem is given by a value of the sub-problem a key and the triangle that splits for the solution
public class SubProblemSolution {
	double value;
	SubProblemKey key;
	PolygonTriangle solutionTriangle=null;

	
	SubProblemSolution(SubProblemKey myKey,PolygonTriangle triangle,double solutionValue){
		value=solutionValue;
		this.key=myKey;
		this.solutionTriangle=triangle;
	}
	
	@Override
	public String toString() {
		return "SubProblemSolution: " + String.valueOf(value)+" "+ this.solutionTriangle.tipId;
	}
}
