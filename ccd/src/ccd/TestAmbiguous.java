package ccd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import ccd.bayes.BayesDisambiguation;
import ccd.bayes.BayesEmptyDisambiguation;
import ccd.tools.Serializer;
import ccd.tools.domain.WordSensePr;
import ccd.tools.repository.WordEmptyRepository;
import ccd.tools.repository.WordSenseRepository;

public class TestAmbiguous {

	/*
	 * The pattern of test file:
	 * 
	 * [ambiguous word]%[context]%[real sense]
	 * 
	 * 
	 * The pattern of output result:
	 * 
	 * [ambiguous word]: [context] [analysis sense] [real sense] [cost time]
	 * 
	 * 
	 */

	private final static int FROM_DATABASE = 1001;
	private final static int FROM_FILE = 1002;

	public final static int WINDOWSIZE = 4;

	private final static String FILENAME = "Prior.ser";

	private static int prior_mode = FROM_DATABASE;

	private static HashMap<String, WordSensePr> prior;
	private static HashMap<String, Float> empty;

	private static WordSenseRepository wordSenseRepository;
	private static WordEmptyRepository wordEmptyRepository;

	@SuppressWarnings("unchecked")
	public static void Disambiguous(double prior_ratio) {
		// server initialize
		// long s = System.currentTimeMillis();
		wordSenseRepository = new WordSenseRepository();
		wordEmptyRepository = new WordEmptyRepository();

		if (prior_mode == FROM_DATABASE) {
			prior = wordSenseRepository.initPrior();
			empty = wordEmptyRepository.initEmpty();
		} else if (prior_mode == FROM_FILE) {
			prior = (HashMap<String, WordSensePr>) Serializer.deserialize(FILENAME);
		}

		BayesDisambiguation da = new BayesDisambiguation(prior, empty, prior_ratio);
		// CRFClassifier da = new CRFClassifier(tools, prior, prior_ratio);

		// System.out.println("Prior Probability Initialize Complete: " +
		// (System.currentTimeMillis()-s) + " ms");

		float total = 0;
		float correct = 0;
		float real = 0;
		float empty = 0;
		float realc = 0;
		float emptyc = 0;

		// start ambiguous
		try {
			FileReader file = new FileReader("");
			BufferedReader reader = null;
			reader = new BufferedReader(file);

			String line;
			ArrayList<String> result;

			while ((line = reader.readLine()) != null) {
				String[] tmp = line.split("%");

				long st = System.currentTimeMillis();

				result = da.Disambiguation(tmp[0], tmp[1]);

				long et = System.currentTimeMillis();
				long t = et - st;

				total++;
				if (BayesEmptyDisambiguation.isEmpty(tmp[0]))
					empty++;
				else
					real++;

				boolean c = false;
				for (String sense : result) {
					if (sense.contains(tmp[2]) || tmp[2].contains(sense)) {
						correct++;

						if (BayesEmptyDisambiguation.isEmpty(tmp[0]))
							emptyc++;
						else
							realc++;

						c = true;
						break;
					}
				}

				if (c) {
					continue;
				}

				System.out.printf("%s : %s\n", tmp[0], tmp[1]);
				System.out.printf("analysis sense: \n");
				for (String sense : result) {
					System.out.println("\t" + sense);
				}
				System.out.printf("real sense: %s\n", tmp[2]);
				System.out.printf("cost time: %d ms\n", t);
				System.out.println();

			}

			System.out.println(correct + " | " + total + ": " + correct / total);
			System.out.println(realc + " | " + real + ": " + realc / real);
			System.out.println(emptyc + " | " + empty + ": " + emptyc / empty);
			// System.out.println(empty);

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * //server shutdown if(prior_mode != FROM_FILE) {
		 * Serialization.serialize(prior, FILENAME); }
		 */
	}

	public static void main(String[] args) {
		TestAmbiguous.Disambiguous(50);
	}
}
