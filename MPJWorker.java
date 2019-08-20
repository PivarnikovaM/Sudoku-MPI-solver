import java.util.HashSet;
import java.util.Set;

import mpi.*;

public class MPJWorker implements Runnable {
	private int rank;

	SudokuMatrix inputMatrix;
	SudokuMatrix matrix;
	private int[] intBuffer;
	private SudokuMatrix[] buffer2 = new SudokuMatrix[1];

	MPJWorker() {
		rank = MPI.COMM_WORLD.Rank();
	}

	public void run() {
		log("Spusteny");

		intBuffer = new int[MPJMaster.SIZE];
		matrix = null;

		Object[] buffer = new Object[1];
		while (true) {
			MPI.COMM_WORLD.Send(intBuffer, 0, 0, MPI.INT, MPJMaster.MASTER_INDEX, MPJMain.TAGS.TAG_ASK.ordinal());
			Status st = MPI.COMM_WORLD.Recv(buffer, 0, 1, MPI.OBJECT, MPJMaster.MASTER_INDEX, MPI.ANY_TAG);

			if (st.tag == MPJMain.TAGS.TAG_WORK.ordinal()) {

				matrix = new SudokuMatrix();
				inputMatrix = (SudokuMatrix) buffer[0];

				// nakopirovanie matice
				for (int i = 0; i < inputMatrix.getMatrix()[0].length; i++) {
					for (int j = 0; j < inputMatrix.getMatrix()[0].length; j++) {
						matrix.setElement(i, j, inputMatrix.getMatrix()[i][j]);
					}
				}

				if (solve(0, 0) && rowCheck()) {
					log("Found correct solution!\n");
					log(matrix.toString());
					buffer2[0] = matrix;
					
				}
				
			} else {
				break;
			}
		}

		// posleme vysledok

		// Matica[] b2 = new Matica[MPI.COMM_WORLD.Size()];
		Op myOp = new Op(new FindResult(), true);
		MPI.COMM_WORLD.Reduce(buffer2, 0, buffer2, 0, 1, MPI.OBJECT, myOp, 0);
		log("sent");
	}

	private boolean rowCheck() {
		Set<Integer> wasInRow = null;

		for (int i = 0; i < matrix.getMatrix()[0].length; i++) {
			wasInRow = new HashSet<>();
			for (int j = 0; j < matrix.getMatrix()[0].length; j++) {
				if (wasInRow.contains(matrix.getMatrix()[i][j])) {
					return false;
				} else {
					wasInRow.add(matrix.getMatrix()[i][j]);
				}

			}
		}

		return true;
	}

	public boolean solve(int row, int col) {

		if (col == MPJMaster.BOARD_SIZE) {
			// Ak sme v 9 stlpci, presunieme sa na dalsi riadok na prvy stlpec
			col = 0;
			row += 1;
			// Ak sa dostaneme na posledny riadok, metoda konci a vraciame true
			if (row == MPJMaster.BOARD_SIZE) {
				return true;
			}
		}

		for (int i = 1; i < MPJMaster.BOARD_SIZE + 1; i++) {
			if (matrix.getMatrix()[row][col] == 0) {
				// Vyplname prazdne miesta (tam kde sa nachadza 0), vyplname ju cislom od 1-9

				if (isOK(row, col, i)) {
					// Ak tam mozme doplnit dane cislo, vyplnime ho a volame rekurzivnu metodu na
					// dalsom okienku

					matrix.getMatrix()[row][col] = i;
					// Volame rekurzivnu metodu, ktora sa snazi vyplnit
					// ostatne prazdne miesta, az kym nevyplni vsetky

					if (solve(row, col + 1))
						return true;
				} else {
					// Ak sa neda doplnit ziadne cislo, funkcia vyhodi false a
					// hodnota v predoslom rekurzivnom kroku sa nastavi na 0 a znova doplna hodnoty
					// od kade skoncil
					matrix.getMatrix()[row][col] = 0;
				}
			} else
				// Ak tam netreba doplnat cislo (nie je tam 0) preskoci sa to okienko
				return solve(row, col + 1);
		}
		// Ak nebolo najdene ziadne riesenie
		return false;
	}

	public boolean isOK(int row, int col, int num) {
		// Kontrola ci sa dane cislo nachadza v riadku
		for (int j = 0; j < MPJMaster.BOARD_SIZE; j++) {
			if (matrix.getMatrix()[row][j] == num) {
				return false;
			}
		}
		// Kontrola ci sa dane cislo nachadza v stlpci
		for (int r = 0; r < MPJMaster.BOARD_SIZE; r++) {
			if (matrix.getMatrix()[r][col] == num) {
				return false;
			}
		}

		// Kontrola ci sa dane cislo nachadza v 3x3 stvorci

		int topRightRow = (row / MPJMaster.BOX_SIZE) * MPJMaster.BOX_SIZE;
		int topRightCol = (col / MPJMaster.BOX_SIZE) * MPJMaster.BOX_SIZE;
		for (int i = 0; i < MPJMaster.BOX_SIZE; i++) {
			for (int j = 0; j < MPJMaster.BOX_SIZE; j++) {
				if (matrix.getMatrix()[topRightRow + i][topRightCol + j] == num) {
					return false;
				}
			}
		}
		return true;
	}

	private void log(String s) {
		System.out.println("Worker " + rank + ": " + s);
	}

}
