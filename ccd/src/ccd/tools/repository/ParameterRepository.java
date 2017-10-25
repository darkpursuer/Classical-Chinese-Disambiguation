package ccd.tools.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import ccd.tools.InstanceFactory;
import ccd.tools.domain.FeatParaBean;
import ccd.tools.entity.EntityBase;
import ccd.tools.repository.base.IDataTools;

public class ParameterRepository {

	public static String tableName = "parameters";
	IDataTools tools;

	public ParameterRepository() {
		tools = InstanceFactory.newTools(tableName);
	}

	public ArrayList<FeatParaBean> getParametersList(String word) {
		ArrayList<FeatParaBean> result = new ArrayList<FeatParaBean>();

		List<EntityBase> entities = tools
				.get(String.format("SELECT * FROM %s WHERE `word` = '%s' LIMIT 999999999999", tableName, word));

		for (EntityBase entity : entities) {
			FeatParaBean bean = new FeatParaBean(UUID.randomUUID(), entity.get("word"), entity.get("sense"),
					UUID.fromString(entity.get("feature")), Integer.parseInt(entity.get("parameter")));

			result.add(bean);
		}

		return result;
	}

	public void insertParameters(HashMap<String, HashMap<Integer, Integer>> B, String word) {
		tools.delete(String.format("DELETE FROM %s WHERE `word` = '%s'", tableName, word));

		ArrayList<EntityBase> list = new ArrayList<>();

		Iterator<Entry<String, HashMap<Integer, Integer>>> iter1 = B.entrySet().iterator();

		while (iter1.hasNext()) {
			Entry<String, HashMap<Integer, Integer>> entry1 = iter1.next();

			String sense = entry1.getKey();
			HashMap<Integer, Integer> fp = entry1.getValue();

			Iterator<Entry<Integer, Integer>> iter2 = fp.entrySet().iterator();

			while (iter2.hasNext()) {
				Entry<Integer, Integer> entry2 = iter2.next();

				int index = entry2.getKey();
				int parameter = entry2.getValue();

				EntityBase entity = new EntityBase(tableName);
				entity.put("word", word);
				entity.put("sense", sense);
				entity.put("feature", index + "");
				entity.put("parameter", parameter + "");
				list.add(entity);
			}

		}

		tools.insert(list);
	}

	public void insertParaArray(ArrayList<HashMap<String, HashMap<UUID, Integer>>> array, ArrayList<String> words) {
		int size = array.size();
		if (size != words.size()) {
			System.out.println("!!!Length Error!!!");
			return;
		}

		System.out.println("There is " + size + " words");

		ArrayList<EntityBase> list = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			HashMap<String, HashMap<UUID, Integer>> B = array.get(i);
			String word = words.get(i);

			Iterator<Entry<String, HashMap<UUID, Integer>>> iter1 = B.entrySet().iterator();

			while (iter1.hasNext()) {
				Entry<String, HashMap<UUID, Integer>> entry1 = iter1.next();

				String sense = entry1.getKey();
				HashMap<UUID, Integer> fp = entry1.getValue();

				Iterator<Entry<UUID, Integer>> iter2 = fp.entrySet().iterator();

				while (iter2.hasNext()) {
					Entry<UUID, Integer> entry2 = iter2.next();

					UUID index = entry2.getKey();
					int parameter = entry2.getValue();

					EntityBase entity = new EntityBase(tableName);
					entity.put("word", word);
					entity.put("sense", sense);
					entity.put("feature", index.toString());
					entity.put("parameter", parameter + "");
					list.add(entity);
				}

			}
		}

		tools.insert(list);
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
