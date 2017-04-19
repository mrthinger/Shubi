package mrthinger.shubi;

public class IntPointer {

	private int i;
	
	public IntPointer(){
		this.i = 0;
	}
	
	public IntPointer(int i) {
		this.i = i;
	}
	
	public int getI(){
		return i;
	}
	
	public void setI(int i){
		this.i = i;
	}
	
	public int decrementI(){
		return i -= 1;
	}

}
