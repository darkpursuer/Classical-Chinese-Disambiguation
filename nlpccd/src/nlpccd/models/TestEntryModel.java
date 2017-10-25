package nlpccd.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import ccd.tools.domain.TestEntry;

public class TestEntryModel {

	@JsonProperty
	public String id;

	@JsonProperty
	public String word;

	@JsonProperty
	public String context;

	@JsonProperty
	public int index;

	@JsonProperty
	public String realSense;

	@JsonProperty
	public String testSense;

	@JsonProperty
	public String confidence;
	
	@JsonProperty
	public String tag;

	public static TestEntryModel convertTo(TestEntry entry) {
		TestEntryModel model = new TestEntryModel();

		model.id = entry.id.toString();
		model.word = entry.word;
		model.context = entry.context;
		model.index = entry.index;
		model.realSense = entry.realSense;
		model.testSense = entry.testSense;
		model.confidence = String.format("%.4f", entry.confidence);
		
		model.tag = entry.tag;

		return model;
	}
}
