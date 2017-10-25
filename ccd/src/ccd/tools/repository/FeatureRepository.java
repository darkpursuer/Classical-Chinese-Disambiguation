package ccd.tools.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ccd.tools.InstanceFactory;
import ccd.tools.domain.FeatParaBean;
import ccd.tools.entity.EntityBase;
import ccd.tools.repository.base.IDataTools;

public class FeatureRepository {

	public static String tableName = "features";
	IDataTools tools;

	public FeatureRepository() {
		tools = InstanceFactory.newTools(tableName);
	}

	public HashMap<UUID, String> getFeatureList(String word) {
		HashMap<UUID, String> result = new HashMap<UUID, String>();

		List<EntityBase> entities = tools
				.get(String.format("SELECT * FROM %s WHERE `word` = '%s' LIMIT 999999999999", tableName, word));

		for (EntityBase entity : entities) {
			result.put(UUID.fromString(entity.get("index")), entity.get("feature_str"));
		}

		return result;
	}

	public void insertFeatures(ArrayList<FeatParaBean> list) {
		ArrayList<String> fields = new ArrayList<>();
		fields.add("index");
		fields.add("word");
		fields.add("feature_str");
		tools.insert(EntityBase.convertAll(tableName, list, fields));
	}

	public boolean delete(ArrayList<String> words) {
		boolean result = true;

		if (words != null && !words.isEmpty()) {
			String wordStr = String.format("'%s'", String.join("', '", words));

			result = tools.delete(String.format("DELETE FROM %s WHERE `word` IN (%s)", tableName, wordStr));
		}

		return result;
	}

	public void truncate() {
		tools.remove();
	}
}
