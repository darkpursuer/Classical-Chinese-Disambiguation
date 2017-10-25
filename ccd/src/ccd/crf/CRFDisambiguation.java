package ccd.crf;

import java.util.ArrayList;
import java.util.HashMap;

import ccd.tools.domain.WordSensePr;
import ccd.tools.repository.SenseCharRepository;
import ccd.tools.repository.WordSourceRepository;

public class CRFDisambiguation {

	// maybe the posterior is too large so we need a prior parameter to balance
	// it
	private double prior = 10;
	private ArrayList<String> senseSet;
	private WordSensePr senses;

	private String word, context, split;

	// word, senses, Prs
	private HashMap<String, WordSensePr> wordsensePr;
	private HashMap<String, Float> empty;

	// sense, char, PrVj
	private HashMap<String, HashMap<String, Double>> chars;
	private HashMap<String, Double> PrVjs;

	// Matrixes
	private int N;
	private int M;
	private int T;
	private double A[][];
	private double B[][];
	private double PI[];

	private ArrayList<Character> v;

	private WordSourceRepository wordSourceRepository;
	private SenseCharRepository senseCharRepository;

	public CRFDisambiguation(HashMap<String, WordSensePr> wordsensePr, HashMap<String, Float> empty, double prior) {
		this.wordsensePr = wordsensePr;
		this.empty = empty;
		this.prior = prior;

		wordSourceRepository = new WordSourceRepository();
		senseCharRepository = new SenseCharRepository();
	}

	/**
	 * Disambiguate one word sense in its context
	 * 
	 * @return the sense list with the highest probability
	 */

	public ArrayList<String> Disambiguation(String word, String context) {
		if (!wordSourceRepository.isWordExists(word)) {
			System.err.println("No such word in the database!");
			return null;
		}

		this.word = word;
		this.context = context;

		/*
		 * get the probabilities from the database
		 */

		// word, sense, PrSk
		senses = wordsensePr.get(word);
		senseSet = senses.senses;
		int sensenum = senseSet.size();
		double score[] = new double[sensenum];

		// word, sense, char, PrVj
		chars = senseCharRepository.getPrVj(word);

		/*
		 * generate the matrixes A, B and Pi
		 */

		N = sensenum;

		initMatrix();

		/*
		 * Initialization
		 */

		double delta[][] = new double[T][N];
		double psi[][] = new double[T][N];
		ArrayList<String> result = new ArrayList<String>();

		for (int i = 0; i < N; i++) {
			delta[0][i] = PI[i] * B[i][v.indexOf(split.charAt(0))];
			psi[0][i] = 0;
		}

		// printMatrix("delta", delta, T, N);
		// printMatrix("psi", psi, T, N);

		/*
		 * Recursion
		 */

		for (int t = 1; t < T; t++) {
			for (int j = 0; j < N; j++) {
				double maxval = 0.0f;
				double maxvalind = 0;

				for (int i = 0; i < N; i++) {
					double val = delta[t - 1][i] * A[i][j];

					if (val > maxval) {
						maxval = val;
						maxvalind = i;

						// System.out.printf("maxval: %f, maxvalind: %f\n",
						// maxval, maxvalind);
					}
				}

				delta[t][j] = maxval * B[j][v.indexOf(split.charAt(t))];
				psi[t][j] = maxvalind;
			}
		}

		// printMatrix("delta", delta, T, N);
		// printMatrix("psi", psi, T, N);

		/*
		 * Termination
		 */

		double pprob = 0;

		for (int i = 0; i < N; i++) {
			if (delta[T - 1][i] > pprob) {
				pprob = delta[T - 1][i];
			}
		}

		for (int i = 0; i < N; i++) {
			if (delta[T - 1][i] == pprob) {
				result.add(senseSet.get(i));
			}
		}

		return result;

	}

	/**
	 * Initialize the Matrixes and M, T Merge the M, which is the total figures
	 * number, including the characters and structure details
	 * 
	 */

	private void initMatrix() {
		// initialize A matrix
		A = new double[N][N];

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (i == j) {
					A[i][j] = 1;
				} else {
					A[i][j] = 0;
				}
			}
		}

		// initialize PI array
		PI = new double[N];

		for (int i = 0; i < N; i++) {
			PI[i] = senses.Prs.get(i);
		}

		getFeatures();

		B = new double[N][M];

		for (int i = 0; i < N; i++) {
			String sense = senseSet.get(i);
			PrVjs = chars.get(sense);

			if (PrVjs == null) {
				for (int j = 0; j < M; j++) {
					B[i][j] = 0;
				}

				continue;
			}

			for (int j = 0; j < M; j++) {
				B[i][j] = getPrVj(sense, v.get(j));
			}
		}

	}

	/**
	 * Abstract features from the test context, and determine the M, T
	 * 
	 */

	private void getFeatures() {
		int index = context.indexOf(word);

		split = context.substring(0, index) + context.substring(index + word.length());

		v = new ArrayList<Character>();
		for (int i = 0; i < split.length(); i++) {
			if (!v.contains(split.charAt(i))) {
				v.add(split.charAt(i));
			}
		}

		M = v.size();
		T = split.length();
	}

	/**
	 * get the PrVj of one character
	 * 
	 * @param word
	 * @param sense
	 * @param v
	 * @return
	 */

	private double getPrVj(String sense, char v) {
		double result;

		if (!PrVjs.containsKey(v + "")) {
			result = empty.get(word);
		} else {
			result = PrVjs.get(v + "");
		}

		return result;
	}

	public void printMatrix(String name, double[][] matrix, int height, int width) {
		System.out.println("Matrix: " + name);

		for (int i = 0; i < height; i++) {
			System.out.print(i + ":\t");

			for (int j = 0; j < width; j++) {
				System.out.print(matrix[i][j] + "\t");
			}

			System.out.println();
		}

		System.out.println();
	}
}
