package ccd.tools.domain;

import java.util.ArrayList;

public class PageResult<T> {

	public PageResult(int pageIndex, int pageSize) {
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}

	public int totalCount, pageIndex, pageSize;
	public ArrayList<T> data;
}
