package ccd.crf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.UUID;

import ccd.tools.domain.ICallback;
import ccd.tools.domain.TrainingHistoryBean;
import ccd.tools.domain.WordBean;
import ccd.tools.domain.WordSensePr;
import ccd.tools.repository.ConfigRepository;
import ccd.tools.repository.FeatureRepository;
import ccd.tools.repository.ParameterRepository;
import ccd.tools.repository.WordSenseRepository;
import ccd.tools.repository.WordSourceRepository;
import ccd.training.TrainingAnalysisPr;

public class CRFTraining extends Thread {

	private static final int FIELD = 4;
	public static final int TYPE = CRFTools.ACCURACY;

	public static final int RANDOMRANGE = 1000;
	public static final int BASICPATH = 1;
	public static final int JUMPPATH = 100;
	public static final int MAXCYCLE = 100;
	public static final int MAXTRAINPERIOD = 10000; // ms

	private static Lock maxLock = new ReentrantLock();

	/*
	 * Database Constants
	 */

	private ICallback callback;

	private ArrayList<String> trainedWords = null;

	private TrainingHistoryBean historyBean;

	private CRFTools tools;
	private TrainingAnalysisPr trainingAnalysisPr;

	private FeatureRepository featureRepository;
	private ParameterRepository parameterRepository;
	private WordSourceRepository wordSourceRepository;
	private WordSenseRepository wordSenseRepository;
	private ConfigRepository configRepository;

	public CRFTraining(ICallback callback) {
		tools = new CRFTools();
		trainingAnalysisPr = new TrainingAnalysisPr();

		featureRepository = new FeatureRepository();
		parameterRepository = new ParameterRepository();
		wordSourceRepository = new WordSourceRepository();
		wordSenseRepository = new WordSenseRepository();
		configRepository = new ConfigRepository();

		this.callback = callback;
	}

	public CRFTraining(ArrayList<String> trainedWords, ICallback callback) {
		this(callback);
		this.trainedWords = trainedWords;
	}

	public void run() {
		Training();
	}

	public void Training() {
		historyBean = configRepository.getLatestTrainingHistory();
		if (historyBean == null || historyBean.status != 1) {
			// if there is no SQL support or the latest training has been
			// finished
			historyBean = new TrainingHistoryBean();
			historyBean.no = 0;
		}

		try {
			/*
			 * Get the trained words
			 */
			ArrayList<WordBean> wordsource = null;
			if (trainedWords == null)
				wordsource = wordSourceRepository.getAllWordBeans();
			else
				wordsource = wordSourceRepository.get(trainedWords);

			/*
			 * Pre-process the corpus
			 */
			trainingAnalysisPr.AnalysisPr(trainedWords);

			historyBean.progress = 10;
			updateHistory();

			Algorithm1(wordsource);
		} catch (Exception e) {
			historyBean.error += "\n" + e.getMessage();
			updateHistory();
			return;
		}

		historyBean.progress = 100;
		historyBean.complete();
		updateHistory();

		if (callback != null) {
			callback.run();
		}
	}

	public void Algorithm1(ArrayList<WordBean> wordsource) {

		/*
		 * remove all the database
		 */
		if (trainedWords != null && !trainedWords.isEmpty()) {
			featureRepository.delete(trainedWords);
			parameterRepository.delete(trainedWords);
		} else {
			featureRepository.truncate();
			parameterRepository.truncate();
		}

		/*
		 * Get training data from the database
		 */
		HashMap<String, WordSensePr> prior = wordSenseRepository.initPrior();
		int totalCount = trainedWords != null && !trainedWords.isEmpty() ? trainedWords.size()
				: wordSourceRepository.getWordCount();

		/*
		 * Start Training
		 */

		ArrayList<HashMap<String, HashMap<UUID, Integer>>> paras = new ArrayList<HashMap<String, HashMap<UUID, Integer>>>();
		ArrayList<String> words = new ArrayList<String>();

		String curword = wordsource.get(0).getWord();
		String cursense = wordsource.get(0).getSense();

		// sense, index, parameter
		HashMap<String, HashMap<UUID, Integer>> B = new HashMap<String, HashMap<UUID, Integer>>();

		// sense, PrSk
		HashMap<String, Double> PI = new HashMap<String, Double>();

		// senses and contexts buffer
		ArrayList<WordBean> contexts = new ArrayList<WordBean>();

		// sense buffer
		ArrayList<String> sensebuffer = new ArrayList<String>();

		// features buffer
		HashMap<UUID, String> featurebuffer = new HashMap<>();

		WordSensePr sensePr = prior.get(curword);
		PI.put(cursense, sensePr.Prs.get(sensePr.senses.indexOf(cursense)));

		sensebuffer.add(cursense);

		int totalBeanCount = wordsource.size();
		int count = 0;
		boolean hasLast = true;

		for (int for_index = 0; for_index < totalBeanCount || hasLast; for_index++) {
			WordBean bean = null;
			String word = null;
			String sense = null;

			if (for_index < totalBeanCount) {
				bean = wordsource.get(for_index);
				word = bean.getWord();
				sense = bean.getSense();
			} else {
				hasLast = false;
				word = "%L%A%S%T%";
				sense = "%L%A%S%T%";
			}

			/*
			 * generate and find features parameters from the training data,
			 * paraList is the adding features
			 */
			if (bean != null)
				tools.generateFeatures(featurebuffer, bean, FIELD, TYPE);

			// The same word and sense, put into the same contexts
			if (curword.equals(word) && cursense.equals(sense)) {
				contexts.add(bean);
			}

			// The same word and different sense, put into the different B and
			// the same PI
			else if (curword.equals(word) && !cursense.equals(sense)) {
				cursense = sense;

				PI.put(cursense, sensePr.Prs.get(sensePr.senses.indexOf(cursense)));

				contexts.add(bean);

				sensebuffer.add(cursense);
			}

			// A different word, start Viterbi Algorithm
			else {
				/*
				 * First train the last word
				 */

				// Get the feature list of the word
				HashMap<UUID, String> featurelist = featureRepository.getFeatureList(curword);

				// showMatrix(B, featurelist, sensebuffer, feature_array);

				HashMap<WordBean, ArrayList<UUID>> OBuffer = new HashMap<WordBean, ArrayList<UUID>>();

				for (WordBean c : contexts) {

					// get the corresponding features
					HashMap<UUID, String> features = tools.findFeatures(featurelist, c.getWord(), c.getContext(),
							c.index, TYPE);

					// create the new parameters in the matrix
					ArrayList<UUID> O = new ArrayList<UUID>();

					Iterator<Entry<UUID, String>> iter = features.entrySet().iterator();

					while (iter.hasNext()) {
						Entry<UUID, String> entry = (Entry<UUID, String>) iter.next();
						UUID index = entry.getKey();

						O.add(index);
					}

					// FeatureBuffer.put(c, features);
					OBuffer.put(c, O);
				}

				// start training

				MaxMatrix max = new MaxMatrix();
				InnerTrain[] threads = new InnerTrain[Math.min(sensebuffer.size(), 5)];
				for (int i = 0; i < threads.length; i++) {
					threads[i] = new InnerTrain(max, sensebuffer, OBuffer, featurelist, contexts, PI);
				}
				for (InnerTrain thread : threads) {
					thread.start();
				}

				long st = System.currentTimeMillis();
				boolean isDone = false;
				while (!isDone) {
					if (System.currentTimeMillis() - st > MAXTRAINPERIOD) {
						B = max.matrix;

						System.out
								.println("Word " + contexts.get(0).word + " is stuck! The performance is: " + max.perf);
						historyBean.error += contexts.get(0).word + " is stuck! The performance is: " + max.perf + ". ";
						break;
					}

					if (Math.abs(max.perf - 1) < 0.00001) {
						B = max.matrix;
						break;
					}

					try {
						sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				for (InnerTrain thread : threads) {
					thread.interrupt();
				}

				boolean isAllInterrupted = false;
				while (!isAllInterrupted) {
					isAllInterrupted = threads[0].isInterrupted();
					for (int threadIndex = 1; threadIndex < threads.length; threadIndex++) {
						isAllInterrupted = isAllInterrupted && threads[threadIndex].isInterrupted();
					}
				}

				count++;

				historyBean.progress = 10 + (int) ((float) ((float) count / (float) totalCount) * 90);
				updateHistory();

				paras.add(B);
				words.add(curword);

				if (paras.size() >= 500) {
					parameterRepository.insertParaArray(paras, words);

					paras.clear();
					words.clear();
				}

				/*
				 * New word initialize
				 */

				if (hasLast) {
					curword = word;
					cursense = sense;

					// Parameters.clear();
					B = new HashMap<String, HashMap<UUID, Integer>>();
					PI.clear();
					contexts.clear();
					sensebuffer.clear();
					featurebuffer.clear();

					sensePr = prior.get(curword);
					PI.put(cursense, sensePr.Prs.get(sensePr.senses.indexOf(cursense)));
					contexts.add(bean);
					sensebuffer.add(cursense);
				}
			}
		}

		if (!paras.isEmpty())
			parameterRepository.insertParaArray(paras, words);
	}

	public void showMatrix(HashMap<String, HashMap<UUID, Integer>> B, HashMap<UUID, String> features,
			ArrayList<String> sensebuffer, ArrayList<UUID> O) {
		System.out.print("\t\t");
		for (UUID index : O) {
			System.out.print("[" + features.get(index) + "]\t");
		}
		System.out.println();

		for (String str : sensebuffer) {
			System.out.print("[" + str + "]\t\t");

			for (UUID index : O) {
				System.out.print(B.get(str).get(index) + "\t");
			}

			System.out.println();
		}
	}

	private boolean updateHistory() {
		if (historyBean.no != 0)
			return configRepository.updateTrainingProgress(historyBean);
		else
			return true;
	}

	class InnerTrain extends Thread {
		private MaxMatrix maxMatrix;
		ArrayList<String> sensebuffer;
		HashMap<WordBean, ArrayList<UUID>> OBuffer;
		HashMap<UUID, String> featurelist;
		ArrayList<WordBean> contexts;
		HashMap<String, Double> PI;

		private boolean shouldRun = true;

		public InnerTrain(MaxMatrix max, ArrayList<String> sensebuffer, HashMap<WordBean, ArrayList<UUID>> OBuffer,
				HashMap<UUID, String> featurelist, ArrayList<WordBean> contexts, HashMap<String, Double> PI) {
			maxMatrix = max;
			this.sensebuffer = sensebuffer;
			this.OBuffer = OBuffer;
			this.featurelist = featurelist;
			this.contexts = contexts;
			this.PI = PI;
		}

		public void run() {
			ArrayList<UUID> feature_array = new ArrayList<UUID>();

			/*
			 * construct B matrix
			 */

			HashMap<String, HashMap<UUID, Integer>> B = new HashMap<>();

			// generate the Parameters matrix
			for (String s : sensebuffer) {
				HashMap<UUID, Integer> senseParas = new HashMap<UUID, Integer>();

				B.put(s, senseParas);

				Iterator<Entry<UUID, String>> iterP = featurelist.entrySet().iterator();

				while (iterP.hasNext()) {
					Entry<UUID, String> entry = (Entry<UUID, String>) iterP.next();
					UUID index = entry.getKey();

					if (!feature_array.contains(index))
						feature_array.add(index);

					int random = (int) (Math.random() * RANDOMRANGE);
					B.get(s).put(index, random);
				}
			}

			int N = sensebuffer.size();
			int M = featurelist.size();
			int totalEntries = contexts.size();

			float performance = 0;

			int cycle = MAXCYCLE;
			ArrayList<Float> his = new ArrayList<>();

			s: while (shouldRun) {
				HashMap<String, ArrayList<UUID>> addParams = new HashMap<>();
				HashMap<String, ArrayList<UUID>> decreaseParams = new HashMap<>();
				int correctEntries = 0;

				for (WordBean c : contexts) {

					if (!shouldRun) {
						break s;
					}

					// create the new parameters in the matrix
					ArrayList<UUID> O = OBuffer.get(c);

					ArrayList<String> result = tools.Viterbi(N, M, B, PI, O, sensebuffer).getMax();

					// the result is equal to the training set, need to
					// modify the database
					if (result.size() == 1 && result.contains(c.getSense())) {
						correctEntries++;
					}

					// the result is wrong, then modify the parameters
					else {
						addParams.put(c.sense, O);
						for (String str : result) {
							if (!str.equals(c.getSense())) {
								decreaseParams.put(str, O);
							}
						}
					}
				}

				// save the recent performances
				performance = (float) correctEntries / (float) totalEntries;
				his.add(performance);

				// save the best B matrix and its performance
				if (performance > maxMatrix.perf) {
					maxLock.lock();
					try {
						if (performance > maxMatrix.perf) {
							// clone the B matrix
							maxMatrix.perf = performance;
							maxMatrix.copy(B);
						}
					} finally {
						maxLock.unlock();
					}
				}

				if (Math.abs(performance - 1) < 0.00001) {
					break;
				}

				cycle--;
				int pace = BASICPATH;

				// if the matrix has been in local maximum, then jump to
				// another area
				if (cycle <= 0) {
					pace = JUMPPATH;
					cycle = MAXCYCLE;
				}

				HashMap<UUID, Integer> p;

				// add to the correct parameters by pace
				Iterator<Entry<String, ArrayList<UUID>>> iterator = addParams.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, ArrayList<UUID>> entry = iterator.next();

					p = B.get(entry.getKey());
					ArrayList<UUID> tmp = entry.getValue();

					for (UUID index : tmp) {
						if (pace != BASICPATH) {
							int random = (int) (Math.random() * pace);
							p.put(index, (p.get(index) + random));
						} else {
							p.put(index, (p.get(index) + pace));
						}
					}
				}

				// decrease every wrong choice by pace
				iterator = decreaseParams.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, ArrayList<UUID>> entry = iterator.next();

					p = B.get(entry.getKey());
					ArrayList<UUID> tmp = entry.getValue();

					for (UUID index : tmp) {
						if (pace != BASICPATH) {
							int random = (int) (Math.random() * pace);
							p.put(index, (p.get(index) - random));
						} else {
							p.put(index, (p.get(index) - pace));
						}
					}
				}
			}
			isInterrupted = true;
		}

		public void interrupt() {
			shouldRun = false;
		}

		private boolean isInterrupted = false;

		public boolean isInterrupted() {
			return isInterrupted;
		}
	}

	class MaxMatrix {
		public float perf;
		public HashMap<String, HashMap<UUID, Integer>> matrix;

		public MaxMatrix() {
			perf = 0;
			matrix = new HashMap<>();
		}

		public void copy(HashMap<String, HashMap<UUID, Integer>> matrix) {
			this.matrix = new HashMap<>();
			Iterator<Entry<String, HashMap<UUID, Integer>>> iterator = matrix.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, HashMap<UUID, Integer>> entry = iterator.next();

				String key = entry.getKey();
				HashMap<UUID, Integer> map = new HashMap<>();
				this.matrix.put(key, map);

				Iterator<Entry<UUID, Integer>> eIterator = entry.getValue().entrySet().iterator();
				while (eIterator.hasNext()) {
					Entry<UUID, Integer> eEntry = eIterator.next();
					map.put(eEntry.getKey(), eEntry.getValue());
				}
			}
		}
	}
}
