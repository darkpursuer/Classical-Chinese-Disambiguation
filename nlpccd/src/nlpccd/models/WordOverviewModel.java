package nlpccd.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import ccd.tools.domain.WordOverviewBean;

public class WordOverviewModel {

	public WordOverviewModel(int index, String word, int senseCount, int contextCount) {
		this.index = index;
		this.word = word;
		this.senseCount = senseCount;
		this.contextCount = contextCount;
	}

	@JsonProperty
	public int index;

	@JsonProperty
	public String word;

	@JsonProperty
	public int senseCount;

	@JsonProperty
	public int contextCount;

	public static WordOverviewModel convertTo(WordOverviewBean bean) {
		return new WordOverviewModel(bean.index, bean.word, bean.senseCount, bean.contextCount);
	}
}
