package ccd.tools;

import ccd.tools.repository.base.IDataTools;
import ccd.tools.repository.base.SQLTools;

public class InstanceFactory {

	public static IDataTools newTools(String name){
		return new SQLTools(name);
	}
}
