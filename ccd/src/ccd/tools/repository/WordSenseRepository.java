package ccd.tools.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ccd.tools.InstanceFactory;
import ccd.tools.domain.WordBean;
import ccd.tools.domain.WordSensePr;
import ccd.tools.entity.EntityBase;
import ccd.tools.repository.base.IDataTools;

public class WordSenseRepository {

	public static String tableName = "wordsenses";
	IDataTools tools;

	public WordSenseRepository() {
		tools = InstanceFactory.newTools(tableName);
	}

	public HashMap<String, WordSensePr> initPrior() {
		HashMap<String, WordSensePr> result = new HashMap<String, WordSensePr>();

		List<EntityBase> entities = tools
				.get(String.format("SELECT * FROM %s ORDER BY `word` LIMIT 999999999999", tableName));
		for (EntityBase entity : entities) {
			String word = entity.get("word");
			if (!result.containsKey(word)) {
				result.put(word, new WordSensePr());
			}

			result.get(word).senses.add(entity.get("sense"));
			result.get(word).Prs.add(Double.parseDouble(entity.get("PrSk")));
		}

		return result;
	}

	public HashMap<String, Double> getSenses(String word) {
		HashMap<String, Double> result = new HashMap<String, Double>();

		List<EntityBase> entities = tools
				.get(String.format("SELECT * FROM %s WHERE `word` = '%s' LIMIT 999999999999", tableName, word));
		for (EntityBase entity : entities) {
			result.put(entity.get("sense"), Double.parseDouble(entity.get("PrSk")));
		}

		return result;
	}

	public double getPr(String word, String sense) {
		List<EntityBase> entities = tools
				.get(String.format("SELECT PrSk FROM %s WHERE `word` = '%s' AND `sense` = '%s' LIMIT 999999999999",
						tableName, word, sense));

		if (entities.isEmpty()) {
			return -1;
		} else {
			return Double.parseDouble(entities.get(0).get("PrSk"));
		}
	}

	public boolean insert(String word, String sense, double PrSk) {
		EntityBase entityBase = new EntityBase(tableName);
		entityBase.put("word", word);
		entityBase.put("sense", sense);
		entityBase.put("PrSk", PrSk + "");

		return tools.insert(entityBase);
	}

	public boolean insert(ArrayList<EntityBase> entities) {

		return tools.insert(entities);
	}
	
	public boolean delete(ArrayList<String> words){
		boolean result = true;
		
		if (words != null && !words.isEmpty()) {
			String wordStr = String.format("'%s'", String.join("', '", words));

			result = tools.delete(
					String.format("DELETE FROM %s WHERE `word` IN (%s)",
							tableName, wordStr));
		}

		return result;
	}

	public void truncate() {
		tools.remove();
	}
}
