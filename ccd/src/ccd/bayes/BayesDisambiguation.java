package ccd.bayes;

import java.util.ArrayList;
import java.util.HashMap;

import ccd.TestAmbiguous;
import ccd.tools.domain.WordSensePr;
import ccd.tools.repository.SenseCharRepository;
import ccd.tools.repository.WordSenseRepository;
import ccd.tools.repository.WordSourceRepository;

public class BayesDisambiguation {

	// maybe the posterior is too large so we need a prior parameter to balance
	// it
	private static double prior = 10;
	// private static SharedTools st;

	// word, senses, Prs
	private HashMap<String, WordSensePr> wordsensePr;
	private static HashMap<String, Float> empty;

	// sense, char, PrVj
	private HashMap<String, HashMap<String, Double>> chars;
	private HashMap<String, Double> PrVjs;

	private SenseCharRepository senseCharRepository;
	private WordSourceRepository wordSourceRepository;
	private WordSenseRepository wordSenseRepository;

	public BayesDisambiguation(HashMap<String, WordSensePr> wordsensePr, HashMap<String, Float> empty, double prior) {
		// this.st = st;
		this.wordsensePr = wordsensePr;
		this.empty = empty;
		this.prior = prior;

		senseCharRepository = new SenseCharRepository();
		wordSourceRepository = new WordSourceRepository();
		wordSenseRepository = new WordSenseRepository();
	}

	public ArrayList<String> Disambiguation(String word, String context) {
		if (!wordSourceRepository.isWordExists(word)) {
			System.err.println("No such word in the database!");
			return null;
		}

		// word, sense, PrSk
		WordSensePr senses = wordsensePr.get(word);
		ArrayList<String> senseSet = senses.senses;
		int sensenum = senseSet.size();
		double score[] = new double[sensenum];

		// word, sense, char, PrVj
		chars = senseCharRepository.getPrVj(word);

		for (int i = 0; i < sensenum; i++) {
			String sense = senseSet.get(i);

			// System.out.printf("W: %s, S: %s, PrSk:
			// %f\n",word,sense,getPrSk(word, sense));

			score[i] = prior * Math.log(senses.Prs.get(i));

			PrVjs = chars.get(sense);
			double PrCSk = PrCSk_Algorithm_1(word, sense, context);

			// System.out.printf("W: %s, S: %s, PrCSk: %f\n",word,sense,PrCSk);

			score[i] += PrCSk;
		}

		ArrayList<Integer> maxindex = new ArrayList<Integer>();

		maxindex.add(0);

		for (int i = 1; i < sensenum; i++) {
			double max = score[maxindex.get(0)];
			if (score[i] > max) {
				maxindex.clear();
				maxindex.add(i);
			} else if (score[i] == max) {
				maxindex.add(i);
			}
		}

		ArrayList<String> result = new ArrayList<String>();

		for (int i = 0; i < maxindex.size(); i++) {
			// System.out.println("±¾´ÊÓïµÄ½âÊÍ£º "+senses[maxindex.get(i)]);
			result.add(senseSet.get(maxindex.get(i)));
		}

		// System.out.println("N * (M + 1) = "+ (sensenum * (context.length() +
		// 1)));

		// tools.closePool();
		return result;
	}

	private double getPrVj(String word, String sense, char v) {
		// String command = "select PrVj from sensechars where word = '"+word+"'
		// and sense = '"+sense+"' and `char` = '"+v+"'";
		// System.out.println(command);
		// double result = tools.getPr(command);
		// double result = tools.getPr(word, sense, v+"");

		double result;

		// System.out.println(v);

		if (!PrVjs.containsKey(v + "")) {
			// if PrVj = 0
			result = empty.get(word);
			// System.out.println(word+" "+sense+" "+v+" "+result);
		} else {
			result = PrVjs.get(v + "");
		}

		return result;
	}

	// P(c|Sk) = P(v0|Sk)*P(v1|Sk)*...*P(vN|Sk)

	private double PrCSk_Algorithm_1(String word, String sense, String context) {
		double result = 0;

		int index = context.indexOf(word);

		int pre, post;
		int cl = context.length();
		int wl = word.length();

		int tmpi = index - TestAmbiguous.WINDOWSIZE;
		pre = (tmpi > 0) ? tmpi : 0;

		tmpi = index + wl + TestAmbiguous.WINDOWSIZE;
		post = (tmpi < cl) ? tmpi : cl;

		String tmp = context.substring(pre, index) + context.substring(index + wl, post);
		// System.out.println(tmp);

		for (int i = 0; i < tmp.length(); i++) {
			char v = tmp.charAt(i);

			double PrVj = 0;
			try {
				PrVj = getPrVj(word, sense, v);
			} catch (Exception e) {
				PrVj = -1;
			}

			if (PrVj == -1)
				continue;

			result += Math.log(PrVj);
		}

		return result;
	}
}
