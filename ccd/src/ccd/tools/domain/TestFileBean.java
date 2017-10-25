package ccd.tools.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ccd.tools.entity.EntityBase;

public class TestFileBean {

	public TestFileBean() {
		this.no = UUID.randomUUID();
		date = new Date().toString();
		entryNum = -1;
		isCompleted = false;
		progress = 0;
		result = "";
		entries = new ArrayList<TestEntry>();
	}

	public TestFileBean(EntityBase main, List<EntityBase> entries) {
		this.no = UUID.fromString(main.get("no"));
		this.date = main.get("date");
		this.entryNum = Integer.parseInt(main.get("entryNum"));
		this.isCompleted = main.get("isCompleted").equals("1");
		this.progress = Integer.parseInt(main.get("progress"));
		this.result = main.get("result");

		this.entries = new ArrayList<>();
		if (entries != null && !entries.isEmpty()) {
			for (EntityBase entity : entries) {
				addEntry(entity);
				;
			}
		}
	}

	public UUID no;
	public String date;
	public int entryNum;
	public boolean isCompleted;
	public int progress;
	public String result;
	public ArrayList<TestEntry> entries;

	public void addEntry(EntityBase entity) {
		this.entries.add(TestEntry.convertTo(entity));
	}

	public EntityBase getMainEntity() {
		EntityBase result = new EntityBase();

		result.put("no", no.toString());
		result.put("date", date);
		result.put("isCompleted", isCompleted ? "1" : "0");
		result.put("progress", progress + "");
		result.put("result", this.result);

		if (entryNum == -1)
			entryNum = entries.size();
		result.put("entryNum", entryNum + "");

		return result;
	}

	public ArrayList<EntityBase> getEntriesEntities() {
		ArrayList<EntityBase> result = new ArrayList<EntityBase>();

		for (TestEntry entry : entries) {
			EntityBase tmp = new EntityBase();

			tmp.put("id", entry.id.toString());
			tmp.put("word", entry.word);
			tmp.put("context", entry.context);
			tmp.put("index", entry.index + "");
			tmp.put("realSense", entry.realSense);
			tmp.put("testSense", entry.testSense);
			tmp.put("testNo", this.no.toString());
			tmp.put("confidence", String.format("%.8f", entry.confidence));
			tmp.put("isRetrained", entry.tag);

			result.add(tmp);
		}

		return result;
	}
}