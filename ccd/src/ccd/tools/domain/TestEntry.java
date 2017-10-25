package ccd.tools.domain;

import java.util.UUID;

import ccd.tools.entity.EntityBase;

public class TestEntry {

	public TestEntry(String word, String context, int index) {
		this.id = UUID.randomUUID();
		this.word = word;
		this.context = context;
		this.index = index;
		this.tag = "";
	}

	public TestEntry(String word, String context, int index, String realSense, String testSense, double confidence,
			String tag) {
		this(word, context, index);
		this.realSense = realSense;
		this.testSense = testSense;
		this.confidence = confidence;
		this.tag = tag;
		this.isRetrained = !tag.isEmpty();
	}

	public TestEntry(UUID id, String word, String context, int index) {
		this.id = id;
		this.word = word;
		this.context = context;
		this.index = index;
		this.tag = "";
	}

	public TestEntry(UUID id, String word, String context, int index, String realSense, String testSense,
			double confidence, String tag) {
		this(id, word, context, index);
		this.realSense = realSense;
		this.testSense = testSense;
		this.confidence = confidence;
		this.tag = tag;
		this.isRetrained = !tag.isEmpty();
	}

	public UUID id;
	public String word;
	public String context;
	public int index;
	public String realSense;
	public String testSense;
	public double confidence;
	public String tag;
	public boolean isRetrained;

	public static TestEntry convertTo(EntityBase entity) {
		double confidence = 0;
		try {
			confidence = Double.parseDouble(entity.get("confidence"));
		} catch (Exception e) {
		}
		return new TestEntry(UUID.fromString(entity.get("id")), entity.get("word"), entity.get("context"),
				Integer.parseInt(entity.get("index")), entity.get("realSense"), entity.get("testSense"), confidence,
				entity.get("isRetrained"));
	}
}
