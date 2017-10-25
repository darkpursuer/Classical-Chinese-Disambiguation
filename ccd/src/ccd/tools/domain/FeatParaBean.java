package ccd.tools.domain;

import java.util.UUID;

public class FeatParaBean {

	public UUID index;
	public String word;
	public String sense;
	public UUID feature;
	public int parameter;
	public String feature_str;

	/**
	 * 
	 * @param index
	 * @param word
	 * @param sense
	 * @param feature
	 * @param parameter
	 */

	public FeatParaBean(UUID index, String word, String sense, UUID feature, int parameter) {
		this.index = index;
		this.word = word;
		this.sense = sense;
		this.feature = feature;
		this.parameter = parameter;
	}

	/**
	 * used when generating
	 * 
	 * @param word
	 * @param sense
	 * @param feature_str
	 * @param parameter
	 */

	public FeatParaBean(String word, String sense, String feature_str, boolean isNew) {
		this.word = word;
		this.sense = sense;
		this.feature_str = feature_str;
		this.parameter = 0;

		if (isNew) {
			index = UUID.randomUUID();
		}
	}
}
