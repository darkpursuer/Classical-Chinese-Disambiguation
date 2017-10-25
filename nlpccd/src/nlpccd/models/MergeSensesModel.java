package nlpccd.models;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MergeSensesModel {

	@JsonProperty
	public String word;

	@JsonProperty
	public ArrayList<String> senses;

	@JsonProperty
	public String targetSense;
}
