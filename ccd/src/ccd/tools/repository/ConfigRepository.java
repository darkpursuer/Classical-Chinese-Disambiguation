package ccd.tools.repository;

import java.util.ArrayList;
import java.util.List;

import ccd.tools.InstanceFactory;
import ccd.tools.domain.TrainingHistoryBean;
import ccd.tools.entity.EntityBase;
import ccd.tools.repository.base.IDataTools;

public class ConfigRepository {

	public static String tableName = "configs";
	IDataTools tools;

	public ConfigRepository() {
		tools = InstanceFactory.newTools(tableName);
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

	private final static String TrainingKeyHeader = "trainrecord_";
	public final static String LatestTrainingKey = "latestTrainingNo";

	public boolean updateTrainingProgress(TrainingHistoryBean bean) {
		String key = TrainingKeyHeader + bean.no;
		return set(key, bean.toString());
	}

	public boolean addTrainingHistory(TrainingHistoryBean bean) {
		String key = TrainingKeyHeader + bean.no;

		EntityBase entityBase = new EntityBase();
		entityBase.put("key", key);
		entityBase.put("value", bean.toString());

		boolean result = tools.insert(entityBase);

		result &= set(LatestTrainingKey, bean.no + "");
		return result;
	}

	public TrainingHistoryBean getLatestTrainingHistory() {
		String no = get(LatestTrainingKey);
		return getTrainingHistory(no);
	}

	public TrainingHistoryBean getTrainingHistory(String no) {
		TrainingHistoryBean result = null;

		List<EntityBase> entities = tools
				.get(String.format("SELECT * FROM %s WHERE `key` = '%s'", tableName, TrainingKeyHeader + no));

		if (!entities.isEmpty())
			result = new TrainingHistoryBean(entities.get(0).get("value"));

		return result;
	}

	public ArrayList<TrainingHistoryBean> getTrainingHistoryList() {
		ArrayList<TrainingHistoryBean> result = new ArrayList<TrainingHistoryBean>();

		List<EntityBase> entities = tools
				.get(String.format("SELECT * FROM %s WHERE `key` LIKE '%s'", tableName, TrainingKeyHeader + "%"));

		for (EntityBase entityBase : entities) {
			TrainingHistoryBean tmp = new TrainingHistoryBean(entityBase.get("value"));

			result.add(tmp);
		}

		return result;
	}
}
