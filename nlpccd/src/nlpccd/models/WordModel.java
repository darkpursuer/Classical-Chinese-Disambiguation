package nlpccd.models;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import ccd.tools.domain.WordBean;

public class WordModel {

	public WordModel() {
		senses = new ArrayList<>();
		contexts = new HashMap<>();
		indexes = new HashMap<>();
		nos = new HashMap<>();
	}

	@JsonProperty
	public String word;

	@JsonProperty
	public ArrayList<String> senses;

	@JsonProperty
	public HashMap<String, ArrayList<String>> contexts;

	@JsonProperty
	public HashMap<String, ArrayList<Integer>> indexes;

	@JsonProperty
	public HashMap<String, ArrayList<String>> nos;

	public static WordModel convertTo(ArrayList<WordBean> beans) {
		WordModel model = new WordModel();

		for (WordBean wordBean : beans) {
			if (model.word == null) {
				model.word = wordBean.word;
			} else if (!model.word.equals(wordBean.word)) {
				continue;
			}

			try {
				if (model.senses.contains(wordBean.sense)) {
					model.contexts.get(wordBean.sense).add(wordBean.context);
					model.indexes.get(wordBean.sense).add(wordBean.index);
					model.nos.get(wordBean.sense).add(wordBean.No);
				} else {
					model.senses.add(wordBean.sense);
					model.contexts.put(wordBean.sense, new ArrayList<String>());
					model.indexes.put(wordBean.sense, new ArrayList<Integer>());
					model.nos.put(wordBean.sense, new ArrayList<String>());

					model.contexts.get(wordBean.sense).add(wordBean.context);
					model.indexes.get(wordBean.sense).add(wordBean.index);
					model.nos.get(wordBean.sense).add(wordBean.No);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return model;
	}
}
