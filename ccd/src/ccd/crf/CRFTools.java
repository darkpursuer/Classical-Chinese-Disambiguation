package ccd.crf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import ccd.tools.domain.DisambResult;
import ccd.tools.domain.FeatParaBean;
import ccd.tools.domain.WordBean;
import ccd.tools.repository.FeatureRepository;

public class CRFTools {

	public static final int ACCURACY = 9001;
	public static final int FUZZY = 9002;
	public static final int MIXED = 9003;

	private FeatureRepository featureRepository;

	public CRFTools() {
		featureRepository = new FeatureRepository();
	}

	/**
	 * find the exact features in the word and context from the database
	 * 
	 * interact with TABLE FEATURES
	 * 
	 * @return List(Feature indexes)
	 */

	public HashMap<UUID, String> findFeatures(String word, String context, int word_index, int type) {
		HashMap<UUID, String> result = new HashMap<UUID, String>();

		/*
		 * get feature and parameter lists from the database
		 */
		HashMap<UUID, String> featurelist = featureRepository.getFeatureList(word);

		Iterator<Entry<UUID, String>> iter = featurelist.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<UUID, String> entry = (Entry<UUID, String>) iter.next();

			UUID index = entry.getKey();

			String feature = featurelist.get(index);

			switch (type) {
			case ACCURACY:

				if (compareFeature(word, context, word_index, feature)) {
					result.put(index, feature);
				}

				break;

			case FUZZY:

				if (compareFuzzyFeature(word, context, word_index, feature)) {
					result.put(index, feature);
				}

				break;

			case MIXED:

				if (compareFuzzyFeature(word, context, word_index, feature)
						| compareFeature(word, context, word_index, feature)) {
					result.put(index, feature);
				}
			}
		}
		// System.out.println(result.size());
		return result;
	}

	/**
	 * find the exact features in the word and context from the GIVEN feature
	 * list
	 * 
	 * @param featurelist
	 * @param word
	 * @param context
	 * @return
	 */

	public HashMap<UUID, String> findFeatures(HashMap<UUID, String> featurelist, String word, String context,
			int word_index, int type) {
		HashMap<UUID, String> result = new HashMap<UUID, String>();

		/*
		 * get feature and parameter lists from the database
		 */

		Iterator<Entry<UUID, String>> iter = featurelist.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<UUID, String> entry = (Entry<UUID, String>) iter.next();

			UUID index = entry.getKey();

			String feature = featurelist.get(index);

			switch (type) {
			case ACCURACY:

				if (compareFeature(word, context, word_index, feature)) {
					result.put(index, feature);
				}

				break;

			case FUZZY:

				if (compareFuzzyFeature(word, context, word_index, feature)) {
					result.put(index, feature);
				}

				break;

			case MIXED:

				if (compareFuzzyFeature(word, context, word_index, feature)
						| compareFeature(word, context, word_index, feature)) {
					result.put(index, feature);
				}
			}
		}
		// System.out.println(result.size());
		return result;
	}

	/**
	 * This function have access to insert data into the database! interact with
	 * TABLE FEATURES and PARAMETERS
	 * 
	 * 1. first find the existed features in the context (findFeatures()). 2.
	 * generate new features from the training context (generateInner()). 3. get
	 * rid of the existing features from the generating. 4. insert the rest new
	 * features in the database. 5. return the list of parameters.
	 * 
	 * @param word
	 * @param context
	 * @return List of features
	 */

	public ArrayList<FeatParaBean> generateFeatures(WordBean wordBean, int field, int type) {
		// HashMap<Integer, String> result = new HashMap<Integer, String>();
		String word = wordBean.word;
		String context = wordBean.context;
		int index = wordBean.index;

		// 1. first find the existed features in the context.
		HashMap<UUID, String> exist = findFeatures(word, context, index, type);

		// 2. generate new features from the training context.
		ArrayList<FeatParaBean> generate = null;

		switch (type) {
		case ACCURACY:
			generate = generateInner(wordBean, field);
			break;

		case FUZZY:
			generate = generateFuzzyInner(wordBean, field);
			break;

		case MIXED:
			generate = generateInner(wordBean, field);
			generate.addAll(generateFuzzyInner(wordBean, field));
		}

		// 3. get rid of the existing features from the generating.
		Iterator<Entry<UUID, String>> iter = exist.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<UUID, String> entry = (Entry<UUID, String>) iter.next();
			String feature = (String) entry.getValue();

			for (FeatParaBean bean : generate) {
				if (bean.feature_str.equals(feature)) {
					generate.remove(bean);

					break;
				}
			}
		}

		// 4. insert the rest new features in the database.
		featureRepository.insertFeatures(generate);

		// 5. return the list of features.
		return generate;
	}

	/**
	 * This function have access to insert data into the database! interact with
	 * TABLE FEATURES and PARAMETERS
	 * 
	 * 1. first find the existed features in the context (findFeatures()). 2.
	 * generate new features from the training context (generateInner()). 3. get
	 * rid of the existing features from the generating. 4. insert the rest new
	 * features in the database. 5. return the list of parameters.
	 * 
	 * @param word
	 * @param context
	 * @return List of features
	 */

	public ArrayList<FeatParaBean> generateFeatures(HashMap<UUID, String> featurelist, WordBean wordBean, int field,
			int type) {
		// HashMap<Integer, String> result = new HashMap<Integer, String>();
		String word = wordBean.word;
		String context = wordBean.context;
		int index = wordBean.index;

		// 1. first find the existed features in the context.
		HashMap<UUID, String> exist = findFeatures(featurelist, word, context, index, type);

		// 2. generate new features from the training context.
		ArrayList<FeatParaBean> generate = null;

		switch (type) {
		case ACCURACY:
			generate = generateInner(wordBean, field);
			break;

		case FUZZY:
			generate = generateFuzzyInner(wordBean, field);
			break;

		case MIXED:
			generate = generateInner(wordBean, field);
			generate.addAll(generateFuzzyInner(wordBean, field));
		}

		// 3. get rid of the existing features from the generating.
		Iterator<Entry<UUID, String>> iter = exist.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<UUID, String> entry = (Entry<UUID, String>) iter.next();
			String feature = (String) entry.getValue();

			for (FeatParaBean bean : generate) {
				if (bean.feature_str.equals(feature)) {
					generate.remove(bean);

					break;
				}
			}
		}

		// 4. insert the rest new features in the database.
		featureRepository.insertFeatures(generate);

		for (int i = 0; i < generate.size(); i++) {
			featurelist.put(generate.get(i).index, generate.get(i).feature_str);
		}

		// 5. return the list of features.
		return generate;
	}

	/**
	 * generate the new features from the context.
	 * 
	 * Feature format: c~, ~c, c1~, ~1c, c~c, cc~, c1c~, c-~, 1-c~, ~-c
	 * 
	 * @param word
	 * @param context
	 * @param field:
	 *            the range(radios) of generating features
	 * 
	 * @return
	 */

	public ArrayList<FeatParaBean> generateInner(WordBean wordBean, int field) {
		ArrayList<FeatParaBean> result = new ArrayList<FeatParaBean>();

		int base = wordBean.index;
		String word = wordBean.word;
		String context = wordBean.context;
		String sense = wordBean.sense;

		int word_s = word.length();

		/*
		 * One character around the word
		 */

		for (int round = 1; round <= field; round++) {
			int fore = base - round;
			int back = base + round + word_s - 1;

			int interval = round - 1;

			// is the indexes legal
			if (fore >= 0 && fore < context.length()) {
				String feature;

				if (interval == 0) {
					feature = context.charAt(fore) + "~";
				} else {
					feature = context.charAt(fore) + "" + interval + "~";
				}

				FeatParaBean bean = new FeatParaBean(word, sense, feature, true);

				result.add(bean);
			}

			if (back >= 0 && back < context.length()) {
				String feature;

				if (interval == 0) {
					feature = "~" + context.charAt(back);
				} else {
					feature = "~" + interval + "" + context.charAt(back);
				}

				FeatParaBean bean = new FeatParaBean(word, sense, feature, true);

				result.add(bean);
			}
		}

		/*
		 * Two characters around the word
		 */

		// generate the list of indexes
		ArrayList<Integer> indexes = new ArrayList<Integer>();

		for (int round = base - field; round <= base + field + word_s - 1; round++) {
			if (round >= 0 && round < context.length() && (round < base || round >= (base + word_s))) {
				indexes.add(round);
			}
		}

		// generate the 2-power set of indexes
		for (int i_i = 0; i_i < indexes.size(); i_i++) {
			int i = indexes.get(i_i);

			for (int j_i = i_i + 1; j_i < indexes.size(); j_i++) {
				int j = indexes.get(j_i);

				char fore = context.charAt(i);
				char back = context.charAt(j);
				String feature;

				// i..j..~
				if (i < base && j < base) {
					if (j - i == 1) {
						feature = fore + "" + back + "";
					} else {
						feature = fore + "" + (j - i - 1) + "" + back + "";
					}

					if (base - j == 1) {
						feature += "~";
					} else {
						feature += (base - j - 1) + "~";
					}
				}

				// i..~..j..
				else if (i < base && j > base) {
					if (base - i == 1) {
						feature = fore + "~";
					} else {
						feature = fore + "" + (base - i - 1) + "~";
					}

					if (j - base == word_s) {
						feature += back;
					} else {
						feature += (j - base - word_s) + "" + back;
					}
				}

				// ~..i..j..
				else {
					if (i - base == word_s) {
						feature = "~" + fore;
					} else {
						feature = "~" + (i - base - word_s) + "" + fore;
					}

					if (j - i == 1) {
						feature += back;
					} else {
						feature += (j - i - 1) + "" + back;
					}
				}

				FeatParaBean bean = new FeatParaBean(word, sense, feature, true);

				result.add(bean);
			}
		}

		/*
		 * Head and tail characters
		 */

		// header
		if (base - field > 0) {
			String feature = context.charAt(0) + "-~";

			FeatParaBean bean = new FeatParaBean(word, sense, feature, true);

			result.add(bean);
		}

		// tail
		if (base + field < context.length() - 1) {
			String feature = "~-" + context.charAt(context.length() - 1);

			FeatParaBean bean = new FeatParaBean(word, sense, feature, true);

			result.add(bean);
		}

		/*
		 * //single characters for(int i = 0; i < context.length(); i++) { char
		 * c = context.charAt(i);
		 * 
		 * if(!word.equals(c + "")) { FeatParaBean bean = new FeatParaBean(word,
		 * sense, c+"");
		 * 
		 * result.add(bean); } }
		 */

		return result;
	}

	/**
	 * Compare the context with the feature (PRECISELY)
	 * 
	 * @param word
	 * @param context
	 * @param feature
	 * @return
	 */

	public boolean compareFeature(String word, String context, int index, String feature) {
		if (!context.contains(word)) {
			return false;
		}

		if (feature.contains(">") || feature.contains("<")) {
			return false;
		}

		// judge that whether is a single character
		if (feature.length() == 1) {
			char c = feature.charAt(0);

			if (c != '~' && c != '-' && (c < '0' || c > '9')) {
				if (context.contains(c + ""))
					return true;
			}
		}

		for (int i = 0; i < feature.length(); i++) {
			char c = feature.charAt(i);

			if (c != '~' && c != '-' && (c < '0' || c > '9')) {
				if (!context.contains(c + "")) {
					return false;
				}
			}
		}

		boolean result = false;

		int base = index;
		int word_s = word.length();

		char first = feature.charAt(0);

		int pointer = base - 1;

		int f_base = feature.indexOf("~");

		// scan backward
		for (int i = f_base - 1; i >= 0 && pointer >= 0; i--) {
			char c = feature.charAt(i);

			// header
			if (c == '-') {
				if (first == context.charAt(0)) {
					return true;
				} else {
					return false;
				}
			}

			// numbers
			else if (c >= '0' && c <= '9') {
				int number = c - '0';

				// whether the fore character is a number?
				char fore = ' ';
				if (i - 1 >= 0) {
					fore = feature.charAt(i - 1);
				}

				if (fore >= '0' && fore <= '9') {
					number = number + 10 * (fore - '0');

					i--;
				}

				pointer -= number;

				if (pointer < 0) {
					return false;
				}
			}

			// normal characters
			else {
				if (context.charAt(pointer) != c) {
					return false;
				}

				pointer--;
			}

			result = true;
		}

		// scan positive
		pointer = base + word_s;

		for (int i = f_base + 1; i < feature.length() && pointer < context.length(); i++) {
			char c = feature.charAt(i);

			// tail
			if (c == '-') {
				if (feature.charAt(feature.length() - 1) == context.charAt(context.length() - 1)) {
					return true;
				} else {
					return false;
				}
			}

			// numbers
			else if (c >= '0' && c <= '9') {
				int number = c - '0';

				// whether the next character is a number?
				char next = ' ';
				if (i + 1 < feature.length())
					next = feature.charAt(i + 1);

				if (next >= '0' && next <= '9') {
					number = 10 * number + (next - '0');

					i++;
				}

				pointer += number;

				if (pointer >= context.length()) {
					return false;
				}
			}

			// normal characters
			else {
				if (context.charAt(pointer) != c) {
					return false;
				}

				pointer++;
			}

			result = true;
		}

		return result;

	}

	/**
	 * generate the new fuzzy features from the context
	 * 
	 * Feature format: <c, >c
	 * 
	 * @param word
	 * @param sense
	 * @param context
	 * @param field
	 * @return
	 */

	public ArrayList<FeatParaBean> generateFuzzyInner(WordBean wordBean, int field) {
		ArrayList<FeatParaBean> result = new ArrayList<FeatParaBean>();

		int base = wordBean.index;
		String word = wordBean.word;
		String context = wordBean.context;
		String sense = wordBean.sense;

		int word_s = word.length();

		/*
		 * One character around the word
		 */

		for (int round = 1; round <= field; round++) {
			int fore = base - round;
			int back = base + round + word_s - 1;

			// is the indexes legal
			if (fore >= 0 && fore < context.length()) {
				String feature = "<" + context.charAt(fore);

				FeatParaBean bean = new FeatParaBean(word, sense, feature, true);

				result.add(bean);
			}

			if (back >= 0 && back < context.length()) {
				String feature = ">" + context.charAt(back);

				FeatParaBean bean = new FeatParaBean(word, sense, feature, true);

				result.add(bean);
			}
		}

		return result;
	}

	/**
	 * Compare the context with the feature (FUZZY) <c, >c
	 * 
	 * @param word
	 * @param context
	 * @param feature
	 * @return
	 */

	public boolean compareFuzzyFeature(String word, String context, int index, String feature) {
		if (!context.contains(word)) {
			return false;
		}

		int base = index;

		String back = context.substring(0, base);
		String fore = context.substring(base);

		if (feature.charAt(0) == '>') {
			if (fore.contains(feature.charAt(1) + "")) {
				return true;
			}
		} else if (feature.charAt(0) == '<') {
			if (back.contains(feature.charAt(1) + "")) {
				return true;
			}
		}

		return false;

	}

	/**
	 * Use Viterbi algorithm to determine the best path A is an identity matrix,
	 * generating by N dynamically
	 * 
	 * choose to consider the frequency of all characters (BAYESIAN)
	 * 
	 * @param N
	 * @param M
	 * @param B
	 * @param PI
	 * @return the best sense
	 */

	@SuppressWarnings("unchecked")
	public DisambResult Viterbi(int N, int M, HashMap<String, HashMap<UUID, Integer>> B, HashMap<String, Double> PI,
			ArrayList<UUID> O, ArrayList<String> senses) {
		DisambResult result = new DisambResult();

		// generate A matrix
		double[][] A = new double[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (i == j) {
					A[i][j] = 1;
				} else {
					A[i][j] = 0;
				}
			}
		}

		int T = O.size();

		if (T == 0) {
			result.data = (HashMap<String, Double>) PI.clone();

			return result;
		}

		double delta[][] = new double[T][N];
		double psi[][] = new double[T][N];

		/*
		 * for(int i = 0; i < N; i++) { System.out.print(PI.get(senses.get(i)) +
		 * " "); } System.out.println();
		 */

		/*
		 * Initialization
		 */

		for (int i = 0; i < N; i++) {
			String sense = senses.get(i);

			delta[0][i] = PI.get(sense) * B.get(sense).get(O.get(0));

			// System.out.println("B.get(sense).get(O.get(0)): " +
			// B.get(sense).get(O.get(0)));

			psi[0][i] = 0;
		}

		// printMatrix("delta", delta, T, N);
		// printMatrix("psi", psi, T, N);

		/*
		 * Recursion
		 */

		for (int t = 1; t < T; t++) {
			for (int j = 0; j < N; j++) {
				String sense = senses.get(j);

				double maxval = -Double.MAX_VALUE;
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

				delta[t][j] = maxval * B.get(sense).get(O.get(t));

				// System.out.printf("[%d][%d] B.get(sense).get(O.get(t)): %d",
				// t, j, B.get(sense).get(O.get(t)));

				psi[t][j] = maxvalind;
			}
		}

		// printMatrix("delta", delta, T, N);
		// printMatrix("psi", psi, T, N);

		/*
		 * Termination
		 */

		for (int i = 0; i < N; i++) {
			result.data.put(senses.get(i), delta[T - 1][i]);
		}

		// System.out.println("pprob: " + pprob);
		// System.out.println("log pprob: " + Math.log(pprob));

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
