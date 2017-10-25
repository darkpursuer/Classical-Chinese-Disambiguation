package ccd.tools.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ccd.tools.InstanceFactory;
import ccd.tools.domain.PageResult;
import ccd.tools.domain.TestEntry;
import ccd.tools.domain.TestFileBean;
import ccd.tools.entity.EntityBase;
import ccd.tools.repository.base.IDataTools;

public class TestFileRepository {

	public static String mainTableName = "testfiles";
	public static String entryTableName = "testentries";
	IDataTools mainTools, entryTools;

	public TestFileRepository() {
		mainTools = InstanceFactory.newTools(mainTableName);
		entryTools = InstanceFactory.newTools(entryTableName);
	}

	public boolean insert(TestFileBean bean) {
		EntityBase main = bean.getMainEntity();
		ArrayList<EntityBase> entries = bean.getEntriesEntities();

		boolean result = mainTools.insert(main);
		result &= entryTools.insert(entries);

		return result;
	}

	public TestFileBean get(UUID no, boolean needData) {
		List<EntityBase> mainEntities = mainTools
				.get(String.format("SELECT * FROM %s WHERE `no` = '%s'", mainTableName, no.toString()));
		EntityBase main = mainEntities.get(0);

		List<EntityBase> entriesEntities = needData ? entryTools.get(
				String.format("SELECT * FROM %s WHERE `testNo` = '%s' ORDER BY `confidence` DESC, `word`, `realSense`",
						entryTableName, no.toString()))
				: null;

		TestFileBean result = new TestFileBean(main, entriesEntities);

		return result;
	}

	public ArrayList<TestFileBean> getAll(boolean needData) {
		List<EntityBase> mainEntities = mainTools.get(String.format("SELECT * FROM %s", mainTableName));
		List<EntityBase> entriesEntities = needData ? entryTools
				.get(String.format("SELECT * FROM %s ORDER BY `confidence` DESC, `word`, `realSense`", entryTableName))
				: null;

		ArrayList<TestFileBean> result = new ArrayList<TestFileBean>();

		HashMap<String, TestFileBean> mapper = new HashMap<>();
		for (EntityBase entity : mainEntities) {
			TestFileBean bean = new TestFileBean(entity, null);
			result.add(bean);

			mapper.put(bean.no.toString(), bean);
		}

		if (needData && entriesEntities != null && !entriesEntities.isEmpty()) {
			for (EntityBase entity : entriesEntities) {
				String no = entity.get("testNo");
				TestFileBean bean = mapper.get(no);
				if (bean != null) {
					bean.addEntry(entity);
				}
			}
		}

		return result;
	}

	public boolean updateProgress(UUID no, int progress) {
		EntityBase entityBase = new EntityBase();
		entityBase.put("progress", progress + "");

		return mainTools.modify(String.format("UPDATE %s SET # WHERE `no` = '%s'", mainTableName, no.toString()),
				entityBase);
	}

	public boolean insertTestSense(UUID no, UUID id, String sense, double confidence) {
		EntityBase entityBase = new EntityBase();
		entityBase.put("testSense", sense);
		entityBase.put("confidence", confidence + "");

		return entryTools.modify(String.format("UPDATE %s SET # WHERE `testNo` = '%s' AND `id` = '%s'", entryTableName,
				no.toString(), id.toString()), entityBase);
	}

	public boolean insertTestSense(UUID no, ArrayList<TestEntry> entries) {
		boolean result = true;

		if (!entries.isEmpty()) {
			String sql = "UPDATE %s SET `testSense` = CASE `id` %s END, "
					+ "`confidence` = CASE `id` %s END WHERE `id` IN ('%s')";

			String template = "WHEN '%s' THEN '%s'";

			ArrayList<String> idList = new ArrayList<>();
			ArrayList<String> senseList = new ArrayList<>();
			ArrayList<String> confList = new ArrayList<>();

			for (TestEntry entry : entries) {
				idList.add(entry.id.toString());
				senseList.add(String.format(template, entry.id, entry.testSense));
				confList.add(String.format(template, entry.id, String.format("%.8f", entry.confidence)));
			}

			String idString = String.join("', '", idList);
			String senseString = String.join(" ", senseList);
			String confString = String.join(" ", confList);

			sql = String.format(sql, entryTableName, senseString, confString, idString);
			result = entryTools.delete(sql);
		}

		return result;
	}

	public boolean update(TestFileBean bean) {
		EntityBase main = bean.getMainEntity();
		return mainTools.modify(String.format("UPDATE %s SET # WHERE `no` = '%s'", mainTableName, bean.no.toString()),
				main);
	}

	public boolean updateRetrainState(ArrayList<TestEntry> entries) {
		boolean result = true;

		if (!entries.isEmpty()) {
			String sql = "UPDATE %s SET `isRetrained` = CASE `id` %s END WHERE `id` IN ('%s')";

			String template = "WHEN '%s' THEN '%s'";

			ArrayList<String> idList = new ArrayList<>();
			ArrayList<String> retrainList = new ArrayList<>();

			for (TestEntry entry : entries) {
				idList.add(entry.id.toString());
				retrainList.add(String.format(template, entry.id, entry.tag));
			}

			String idString = String.join("', '", idList);
			String retrainString = String.join(" ", retrainList);

			sql = String.format(sql, entryTableName, retrainString, idString);
			result = entryTools.delete(sql);
		}

		return result;
	}

	/**
	 * 
	 * @param showType:
	 *            0: Show All; 1: Show Errors; 2: Show No Tagged; 3: Show
	 *            Tagged; 4: Show Tested; 5: Show Not Retrained; 6: Show Not
	 *            Tagged 'Yes'
	 * 
	 */
	public PageResult<TestEntry> getEntries(UUID no, String word, int pageIndex, int pageSize, int showType) {
		PageResult<TestEntry> result = new PageResult<>(pageIndex, pageSize);
		result.data = new ArrayList<TestEntry>();

		String orderBy = "ORDER BY `confidence` DESC, `word`, `realSense`";
		String sql = "";
		String countSql = "";
		switch (showType) {
		case 0:
			sql = "SELECT * FROM %s WHERE `testNo` = '%s' %s %s %s";
			countSql = "SELECT COUNT(*) AS `totalcount` FROM %s WHERE `testNo` = '%s' %s";
			break;
		case 1:
			sql = "SELECT t.* FROM %s AS t WHERE t.`realSense` <> t.`testSense` AND (t.`realSense` <> null OR t.`realSense` <> '') AND `testNo` = '%s' %s %s %s";
			countSql = "SELECT COUNT(t.`id`) AS `totalcount` FROM %s AS t WHERE t.`realSense` <> t.`testSense` AND (t.`realSense` <> null OR t.`realSense` <> '') AND `testNo` = '%s' %s";
			break;
		case 2:
			sql = "SELECT * FROM %s WHERE (`realSense` = '' OR `realSense` = NULL) AND `testNo` = '%s' %s %s %s";
			countSql = "SELECT COUNT(*) AS `totalcount` FROM %s WHERE (`realSense` = '' OR `realSense` = NULL) AND `testNo` = '%s' %s";
			break;
		case 3:
			sql = "SELECT * FROM %s WHERE `realSense` <> '' AND `testNo` = '%s' %s %s %s";
			countSql = "SELECT COUNT(*) AS `totalcount` FROM %s WHERE `realSense` <> '' AND `testNo` = '%s' %s";
			break;
		case 4:
			sql = "SELECT * FROM %s WHERE `testSense` <> '' AND `testNo` = '%s' %s %s %s";
			countSql = "SELECT COUNT(*) AS `totalcount` FROM %s WHERE `testSense` <> '' AND `testNo` = '%s' %s";
			break;
		case 5:
			sql = "SELECT * FROM %s WHERE (`isRetrained` = '' OR `isRetrained` = NULL) AND `testNo` = '%s' %s %s %s";
			countSql = "SELECT COUNT(*) AS `totalcount` FROM %s WHERE (`isRetrained` = '' OR `isRetrained` = NULL) AND `testNo` = '%s' %s";
			break;
		case 6:
			sql = "SELECT * FROM %s WHERE `isRetrained` <> '1' AND `testNo` = '%s' %s %s %s";
			countSql = "SELECT COUNT(*) AS `totalcount` FROM %s WHERE `isRetrained` <> '1' AND `testNo` = '%s' %s";
			break;
		}

		if (!sql.isEmpty()) {
			String query = (word == null || word.isEmpty()) ? "" : "AND `word` LIKE '%" + word + "%'";
			String limit = "LIMIT " + (pageIndex * pageSize) + ", " + pageSize;
			List<EntityBase> entities = entryTools
					.get(String.format(sql, entryTableName, no.toString(), query, orderBy, limit));

			for (int i = 0; i < entities.size(); i++) {
				EntityBase entityBase = entities.get(i);

				result.data.add(TestEntry.convertTo(entityBase));
			}

			entities = entryTools.get(String.format(countSql, entryTableName, no.toString(), query));
			result.totalCount = Integer.parseInt(entities.get(0).get("totalcount"));
		}

		return result;
	}

	public ArrayList<TestEntry> getEntries(UUID no, ArrayList<UUID> idList) {
		ArrayList<TestEntry> result = new ArrayList<>();

		if (!idList.isEmpty()) {
			String sql = "";
			sql = "SELECT * FROM %s WHERE `testNo` = '%s' AND `id` IN (%s)";

			String params = "'" + idList.get(0);
			for (int i = 1; i < idList.size(); i++) {
				params += "', '" + idList.get(i);
			}
			params += "'";

			List<EntityBase> entities = entryTools.get(String.format(sql, entryTableName, no.toString(), params));
			for (int i = 0; i < entities.size(); i++) {
				EntityBase entityBase = entities.get(i);

				result.add(TestEntry.convertTo(entityBase));
			}
		}

		return result;
	}

	public boolean delete(String no) {
		boolean result = mainTools
				.delete(String.format("DELETE FROM %s WHERE `no` = '%s'", mainTableName, no.toString()));
		result &= entryTools
				.delete(String.format("DELETE FROM %s WHERE `testNo` = '%s'", entryTableName, no.toString()));
		return result;
	}
}
