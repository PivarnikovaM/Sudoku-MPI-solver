import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import mpi.*;

public class MPJMaster implements Runnable {

	private int rank;
	private SudokuMatrix matrix;
	private SudokuMatrix inputMatrix;
	private SudokuMatrix[] buffer;
	public static int MASTER_INDEX = 0;
	public static int SIZE = 1;
	public static final int BOARD_SIZE = 9;
	public static final int BOX_SIZE = 3;

	private int[] intBuffer = new int[SIZE];

	MPJMaster() {
		rank = MPI.COMM_WORLD.Rank();
	}

	public void run() {

		log("Spusteny");

		// Nacitame vstup sudoku zo suboru
		int[][] inpMatrix = new int[BOARD_SIZE][BOARD_SIZE];
		int[][] orgMatrix = new int[BOARD_SIZE][BOARD_SIZE];

		BufferedReader br;
		try {
			br = Files.newBufferedReader(Paths.get("Sudoku.txt"));
			String line;
			String[] nums;

			for (int k = 0; k < inpMatrix.length; k++) {
				try {
					line = br.readLine();
					nums = line.split(" ");
					for (int j = 0; j < inpMatrix.length; j++) {
						inpMatrix[k][j] = Integer.parseInt(nums[j]);
						orgMatrix[k][j] = Integer.parseInt(nums[j]);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		matrix = new SudokuMatrix(inpMatrix);
		inputMatrix = new SudokuMatrix(orgMatrix);

		// Vytvarame prefix - vyplname prvy stlpec posielanej matice vsetkymi moznymi
		// vyplneniami

		// Najdeme cisla, ktore sa nenachadzaju v prvom stlpci, teda cisla
		// ktorych moznosti treba generovat do prveho stlpca

		int[] colArray = new int[BOARD_SIZE];
		for (int row = 0; row < BOARD_SIZE; row++) {
			colArray[row] = matrix.getMatrix()[row][0];
		}
		int[] numbersToGenerate = new int[numOfZerosInRow(colArray)];
		Set<Integer> mySet = Arrays.stream(colArray).boxed().collect(Collectors.toSet());

		int k = 0;
		for (int i = 1; i <= BOARD_SIZE; i++) {
			if (!mySet.contains(i)) {
				numbersToGenerate[k] = i;
				k++;
			}
		}

		// Generujeme cisla do prveho stlpca matice
		generate(numbersToGenerate.length, numbersToGenerate);

		// Posielam spravu, nech workeri skoncia a vratia mi vysledok
		buffer = new SudokuMatrix[1];
		for (int i = 2; i < MPI.COMM_WORLD.Size(); i++) {
			Status st = MPI.COMM_WORLD.Recv(intBuffer, 0, 0, MPI.INT, MPI.ANY_SOURCE, MPJMain.TAGS.TAG_ASK.ordinal());
			MPI.COMM_WORLD.Send(buffer, 0, 1, MPI.OBJECT, st.source, MPJMain.TAGS.TAG_SEND.ordinal());

		}

		// Reduce pomocou vlastnej funkcie, vypiseme vyriesenu sudoku
		Op myOp = new Op(new FindResult(), true);
		SudokuMatrix[] reduce = new SudokuMatrix[MPI.COMM_WORLD.Size()];
		MPI.COMM_WORLD.Reduce(reduce, 0, reduce, 0, 1, MPI.OBJECT, myOp, 0);
		log("Correct result: " + reduce[0].toString());
	}

	private void addNumsToFirstColumn(int[] elements) {
		int j = 0;
		for (int i = 0; i < matrix.getMatrix()[0].length; i++) {
			if (inputMatrix.getMatrix()[i][0] == 0) {
				matrix.getMatrix()[i][0] = elements[j];
				j++;
			}
		}

	}

	private void process() {

		SudokuMatrix[] buffer = { matrix };
		Status st = MPI.COMM_WORLD.Recv(intBuffer, 0, 0, MPI.INT, MPI.ANY_SOURCE, MPJMain.TAGS.TAG_ASK.ordinal());
		MPI.COMM_WORLD.Send(buffer, 0, buffer.length, MPI.OBJECT, st.source, MPJMain.TAGS.TAG_WORK.ordinal());
	}

	private int numOfZerosInRow(int[] arr) {

		int counter = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == 0) {
				counter++;
			}
		}

		return counter;
	}

	// Recurzivne generovanie vsetkych moznosti

	public void generate(int n, int[] elements) {

		if (n == 1) {
			addNumsToFirstColumn(elements);
			process();

		} else {
			for (int i = 0; i < n - 1; i++) {
				generate(n - 1, elements);
				if (n % 2 == 0) {
					swap(elements, i, n - 1);
				} else {
					swap(elements, 0, n - 1);
				}
			}
			generate(n - 1, elements);
		}
	}

	private void swap(int[] input, int a, int b) {
		int tmp = input[a];
		input[a] = input[b];
		input[b] = tmp;
	}

	private void log(String s) {
		System.out.println("Master: " + s);
	}

}
