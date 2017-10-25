package ccd.tools.repository.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.xpath.DefaultXPath;

import ccd.tools.entity.EntityBase;

public class XmlTools implements IDataTools {

	private final static String rootName = "root";

	private String dirPath = "data/";
	private String tableName;
	private String filePath;

	SAXReader reader;
	Document document;
	Element root;
	XMLWriter writer;

	public XmlTools(String tableName) {
		this.tableName = tableName;
		filePath = dirPath + tableName + ".xml";

		try {
			File file = new File(filePath);
			if (file.exists()) {
				reader = new SAXReader();
				document = reader.read(file);
				root = document.getRootElement();
			} else {
				createXML();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	public boolean insert(EntityBase entity) {
		refresh();

		Element ele = root.addElement(entity.entityName);

		for (Map.Entry<String, String> entry : entity.data.entrySet()) {
			ele.addElement(entry.getKey()).addText(entry.getValue());
		}

		return update();
	}

	@Override
	public boolean insert(List<EntityBase> entities) {
		refresh();

		for (int i = 0; i < entities.size(); i++) {
			EntityBase entity = entities.get(i);
			Element ele = root.addElement(entity.entityName);

			for (Map.Entry<String, String> entry : entity.data.entrySet()) {
				ele.addElement(entry.getKey()).addText(entry.getValue());
			}
		}

		return update();
	}

	@Override
	public List<EntityBase> get(String query) {
		refresh();

		List<Node> nodes = document.selectNodes(rootName + "/" + query);
		List<EntityBase> result = new ArrayList<>();

		for (Node node : nodes) {
			EntityBase entity = new EntityBase();
			entity.entityName = node.getName();

			Iterator<Element> iterator = ((Element) node).elementIterator();
			while (iterator.hasNext()) {
				Element element = iterator.next();
				entity.data.put(element.getName(), element.getStringValue());
			}
			result.add(entity);
		}

		return result;
	}

	@Override
	public List<EntityBase> get(String query, String order) {
		refresh();

		List<Node> nodes = document.selectNodes(rootName + "/" + query);
		org.dom4j.XPath path = new MultiSortXPath(order);
		path.sort(nodes);

		List<EntityBase> result = new ArrayList<>();

		for (Node node : nodes) {
			EntityBase entity = new EntityBase();
			entity.entityName = node.getName();

			Iterator<Element> iterator = ((Element) node).elementIterator();
			while (iterator.hasNext()) {
				Element element = iterator.next();
				entity.data.put(element.getName(), element.getStringValue());
			}
			result.add(entity);
		}

		return result;
	}

	@Override
	public boolean modify(String query, EntityBase entity) {
		refresh();

		List<Node> nodes = document.selectNodes(rootName + "/" + query);

		for (Node node : nodes) {
			Iterator<Element> iterator = ((Element) node).elementIterator();
			while (iterator.hasNext()) {
				Element element = iterator.next();
				element.setText(entity.data.get(element.getName()));
			}
		}

		return update();
	}

	@Override
	public boolean delete(String query) {
		refresh();

		List<Node> nodes = document.selectNodes(rootName + "/" + query);

		for (Node node : nodes) {
			root.remove((Element) node);
		}

		return update();
	}

	private void createXML() {

		try {
			reader = new SAXReader();
			document = DocumentHelper.createDocument();
			root = document.addElement(rootName);

			writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"),
					OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void refresh() {
		try {
			File file = new File(filePath);
			document = reader.read(file);
			root = document.getRootElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean update() {
		try {
			writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"),
					OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.flush();
			writer.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void remove() {
		reader = null;
		document = null;
		root = null;
		try {
			writer.close();
		} catch (Exception e) {
		}
		File file = new File(filePath);
		file.delete();
		createXML();
	}

	private class MultiSortXPath extends DefaultXPath {

		private static final long serialVersionUID = -3846703641502168754L;
		private String text;
		private String[] splits;

		public MultiSortXPath(String text) {
			super(text);
			this.text = text;
			splits = text.split("/");
		}

		protected Object getCompareValue(Node node) {
			String[] result = null;
			if (text != null && !text.isEmpty()) {
				result = new String[splits.length];
				Iterator<Element> iterator = ((Element) node).elementIterator();
				while (iterator.hasNext()) {
					Element element = iterator.next();
					String name = element.getName();
					String value = element.getStringValue();

					for (int i = 0; i < splits.length; i++) {
						if (splits[i].equals(name)) {
							result[i] = value;
						}
					}
				}
			}
			return String.join(",", result);
		}
	}
}
