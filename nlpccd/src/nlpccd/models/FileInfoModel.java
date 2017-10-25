package nlpccd.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileInfoModel {
	
	@JsonProperty
	public String fileName;

	@JsonProperty
	public String size;

	@JsonProperty
	public String uploadDate;
}
