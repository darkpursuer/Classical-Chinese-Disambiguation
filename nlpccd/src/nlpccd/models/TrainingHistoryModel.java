package nlpccd.models;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import ccd.tools.domain.TrainingHistoryBean;

public class TrainingHistoryModel {

	public TrainingHistoryModel(long no, String date, int progress, int status, ArrayList<String> scope, String error) {
		this.no = no;
		this.date = date;
		this.progress = progress;
		this.status = status;
		this.scope = scope;
		this.error = error;
	}

	@JsonProperty
	public long no;

	@JsonProperty
	public String date;

	@JsonProperty
	public int progress;

	@JsonProperty
	public int status;

	@JsonProperty
	public ArrayList<String> scope;

	@JsonProperty
	public String error;

	public static TrainingHistoryModel convertTo(TrainingHistoryBean bean) {
		return new TrainingHistoryModel(bean.no, bean.date, bean.progress, bean.status, bean.scope, bean.error);
	}
}
