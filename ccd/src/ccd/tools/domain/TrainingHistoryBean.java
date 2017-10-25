package ccd.tools.domain;

import java.util.ArrayList;
import java.util.Date;

public class TrainingHistoryBean {

	public final static int RUNNING = 1;
	public final static int COMPLETED = 2;
	public final static int ERROR = 3;

	public TrainingHistoryBean() {
		dateModel = new Date();
		no = System.currentTimeMillis();
		progress = 0;
		status = RUNNING;
		scope = null;
		date = dateModel.toString();
	}

	public TrainingHistoryBean(long no, String date, int progress, int status, ArrayList<String> scope, String error) {
		this.no = no;
		this.date = date;
		this.progress = progress;
		this.status = status;
		this.scope = scope;
		this.error = error;
	}

	public TrainingHistoryBean(String string) {
		String[] splits = string.split("#");
		if (splits.length == 6) {
			no = Long.parseLong(splits[0]);
			date = splits[1];
			progress = Integer.parseInt(splits[2]);
			status = Integer.parseInt(splits[3]);
			error = splits[5];

			String scopes = splits[4];
			if (scopes != null && !scopes.isEmpty()) {
				scope = new ArrayList<>();
				String[] scopeArray = scopes.split(", ");
				for (String str : scopeArray) {
					scope.add(str);
				}
			}
		}
	}

	public long no;
	public String date;
	public int progress;
	public int status;
	public ArrayList<String> scope;
	public String error;

	private Date dateModel;

	public Date getDate() {
		return dateModel;
	}

	public void complete() {
		status = COMPLETED;
	}

	public void error(String info) {
		status = ERROR;
		error = info;
	}

	public String toString() {
		String result = no + "#" + date + "#" + progress + "#" + status + "#"
				+ (scope == null ? "" : String.join(", ", scope)) + "#" + error;

		return result;
	}
}
