package ccd.tools.domain;

public class WordBean {

	public String word;
	public String sense;
	public String context;
	public int index;
	public String No;

	public WordBean(String word, String sense, String context, int index) {
		this.word = word;
		this.sense = sense;
		this.context = context;
		this.index = index;
	}
	
	public WordBean(String word, String sense, String context, int index, String No) {
		this.word = word;
		this.sense = sense;
		this.context = context;
		this.index = index;
		this.No = No;

//		int i = Integer.parseInt(index);
//
//		if (i == -1) {
//			return;
//		}
//
//		int pre, post;
//		int cl = context.length();
//		int wl = word.length();
//
//		int tmp = i - TestAmbiguous.WINDOWSIZE;
//		pre = (tmp > 0) ? tmp : 0;
//
//		tmp = i + wl + TestAmbiguous.WINDOWSIZE;
//		post = (tmp < cl) ? tmp : cl;
//
//		this.context = context.substring(pre, post);
//		this.index = i - pre + "";

		/*
		 * if(!(this.context.charAt(Integer.parseInt(this.index))+"").equals(
		 * word.charAt(0)+"")) { System.out.println(word + ": " + context); }
		 */
	}

	public String getWord() {
		return word;
	}

	public String getSense() {
		return sense;
	}

	public String getContext() {
		return context;
	}

	public int getIndex() {
		return index;
	}

	public String getNo() {
		return No;
	}
}
