package nlpccd.services;

import java.util.List;

import ccd.tools.entity.EntityBase;
import ccd.tools.repository.base.IDataTools;
import ccd.tools.repository.base.SQLTools;

public class ConfigService {

	public static String tableName = "configs";
	IDataTools tools;

	public ConfigService() {
		tools = new SQLTools(tableName);
	}

	public String get(String key) {
		List<EntityBase> entities = tools
				.get(String.format("SELECT `value` FROM %s WHERE `key` = '%s'", tableName, key));

		String string = null;
		if (!entities.isEmpty())
			string = entities.get(0).get("value");

		return string;
	}

	public boolean set(String key, String value) {
		String getter = get(key);

		EntityBase entity = new EntityBase();
		entity.put("key", key);
		entity.put("value", value);

		boolean result = false;
		if (getter == null) {
			result = tools.insert(entity);
		} else {
			result = tools.modify(String.format("UPDATE %s SET # WHERE `key` = '%s'", tableName, key), entity);
		}
		return result;
	}
}
