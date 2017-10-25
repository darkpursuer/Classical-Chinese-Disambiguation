package ccd.tools.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ccd.tools.InstanceFactory;
import ccd.tools.entity.EntityBase;
import ccd.tools.repository.base.IDataTools;

public class SenseCharRepository {

	public static String tableName = "sensechars";
	IDataTools tools;

	public SenseCharRepository() {
		tools = InstanceFactory.newTools(tableName);
	}

	public HashMap<String, HashMap<String, Double>> getPrVj(String word) {
		HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();

		List<EntityBase> entities = tools.get(String.format("SELECT * FROM %s WHERE `word` = '%s' LIMIT 999999999999", tableName, word));

		HashMap<String, Double> tmp = null;
		for (EntityBase entity : entities) {
			String sense = entity.get("sense");

			if (!result.containsKey(sense)) {
				tmp = new HashMap<String, Double>();
				result.put(sense, tmp);
			}

			tmp.put(entity.get("char"), Double.parseDouble(entity.get("PrVj")));
		}
		return result;
	}

	public double getPr(String word, String sense, String v) {
		List<EntityBase> entities = tools
				.get(String.format("SELECT PrVj FROM %s WHERE `word` = '%s' AND `sense` = '%s' AND `char` = '%s' LIMIT 999999999999", tableName, word, sense, v));

		if (entities.isEmpty()) {
			return -1;
		} else {
			return Double.parseDouble(entities.get(0).get("PrVj"));
		}
	}

	public boolean insert(String word, String sense, String character, double PrVj) {
		EntityBase entityBase = new EntityBase(tableName);
		entityBase.put("word", word);
		entityBase.put("sense", sense);
		entityBase.put("char", character);
		entityBase.put("PrVj", PrVj + "");

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
	
	public void truncate(){
		tools.remove();
	}
}
