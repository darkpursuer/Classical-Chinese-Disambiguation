package ccd.tools.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ccd.tools.InstanceFactory;
import ccd.tools.entity.EntityBase;
import ccd.tools.repository.base.IDataTools;

public class WordEmptyRepository {

	public static String tableName = "wordempties";
	IDataTools tools;

	public WordEmptyRepository() {
		tools = InstanceFactory.newTools(tableName);
	}
	
	public HashMap<String, Float> initEmpty()
	{
		HashMap<String, Float> result = new HashMap<String, Float>();
		
		List<EntityBase> entities = tools.get(String.format("SELECT * FROM %s ORDER BY `word` LIMIT 999999999999", tableName));
		for (EntityBase entity : entities) {
			String word = entity.get("word");
			result.put(word, Float.parseFloat(entity.get("PrEmpty")));
		}
		
		return result;
	}

	public boolean insert(String word, float PrEmpty) {
		EntityBase entityBase = new EntityBase(tableName);
		entityBase.put("word", word);
		entityBase.put("PrEmpty", PrEmpty + "");

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
