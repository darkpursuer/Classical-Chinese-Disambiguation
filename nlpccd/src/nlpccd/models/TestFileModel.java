package nlpccd.models;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import ccd.tools.domain.TestEntry;
import ccd.tools.domain.TestFileBean;

public class TestFileModel {

	@JsonProperty
	public String no;

	@JsonProperty
	public String date;

	@JsonProperty
	public int entryNum;

	@JsonProperty
	public boolean isCompleted;

	@JsonProperty
	public int progress;

	@JsonProperty
	public String result;

	@JsonProperty
	public ArrayList<TestEntryModel> entries;

	public static TestFileModel convertTo(TestFileBean bean) {
		TestFileModel result = new TestFileModel();

		result.no = bean.no.toString();
		result.date = bean.date;
		result.entryNum = bean.entryNum;
		result.isCompleted = bean.isCompleted;
		result.progress = bean.progress;
		result.result = bean.result;
		result.entries = new ArrayList<>();

		for (TestEntry entry : bean.entries) {

			result.entries.add(TestEntryModel.convertTo(entry));
		}

		return result;
	}
}
