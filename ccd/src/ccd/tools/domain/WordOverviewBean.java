package ccd.tools.domain;

public class WordOverviewBean {

	public WordOverviewBean(int index, String word, int senseCount, int contextCount) {
		this.index = index;
		this.word = word;
		this.senseCount = senseCount;
		this.contextCount = contextCount;
	}

	public int index;
	public String word;
	public int senseCount, contextCount;
}
