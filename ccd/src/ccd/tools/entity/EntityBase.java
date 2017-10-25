package ccd.tools.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class EntityBase {

	public EntityBase() {
		data = new HashMap<>();
	}

	public EntityBase(String entityName) {
		this();
		this.entityName = entityName;
	}

	public String entityName;

	public HashMap<String, String> data;

	public String get(String key) {
		return data.get(key);
	}

	public void put(String key, String value) {
		data.put(key, value);
	}

	public static <T> EntityBase convert(String entityName, T obj) {
		EntityBase result = new EntityBase();

		Class<?> cla = obj.getClass();
		Field[] fields = cla.getFields();

		result.entityName = entityName;
		for (Field field : fields) {
			try {
				result.data.put(field.getName(), field.get(obj).toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public static <T> ArrayList<EntityBase> convertAll(String entityName, ArrayList<T> objs) {
		ArrayList<EntityBase> result = new ArrayList<EntityBase>();

		if (!objs.isEmpty()) {
			Class<?> cla = objs.get(0).getClass();
			Field[] fields = cla.getFields();
			String name = entityName;

			for (Object obj : objs) {
				EntityBase entityBase = new EntityBase();
				result.add(entityBase);
				entityBase.entityName = name;
				for (Field field : fields) {
					try {
						entityBase.data.put(field.getName(), field.get(obj).toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return result;
	}

	public static <T> ArrayList<EntityBase> convertAll(String entityName, ArrayList<T> objs,
			ArrayList<String> requiredFields) {
		ArrayList<EntityBase> result = new ArrayList<EntityBase>();

		if (!objs.isEmpty()) {
			Class<?> cla = objs.get(0).getClass();
			Field[] fields = cla.getFields();
			String name = entityName;

			for (Object obj : objs) {
				EntityBase entityBase = new EntityBase();
				result.add(entityBase);
				entityBase.entityName = name;
				for (Field field : fields) {
					if (requiredFields.contains(field.getName())) {
						try {
							entityBase.data.put(field.getName(), field.get(obj).toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		return result;
	}
}
