package ccd.crf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import ccd.tools.domain.DisambResult;
import ccd.tools.domain.FeatParaBean;
import ccd.tools.domain.WordSensePr;
import ccd.tools.repository.FeatureRepository;
import ccd.tools.repository.ParameterRepository;

public class CRFClassifier {

	private double prior = 10;

	// word, senses, Prs
	private HashMap<String, WordSensePr> wordsensePr;

	private CRFTools tools;

	private FeatureRepository featureRepository;
	private ParameterRepository parameterRepository;

	private static HashMap<String, HashMap<String, Double>> piBuffer;
	private static HashMap<String, HashMap<UUID, String>> featureBuffer;
	private static HashMap<String, ArrayList<FeatParaBean>> paraBuffer;

	static {
		piBuffer = new HashMap<>();
		featureBuffer = new HashMap<>();
		paraBuffer = new HashMap<>();
	}

	public CRFClassifier(HashMap<String, WordSensePr> wordsensePr, double prior) {
		this.wordsensePr = wordsensePr;
		this.prior = prior;

		tools = new CRFTools();

		featureRepository = new FeatureRepository();
		parameterRepository = new ParameterRepository();
	}

	public static void reset() {
		piBuffer = new HashMap<>();
		featureBuffer = new HashMap<>();
		paraBuffer = new HashMap<>();
	}

	/**
	 * Disambiguate one word sense in its context
	 * 
	 * @return the sense list with the highest probability
	 */

	public DisambResult Disambiguate(String word, String context, int wordIndex) {
		if (!wordsensePr.containsKey(word)) {
			System.err.println("No such word " + word + " in the database!");
			return null;
		}

		// sense, index, parameter
		HashMap<String, HashMap<UUID, Integer>> B = new HashMap<String, HashMap<UUID, Integer>>();

		// sense, PrSk
		ArrayList<String> sensebuffer = wordsensePr.get(word).senses;

		HashMap<String, Double> PI;
		if (piBuffer.containsKey(word)) {
			PI = piBuffer.get(word);
		} else {
			PI = new HashMap<String, Double>();

			ArrayList<Double> prbuffer = wordsensePr.get(word).Prs;

			for (int i = 0; i < sensebuffer.size(); i++) {
				PI.put(sensebuffer.get(i), prbuffer.get(i));
			}

			piBuffer.put(word, PI);
		}

		// long pit = System.currentTimeMillis();

		// Get the feature list of the word
		HashMap<UUID, String> featurelist;
		if (featureBuffer.containsKey(word)) {
			featurelist = featureBuffer.get(word);
		} else {
			featurelist = featureRepository.getFeatureList(word);
			featureBuffer.put(word, featurelist);
		}

		int N = sensebuffer.size();

		// get the parameters from the database
		ArrayList<FeatParaBean> paraList;
		if (paraBuffer.containsKey(word)) {
			paraList = paraBuffer.get(word);
		} else {
			paraList = parameterRepository.getParametersList(word);
			paraBuffer.put(word, paraList);
		}

		// long databaset = System.currentTimeMillis();

		// generate the Parameters matrix
		for (String s : sensebuffer) {
			HashMap<UUID, Integer> senseParas = new HashMap<UUID, Integer>();

			Iterator<Entry<UUID, String>> iterP = featurelist.entrySet().iterator();
			while (iterP.hasNext()) {
				Entry<UUID, String> entry = (Entry<UUID, String>) iterP.next();
				UUID index = entry.getKey();

				senseParas.put(index, 0);
			}

			B.put(s, senseParas);
		}

		// insert the existing features into the matrix, INITIALIZING
		for (FeatParaBean fp : paraList) {
			B.get(fp.sense).put(fp.feature, fp.parameter);
		}

		// long constructBt = System.currentTimeMillis();

		// get the corresponding features
		HashMap<UUID, String> features = tools.findFeatures(featurelist, word, context, wordIndex, CRFTraining.TYPE);

		// create the new parameters in the matrix
		ArrayList<UUID> O = new ArrayList<UUID>();
		O.addAll(features.keySet());

		int M = features.size();

		// long ft = System.currentTimeMillis();

		DisambResult result = tools.Viterbi(N, M, B, PI, O, sensebuffer);

		// long disambt = System.currentTimeMillis();

		// double total = disambt - st;
		//
		// double pid = pit - st;
		// double dd = databaset - pit;
		// double bd = constructBt - databaset;
		// double fd = ft - constructBt;
		// double disd = disambt - ft;
		// System.out.printf("Total time: %d ms; PI: %.2f%%; Database: %.2f%%;
		// B: %.2f%%; Feature: %.2f%%; Dis: %.2f%%.\n",
		// (int) total, pid / total, dd / total, bd / total, fd / total, disd /
		// total);

		return result;
	}
}
