import mpi.Datatype;
import mpi.MPIException;
import mpi.User_function;

public class FindResult extends User_function {
    @Override
    public void Call(Object o, int i, Object o1, int i1, int i2, Datatype datatype) throws MPIException {
        //prichadzajuce pole
        SudokuMatrix[] m = (SudokuMatrix[]) o;
        
        SudokuMatrix matica = m[i];

        Object[] out = (Object[]) o1;

        if (matica != null) {
            out[i1] = matica;
        }
    }
}
