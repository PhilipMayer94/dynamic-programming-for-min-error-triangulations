package dynamic_programming_for_MTWT;

//the keys that are used for storing different solution
public class SubProblemKey {
	int k1;
	int k2;
	
	
	SubProblemKey(int i, int j){
		k1=i;
		k2=j;
	}
	
	@Override
	public boolean equals(Object o) {
		if((k1==((SubProblemKey)o).k1)&&(k2==((SubProblemKey)o).k2)) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 23;
		hash = hash * 31 + k1+1;
		hash = hash * 31 + k2+1;
		return hash;
	}
	@Override
	public String toString() {
		return "(" + String.valueOf(k1)+", "+String.valueOf(k2)+ ")";
	}

}

	
