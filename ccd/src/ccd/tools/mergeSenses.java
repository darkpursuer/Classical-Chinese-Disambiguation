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

		String[] wordblacklist = { "ÏÂ", "ÊÀ", "ºõ", "ÀÖ", "Óè", "Î°", "µ«", "×÷", "¶Ò", "¾¤", "Èë", "¹«", "ĞË", "Æä", "¹Ú", "·ë", "×¼",
				"º¯", "„\", "É¥", "Îª", "ÔÆ", "ÁĞ", "Ôò", "ÛÀ", "»ª", "×ä", "Ó¡", "¼´", "À÷", "ØÊ", "³ø", "¼°", "ÓÒ", "ºó", "·ñ", "ÖÜ", "Ãü",
				"ºÍ", "ÍÙ", "à¢", "ËÃ", "Ïù", "Çô", "»Ø", "¹Ì", "ÆÔ", "àö", "¼á", "·Ø", "Ğæ", "Ä«", "Éù", "ÏÄ", "Ùí", "·ò", "ÒÄ", "ŞÉ", "°Â",
				"Èç", "Òö", "ÃÄ", "±¦", "Êµ", "å·", "Èİ", "ÊÙ", "·â", "½«", "Ğ¡", "¶û", "Çü", "Õ¹", "ÍÀ", "åï", "ÂÄ", "Ëê", "ÇÉ", "´ø", "³£",
				"¸É", "Æ½", "Ó×", "¹ã", "Êü", "ºë", "µÜ", "ÃÖ", "¹é", "åç", "»³", "»ó", "Êù", "³É", "Ëù", "²Å", "ÖÇ", "êÂ", "·ş", "Öì", "ÕÈ",
				"¼«", "Õí", "èş", "°ñ", "š", "›", "ä½", "Ìé", "Àì", "x", "F", "åª", "ÁÒÁÒ", "ë¼", "¾ô", "ë»", "Äµ", "ÄÁ", "Ï¬", "¶À",
				"â¢", "Àí", "Ğó", "Êè", "½Ô", "µÁ", "Ö±", "Ïà", "²‰", "Ë¶", "Æí", "×İ", "¼¨", "Î¬", "ç·", "ÕÀ", "¸¿", "Øè", "Ğß", "´ä", "´Ï",
				"·Ê", "ËÁ", "Ã‘", "ÄŠ", "³¼", "Éá", "ÖÛ", "ÜÀ", "ËÕ", "Üé", "ÈÙ", "İ·", "İÏ", "Ãï", "Êß" };

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
