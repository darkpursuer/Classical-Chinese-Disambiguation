package ccd.training;

import java.util.*;

import ccd.tools.domain.WordBean;
import ccd.tools.entity.EntityBase;
import ccd.tools.repository.SenseCharRepository;
import ccd.tools.repository.WordEmptyRepository;
import ccd.tools.repository.WordSenseRepository;
import ccd.tools.repository.WordSourceRepository;

public class TrainingAnalysisPr {

	public static final String REALSOURCE = "wordsourcecontexts";
	public static final String REALSENSE = "wordsenses";
	public static final String REALCHAR = "sensechars";
	public static final String REALEMPTY = "wordempty";

	public static final String EMPTYSOURCE = "emptysource";
	public static final String EMPTYSENSE = "emptysense";
	public static final String EMPTYCHAR = "emptychars";
	public static final String EMPTYEMPTY = "emptyempty";

	public static final String SOURCE = REALSOURCE;
	public static final String SENSE = REALSENSE;
	public static final String CHARS = REALCHAR;
	public static final String EMPTY = REALEMPTY;

	private static int PGSIZE = 1000;

	private WordSenseRepository wordSenseRepository;
	private WordSourceRepository wordSourceRepository;
	private WordEmptyRepository wordEmptyRepository;
	private SenseCharRepository senseCharRepository;

	public TrainingAnalysisPr() {

		wordSenseRepository = new WordSenseRepository();
		wordSourceRepository = new WordSourceRepository();
		wordEmptyRepository = new WordEmptyRepository();
		senseCharRepository = new SenseCharRepository();
	}

	public void AnalysisPr(ArrayList<String> trainedWords) {
		try {

			AnalysisAlgorithm1(trainedWords);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void AnalysisAlgorithmWindowed() throws Exception {
		long start = System.currentTimeMillis();

		ArrayList<WordBean> wordsource = wordSourceRepository.getAllWordBeans();
		// tools.getWordsBean("SELECT * FROM " + SOURCE + " order by `word`,
		// `sense` limit 99999");

		System.out.println("Get From Database: " + (System.currentTimeMillis() - start) + " ms");

		// count the number of contexts
		for (WordBean bean : wordsource) {
			System.out.println(bean.getWord() + ": " + bean.getContext());
		}
	}

	private void AnalysisAlgorithm1(ArrayList<String> trainedWords) throws Exception {
		/*
		 * remove all the database
		 */
		if (trainedWords != null && !trainedWords.isEmpty()) {
			wordSenseRepository.delete(trainedWords);
			senseCharRepository.delete(trainedWords);
			wordEmptyRepository.delete(trainedWords);
		} else {
			wordSenseRepository.truncate();
			senseCharRepository.truncate();
			wordEmptyRepository.truncate();
		}

		/*
		 * new, more efficient methods
		 */

		long start = System.currentTimeMillis();

		ArrayList<WordBean> wordsource = null;
		if (trainedWords == null)
			wordsource = wordSourceRepository.getAllWordBeans();
		else
			wordsource = wordSourceRepository.get(trainedWords);

		System.out.println("Get From Database: " + (System.currentTimeMillis() - start) + " ms");

		String curword = wordsource.get(0).getWord();

		HashMap<String, Integer> contextnumber = new HashMap<String, Integer>();
		HashMap<String, Integer> sensenumber = new HashMap<String, Integer>();
		HashMap<String, String> wordcontexts = new HashMap<String, String>();
		HashMap<String, String> sensecontexts = new HashMap<String, String>();

		int count = 0;
		String allcontext = "";

		// count the number of contexts
		for (WordBean bean : wordsource) {
			String word = bean.getWord();
			String context = bean.getContext();
			int index = bean.getIndex();

			if (curword.equals(word)) {
				count++;

				if (index != -1) {
					String tmp = context.substring(0, index) + context.substring(index + word.length());
					allcontext += tmp;
				}
			} else {
				contextnumber.put(curword, count);
				wordcontexts.put(curword, allcontext);

				curword = word;
				count = 1;
				allcontext = "";

				if (index != -1) {
					String tmp = context.substring(0, index) + context.substring(index + word.length());
					allcontext += tmp;
				}
			}
		}
		contextnumber.put(curword, count);
		wordcontexts.put(curword, allcontext);

		// System.out.println(contextnumber.size());
		// System.out.println(wordsource.size());

		// calculate the PrSk
		count = 0;
		curword = wordsource.get(0).getWord();
		String cursense = wordsource.get(0).getSense();
		String allsensecontext = "";

		// count the N in ONE word
		HashMap<String, Integer> countSense = new HashMap<String, Integer>();
		int count_sense = 1;

		ArrayList<EntityBase> entities = new ArrayList<>();

		for (WordBean bean : wordsource) {
			String sense = bean.getSense();
			String word = bean.getWord();
			String context = bean.getContext();
			int index = bean.getIndex();

			if (cursense.equals(sense) && curword.equals(word)) {
				count++;

				if (index != -1) {
					String tmp = context.substring(0, index) + context.substring(index + word.length());
					allsensecontext += tmp;
				}
			} else {
				// insert the last sense
				float c = contextnumber.get(curword);

				float PrSk = (float) count / c;

				EntityBase entityBase = new EntityBase(WordSenseRepository.tableName);
				entityBase.put("word", curword);
				entityBase.put("sense", cursense);
				entityBase.put("PrSk", PrSk + "");
				entities.add(entityBase);

				sensecontexts.put(curword + cursense, allsensecontext);
				sensenumber.put(curword + cursense, count);
				// System.out.printf("W: %s, S: %s, Pr(Sk): %f\n", curword,
				// cursense, PrSk);

				cursense = sense;
				count = 1;
				allsensecontext = "";

				// increase the count of senses
				if (curword.equals(word)) {
					count_sense++;
				} else {
					countSense.put(curword, count_sense);

					count_sense = 1;
				}

				if (index != -1) {
					String tmp = context.substring(0, index) + context.substring(index + word.length());
					allsensecontext += tmp;
				}
			}

			curword = bean.getWord();
		}
		float c_tmp = contextnumber.get(curword);
		float PrSk_tmp = (float) count / c_tmp;

		EntityBase entityBase = new EntityBase(WordSenseRepository.tableName);
		entityBase.put("word", curword);
		entityBase.put("sense", cursense);
		entityBase.put("PrSk", PrSk_tmp + "");
		entities.add(entityBase);

		sensecontexts.put(curword + cursense, allsensecontext);
		sensenumber.put(curword + cursense, count);
		countSense.put(curword, count_sense);

		wordSenseRepository.insert(entities);

		// insert in the empty sets
		curword = wordsource.get(0).getWord();

		entities.clear();

		for (WordBean bean : wordsource) {
			String word = bean.getWord();

			if (!curword.equals(word)) {
				// put the empty character sense in the database
				entityBase = new EntityBase(WordEmptyRepository.tableName);
				entityBase.put("word", curword);
				entityBase.put("PrEmpty", defaultPrVj(countSense.get(curword)) + "");
				entities.add(entityBase);

				curword = word;
			}
		}

		wordEmptyRepository.insert(entities);

		// calculate the PrVj
		ArrayList<String> tested = new ArrayList<String>();
		curword = wordsource.get(0).getWord();
		cursense = wordsource.get(0).getSense();

		entities.clear();

		for (WordBean bean : wordsource) {
			String sense = bean.getSense();
			String word = bean.getWord();

			if (!cursense.equals(sense)) {
				tested.clear();
				cursense = sense;
			}

			String all = sensecontexts.get(word + sense);
			// System.out.printf("%s\n%s\n%s\n\n", word, sense, all);

			String v;
			for (int i = 0; i < all.length(); i++) {
				v = all.charAt(i) + "";

				if (!tested.contains(v)) {
					// CVj
					float CVj = getNumString(all, v);

					// CCSk
					float CCSk = sensenumber.get(word + sense);

					// CVjW
					float CVjW = getNumString(wordcontexts.get(word), v);

					// CW
					float CW = contextnumber.get(curword);

					float PrVj = calPrVj(CVj, CCSk, CVjW, CW);

					entityBase = new EntityBase(SenseCharRepository.tableName);
					entityBase.put("word", word);
					entityBase.put("sense", sense);
					entityBase.put("char", v);
					entityBase.put("PrVj", PrVj + "");
					entities.add(entityBase);

					tested.add(v);
				}
			}
		}

		senseCharRepository.insert(entities);

		/*
		 * Old Methods
		 */
		/*
		 * int wordnum = countword(); String[] words = st.getWords();
		 * 
		 * for(int i = 0; i < wordnum; i++) { String word = words[i];
		 * 
		 * String[] senses = st.getSenses(word); int sensenum = senses.length;
		 * 
		 * for(int j = 0; j < sensenum; j++) { String sense = senses[j];
		 * 
		 * //first calculate the PrSk
		 * 
		 * double PrSk = getPrSk(word, sense);
		 * 
		 * System.out.printf("W: %s, S: %s, Pr(Sk): %f\n", word, sense, PrSk);
		 * 
		 * insertPrSk(word, sense, PrSk);
		 * 
		 * 
		 * //then calculate the PrVj String[] contexts = st.getContexts(word,
		 * sense);
		 * 
		 * String allcontext = "";
		 * 
		 * for(int k = 0; k < contexts.length; k++) { String context =
		 * contexts[k];
		 * 
		 * int index = st.getindex(word, sense, context)[0];
		 * 
		 * String tmp = null;
		 * 
		 * try { tmp = context.substring(0, index) +
		 * context.substring(index+word.length()); }catch(Exception e) {
		 * continue; }
		 * 
		 * allcontext += tmp; }
		 * 
		 * //two array-lists to count characters and their count number
		 * ArrayList<Character> chars = new ArrayList<Character>();
		 * ArrayList<Integer> counts = new ArrayList<Integer>();
		 * 
		 * for(int k = 0; k < allcontext.length(); k++) { char v =
		 * allcontext.charAt(k);
		 * 
		 * if(chars.contains(v)) { int index = chars.indexOf(v); int countnum =
		 * counts.get(index); countnum++; counts.set(index, countnum); } else {
		 * //if character is not existed in the list, then new one chars.add(v);
		 * counts.add(1); } }
		 * 
		 * for(int k = 0; k < chars.size(); k++) { double PrVj =
		 * (double)counts.get(k) / (double)contexts.length; insertPrVj(word,
		 * sense, chars.get(k), PrVj); }
		 * 
		 * } }
		 */
	}

	/**
	 * Get the count times of one word in the context
	 * 
	 * @param context
	 * @param word
	 * @return count times
	 */

	private int getNumString(String context, String word) {
		int count = 0;
		int index;
		String tmp = context + "";

		while ((index = tmp.indexOf(word)) != -1) {
			tmp = tmp.substring(index + word.length());

			count++;
		}

		return count;
	}

	/**
	 * Calculate the PrVj of each character
	 * 
	 * @param CVj:
	 *            the count number of one character appearing in all of the
	 *            training contexts in ONE SENSE
	 * @param CCSk:
	 *            the count number of the contexts in ONE SENSE
	 * @param CVjW:
	 *            the count number of one character appearing in all of the word
	 *            senses contexts
	 * @param CW:
	 *            the count number of all senses contexts in the word
	 * @return PrVj
	 */

	private float calPrVj(float CVj, float CCSk, float CVjW, float CW) {
		if (CCSk == CW) {
			return (float) (Math.pow(CVj, 2) / (CCSk * CVjW));
		} else {
			return (float) (Math.pow(CVj, 2) / (CCSk * CVjW) * Math.pow(Math.E, -((CVjW - CVj) / (CW - CCSk))));
		}
	}

	/**
	 * Calculate the character probabilities which is not appear in training
	 * sets
	 * 
	 * @param N
	 * @return default PrVj
	 */
	private float defaultPrVj(float N) {
		return calPrVj(1, 1, N, N);
	}

	private void AnalysisAlgorithm2() throws Exception {
		ArrayList<WordBean> allWords = wordSourceRepository.getAllWordBeans();

		// count the C(w)
		HashMap<String, Integer> Wordcounts = new HashMap<String, Integer>();

		// count the C(Sk)
		HashMap<WordSense, Integer> Sensecounts = new HashMap<WordSense, Integer>();

		// count the C(Sk|Vj)
		HashMap<SenseV, Integer> sensev = new HashMap<SenseV, Integer>();

		int size = allWords.size();

		for (int i = 0; i < size; i++) {
			WordBean tmp = allWords.get(i);

			String word = tmp.getWord();

			WordSense sense = new WordSense(word, tmp.getSense());

			if (Wordcounts.containsKey(word)) {
				// put in C(w)
				int count = Wordcounts.get(tmp.getWord());
				count++;

				Wordcounts.put(word, count);

				// put in C(Sk)
				if (Sensecounts.containsKey(sense)) {
					count = Sensecounts.get(sense);
					count++;

					Sensecounts.put(sense, count);
				} else {
					Sensecounts.put(sense, 1);
				}

			} else {
				// put in C(w)
				Wordcounts.put(word, 1);

				// put in C(Sk)
				Sensecounts.put(sense, 1);

			}

			// put in C(Sk|Vj)

			int index = tmp.getIndex();
			String context = tmp.getContext();

			String purecontext = null;
			try {
				purecontext = context.substring(0, index) + context.substring(index + word.length());
			} catch (Exception e) {
				continue;
			}

			for (int j = 0; j < purecontext.length(); j++) {
				SenseV chars = new SenseV(word, tmp.getSense(), purecontext.charAt(j));

				if (sensev.containsKey(chars)) {
					int count = sensev.get(chars);
					count++;

					sensev.put(chars, count);
				} else {
					sensev.put(chars, 1);
				}
			}
		}

	}

	// private static void insertPrVj(String word, String sense, char v, double
	// PrVj) {
	// senseCharRepository.insert(word, sense, v + "", PrVj);
	// }
	//
	// private static void insertPrSk(String word, String sense, double PrSk) {
	// wordSenseRepository.insert(word, sense, PrSk);
	// }
	//
	// private static double getPrSk(String word, String sense) {
	// double result = (double) countSk(word, sense) / (double)
	// countAllWord(word);
	//
	// return result;
	// }
	//
	// private static int countSk(String word, String sense) {
	// String command = "select count(sense) from wordsourcecontexts " + "where
	// word = '" + word + "' and sense = '"
	// + sense + "'";
	//
	// return tools.getcount(command);
	// }
	//
	// private static int countAllWord(String word) {
	// String command = "select count(word) from wordsourcecontexts " + "where
	// word = '" + word + "'";
	//
	// return tools.getcount(command);
	// }
	//
	// private static int countsense(String word) {
	// String command = "select count(distinct sense) from wordsourcecontexts "
	// + "where word = '" + word + "'";
	//
	// return tools.getcount(command);
	// }
	//
	// private static int countword() {
	// String command = "select count(distinct word) from wordsourcecontexts";
	//
	// return tools.getcount(command);
	// }

}

class WordSense {
	public String word;
	public String sense;

	public WordSense(String word, String sense) {
		this.word = word;
		this.sense = sense;
	}
}

class SenseV {
	public String word;
	public String sense;
	public char v;

	public SenseV(String word, String sense, char v) {
		this.word = word;
		this.sense = sense;
		this.v = v;
	}
}
