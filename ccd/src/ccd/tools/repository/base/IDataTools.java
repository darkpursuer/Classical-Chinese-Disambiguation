package ccd.tools.repository.base;

import java.util.List;

import ccd.tools.entity.EntityBase;

public interface IDataTools {

	public String getTableName();

	public boolean insert(EntityBase entity);

	public boolean insert(List<EntityBase> entities);

	public List<EntityBase> get(String query);

	public List<EntityBase> get(String query, String order);

	public boolean modify(String query, EntityBase entity);

	public boolean delete(String query);

	public void remove();
}
