package nlpccd.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RetrainRequestModel {

	@JsonProperty
	public String id;

	// the tag of this entry
	// 0: unsure; 1: yes; 2: no
	@JsonProperty
	public int tag;
}
