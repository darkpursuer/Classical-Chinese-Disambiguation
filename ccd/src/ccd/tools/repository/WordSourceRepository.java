package ccd.tools.repository;

import java.util.ArrayList;
import java.util.List;

import ccd.tools.InstanceFactory;
import ccd.tools.domain.PageResult;
import ccd.tools.domain.WordBean;
import ccd.tools.domain.WordOverviewBean;
import ccd.tools.entity.EntityBase;
import ccd.tools.repository.base.IDataTools;

public class WordSourceRepository {

	public static String tableName = "wordsrcs";
	IDataTools tools;

	public WordSourceRepository() {
		tools = InstanceFactory.newTools(tableName);
	}

	public boolean insert(ArrayList<WordBean> beans) {
		if (beans.isEmpty()) {
			return true;
		}
		ArrayList<String> fields = new ArrayList<>();
		fields.add("word");
		fields.add("sense");
		fields.add("context");
		fields.add("index");
		boolean result = tools.insert(EntityBase.convertAll(tableName, beans, fields));
		return result;
	}

	public int[] getindex(String word, String sense, String context) {
		List<EntityBase> entities = tools.get(String.format(
				"SELECT index FROM %s WHERE `word` = '%s' AND `sense` = '%s' AND `context` = '%s' ORDER BY `word`, `sense` LIMIT 999999999999",
				tableName, word, sense, context));

		int[] result = new int[entities.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(entities.get(i).get("index"));
		}

		return result;
	}

	public ArrayList<WordBean> get(String word) {
		ArrayList<WordBean> result = new ArrayList<WordBean>();

		List<EntityBase> entities = tools.get(String.format(
				"SELECT * FROM %s WHERE `word` = '%s' ORDER BY `word`, `sense` LIMIT 999999999999", tableName, word));

		for (EntityBase entityBase : entities) {
			WordBean tmp = new WordBean(entityBase.get("word"), entityBase.get("sense"), entityBase.get("context"),
					Integer.parseInt(entityBase.get("index")), entityBase.get("No"));

			result.add(tmp);
		}

		return result;
	}

	public ArrayList<WordBean> get(ArrayList<String> words) {
		ArrayList<WordBean> result = new ArrayList<WordBean>();

		if (words != null && !words.isEmpty()) {
			String wordStr = String.format("'%s'", String.join("', '", words));

			List<EntityBase> entities = tools.get(
					String.format("SELECT * FROM %s WHERE `word` IN (%s) ORDER BY `word`, `sense` LIMIT 999999999999",
							tableName, wordStr));

			for (EntityBase entityBase : entities) {
				WordBean tmp = new WordBean(entityBase.get("word"), entityBase.get("sense"), entityBase.get("context"),
						Integer.parseInt(entityBase.get("index")), entityBase.get("No"));

				result.add(tmp);
			}
		}

		return result;
	}

	public ArrayList<WordBean> getAllWordBeans() {
		ArrayList<WordBean> result = new ArrayList<WordBean>();

		List<EntityBase> entities = tools
				.get(String.format("SELECT * FROM %s ORDER BY `word`, `sense` LIMIT 999999999999", tableName));

		for (EntityBase entityBase : entities) {
			WordBean tmp = new WordBean(entityBase.get("word"), entityBase.get("sense"), entityBase.get("context"),
					Integer.parseInt(entityBase.get("index")), entityBase.get("No"));

			result.add(tmp);
		}

		return result;
	}

	public PageResult<WordOverviewBean> getOverview(String word, int pageIndex, int pageSize) {
		PageResult<WordOverviewBean> result = new PageResult<>(pageIndex, pageSize);
		result.data = new ArrayList<WordOverviewBean>();

		String query = word == null || word.isEmpty() ? "" : "WHERE `word` LIKE '%" + word + "%'";
		String limit = "LIMIT " + (pageIndex * pageSize) + ", " + pageSize;
		List<EntityBase> entities = tools.get(String.format(
				"SELECT * FROM (SELECT DISTINCT(`word`) AS `word`, COUNT(DISTINCT(`sense`)) AS `sensecount`, COUNT(DISTINCT(`No`)) AS contextcount FROM %s GROUP BY `word` ORDER BY `word`, `sense`) newtable %s %s",
				tableName, query, limit));

		for (int i = 0; i < entities.size(); i++) {
			EntityBase entityBase = entities.get(i);
			WordOverviewBean bean = new WordOverviewBean(i, entityBase.get("word"),
					Integer.parseInt(entityBase.get("sensecount")), Integer.parseInt(entityBase.get("contextcount")));

			result.data.add(bean);
		}

		entities = tools.get(String.format(
				"SELECT COUNT(*) AS `totalcount` FROM (SELECT DISTINCT(`word`) AS `word`, COUNT(DISTINCT(`sense`)) AS `sensecount`, COUNT(DISTINCT(`context`)) AS contextcount FROM %s GROUP BY `word` ORDER BY `word`, `sense`) newtable %s",
				tableName, query));
		result.totalCount = Integer.parseInt(entities.get(0).get("totalcount"));

		return result;
	}

	public int getWordCount() {
		List<EntityBase> entities = tools
				.get(String.format("SELECT COUNT(DISTINCT(`word`)) AS `wordcount` FROM %s", tableName));

		String string = entities.get(0).get("wordcount");

		return string.isEmpty() ? 0 : Integer.parseInt(string);
	}

	public String[] getContexts(String word, String sense) {
		List<EntityBase> entities = tools.get(String.format(
				"SELECT context FROM %s WHERE `word` = '%s' AND `sense` = '%s' ORDER BY `word`, `sense` LIMIT 999999999999",
				tableName, word, sense));

		String[] result = new String[entities.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = entities.get(i).get("context");
		}

		return result;
	}

	public String[] getWords() {
		List<EntityBase> entities = tools
				.get(String.format("SELECT DISTINCT word FROM %s LIMIT 999999999999", tableName));

		String[] result = new String[entities.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = entities.get(i).get("word");
		}

		return result;
	}

	public String[] getSenses(String word) {
		List<EntityBase> entities = tools.get(
				String.format("SELECT DISTINCT sense FROM %s WHERE `word` = '%s' LIMIT 999999999999", tableName, word));

		String[] result = new String[entities.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = entities.get(i).get("sense");
		}

		return result;
	}

	public boolean isWordExists(String word) {
		List<EntityBase> entities = tools
				.get(String.format("SELECT * FROM %s WHERE `word` = '%s' LIMIT 999999999999", tableName, word));
		return !entities.isEmpty();
	}

	public boolean update(WordBean wordBean) {
		return tools.modify(String.format("UPDATE %s SET # WHERE `No` = '%s'", tableName, wordBean.No),
				EntityBase.convert(tableName, wordBean));
	}

	public boolean updateSenses(String word, ArrayList<String> senses, String targetSense) {
		EntityBase entityBase = new EntityBase();
		entityBase.put("sense", targetSense);

		String senseComb = "'" + String.join("', '", senses) + "'";

		return tools.modify(
				String.format("UPDATE %s SET # WHERE `word` = '%s' AND `sense` IN (%s)", tableName, word, senseComb),
				entityBase);
	}

	public boolean removeRedundant() {
		return tools.delete(
				String.format("DELETE FROM %s WHERE `No` IN (SELECT tmp.* FROM (SELECT S.`No` FROM %s AS S, %s AS T "
						+ "WHERE S.`word` = T.`word` AND S.`context` = T.`context` AND S.`index` = T.`index` AND S.`sense` = T.`sense` AND S.`No` > T.`No` "
						+ "GROUP BY `No`) tmp)", tableName, tableName, tableName));
	}

	public void truncate() {
		tools.remove();
	}
}
