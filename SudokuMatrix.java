import java.io.Serializable;
import java.util.Arrays;

public class SudokuMatrix implements Serializable{
	
	private int[][] matrix;

	public SudokuMatrix(int[][] matica) {
		super();
		this.matrix = matica;
	}
	

	public SudokuMatrix() {
		super();
		matrix = new int[9][9];
	}


	public int[][] getMatrix() {
		return matrix;
	}

	public void setMatrix(int[][] matica) {
		this.matrix = matica;
	}
	
	public void setElement(int i, int j, int element){
		this.matrix[i][j] = element;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
	    for (int r = 0; r < matrix.length; r++) 
	    { 
	        for (int d = 0; d < matrix.length; d++) 
	        { 
	            sb.append(matrix[r][d]); 
	            sb.append(" "); 
	        } 
	        sb.append("\n"); 
	          
	        if ((r + 1) % (int) Math.sqrt(matrix.length) == 0)  
	        { 
	        	 sb.append(""); 
	        } 
	    } 
		//return "\n" + Arrays.deepToString(matica).replace("], ", "]\n");
	    return "\n" + sb.toString();
	}
	
	


}
