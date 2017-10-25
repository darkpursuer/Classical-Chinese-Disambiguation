package ccd.tools;

import java.io.*;

public class Serializer {
	
	//serialize the object instances to a file
	public static boolean serialize(Object object, String filename)
	{
		try {
			FileOutputStream fs = new FileOutputStream(filename);
			ObjectOutputStream os = new ObjectOutputStream(fs);
			os.writeObject(object);
			os.flush();
			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	//deserialize the class files
	public static Object deserialize(String filename)
	{
		try {
			FileInputStream fs = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fs);
			Object object = ois.readObject();
			ois.close();
			
			return object;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
