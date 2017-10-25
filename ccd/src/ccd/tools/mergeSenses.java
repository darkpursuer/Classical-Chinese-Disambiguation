package ccd.tools;

import java.util.ArrayList;

import ccd.tools.domain.WordBean;
import ccd.tools.repository.WordSourceRepository;
import ccd.tools.repository.base.SQLTools;

public class mergeSenses {

	public static void main(String[] args) {
		WordSourceRepository wordSourceRepository = new WordSourceRepository();
		ArrayList<WordBean> wordsource = wordSourceRepository.getAllWordBeans();

		int[] blacklist = {};

		String[] wordblacklist = { "��", "��", "��", "��", "��", "ΰ", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "׼",
				"��", "�\", "ɥ", "Ϊ", "��", "��", "��", "��", "��", "��", "ӡ", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��",
				"��", "��", "�", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "ī", "��", "��", "��", "��", "��", "��", "��",
				"��", "��", "��", "��", "ʵ", "�", "��", "��", "��", "��", "С", "��", "��", "չ", "��", "��", "��", "��", "��", "��", "��",
				"��", "ƽ", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��",
				"��", "��", "��", "��", "��", "��", "�", "��", "��", "�x", "�F", "�", "����", "�", "��", "�", "ĵ", "��", "Ϭ", "��",
				"�", "��", "��", "��", "��", "��", "ֱ", "��", "��", "˶", "��", "��", "��", "ά", "�", "��", "��", "��", "��", "��", "��",
				"��", "��", "Ñ", "Ċ", "��", "��", "��", "��", "��", "��", "��", "ݷ", "��", "��", "��" };

		String curword = wordsource.get(0).getWord();
		int start = 0;
		ArrayList<String> updatedsense = new ArrayList<String>();

		for (int i = 0; i < wordsource.size(); i++) {
			WordBean bean = wordsource.get(i);
			String word = bean.getWord();

			// judge whether is a different word
			if (!word.equals(curword)) {
				// a new word

				// handle the word and senses
				for (int j = start; j < i; j++) {
					WordBean basebean = wordsource.get(j);

					String basesense = basebean.getSense();

					// if the sense has been merged
					if (updatedsense.contains(basesense)) {
						// continue;
					}

					s: for (int k = start; k < i; k++) {
						WordBean targetbean = wordsource.get(k);

						String targetsense = targetbean.getSense();

						// if the sense is the same
						if (targetsense.equals(basesense)) {
							continue;
						}

						// if the sense has been merged
						if (updatedsense.contains(targetsense)) {
							continue;
						}

						for (int bi = 0; bi < blacklist.length; bi++) {
							if (blacklist[bi] == Integer.parseInt(targetbean.getNo())) {
								continue s;
							}
						}

						for (int bi = 0; bi < wordblacklist.length; bi++) {
							if (wordblacklist[bi].equals(targetbean.getWord())) {
								continue s;
							}
						}

						double max = MaxStringMerge.LCS(basesense, targetsense);
						double shorter = Math.min(basesense.length(), targetsense.length());

						// if the base sense contains the target sense
						// if(basesense.contains(targetsense))
						if (max / shorter >= 0.6) {
							// update the target sense to base sense
							String no = targetbean.getNo();

							String modify;
							if (basesense.length() >= targetsense.length()) {
								modify = basesense;
							} else {
								modify = targetsense;
							}

//							String command = "UPDATE wordsourcecontexts SET `sense`='" + modify + "' WHERE `No`='" + no + "';";
							// tools.executeSQL(command);
							
							targetbean.sense = modify;
							wordSourceRepository.update(targetbean);

							System.out.printf("word: %s, basesense: %s, targetsense: %s, No: %s\n", curword, basesense,
									targetsense, no);
//							System.out.println(command);

							// update the target sense in the updated list
							updatedsense.add(targetsense);

						}

					}
				}

				// clear the updated sense list
				updatedsense.clear();

				// update the current word and the start index
				curword = word;
				start = i;
			}

		}
	}

}
