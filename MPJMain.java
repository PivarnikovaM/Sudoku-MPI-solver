import mpi.*;

public class MPJMain {
	
	public static enum TAGS {
		TAG_ASK, TAG_WORK, TAG_SEND, TAG_SOLUTION;
	}
	
	 public static void main(String[] args) {
		  MPI.Init(args);
		  int size = MPI.COMM_WORLD.Size();
		  int me = MPI.COMM_WORLD.Rank();
		  Runnable r = (me == 0) ? new MPJMaster() : new MPJWorker();
		  r.run();
		  MPI.Finalize();
	 }
}
